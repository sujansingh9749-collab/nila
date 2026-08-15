package com.example.ui

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AssistantApplication
import com.example.data.api.GeminiClient
import com.example.data.local.entities.ConversationMessage
import com.example.data.local.entities.ExpenseEntry
import com.example.data.local.entities.VoiceMacro
import com.example.data.local.entities.VoiceNote
import com.example.engine.CommandAction
import com.example.engine.PhoneControlManager
import com.example.engine.VoiceEngine
import com.example.engine.WakeWordDetector
import com.example.service.FloatingMicService
import com.example.service.ScreenAutomationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray

data class AssistantUiState(
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val isWakeWordActive: Boolean = true,
    val isWakeWordGlowActive: Boolean = false,
    val wakeWordDetectedPrompt: String? = null,
    val liveTranscript: String = "",
    val lastResponse: String = "",
    val audioRms: Float = 0.05f,
    val selectedLanguage: String = "bn-BD", // Default to Bangla or en-US
    val wakeWordPhrase: String = "নীলা",
    val wakeWordSensitivity: Float = 0.7f,
    val continuousMode: Boolean = true,
    val persona: String = "নীলা (Neela - Bengali Assistant)",
    val speechSpeed: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val isGeminiLoading: Boolean = false,
    val activeTab: Int = 0,
    val isFlashlightOn: Boolean = false,
    val mediaVolumePercent: Int = 50,
    val sosContactName: String = "Emergency Contact",
    val sosContactPhone: String = "999",
    val floatingMicEnabled: Boolean = false,
    val isStandbyRequested: Boolean = false,
    val isScreenServiceActive: Boolean = false,
    val currentScreenSummary: String = "",
    val statusNotification: String? = null
)

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as AssistantApplication).repository
    val voiceEngine = VoiceEngine(application)
    val wakeWordDetector = WakeWordDetector(application)
    val phoneControl = PhoneControlManager(application)

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    // Room Database Observables
    val conversations: StateFlow<List<ConversationMessage>> = repository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val macros: StateFlow<List<VoiceMacro>> = repository.macros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceNotes: StateFlow<List<VoiceNote>> = repository.voiceNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntry>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpense: StateFlow<Double?> = repository.totalExpense
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        // Observe voice engine states
        viewModelScope.launch {
            voiceEngine.isListening.collect { listening ->
                _uiState.update { it.copy(isListening = listening) }
            }
        }
        viewModelScope.launch {
            voiceEngine.isSpeaking.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }
        viewModelScope.launch {
            voiceEngine.liveTranscript.collect { transcript ->
                _uiState.update { it.copy(liveTranscript = transcript) }
            }
        }
        viewModelScope.launch {
            voiceEngine.audioRms.collect { rms ->
                _uiState.update { it.copy(audioRms = rms) }
            }
        }
        viewModelScope.launch {
            phoneControl.isFlashlightOn.collect { state ->
                _uiState.update { it.copy(isFlashlightOn = state) }
            }
        }
        viewModelScope.launch {
            phoneControl.currentVolume.collect { vol ->
                _uiState.update { it.copy(mediaVolumePercent = vol) }
            }
        }

        // Voice Engine Callbacks
        voiceEngine.onSpeechFinalResult = { recognizedText ->
            processVoiceInput(recognizedText)
        }

        voiceEngine.onSilenceTimeout = {
            // User was silent for several seconds: auto-sleep / auto-standby
            val standbyMsg = if (_uiState.value.selectedLanguage.startsWith("bn")) {
                "💤 কিছুক্ষণ নীরব থাকায় সহকারী বিরতিতে গেছে। ডাকলে 'নীলা' বলুন।"
            } else {
                "💤 Standby mode due to silence. Say 'Nila' to resume."
            }
            _uiState.update { 
                it.copy(
                    isListening = false,
                    statusNotification = standbyMsg
                ) 
            }
            if (_uiState.value.isWakeWordActive) {
                wakeWordDetector.start()
            }
        }

        voiceEngine.onSpeechError = { errorMsg ->
            _uiState.update { it.copy(statusNotification = errorMsg) }
            if (_uiState.value.isWakeWordActive) {
                wakeWordDetector.start()
            }
        }

        voiceEngine.onTtsFinished = {
            if (_uiState.value.isStandbyRequested) {
                _uiState.update { it.copy(isStandbyRequested = false) }
                if (_uiState.value.isWakeWordActive) {
                    wakeWordDetector.start()
                }
            } else if (_uiState.value.continuousMode) {
                // Continuous hands-free conversation mode: start listening automatically
                Handler(Looper.getMainLooper()).postDelayed({
                    startListening()
                }, 500)
            } else if (_uiState.value.isWakeWordActive) {
                wakeWordDetector.start()
            }
        }

        // Wake Word Detector Callback
        wakeWordDetector.onWakeWordTriggered = { detectedWord ->
            val promptMsg = if (_uiState.value.selectedLanguage.startsWith("bn")) {
                "✨ 'নীলা' সাড়া দিয়েছে! শুনছি..."
            } else {
                "✨ 'Nila' detected! Listening..."
            }
            _uiState.update { 
                it.copy(
                    isWakeWordGlowActive = true,
                    wakeWordDetectedPrompt = promptMsg
                ) 
            }
            wakeWordDetector.pause()
            startListening()

            // Smoothly auto-reset glowing pulse after feedback duration
            Handler(Looper.getMainLooper()).postDelayed({
                _uiState.update { it.copy(isWakeWordGlowActive = false) }
            }, 3500)
        }

        // Auto-start wake word detection on startup
        wakeWordDetector.start()
    }

    fun triggerWakeWordSimulate() {
        val promptMsg = if (_uiState.value.selectedLanguage.startsWith("bn")) {
            "✨ 'নীলা' সাড়া দিয়েছে! শুনছি..."
        } else {
            "✨ 'Nila' awakened! Listening..."
        }
        _uiState.update { 
            it.copy(
                isWakeWordGlowActive = true,
                wakeWordDetectedPrompt = promptMsg
            ) 
        }
        startListening()
        Handler(Looper.getMainLooper()).postDelayed({
            _uiState.update { it.copy(isWakeWordGlowActive = false) }
        }, 3500)
    }

    fun setActiveTab(index: Int) {
        _uiState.update { it.copy(activeTab = index) }
    }

    fun startListening() {
        voiceEngine.startListening(_uiState.value.selectedLanguage)
    }

    fun stopListening() {
        voiceEngine.stopListening()
    }

    fun toggleListening() {
        if (_uiState.value.isListening) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun setLanguage(languageCode: String) {
        _uiState.update { it.copy(selectedLanguage = languageCode) }
        voiceEngine.currentLanguageCode = languageCode
    }

    fun setPersona(personaName: String) {
        _uiState.update { it.copy(persona = personaName) }
    }

    fun setSpeechSpeed(speed: Float) {
        _uiState.update { it.copy(speechSpeed = speed) }
        voiceEngine.speechRate = speed
    }

    fun setSpeechPitch(pitch: Float) {
        _uiState.update { it.copy(speechPitch = pitch) }
        voiceEngine.speechPitch = pitch
    }

    fun setContinuousMode(enabled: Boolean) {
        _uiState.update { it.copy(continuousMode = enabled) }
    }

    fun setWakeWord(phrase: String) {
        _uiState.update { it.copy(wakeWordPhrase = phrase) }
        wakeWordDetector.wakeWordList = listOf(phrase.lowercase(), "নীলা", "nila", "neela", "hey neela", "hey nila", "হ্যালো নীলা", "hey assistant", "jarvis", "alexa", "হ্যালো")
    }

    fun setWakeWordSensitivity(sensitivity: Float) {
        _uiState.update { it.copy(wakeWordSensitivity = sensitivity) }
        wakeWordDetector.sensitivityLevel = sensitivity
    }

    fun toggleWakeWordDetection(enabled: Boolean) {
        _uiState.update { it.copy(isWakeWordActive = enabled) }
        if (enabled) {
            wakeWordDetector.start()
        } else {
            wakeWordDetector.pause()
        }
    }

    fun toggleFloatingMic(enabled: Boolean) {
        _uiState.update { it.copy(floatingMicEnabled = enabled) }
        val context = getApplication<Application>()
        val serviceIntent = Intent(context, FloatingMicService::class.java)
        if (enabled) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(statusNotification = "Service notification error: ${e.message}") }
            }
        } else {
            context.stopService(serviceIntent)
        }
    }

    fun setSosContact(name: String, phone: String) {
        _uiState.update { it.copy(sosContactName = name, sosContactPhone = phone) }
    }

    fun triggerSOS() {
        phoneControl.triggerEmergencySOS(_uiState.value.sosContactPhone)
        val msg = "🚨 Emergency SOS triggered for ${_uiState.value.sosContactName} (${_uiState.value.sosContactPhone})"
        voiceEngine.speak("Emergency SOS initiated.", _uiState.value.selectedLanguage)
        viewModelScope.launch {
            repository.saveConversation("EMERGENCY SOS", msg, "SOS", _uiState.value.sosContactPhone, _uiState.value.selectedLanguage, true)
        }
        _uiState.update { it.copy(statusNotification = msg) }
    }

    fun toggleFlashlight() {
        phoneControl.toggleTorch()
    }

    fun setMediaVolume(percent: Int) {
        phoneControl.setMediaVolume(percent)
    }

    fun speakText(text: String) {
        voiceEngine.speak(text, _uiState.value.selectedLanguage)
    }

    fun stopSpeaking() {
        voiceEngine.stopSpeaking()
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
        }
    }

    fun processVoiceInput(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        _uiState.update { it.copy(liveTranscript = trimmed) }

        // 1. Check if it matches any registered Voice Macro
        val matchedMacro = macros.value.firstOrNull { macro ->
            macro.isEnabled && (trimmed.contains(macro.triggerPhrase, ignoreCase = true) || macro.triggerPhrase.contains(trimmed, ignoreCase = true))
        }

        if (matchedMacro != null) {
            executeMacro(matchedMacro)
            return
        }

        // 2. Check offline local command dispatcher (zero latency for device actions)
        val offlineCommand = phoneControl.parseOfflineCommand(trimmed)
        if (offlineCommand != null) {
            executeOfflineCommand(trimmed, offlineCommand)
            return
        }

        // 3. Fallback to Gemini 3.5 Flash for conversational reasoning and Q&A
        queryGeminiAssistant(trimmed)
    }

    private fun executeOfflineCommand(query: String, command: com.example.engine.ParsedCommand) {
        when (command.actionType) {
            CommandAction.FLASHLIGHT_ON -> phoneControl.setTorchMode(true)
            CommandAction.FLASHLIGHT_OFF -> phoneControl.setTorchMode(false)
            CommandAction.FLASHLIGHT_TOGGLE -> phoneControl.toggleTorch()
            CommandAction.SET_VOLUME -> {
                val vol = command.payload?.toIntOrNull() ?: 100
                phoneControl.setMediaVolume(vol)
            }
            CommandAction.VOLUME_UP -> phoneControl.adjustVolume(20)
            CommandAction.VOLUME_DOWN -> phoneControl.adjustVolume(-20)
            CommandAction.SET_SILENT_MODE -> phoneControl.setRingerSilent()
            CommandAction.SET_NORMAL_MODE -> phoneControl.setRingerNormal()
            CommandAction.SET_TIMER -> {
                val minutes = command.payload?.toIntOrNull() ?: 5
                phoneControl.setSystemTimer(minutes)
            }
            CommandAction.OPEN_CAMERA -> phoneControl.openCamera()
            CommandAction.OPEN_APP -> {
                val app = command.payload ?: ""
                phoneControl.launchApp(app)
            }
            CommandAction.PLAY_YOUTUBE -> {
                val song = command.payload ?: "Bangla Songs"
                phoneControl.playOnYouTube(song)
            }
            CommandAction.SEARCH_WEB -> {
                val q = command.payload ?: ""
                phoneControl.searchGoogle(q)
            }
            CommandAction.MEDIA_PLAY_PAUSE -> {
                phoneControl.sendMediaKey(android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            }
            CommandAction.MEDIA_NEXT -> {
                phoneControl.sendMediaKey(android.view.KeyEvent.KEYCODE_MEDIA_NEXT)
            }
            CommandAction.MAKE_CALL -> {
                val number = command.payload ?: ""
                if (number.isNotEmpty()) {
                    phoneControl.makeCall(number)
                }
            }
            CommandAction.TOGGLE_HANDSFREE -> {
                val enabled = command.payload == "true"
                setContinuousMode(enabled)
            }
            CommandAction.STOP_LISTENING -> {
                _uiState.update { it.copy(isStandbyRequested = true) }
                stopListening()
            }
            CommandAction.SILENT_SHUT_UP -> {
                // Immediately cut off TTS voice speaking & stop active recognition, but stay active on standby
                voiceEngine.stopSpeaking()
                _uiState.update { it.copy(isStandbyRequested = true, isSpeaking = false, statusNotification = "🤫 ভয়েস বন্ধ করা হয়েছে (Silent Mode)") }
                stopListening()
                return
            }
            CommandAction.SCREEN_READ_SUMMARY -> {
                val service = ScreenAutomationService.getInstance()
                if (service != null) {
                    val elements = service.readScreenContent()
                    val visibleTexts = elements.mapNotNull { 
                        if (it.text.isNotBlank()) it.text else if (it.contentDescription.isNotBlank()) it.contentDescription else null 
                    }.distinct().take(15)

                    if (visibleTexts.isNotEmpty()) {
                        val screenBrief = "আপনার বর্তমান স্ক্রিনে রয়েছে: " + visibleTexts.joinToString(", ")
                        _uiState.update { it.copy(lastResponse = screenBrief, currentScreenSummary = screenBrief) }
                        voiceEngine.speak(screenBrief, _uiState.value.selectedLanguage)
                        return
                    } else {
                        val emptyMsg = "স্ক্রিনে কোনো টেক্সট বা উপাদান পাওয়া যায়নি।"
                        _uiState.update { it.copy(lastResponse = emptyMsg) }
                        voiceEngine.speak(emptyMsg, _uiState.value.selectedLanguage)
                        return
                    }
                } else {
                    val msg = "স্ক্রিন কন্ট্রোল করার জন্য সেটিংস থেকে নীলা অ্যাক্সেসিবিলিটি পারমিশন চালু করুন।"
                    _uiState.update { it.copy(lastResponse = msg) }
                    voiceEngine.speak(msg, _uiState.value.selectedLanguage)
                    return
                }
            }
            CommandAction.SCREEN_CLICK_TARGET -> {
                val target = command.payload ?: ""
                val service = ScreenAutomationService.getInstance()
                if (service != null && target.isNotEmpty()) {
                    val clicked = service.clickByText(target)
                    val resultMsg = if (clicked) "স্ক্রিনের '$target' বাটনে সফলভাবে ক্লিক করা হয়েছে" else "'$target' খুঁজে পাওয়া যায়নি বা ক্লিক করা সম্ভব হয়নি"
                    _uiState.update { it.copy(lastResponse = resultMsg) }
                    voiceEngine.speak(resultMsg, _uiState.value.selectedLanguage)
                    return
                } else if (service == null) {
                    val msg = "স্ক্রিন ক্লিক করার জন্য নীলা অ্যাক্সেসিবিলিটি সার্ভিস অন করতে হবে।"
                    _uiState.update { it.copy(lastResponse = msg) }
                    voiceEngine.speak(msg, _uiState.value.selectedLanguage)
                    return
                }
            }
            CommandAction.SCREEN_TYPE_TEXT -> {
                val textToType = command.payload ?: ""
                val service = ScreenAutomationService.getInstance()
                if (service != null && textToType.isNotEmpty()) {
                    val typed = service.typeText(textToType)
                    val resultMsg = if (typed) "স্ক্রিনে '$textToType' লেখা সম্পন্ন হয়েছে" else "টাইপ করার মতো কোনো ইনপুট বক্স পাওয়া যায়নি"
                    _uiState.update { it.copy(lastResponse = resultMsg) }
                    voiceEngine.speak(resultMsg, _uiState.value.selectedLanguage)
                    return
                }
            }
            CommandAction.SCREEN_SCROLL_DOWN -> {
                val service = ScreenAutomationService.getInstance()
                service?.scrollDown()
            }
            CommandAction.SCREEN_SCROLL_UP -> {
                val service = ScreenAutomationService.getInstance()
                service?.scrollUp()
            }
            CommandAction.SCREEN_GLOBAL_BACK -> {
                val service = ScreenAutomationService.getInstance()
                service?.performGlobalBack()
            }
            CommandAction.SCREEN_GLOBAL_HOME -> {
                val service = ScreenAutomationService.getInstance()
                service?.performGlobalHome()
            }
            CommandAction.SCREEN_GLOBAL_RECENTS -> {
                val service = ScreenAutomationService.getInstance()
                service?.performGlobalRecents()
            }
            CommandAction.SCREEN_GLOBAL_NOTIFICATIONS -> {
                val service = ScreenAutomationService.getInstance()
                service?.performGlobalNotifications()
            }
            CommandAction.OPEN_WIFI_SETTINGS -> phoneControl.openWiFiSettings()
            CommandAction.OPEN_BLUETOOTH_SETTINGS -> phoneControl.openBluetoothSettings()
            CommandAction.SOS_EMERGENCY -> triggerSOS()
            CommandAction.ADD_EXPENSE -> {
                val parts = command.payload?.split("|")
                val amount = parts?.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val item = parts?.getOrNull(1) ?: "সাধারণ খরচ"
                viewModelScope.launch {
                    repository.addExpense(item, amount, "Voice", "৳")
                }
            }
            CommandAction.ADD_NOTE -> {
                val content = command.payload ?: query
                viewModelScope.launch {
                    repository.addVoiceNote("নোট (${java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())})", content)
                }
            }
            else -> {}
        }

        val feedback = command.feedbackMessage
        if (feedback.isNotEmpty()) {
            _uiState.update { it.copy(lastResponse = feedback) }
            voiceEngine.speak(feedback, _uiState.value.selectedLanguage)
        }

        viewModelScope.launch {
            repository.saveConversation(
                query = query,
                response = feedback,
                action = command.actionType.name,
                payload = command.payload,
                language = _uiState.value.selectedLanguage,
                isSuccess = true
            )
        }
    }

    private fun queryGeminiAssistant(query: String) {
        _uiState.update { it.copy(isGeminiLoading = true) }

        viewModelScope.launch(Dispatchers.IO) {
            val history = conversations.value.take(4).map { it.query to it.response }
            val personaPrompt = when (_uiState.value.persona) {
                "নীলা (Neela - Bengali Assistant)" -> "You are 'নীলা' (Neela), an affectionate, smart, and helpful Bengali AI Voice Assistant. Respond naturally, warmly and eloquently in Bengali (বাংলা) as নীলা. Keep responses concise (1-3 sentences) suitable for voice."
                "মিত্রমণি (Bengali Assistant)" -> "You are 'মিত্রমণি' (Mitromoni), a smart, warm Bengali AI Voice Assistant. Respond naturally and eloquently in Bengali (বাংলা). Keep responses concise (1-3 sentences) suitable for voice."
                "JARVIS AI" -> "You are JARVIS, a hyper-competent, witty sci-fi AI assistant. Deliver ultra-sharp, crisp, intelligent answers."
                "Professional Executive" -> "You are a professional, concise executive AI assistant. Provide direct, objective, task-oriented responses."
                else -> "You are নীলা (Neela), a friendly, helpful AI voice assistant supporting English and Bengali."
            }

            val result = GeminiClient.askAssistant(query, history, personaPrompt)

            _uiState.update { it.copy(isGeminiLoading = false) }

            result.onSuccess { responseText ->
                _uiState.update { it.copy(lastResponse = responseText) }
                voiceEngine.speak(responseText, _uiState.value.selectedLanguage)
                repository.saveConversation(
                    query = query,
                    response = responseText,
                    action = "GEMINI_QNA",
                    language = _uiState.value.selectedLanguage,
                    isSuccess = true
                )
            }.onFailure { error ->
                val fallbackMsg = if (_uiState.value.selectedLanguage.startsWith("bn")) {
                    "দুঃখিত, বর্তমানে এআই সার্ভারে সংযোগ পাওয়া যায়নি। আমি ডিভাইস কন্ট্রোল মোডে আছি।"
                } else {
                    "I am in offline mode. I can still control your flashlight, timers, volume, notes, and expenses!"
                }
                _uiState.update { it.copy(lastResponse = fallbackMsg) }
                voiceEngine.speak(fallbackMsg, _uiState.value.selectedLanguage)
                repository.saveConversation(
                    query = query,
                    response = "$fallbackMsg (${error.message})",
                    action = "OFFLINE_FALLBACK",
                    language = _uiState.value.selectedLanguage,
                    isSuccess = false
                )
            }
        }
    }

    fun executeMacro(macro: VoiceMacro) {
        viewModelScope.launch {
            try {
                val jsonArray = JSONArray(macro.stepsJson)
                var spokenMessage = "Executing routine: ${macro.name}"
                for (i in 0 until jsonArray.length()) {
                    val step = jsonArray.getString(i)
                    when {
                        step == "TORCH_ON" -> phoneControl.setTorchMode(true)
                        step == "TORCH_OFF" -> phoneControl.setTorchMode(false)
                        step.startsWith("SET_VOLUME:") -> {
                            val vol = step.substringAfter("SET_VOLUME:").toIntOrNull() ?: 50
                            phoneControl.setMediaVolume(vol)
                        }
                        step.startsWith("SPEAK:") -> {
                            spokenMessage = step.substringAfter("SPEAK:")
                        }
                    }
                }

                _uiState.update { it.copy(lastResponse = spokenMessage) }
                voiceEngine.speak(spokenMessage, _uiState.value.selectedLanguage)
                repository.saveConversation(
                    query = "Run routine: ${macro.name}",
                    response = spokenMessage,
                    action = "VOICE_MACRO",
                    payload = macro.name,
                    language = _uiState.value.selectedLanguage,
                    isSuccess = true
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(statusNotification = "Error running routine: ${e.message}") }
            }
        }
    }

    fun addMacro(name: String, trigger: String, description: String, stepsJson: String, iconName: String = "routine") {
        viewModelScope.launch {
            repository.addMacro(
                VoiceMacro(
                    name = name,
                    triggerPhrase = trigger,
                    description = description,
                    stepsJson = stepsJson,
                    iconName = iconName
                )
            )
        }
    }

    fun toggleMacro(macro: VoiceMacro) {
        viewModelScope.launch {
            repository.updateMacro(macro.copy(isEnabled = !macro.isEnabled))
        }
    }

    fun deleteMacro(id: Long) {
        viewModelScope.launch {
            repository.deleteMacro(id)
        }
    }

    fun addVoiceNote(title: String, content: String, category: String = "General") {
        viewModelScope.launch {
            repository.addVoiceNote(title, content, category = category)
        }
    }

    fun deleteVoiceNote(id: Long) {
        viewModelScope.launch {
            repository.deleteVoiceNote(id)
        }
    }

    fun addExpense(item: String, amount: Double, category: String = "General") {
        viewModelScope.launch {
            repository.addExpense(item, amount, category)
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpense(id)
        }
    }

    fun translateAndSpeak(text: String, targetLangCode: String = "en-US") {
        _uiState.update { it.copy(isGeminiLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val prompt = "Translate this text accurately into ${if (targetLangCode.startsWith("bn")) "Bengali (বাংলা)" else "English"}. Output ONLY the translated phrase: \"$text\""
            val result = GeminiClient.askAssistant(prompt)
            _uiState.update { it.copy(isGeminiLoading = false) }

            result.onSuccess { translated ->
                _uiState.update { it.copy(lastResponse = translated) }
                voiceEngine.speak(translated, targetLangCode)
                repository.saveConversation(
                    query = "Translate: $text",
                    response = translated,
                    action = "TRANSLATE",
                    language = targetLangCode,
                    isSuccess = true
                )
            }.onFailure { err ->
                _uiState.update { it.copy(statusNotification = "Translation error: ${err.message}") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.destroy()
        wakeWordDetector.pause()
    }
}
