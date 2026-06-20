package com.companion.aria

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var conversationText: TextView
    private lateinit var statusText: TextView
    private lateinit var micButton: Button
    private lateinit var scrollView: ScrollView

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var companionClient: CompanionClient

    private val sessionId = UUID.randomUUID().toString()
    private val mainScope = CoroutineScope(Dispatchers.Main)

    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        conversationText = findViewById(R.id.conversationText)
        statusText = findViewById(R.id.statusText)
        micButton = findViewById(R.id.micButton)
        scrollView = findViewById(R.id.scrollView)

        companionClient = CompanionClient(BuildConfig.BACKEND_URL)

        textToSpeech = TextToSpeech(this) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                textToSpeech.language = Locale.US
            }
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(recognitionListener)

        micButton.setOnClickListener { onMicTapped() }

        appendLine("Aria", "Hey there. Tap the button and say something to me.")
    }

    private fun onMicTapped() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            return
        }
        startListening()
    }

    private fun startListening() {
        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
        }
        statusText.text = "Listening..."
        speechRecognizer.startListening(intent)
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onResults(results: Bundle) {
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val spokenText = matches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                appendLine("You", spokenText)
                sendToCompanion(spokenText)
            } else {
                statusText.text = "Didn't catch that. Tap to try again."
            }
        }

        override fun onError(error: Int) {
            statusText.text = "Mic error. Tap to try again."
        }

        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            statusText.text = "Thinking..."
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun sendToCompanion(message: String) {
        mainScope.launch {
            statusText.text = "Thinking..."
            val reply = withContext(Dispatchers.IO) {
                companionClient.sendMessage(sessionId, message)
            }
            statusText.text = "Tap to talk"
            if (reply != null) {
                appendLine("Aria", reply)
                speak(reply)
            } else {
                appendLine("Aria", "Sorry, I'm having trouble connecting right now.")
            }
        }
    }

    private fun speak(text: String) {
        if (ttsReady) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "aria_reply")
        }
    }

    private fun appendLine(speaker: String, text: String) {
        conversationText.append("$speaker: $text\n\n")
        scrollView.post { scrollView.fullScroll(android.view.View.FOCUS_DOWN) }
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
        super.onDestroy()
    }
}
