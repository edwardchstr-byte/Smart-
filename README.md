# Aria — Voice Chat Companion

A flirty-but-tasteful voice companion app. Android (Kotlin) frontend talks to a small backend
server, which calls the Claude API and keeps the API key off the device.

## Architecture

- `android/` — native Android app. Records your voice (`SpeechRecognizer`), sends the
  transcribed text to the backend, and speaks the reply aloud (`TextToSpeech`).
- `server/` — Node/TypeScript backend. Holds the Anthropic API key, maintains per-session
  conversation history, and calls Claude with the companion's persona system prompt
  (see `server/src/persona.ts`).

## Running the backend

```bash
cd server
npm install
cp .env.example .env   # then fill in ANTHROPIC_API_KEY
npm run dev
```

The server listens on `http://localhost:3000` by default, with a `POST /chat` endpoint
(`{ sessionId, message }` -> `{ reply }`) and `POST /reset` to clear a session's history.

## Running the Android app

1. Open `android/` in Android Studio.
2. If testing on the emulator, the default backend URL (`http://10.0.2.2:3000`) already
   points at your machine's `localhost:3000`. For a physical device or a deployed backend,
   build with `-PbackendUrl=https://your-server.example.com`.
3. Run on a device/emulator, grant the microphone permission, tap "Tap to talk", and speak.

## Persona & boundaries

Aria is designed to be warm, playful, and flirty — charm and teasing banter, not explicit
content. The system prompt in `server/src/persona.ts` enforces this boundary regardless of
how the conversation is steered.
