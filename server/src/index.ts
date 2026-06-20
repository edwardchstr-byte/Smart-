import express from "express";
import cors from "cors";
import Anthropic from "@anthropic-ai/sdk";
import { SYSTEM_PROMPT } from "./persona";

const PORT = process.env.PORT ? parseInt(process.env.PORT, 10) : 3000;
const MODEL = "claude-opus-4-8";
const MAX_HISTORY_MESSAGES = 30;

const client = new Anthropic(); // reads ANTHROPIC_API_KEY from env

type Role = "user" | "assistant";
interface HistoryMessage {
  role: Role;
  content: string;
}

const sessions = new Map<string, HistoryMessage[]>();

const app = express();
app.use(cors());
app.use(express.json());

app.post("/chat", async (req, res) => {
  const { sessionId, message } = req.body as { sessionId?: string; message?: string };

  if (!sessionId || typeof sessionId !== "string") {
    return res.status(400).json({ error: "sessionId is required" });
  }
  if (!message || typeof message !== "string") {
    return res.status(400).json({ error: "message is required" });
  }

  const history = sessions.get(sessionId) ?? [];
  history.push({ role: "user", content: message });

  try {
    const response = await client.messages.create({
      model: MODEL,
      max_tokens: 512,
      system: SYSTEM_PROMPT,
      messages: history.map((m) => ({ role: m.role, content: m.content })),
    });

    const replyBlock = response.content.find((block) => block.type === "text");
    const reply = replyBlock && replyBlock.type === "text" ? replyBlock.text : "";

    history.push({ role: "assistant", content: reply });
    if (history.length > MAX_HISTORY_MESSAGES) {
      history.splice(0, history.length - MAX_HISTORY_MESSAGES);
    }
    sessions.set(sessionId, history);

    res.json({ reply });
  } catch (err) {
    console.error("Claude API error:", err);
    res.status(500).json({ error: "Failed to get a response from the companion." });
  }
});

app.post("/reset", (req, res) => {
  const { sessionId } = req.body as { sessionId?: string };
  if (sessionId) sessions.delete(sessionId);
  res.json({ ok: true });
});

app.get("/health", (_req, res) => res.json({ ok: true }));

app.listen(PORT, () => {
  console.log(`Companion server listening on port ${PORT}`);
});
