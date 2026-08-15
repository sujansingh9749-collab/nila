package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.AssistantViewModel
import com.example.ui.screens.AssistantHomeScreen
import com.example.ui.screens.MacrosScreen
import com.example.ui.screens.PhoneControlScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VoiceToolsScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SlateBorderDark
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark

class MainActivity : ComponentActivity() {

    private val viewModel: AssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle direct Assistant trigger from system
        handleAssistantIntent(intent)

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }

                val uiState by viewModel.uiState.collectAsState()
                val conversations by viewModel.conversations.collectAsState()
                val macros by viewModel.macros.collectAsState()
                val voiceNotes by viewModel.voiceNotes.collectAsState()
                val expenses by viewModel.expenses.collectAsState()
                val totalExpense by viewModel.totalExpense.collectAsState()

                // Permission Launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
                    if (audioGranted && uiState.isWakeWordActive) {
                        viewModel.toggleWakeWordDetection(true)
                    }
                }

                LaunchedEffect(Unit) {
                    val neededPermissions = mutableListOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        neededPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    val ungranted = neededPermissions.filter {
                        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                    }

                    if (ungranted.isNotEmpty()) {
                        permissionLauncher.launch(ungranted.toTypedArray())
                    }
                }

                LaunchedEffect(uiState.statusNotification) {
                    uiState.statusNotification?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = SpaceBlack,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SpaceBlack)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            NavigationBar(
                                containerColor = ObsidianDark,
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .border(1.dp, SlateBorderDark, RoundedCornerShape(24.dp))
                            ) {
                                val isBn = uiState.selectedLanguage.startsWith("bn")
                                val items = listOf(
                                    Triple(0, if (isBn) "সহায়ক" else "Voice", Icons.Default.Mic),
                                    Triple(1, if (isBn) "কন্ট্রোল" else "Control", Icons.Default.PhoneAndroid),
                                    Triple(2, if (isBn) "রুটিন" else "Macros", Icons.Default.SmartToy),
                                    Triple(3, if (isBn) "টুলস" else "Tools", Icons.Default.Build),
                                    Triple(4, if (isBn) "সেটিংস" else "Settings", Icons.Default.Settings)
                                )

                                items.forEach { (index, title, icon) ->
                                    val isSelected = uiState.activeTab == index
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { viewModel.setActiveTab(index) },
                                        icon = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = title,
                                                modifier = Modifier.size(20.dp),
                                                tint = if (isSelected) CyberCyan else TextMutedDark
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = title,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) CyberCyan else TextMutedDark
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier.testTag("nav_tab_$index")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (uiState.activeTab) {
                            0 -> AssistantHomeScreen(
                                uiState = uiState,
                                conversations = conversations,
                                onMicClick = { viewModel.toggleListening() },
                                onLanguageChange = { viewModel.setLanguage(it) },
                                onQuickPrompt = { viewModel.processVoiceInput(it) },
                                onSpeakResponse = { viewModel.speakText(it) },
                                onDeleteConversation = { viewModel.deleteConversation(it) },
                                onClearAll = { viewModel.clearHistory() },
                                onToggleWakeWord = { viewModel.toggleWakeWordDetection(it) },
                                onTriggerSos = { viewModel.triggerSOS() }
                            )
                            1 -> PhoneControlScreen(
                                uiState = uiState,
                                onToggleFlashlight = { viewModel.toggleFlashlight() },
                                onSetVolume = { viewModel.setMediaVolume(it) },
                                onSetTimer = { viewModel.phoneControl.setSystemTimer(it) },
                                onSetAlarm = { hour, min -> viewModel.phoneControl.setSystemAlarm(hour, min, "AI Assistant Alarm") },
                                onMakeCall = { viewModel.phoneControl.makeCall(it) },
                                onSendSms = { num, msg -> viewModel.phoneControl.sendSms(num, msg) },
                                onSendWhatsApp = { num, msg -> viewModel.phoneControl.sendWhatsApp(num, msg) },
                                onOpenWiFi = { viewModel.phoneControl.openWiFiSettings() },
                                onOpenBluetooth = { viewModel.phoneControl.openBluetoothSettings() },
                                onOpenCamera = { viewModel.phoneControl.openCamera() },
                                onTriggerSos = { viewModel.triggerSOS() }
                            )
                            2 -> MacrosScreen(
                                macros = macros,
                                onRunMacro = { viewModel.executeMacro(it) },
                                onToggleMacro = { viewModel.toggleMacro(it) },
                                onDeleteMacro = { viewModel.deleteMacro(it) },
                                onAddMacro = { name, trigger, desc, steps ->
                                    viewModel.addMacro(name, trigger, desc, steps)
                                }
                            )
                            3 -> VoiceToolsScreen(
                                uiState = uiState,
                                voiceNotes = voiceNotes,
                                expenses = expenses,
                                totalExpense = totalExpense,
                                onAddNote = { title, content -> viewModel.addVoiceNote(title, content) },
                                onDeleteNote = { viewModel.deleteVoiceNote(it) },
                                onAddExpense = { item, amt, cat -> viewModel.addExpense(item, amt, cat) },
                                onDeleteExpense = { viewModel.deleteExpense(it) },
                                onTranslate = { text, lang -> viewModel.translateAndSpeak(text, lang) },
                                onSpeak = { viewModel.speakText(it) }
                            )
                            4 -> SettingsScreen(
                                uiState = uiState,
                                onSetWakeWord = { viewModel.setWakeWord(it) },
                                onSetSensitivity = { viewModel.setWakeWordSensitivity(it) },
                                onToggleWakeWord = { viewModel.toggleWakeWordDetection(it) },
                                onSetContinuousMode = { viewModel.setContinuousMode(it) },
                                onSetPersona = { viewModel.setPersona(it) },
                                onSetSpeechSpeed = { viewModel.setSpeechSpeed(it) },
                                onSetSpeechPitch = { viewModel.setSpeechPitch(it) },
                                onSetSosContact = { name, phone -> viewModel.setSosContact(name, phone) },
                                onToggleFloatingMic = { viewModel.toggleFloatingMic(it) },
                                onTestAudio = {
                                    val testMsg = if (uiState.selectedLanguage.startsWith("bn")) {
                                        "নমস্কার! আমি আপনার স্মার্ট ভয়েস অ্যাসিস্ট্যান্ট নীলা।"
                                    } else {
                                        "Hello! I am your AI Voice Assistant, Nila, ready to assist."
                                    }
                                    viewModel.speakText(testMsg)
                                },
                                onTestWakeGlow = {
                                    viewModel.setActiveTab(0)
                                    viewModel.triggerWakeWordSimulate()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAssistantIntent(intent)
    }

    private fun handleAssistantIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == Intent.ACTION_ASSIST || intent.action == Intent.ACTION_VOICE_COMMAND) {
            viewModel.startListening()
        }
    }
}
