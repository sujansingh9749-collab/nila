package com.example.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private val TAG = "VoiceEngine"

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    // State flows
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    // Callbacks
    var onSpeechFinalResult: ((String) -> Unit)? = null
    var onSpeechError: ((String) -> Unit)? = null
    var onSilenceTimeout: (() -> Unit)? = null
    var onTtsFinished: (() -> Unit)? = null

    // Preferences
    var currentLanguageCode: String = "en-US" // "en-US", "bn-BD", "bn-IN"
    var speechRate: Float = 1.0f
    var speechPitch: Float = 1.0f

    // Silence detection handler
    private val silenceHandler = Handler(Looper.getMainLooper())
    private val silenceTimeoutDurationMs = 4500L // 4.5 seconds of absolute silence before auto-standby
    private val silenceRunnable = Runnable {
        if (_isListening.value && _liveTranscript.value.isEmpty()) {
            Log.d(TAG, "Silence timeout detected. Auto-stopping listening.")
            stopListening()
            onSilenceTimeout?.invoke()
        }
    }

    init {
        tts = TextToSpeech(context.applicationContext, this)
        initSpeechRecognizer()
    }

    private fun initSpeechRecognizer() {
        Handler(Looper.getMainLooper()).post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer?.destroy()
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createRecognitionListener())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize SpeechRecognizer: ${e.message}")
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
            resetSilenceTimer()
        }

        override fun onBeginningOfSpeech() {
            _isListening.value = true
            cancelSilenceTimer()
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Normalize RMS for UI soundwave (0.0 to 1.0)
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
            _audioRms.value = normalized
            if (normalized > 0.25f) {
                cancelSilenceTimer()
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _isListening.value = false
            _audioRms.value = 0.05f
            cancelSilenceTimer()
        }

        override fun onError(error: Int) {
            _isListening.value = false
            _audioRms.value = 0.05f
            cancelSilenceTimer()

            if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                // User remained silent for several seconds
                Log.d(TAG, "Speech timeout/no match -> Silence detected.")
                onSilenceTimeout?.invoke()
                return
            }

            val errorMsg = when (error) {
                SpeechRecognizer.ERROR_NETWORK -> "Network issue in speech recognition."
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                else -> "Speech recognition error ($error)"
            }
            Log.w(TAG, "Speech Error: $errorMsg")
            onSpeechError?.invoke(errorMsg)
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            _audioRms.value = 0.05f
            cancelSilenceTimer()
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull()?.trim().orEmpty()
            if (recognizedText.isNotEmpty()) {
                _liveTranscript.value = recognizedText
                onSpeechFinalResult?.invoke(recognizedText)
            } else {
                onSilenceTimeout?.invoke()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            cancelSilenceTimer()
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partial = matches?.firstOrNull()?.trim().orEmpty()
            if (partial.isNotEmpty()) {
                _liveTranscript.value = partial
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun resetSilenceTimer() {
        silenceHandler.removeCallbacks(silenceRunnable)
        silenceHandler.postDelayed(silenceRunnable, silenceTimeoutDurationMs)
    }

    private fun cancelSilenceTimer() {
        silenceHandler.removeCallbacks(silenceRunnable)
    }

    fun startListening(languageCode: String = currentLanguageCode) {
        currentLanguageCode = languageCode
        Handler(Looper.getMainLooper()).post {
            try {
                if (tts?.isSpeaking == true) {
                    tts?.stop()
                    _isSpeaking.value = false
                }

                if (speechRecognizer == null) {
                    initSpeechRecognizer()
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L)
                }

                _liveTranscript.value = ""
                speechRecognizer?.startListening(intent)
                _isListening.value = true
                resetSilenceTimer()
            } catch (e: Exception) {
                Log.e(TAG, "Error starting speech recognition: ${e.message}")
                _isListening.value = false
                cancelSilenceTimer()
            }
        }
    }

    fun stopListening() {
        cancelSilenceTimer()
        Handler(Looper.getMainLooper()).post {
            try {
                speechRecognizer?.stopListening()
                _isListening.value = false
                _audioRms.value = 0.05f
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping listening: ${e.message}")
            }
        }
    }

    fun speak(text: String, languageCode: String = currentLanguageCode) {
        if (!isTtsReady || tts == null) {
            Log.w(TAG, "TTS not ready yet")
            return
        }

        Handler(Looper.getMainLooper()).post {
            try {
                val locale = if (languageCode.startsWith("bn")) {
                    Locale("bn", "BD")
                } else {
                    Locale.US
                }

                tts?.let { engine ->
                    engine.language = locale
                    engine.setSpeechRate(speechRate)
                    engine.setPitch(speechPitch)

                    val utteranceId = "TTS_${System.currentTimeMillis()}"
                    _isSpeaking.value = true
                    engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error speaking text: ${e.message}")
                _isSpeaking.value = false
            }
        }
    }

    fun stopSpeaking() {
        Handler(Looper.getMainLooper()).post {
            try {
                tts?.stop()
                _isSpeaking.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping TTS: ${e.message}")
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onTtsFinished?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        } else {
            Log.e(TAG, "TTS Initialization failed: $status")
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying VoiceEngine: ${e.message}")
        }
    }
}
