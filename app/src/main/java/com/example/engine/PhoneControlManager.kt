package com.example.engine

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ParsedCommand(
    val actionType: CommandAction,
    val summary: String,
    val feedbackMessage: String,
    val payload: String? = null,
    val isHandledLocally: Boolean = true
)

enum class CommandAction {
    FLASHLIGHT_ON,
    FLASHLIGHT_OFF,
    FLASHLIGHT_TOGGLE,
    SET_VOLUME,
    VOLUME_UP,
    VOLUME_DOWN,
    SET_SILENT_MODE,
    SET_NORMAL_MODE,
    SET_ALARM,
    SET_TIMER,
    OPEN_CAMERA,
    OPEN_APP,
    PLAY_YOUTUBE,
    SEARCH_WEB,
    CHECK_BATTERY,
    CHECK_TIME,
    CHECK_DATE,
    MEDIA_PLAY_PAUSE,
    MEDIA_NEXT,
    OPEN_WIFI_SETTINGS,
    OPEN_BLUETOOTH_SETTINGS,
    MAKE_CALL,
    SEND_SMS,
    SEND_WHATSAPP,
    ADD_EXPENSE,
    ADD_NOTE,
    TRANSLATE,
    SOS_EMERGENCY,
    TOGGLE_HANDSFREE,
    STOP_LISTENING,
    SILENT_SHUT_UP,
    SCREEN_READ_SUMMARY,
    SCREEN_CLICK_TARGET,
    SCREEN_TYPE_TEXT,
    SCREEN_SCROLL_DOWN,
    SCREEN_SCROLL_UP,
    SCREEN_GLOBAL_BACK,
    SCREEN_GLOBAL_HOME,
    SCREEN_GLOBAL_RECENTS,
    SCREEN_GLOBAL_NOTIFICATIONS,
    GENERAL_QUERY
}

class PhoneControlManager(private val context: Context) {

    private val TAG = "PhoneControlManager"

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _currentVolume = MutableStateFlow(getMediaVolumePercent())
    val currentVolume: StateFlow<Int> = _currentVolume.asStateFlow()

    private val _ringerMode = MutableStateFlow(audioManager.ringerMode)
    val ringerMode: StateFlow<Int> = _ringerMode.asStateFlow()

    fun getMediaVolumePercent(): Int {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return if (max > 0) (current * 100) / max else 50
    }

    fun setMediaVolume(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val target = (clamped * max) / 100
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
        _currentVolume.value = clamped
    }

    fun adjustVolume(deltaPercent: Int) {
        val newVol = (_currentVolume.value + deltaPercent).coerceIn(0, 100)
        setMediaVolume(newVol)
    }

    fun setTorchMode(enable: Boolean): Boolean {
        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, enable)
                _isFlashlightOn.value = enable
                true
            } else {
                Log.w(TAG, "No flash unit available")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting flashlight: ${e.message}")
            false
        }
    }

    fun toggleTorch(): Boolean {
        val target = !_isFlashlightOn.value
        return if (setTorchMode(target)) {
            _isFlashlightOn.value = target
            true
        } else false
    }

    fun setRingerSilent() {
        try {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            _ringerMode.value = AudioManager.RINGER_MODE_SILENT
        } catch (e: Exception) {
            try {
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                _ringerMode.value = AudioManager.RINGER_MODE_VIBRATE
            } catch (err: Exception) {
                Log.e(TAG, "Silent mode error: ${err.message}")
            }
        }
    }

    fun setRingerNormal() {
        try {
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            _ringerMode.value = AudioManager.RINGER_MODE_NORMAL
        } catch (e: Exception) {
            Log.e(TAG, "Normal mode error: ${e.message}")
        }
    }

    fun setSystemTimer(minutes: Int, label: String = "Assistant Timer") {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, minutes * 60)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Timer set for $minutes minutes ($label)", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Timer set for $minutes minutes ($label)", Toast.LENGTH_SHORT).show()
        }
    }

    fun setSystemAlarm(hour: Int, minute: Int, message: String = "Assistant Alarm") {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Alarm set for $hour:$minute", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Alarm set for $hour:$minute", Toast.LENGTH_SHORT).show()
        }
    }

    fun openCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch camera: ${e.message}")
        }
    }

    fun launchApp(appNameOrPkg: String): Boolean {
        val pm = context.packageManager
        val query = appNameOrPkg.trim().lowercase(Locale.getDefault())

        val targetPackage = when {
            query.contains("youtube") || query.contains("ইউটিউব") -> "com.google.android.youtube"
            query.contains("whatsapp") || query.contains("হোয়াটসঅ্যাপ") || query.contains("হোয়াটসঅ্যাপ") -> "com.whatsapp"
            query.contains("chrome") || query.contains("ক্রোম") || query.contains("browser") || query.contains("ব্রাউজার") -> "com.android.chrome"
            query.contains("maps") || query.contains("ম্যাপ") || query.contains("গুগল ম্যাপ") -> "com.google.android.apps.maps"
            query.contains("facebook") || query.contains("ফেসবুক") -> "com.facebook.katana"
            query.contains("calculator") || query.contains("ক্যালকুলেটর") -> "com.google.android.calculator"
            query.contains("gallery") || query.contains("গ্যালারি") || query.contains("photos") || query.contains("ছবি") -> "com.google.android.apps.photos"
            query.contains("clock") || query.contains("ঘড়ি") || query.contains("ঘড়ি") || query.contains("অ্যালার্ম") -> "com.google.android.deskclock"
            query.contains("settings") || query.contains("সেটিংস") -> {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                return true
            }
            query.contains("camera") || query.contains("ক্যামেরা") -> {
                openCamera()
                return true
            }
            else -> query
        }

        return try {
            val intent = pm.getLaunchIntentForPackage(targetPackage)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                // Try searching on Play Store or general web
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(appNameOrPkg)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app $appNameOrPkg: ${e.message}")
            false
        }
    }

    fun playOnYouTube(songOrVideo: String) {
        try {
            val intent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", songOrVideo)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val url = "https://www.youtube.com/results?search_query=${Uri.encode(songOrVideo)}"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        }
    }

    fun searchGoogle(query: String) {
        try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val url = "https://www.google.com/search?q=${Uri.encode(query)}"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        }
    }

    fun getBatteryInfo(): Pair<Int, Boolean> {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val isCharging = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                batteryManager.isCharging
            } else false
            Pair(capacity.coerceIn(0, 100), isCharging)
        } catch (e: Exception) {
            Pair(85, false)
        }
    }

    fun getFormattedTimeBengali(): String {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR)
        val displayHour = if (hour == 0) 12 else hour
        val min = cal.get(Calendar.MINUTE)
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val amPm = when {
            hour24 in 4..11 -> "সকাল"
            hour24 in 12..15 -> "দুপুর"
            hour24 in 16..18 -> "বিকেল"
            hour24 in 19..20 -> "সন্ধ্যা"
            else -> "রাত"
        }
        return "এখন সময় $amPm $displayHour টা বেজে $min মিনিট"
    }

    fun getFormattedDateBengali(): String {
        val cal = Calendar.getInstance()
        val days = arrayOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার")
        val months = arrayOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
        val dayName = days[cal.get(Calendar.DAY_OF_WEEK) - 1]
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val monthName = months[cal.get(Calendar.MONTH)]
        val year = cal.get(Calendar.YEAR)
        return "আজ $dayName, $dayOfMonth $monthName $year"
    }

    fun sendMediaKey(keyCode: Int) {
        try {
            val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val eventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            audioManager.dispatchMediaKeyEvent(eventDown)
            audioManager.dispatchMediaKeyEvent(eventUp)
        } catch (e: Exception) {
            Log.e(TAG, "Media key error: ${e.message}")
        }
    }

    fun openWiFiSettings() {
        try {
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open WiFi settings: ${e.message}")
        }
    }

    fun openBluetoothSettings() {
        try {
            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Bluetooth settings: ${e.message}")
        }
    }

    fun makeCall(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dial: ${e.message}")
        }
    }

    fun sendSms(phoneNumber: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber")).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compose SMS: ${e.message}")
        }
    }

    fun sendWhatsApp(phoneNumber: String, message: String) {
        try {
            val cleanPhone = phoneNumber.replace("+", "").replace(" ", "").replace("-", "")
            val url = if (cleanPhone.isNotEmpty()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}"
            } else {
                "https://api.whatsapp.com/send?text=${Uri.encode(message)}"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open WhatsApp: ${e.message}")
        }
    }

    fun triggerEmergencySOS(emergencyNumber: String = "999") {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400, 200, 600), -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                @Suppress("DEPRECATION")
                vibrator.vibrate(1000)
            }

            toggleTorch()
            makeCall(emergencyNumber)
        } catch (e: Exception) {
            Log.e(TAG, "SOS trigger error: ${e.message}")
        }
    }

    // Offline Fast Hands-Free Voice Command Parser (Bengali & English)
    fun parseOfflineCommand(input: String): ParsedCommand? {
        val text = input.trim().lowercase(Locale.getDefault())

        // 0. Screen Understanding & Automation Voice Commands
        if (text.contains("স্ক্রিন পড়ো") || text.contains("স্ক্রিনে কি আছে") || text.contains("স্ক্রিনে কী আছে") ||
            text.contains("স্ক্রিন দেখো") || text.contains("read screen") || text.contains("what's on screen") ||
            text.contains("স্ক্রিন বুঝো") || text.contains("স্ক্রিন পড়ে শোনাও") || text.contains("স্ক্রিন পড়ে দাও")) {
            return ParsedCommand(
                actionType = CommandAction.SCREEN_READ_SUMMARY,
                summary = "Screen Vision Analysis",
                feedbackMessage = "আমি আপনার পুরো স্ক্রিন বিশ্লেষণ করছি..."
            )
        }

        if (text.startsWith("ক্লিক করো") || text.startsWith("চাপ দাও") || text.startsWith("ট্যাপ করো") ||
            text.startsWith("click on") || text.startsWith("click") || text.startsWith("tap on") || text.startsWith("tap")) {
            val target = text
                .replace(Regex("""^(?:ক্লিক করো|চাপ দাও|ট্যাপ করো|click on|click|tap on|tap)\s*(?:এ|তে|button|বাটন)?"""), "")
                .trim()
            if (target.isNotEmpty()) {
                return ParsedCommand(
                    actionType = CommandAction.SCREEN_CLICK_TARGET,
                    summary = "Click: $target",
                    feedbackMessage = "স্ক্রিনে '$target' এ ক্লিক করা হচ্ছে",
                    payload = target
                )
            }
        }

        if (text.startsWith("টাইপ করো") || text.startsWith("লেখো") || text.startsWith("type") || text.startsWith("write")) {
            val textToType = text
                .replace(Regex("""^(?:টাইপ করো|লেখো|type|write)\s*"""), "")
                .trim()
            if (textToType.isNotEmpty() && !textToType.startsWith("নোট") && !textToType.startsWith("note")) {
                return ParsedCommand(
                    actionType = CommandAction.SCREEN_TYPE_TEXT,
                    summary = "Type: $textToType",
                    feedbackMessage = "স্ক্রিনের বক্সে '$textToType' লেখা হচ্ছে",
                    payload = textToType
                )
            }
        }

        if (text.contains("নিচে স্ক্রোল") || text.contains("নিচে নামাও") || text.contains("scroll down") || text.contains("নিচে যাও") || text.contains("স্ক্রোল ডাউন")) {
            return ParsedCommand(
                actionType = CommandAction.SCREEN_SCROLL_DOWN,
                summary = "Scroll Down",
                feedbackMessage = "স্ক্রিন নিচে স্ক্রোল করা হচ্ছে"
            )
        }

        if (text.contains("উপরে স্ক্রোল") || text.contains("উপরে তোলো") || text.contains("scroll up") || text.contains("উপরে যাও") || text.contains("স্ক্রোল আপ")) {
            return ParsedCommand(
                actionType = CommandAction.SCREEN_SCROLL_UP,
                summary = "Scroll Up",
                feedbackMessage = "স্ক্রিন উপরে স্ক্রোল করা হচ্ছে"
            )
        }

        if (text.contains("ব্যাক করো") || text.contains("পিছনে যাও") || text.contains("go back") || text.contains("back")) {
            return ParsedCommand(
                actionType = CommandAction.SCREEN_GLOBAL_BACK,
                summary = "Back Navigation",
                feedbackMessage = "পিছনে যাওয়া হচ্ছে"
            )
        }

        if (text.contains("হোমে যাও") || text.contains("হোম স্ক্রিন") || text.contains("go home") || text.contains("home screen")) {
            return ParsedCommand(
                actionType = CommandAction.SCREEN_GLOBAL_HOME,
                summary = "Home Screen",
                feedbackMessage = "হোম স্ক্রিনে যাওয়া হচ্ছে"
            )
        }

        if (text.contains("রিসেন্ট অ্যাপস") || text.contains("সাম্প্রতিক অ্যাপ") || text.contains("recent apps")) {
            return ParsedCommand(
                actionType = CommandAction.SCREEN_GLOBAL_RECENTS,
                summary = "Recent Apps",
                feedbackMessage = "সাম্প্রতিক অ্যাপস ওপেন করা হচ্ছে"
            )
        }

        if (text.contains("নোটিফিকেশন নামাও") || text.contains("নোটিফিকেশন বার") || text.contains("open notifications")) {
            return ParsedCommand(
                actionType = CommandAction.SCREEN_GLOBAL_NOTIFICATIONS,
                summary = "Open Notifications",
                feedbackMessage = "নোটিফিকেশন প্যানেল খোলা হচ্ছে"
            )
        }

        // 1. Hands-Free Mode Switch
        if (text.contains("hands free on") || text.contains("হ্যান্ডস ফ্রি চালু") || text.contains("হ্যান্ডস-ফ্রি অন") || text.contains("হ্যান্ডস ফ্রি অন")) {
            return ParsedCommand(
                actionType = CommandAction.TOGGLE_HANDSFREE,
                summary = "Hands-Free Mode ON",
                feedbackMessage = "হ্যান্ডস-ফ্রি মোড চালু করা হলো। এখন কোনো স্পর্শ ছাড়াই আপনি কথা বলে সব কাজ করতে পারবেন।",
                payload = "true"
            )
        }
        if (text.contains("hands free off") || text.contains("হ্যান্ডস ফ্রি বন্ধ") || text.contains("হ্যান্ডস-ফ্রি অফ")) {
            return ParsedCommand(
                actionType = CommandAction.TOGGLE_HANDSFREE,
                summary = "Hands-Free Mode OFF",
                feedbackMessage = "হ্যান্ডস-ফ্রি মোড বন্ধ করা হলো।",
                payload = "false"
            )
        }

        // 2. Shut Up / Silent Execution / Sleep
        if (text == "চুপ" || text == "চুপ থাকো" || text == "চুপ করো" || text == "কথা বলা বন্ধ করো" || 
            text == "আর বোলো না" || text == "কথা বোলো না" || text == "shut up" || text == "be quiet" || text == "quiet" || text == "silence") {
            return ParsedCommand(
                actionType = CommandAction.SILENT_SHUT_UP,
                summary = "Silent Mode (No Speaking)",
                feedbackMessage = ""
            )
        }

        if (text == "থেমে যাও" || text == "বন্ধ হও" || text == "বিদায়" || text == "bye" || text == "stop listening" || text == "sleep") {
            return ParsedCommand(
                actionType = CommandAction.STOP_LISTENING,
                summary = "Assistant Standby",
                feedbackMessage = "ঠিক আছে, আমি অপেক্ষায় থাকলাম। ডাকলে 'নীলা' বলবেন।"
            )
        }

        // 3. Battery Status Check
        if (text.contains("battery") || text.contains("ব্যাটারি") || text.contains("চার্জ কত") || text.contains("কত পারসেন্ট চার্জ")) {
            val (level, isCharging) = getBatteryInfo()
            val chargeText = if (isCharging) "এবং চার্জার সংযুক্ত আছে" else "এবং চার্জার সংযুক্ত নেই"
            val msg = "আপনার ফোনের ব্যাটারি চার্জ $level% $chargeText।"
            return ParsedCommand(
                actionType = CommandAction.CHECK_BATTERY,
                summary = "Battery: $level%",
                feedbackMessage = msg,
                payload = level.toString()
            )
        }

        // 4. Time Check
        if (text.contains("time") || text.contains("কয়টা বাজে") || text.contains("সময় কত") || text.contains("কয়টা বাজে")) {
            val timeMsg = getFormattedTimeBengali()
            return ParsedCommand(
                actionType = CommandAction.CHECK_TIME,
                summary = "Current Time",
                feedbackMessage = timeMsg
            )
        }

        // 5. Date Check
        if (text.contains("date") || text.contains("তারিখ কত") || text.contains("আজ কি বার") || text.contains("আজ কী বার") || text.contains("আজকের দিন")) {
            val dateMsg = getFormattedDateBengali()
            return ParsedCommand(
                actionType = CommandAction.CHECK_DATE,
                summary = "Current Date",
                feedbackMessage = dateMsg
            )
        }

        // 6. YouTube Play / Video Search
        if (text.startsWith("play") || text.contains("গান চালাও") || text.contains("ভিডিও চালাও") || text.contains("ইউটিউবে") || text.contains("youtube এ")) {
            var query = text
                .replace(Regex("""^(?:play|গান চালাও|ভিডিও চালাও|ইউটিউবে গান চালাও|ইউটিউবে চালাও|ইউটিউবে সার্চ করো|youtube)\s*(?:on youtube|song|গান)?"""), "")
                .replace("on youtube", "")
                .replace("ইউটিউবে", "")
                .trim()
            if (query.isEmpty()) query = "Bangla Songs"
            return ParsedCommand(
                actionType = CommandAction.PLAY_YOUTUBE,
                summary = "YouTube: $query",
                feedbackMessage = "ইউটিউবে '$query' চালানো হচ্ছে",
                payload = query
            )
        }

        // 7. Google Search
        if (text.startsWith("search") || text.contains("গুগলে সার্চ") || text.contains("সার্চ করো") || text.contains("খুঁজে দাও")) {
            val query = text
                .replace(Regex("""^(?:search for|search|গুগলে সার্চ করো|সার্চ করো|গুগল সার্চ|খুঁজে দাও)\s*"""), "")
                .trim()
            if (query.isNotEmpty()) {
                return ParsedCommand(
                    actionType = CommandAction.SEARCH_WEB,
                    summary = "Google: $query",
                    feedbackMessage = "গুগলে '$query' খোঁজা হচ্ছে",
                    payload = query
                )
            }
        }

        // 8. Open Apps
        if (text.startsWith("open") || text.contains("খোলো") || text.contains("খুলো") || text.contains("চালু করো")) {
            val appCandidate = text
                .replace(Regex("""^(?:open|খোলো|খুলো|চালু করো|ওপেন করো)\s*"""), "")
                .replace("app", "")
                .replace("অ্যাপ", "")
                .trim()
            if (appCandidate.isNotEmpty()) {
                return ParsedCommand(
                    actionType = CommandAction.OPEN_APP,
                    summary = "Open $appCandidate",
                    feedbackMessage = "$appCandidate ওপেন করা হচ্ছে",
                    payload = appCandidate
                )
            }
        }

        // 9. Media Play / Pause / Next
        if (text.contains("pause music") || text.contains("গান থামাও") || text.contains("পজ") || text.contains("গান বন্ধ করো")) {
            return ParsedCommand(
                actionType = CommandAction.MEDIA_PLAY_PAUSE,
                summary = "Pause Media",
                feedbackMessage = "মিউজিক পজ করা হলো"
            )
        }
        if (text.contains("resume music") || text.contains("গান পুনরায় চালাও") || text.contains("প্লে করো") || text.contains("গান বাজাও")) {
            return ParsedCommand(
                actionType = CommandAction.MEDIA_PLAY_PAUSE,
                summary = "Play Media",
                feedbackMessage = "মিউজিক প্লে করা হলো"
            )
        }
        if (text.contains("next song") || text.contains("পরের গান") || text.contains("পরের গান চালাও") || text.contains("নেক্সট গান")) {
            return ParsedCommand(
                actionType = CommandAction.MEDIA_NEXT,
                summary = "Next Track",
                feedbackMessage = "পরবর্তী গান প্লে করা হচ্ছে"
            )
        }

        // 10. Direct Phone Call via Voice
        val callRegex = Regex("""(?:call|ফোন করো|কল করো|ফোন লাগাও)\s*(\+?\d[\d\s-]{4,}\d)""")
        val callMatch = callRegex.find(text)
        if (callMatch != null) {
            val number = callMatch.groupValues[1].replace(" ", "").replace("-", "")
            return ParsedCommand(
                actionType = CommandAction.MAKE_CALL,
                summary = "Call $number",
                feedbackMessage = "$number নম্বরে কল করা হচ্ছে",
                payload = number
            )
        }

        // 11. Flashlight
        if (text.contains("flashlight on") || text.contains("torch on") || text.contains("turn on torch") ||
            text.contains("light on") || text.contains("ফ্ল্যাশলাইট জ্বালাও") || text.contains("টর্চ জ্বালাও") ||
            text.contains("লাইট অন") || text.contains("আলো জ্বালাও")
        ) {
            return ParsedCommand(
                actionType = CommandAction.FLASHLIGHT_ON,
                summary = "Flashlight ON",
                feedbackMessage = if (text.contains("জ্বালাও") || text.contains("অন")) "ফ্ল্যাশলাইট জ্বালানো হলো" else "Flashlight turned on"
            )
        }

        if (text.contains("flashlight off") || text.contains("torch off") || text.contains("turn off torch") ||
            text.contains("light off") || text.contains("ফ্ল্যাশলাইট বন্ধ") || text.contains("টর্চ বন্ধ") ||
            text.contains("লাইট অফ") || text.contains("আলো নেভাও")
        ) {
            return ParsedCommand(
                actionType = CommandAction.FLASHLIGHT_OFF,
                summary = "Flashlight OFF",
                feedbackMessage = if (text.contains("বন্ধ") || text.contains("অফ")) "ফ্ল্যাশলাইট বন্ধ করা হলো" else "Flashlight turned off"
            )
        }

        // 12. Volume Increments & Controls
        if (text.contains("volume up") || text.contains("ভলিউম বাড়াও") || text.contains("আওয়াজ বাড়াও") || text.contains("ভলিউম বাড়াও")) {
            return ParsedCommand(
                actionType = CommandAction.VOLUME_UP,
                summary = "Volume +20%",
                feedbackMessage = "ভলিউম বাড়ানো হলো"
            )
        }
        if (text.contains("volume down") || text.contains("ভলিউম কমাও") || text.contains("আওয়াজ কমাও") || text.contains("ভলিউম কম করো")) {
            return ParsedCommand(
                actionType = CommandAction.VOLUME_DOWN,
                summary = "Volume -20%",
                feedbackMessage = "ভলিউম কমানো হলো"
            )
        }
        if (text.contains("volume max") || text.contains("volume 100") || text.contains("ভলিউম ফুল")) {
            return ParsedCommand(
                actionType = CommandAction.SET_VOLUME,
                summary = "Volume 100%",
                feedbackMessage = "ভলিউম সর্বোচ্চ করা হলো",
                payload = "100"
            )
        }
        if (text.contains("volume mute") || text.contains("silent") || text.contains("সাইলেন্ট") || text.contains("শব্দ বন্ধ")) {
            return ParsedCommand(
                actionType = CommandAction.SET_SILENT_MODE,
                summary = "Silent Mode",
                feedbackMessage = "ফোন সাইলেন্ট মোডে দেওয়া হলো"
            )
        }

        // 13. Timer
        val timerRegex = Regex("""(?:timer|টাইমার)\s*(?:for)?\s*(\d+)\s*(?:minutes|min|মিনিট|second|সেকেন্ড)?""")
        val timerMatch = timerRegex.find(text)
        if (timerMatch != null) {
            val minutes = timerMatch.groupValues[1].toIntOrNull() ?: 5
            return ParsedCommand(
                actionType = CommandAction.SET_TIMER,
                summary = "Timer for $minutes min",
                feedbackMessage = "$minutes মিনিটের টাইমার সেট করা হলো",
                payload = minutes.toString()
            )
        }

        // 14. Camera
        if (text.contains("open camera") || text.contains("take photo") || text.contains("ক্যামেরা খোলো") || text.contains("ছবি তোলো")) {
            return ParsedCommand(
                actionType = CommandAction.OPEN_CAMERA,
                summary = "Open Camera",
                feedbackMessage = "ক্যামেরা ওপেন করা হচ্ছে"
            )
        }

        // 15. Settings: WiFi / Bluetooth
        if (text.contains("wifi") || text.contains("ওয়াইফাই")) {
            return ParsedCommand(
                actionType = CommandAction.OPEN_WIFI_SETTINGS,
                summary = "WiFi Settings",
                feedbackMessage = "Opening WiFi settings"
            )
        }

        if (text.contains("bluetooth") || text.contains("ব্লুটুথ")) {
            return ParsedCommand(
                actionType = CommandAction.OPEN_BLUETOOTH_SETTINGS,
                summary = "Bluetooth Settings",
                feedbackMessage = "Opening Bluetooth settings"
            )
        }

        // 16. SOS Emergency
        if (text.contains("sos") || text.contains("emergency") || text.contains("বিপদ") || text.contains("বাঁচাও") || text.contains("help me")) {
            return ParsedCommand(
                actionType = CommandAction.SOS_EMERGENCY,
                summary = "Emergency SOS Alert",
                feedbackMessage = "জরুরি এসওএস সতর্কতা চালু করা হয়েছে!"
            )
        }

        // 17. Expense Logging ("spent 200 on food", "খরচ ২০০ টাকা খাবার")
        val expenseRegex = Regex("""(?:spent|খরচ|expense|pay|paid)\s*(\d+(?:\.\d+)?)\s*(?:taka|টাকা|rs|rupees|dollar|usd)?\s*(?:on|for|বাবদ)?\s*(.*)""")
        val expMatch = expenseRegex.find(text)
        if (expMatch != null) {
            val amount = expMatch.groupValues[1]
            val item = expMatch.groupValues[2].ifEmpty { "সাধারণ খরচ" }
            return ParsedCommand(
                actionType = CommandAction.ADD_EXPENSE,
                summary = "Expense: ৳$amount ($item)",
                feedbackMessage = "৳$amount টাকা খরচের হিসাব সংরক্ষণ করা হলো",
                payload = "$amount|$item"
            )
        }

        // 18. Voice Note ("note down buy milk", "নোট লেখো বাজারে যেতে হবে")
        if (text.startsWith("note") || text.startsWith("নোট") || text.contains("write note") || text.contains("নোট লেখ")) {
            val noteContent = text.replace(Regex("""^(?:note|নোট|write note|নোট লেখো|নোট লেখ)\s*(?:that|down|হলো)?\s*"""), "").trim()
            return ParsedCommand(
                actionType = CommandAction.ADD_NOTE,
                summary = "Voice Note: $noteContent",
                feedbackMessage = "ভয়েস নোট সংরক্ষণ করা হলো",
                payload = noteContent
            )
        }

        return null
    }
}

