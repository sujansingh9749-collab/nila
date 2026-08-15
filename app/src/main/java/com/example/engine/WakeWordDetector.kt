package com.example.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WakeWordDetector(private val context: Context) {

    private val TAG = "WakeWordDetector"
    private var backgroundRecognizer: SpeechRecognizer? = null
    private var isContinuousListening = false

    private val _isWakeWordActive = MutableStateFlow(false)
    val isWakeWordActive: StateFlow<Boolean> = _isWakeWordActive.asStateFlow()

    private val _detectedWakeWord = MutableStateFlow<String?>(null)
    val detectedWakeWord: StateFlow<String?> = _detectedWakeWord.asStateFlow()

    var onWakeWordTriggered: ((String) -> Unit)? = null

    // Configurable parameters
    var wakeWordList: List<String> = listOf("নীলা", "nila", "neela", "hey neela", "hey nila", "হ্যালো নীলা", "hey assistant", "jarvis", "alexa", "হ্যালো", "নমস্কার")
    var sensitivityLevel: Float = 0.7f // 0.1 to 1.0

    private var retryCount = 0

    companion object {
        private const val MAX_RETRY_DELAY_MS = 10000L
        private const val BASE_RETRY_DELAY_MS = 600L
    }

    private val restartHandler = Handler(Looper.getMainLooper())
    private val restartRunnable = Runnable {
        if (isContinuousListening) {
            startInternalListening()
        }
    }

    private fun getNextRetryDelay(): Long {
        val delay = BASE_RETRY_DELAY_MS * (1 shl retryCount.coerceAtMost(4))
        retryCount++
        return delay.coerceAtMost(MAX_RETRY_DELAY_MS)
    }

    private fun resetRetryCount() {
        retryCount = 0
    }

    fun start() {
        if (isContinuousListening) return
        isContinuousListening = true
        _isWakeWordActive.value = true
        startInternalListening()
    }

    fun pause() {
        isContinuousListening = false
        _isWakeWordActive.value = false
        restartHandler.removeCallbacks(restartRunnable)
        Handler(Looper.getMainLooper()).post {
            try {
                backgroundRecognizer?.cancel()
                backgroundRecognizer?.destroy()
                backgroundRecognizer = null
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing wake word detector: ${e.message}")
            }
        }
    }

    private fun startInternalListening() {
        Handler(Looper.getMainLooper()).post {
            try {
                backgroundRecognizer?.destroy()
                backgroundRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {}
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {}

                        override fun onError(error: Int) {
                            if (isContinuousListening) {
                                val retryDelay = getNextRetryDelay()
                                Log.d(TAG, "Wake word error ($error), retrying in ${retryDelay}ms (attempt $retryCount)")
                                restartHandler.postDelayed(restartRunnable, retryDelay)
                            }
                        }

                        override fun onResults(results: Bundle?) {
                            resetRetryCount()
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            checkMatchesForWakeWord(matches)
                            if (isContinuousListening) {
                                restartHandler.postDelayed(restartRunnable, 300)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            checkMatchesForWakeWord(matches)
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                backgroundRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error in wake word listener loop: ${e.message}")
                if (isContinuousListening) {
                    restartHandler.postDelayed(restartRunnable, 1000)
                }
            }
        }
    }

    private fun checkMatchesForWakeWord(matches: ArrayList<String>?) {
        if (matches == null) return
        for (phrase in matches) {
            val lower = phrase.lowercase().trim()
            for (wakeWord in wakeWordList) {
                val wakeLower = wakeWord.lowercase().trim()
                if (lower.contains(wakeLower) || fuzzyMatch(lower, wakeLower)) {
                    _detectedWakeWord.value = wakeWord
                    onWakeWordTriggered?.invoke(phrase)
                    return
                }
            }
        }
    }

    private fun fuzzyMatch(input: String, target: String): Boolean {
        if (input.contains(target)) return true
        val words = input.split(" ")
        val targetWords = target.split(" ")
        return words.any { w -> targetWords.any { tw -> w.startsWith(tw) || tw.startsWith(w) } }
    }
}
