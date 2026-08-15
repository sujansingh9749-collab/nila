package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AssistantUiState
import com.example.ui.theme.AmbientMint
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerAmber
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SlateBorderDark
import com.example.ui.theme.SlateCardDark
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.util.Locale

@Composable
fun SettingsScreen(
    uiState: AssistantUiState,
    onSetWakeWord: (String) -> Unit,
    onSetSensitivity: (Float) -> Unit,
    onToggleWakeWord: (Boolean) -> Unit,
    onSetContinuousMode: (Boolean) -> Unit,
    onSetPersona: (String) -> Unit,
    onSetSpeechSpeed: (Float) -> Unit,
    onSetSpeechPitch: (Float) -> Unit,
    onSetSosContact: (String, String) -> Unit,
    onToggleFloatingMic: (Boolean) -> Unit,
    onTestAudio: () -> Unit,
    onTestWakeGlow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var wakeWordInput by remember { mutableStateOf(uiState.wakeWordPhrase) }
    var sosNameInput by remember { mutableStateOf(uiState.sosContactName) }
    var sosPhoneInput by remember { mutableStateOf(uiState.sosContactPhone) }

    val isBn = uiState.selectedLanguage.startsWith("bn")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = if (isBn) "অ্যাসিস্ট্যান্ট সেটিংস" else "Assistant Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = if (isBn) "ভয়েস, পার্সোনা ও ওয়েক-ওয়ার্ড কনফিগারেশন" else "Configure wake word, voice synthesis, persona and safety",
                    fontSize = 13.sp,
                    color = TextSecondaryDark
                )
            }
        }

        // 1. Wake-Word Activation Configuration
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Hearing, contentDescription = null, tint = AmbientMint, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isBn) "ওয়েক-ওয়ার্ড সক্রিয়তা" else "Wake-Word Detection",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = if (isBn) "নাম ধরে ডাকলে স্বয়ংক্রিয়ভাবে জাগবে" else "Activate when calling wake phrase",
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark
                                )
                            }
                        }

                        Switch(
                            checked = uiState.isWakeWordActive,
                            onCheckedChange = { onToggleWakeWord(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = AmbientMint)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = wakeWordInput,
                        onValueChange = {
                            wakeWordInput = it
                            onSetWakeWord(it)
                        },
                        label = { Text("Custom Wake Phrase", color = TextSecondaryDark) },
                        placeholder = { Text("Hey Assistant / Jarvis / Mitro", color = TextMutedDark) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateBorderDark,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sensitivity Slider
                    Text(
                        text = "Detection Sensitivity: ${(uiState.wakeWordSensitivity * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondaryDark
                    )
                    Slider(
                        value = uiState.wakeWordSensitivity,
                        onValueChange = { onSetSensitivity(it) },
                        valueRange = 0.2f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = AmbientMint,
                            activeTrackColor = AmbientMint,
                            inactiveTrackColor = SlateBorderDark
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onTestWakeGlow,
                        colors = ButtonDefaults.buttonColors(containerColor = SlateCardDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = AmbientMint, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "✨ ওয়েক-ওয়ার্ড গ্লো ইফেক্ট পরীক্ষা করুন" else "✨ Test Wake-Word Glow Indicator", color = AmbientMint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 2. Persona Selection
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "এআই পার্সোনা নির্বাচন" else "AI Voice Persona",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val personas = listOf(
                        "নীলা (Neela - Bengali Assistant)",
                        "মিত্রমণি (Bengali Assistant)",
                        "JARVIS AI",
                        "Friendly Copilot",
                        "Professional Executive"
                    )

                    personas.forEach { personaName ->
                        val isSelected = uiState.persona == personaName
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SlateCardDark else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonViolet else SlateBorderDark,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSetPersona(personaName) }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = personaName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonViolet else TextPrimaryDark
                            )
                        }
                    }
                }
            }
        }

        // 3. Speech Rate, Pitch & Continuous Conversation
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Continuous Conversation toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "ধারাবাহিক কথোপকথন" else "Continuous Conversation",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = if (isBn) "উত্তরের পর স্বয়ংক্রিয়ভাবে পরবর্তী প্রশ্নের জন্য শুনবে" else "Keep listening for follow-ups after speaking",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                        Switch(
                            checked = uiState.continuousMode,
                            onCheckedChange = { onSetContinuousMode(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = CyberCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speech Speed Slider
                    Text(
                        text = "Speech Speed: ${String.format(Locale.US, "%.1f", uiState.speechSpeed)}x",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondaryDark
                    )
                    Slider(
                        value = uiState.speechSpeed,
                        onValueChange = { onSetSpeechSpeed(it) },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Speech Pitch Slider
                    Text(
                        text = "Speech Pitch: ${String.format(Locale.US, "%.1f", uiState.speechPitch)}x",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondaryDark
                    )
                    Slider(
                        value = uiState.speechPitch,
                        onValueChange = { onSetSpeechPitch(it) },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onTestAudio,
                        colors = ButtonDefaults.buttonColors(containerColor = SlateCardDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Voice Synthesis", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 4. Floating Mic Orb Overlay Switch
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderDark, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBn) "ফ্লোটিং মাইক বাটন (Background)" else "Floating Mic Overlay Service",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = if (isBn) "অন্যান্য অ্যাপের উপর সর্বদা রেডি থাকবে" else "Keep assistant primed in background notification",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }

                    Switch(
                        checked = uiState.floatingMicEnabled,
                        onCheckedChange = { onToggleFloatingMic(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = CyberCyan)
                    )
                }
            }
        }

        // 5. Emergency SOS Contact Configuration
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DangerAmber.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Emergency, contentDescription = null, tint = DangerAmber, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "জরুরি এসওএস কন্টাক্ট" else "Emergency SOS Contact",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = sosNameInput,
                            onValueChange = {
                                sosNameInput = it
                                onSetSosContact(it, sosPhoneInput)
                            },
                            label = { Text("Contact Name", color = TextSecondaryDark) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DangerAmber,
                                unfocusedBorderColor = SlateBorderDark,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = sosPhoneInput,
                            onValueChange = {
                                sosPhoneInput = it
                                onSetSosContact(sosNameInput, it)
                            },
                            label = { Text("Phone Number", color = TextSecondaryDark) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DangerAmber,
                                unfocusedBorderColor = SlateBorderDark,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
