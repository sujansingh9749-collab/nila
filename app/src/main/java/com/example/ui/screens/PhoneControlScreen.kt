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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.WarningGold

@Composable
fun PhoneControlScreen(
    uiState: AssistantUiState,
    onToggleFlashlight: () -> Unit,
    onSetVolume: (Int) -> Unit,
    onSetTimer: (Int) -> Unit,
    onSetAlarm: (Int, Int) -> Unit,
    onMakeCall: (String) -> Unit,
    onSendSms: (String, String) -> Unit,
    onSendWhatsApp: (String, String) -> Unit,
    onOpenWiFi: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenCamera: () -> Unit,
    onTriggerSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dialNumber by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var autoReplyEnabled by remember { mutableStateOf(false) }
    var selectedAutoReplyTemplate by remember { mutableStateOf("I'm in a meeting right now, will call back later.") }

    val isBn = uiState.selectedLanguage.startsWith("bn")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = if (isBn) "ডিভাইস ও ফোন কন্ট্রোল" else "Device & Phone Control",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = if (isBn) "১০০% হ্যান্ডস-ফ্রি ভয়েস বা এক ট্যাপে ফোন পরিচালনা করুন" else "100% Hands-Free voice & quick touch phone control",
                    fontSize = 13.sp,
                    color = TextSecondaryDark
                )
            }
        }

        // 0. 100% Hands-Free Voice Hub Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Hands Free",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBn) "🎙️ সম্পূর্ণ হ্যান্ডস-ফ্রি ভয়েস কমান্ড" else "🎙️ 100% Hands-Free Voice Control",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = if (isBn) "ফোনে স্পর্শ না করেই কথা বলে কাজ করুন" else "Control entire phone without touching the screen",
                                    fontSize = 11.sp,
                                    color = AmbientMint
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val commandExamples = if (isBn) {
                        listOf(
                            "👁️ স্ক্রিন পড়া" to "\"স্ক্রিনে কি আছে?\", \"স্ক্রিন পড়ে শোনাও\"",
                            "🖱️ স্ক্রিন ক্লিক" to "\"ক্লিক করো [নাম]\", \"সাবমিট চাপ দাও\"",
                            "✍️ অটো টাইপ" to "\"টাইপ করো [লেখা]\", \"লেখো হ্যালো\"",
                            "📜 স্ক্রোলিং" to "\"নিচে স্ক্রোল করো\", \"উপরে যাও\"",
                            "📱 অ্যাপস" to "\"ইউটিউব / হোয়াটসঅ্যাপ / গুগল / ক্যালকুলেটর খোলো\"",
                            "🎵 মিডিয়া" to "\"ইউটিউবে গান চালাও\", \"গান থামাও\", \"পরের গান\"",
                            "🔋 ব্যাটারি" to "\"ব্যাটারি চার্জ কত?\", \"আজকের তারিখ কত?\"",
                            "🔊 ভলিউম" to "\"ভলিউম বাড়াও\", \"ভলিউম কমাও\", \"ফোন সাইলেন্ট করো\"",
                            "💡 টর্চ ও ক্যামেরা" to "\"ফ্ল্যাশলাইট জ্বালাও / বন্ধ\", \"ক্যামেরা খোলো\"",
                            "⏰ সময় ও এলার্ম" to "\"কয়টা বাজে?\", \"৫ মিনিটের টাইমার দাও\"",
                            "📞 কল ও SOS" to "\"কল করো ০১৭১...\", \"বিপদ / SOS\"",
                            "🛑 সহকারী বিরতি" to "\"থেমে যাও\" বা \"বিদায়\""
                        )
                    } else {
                        listOf(
                            "👁️ Screen Vision" to "\"What's on screen?\", \"Read screen\"",
                            "🖱️ Screen Click" to "\"Click on [Name]\", \"Tap submit\"",
                            "✍️ Screen Type" to "\"Type [text]\", \"Write hello\"",
                            "📜 Scrolling" to "\"Scroll down\", \"Scroll up\"",
                            "📱 Apps" to "\"Open YouTube / WhatsApp / Calculator\"",
                            "🎵 Media" to "\"Play music on YouTube\", \"Pause music\", \"Next song\"",
                            "🔋 Battery" to "\"Battery percentage\", \"What is today's date\"",
                            "🔊 Volume" to "\"Volume up\", \"Volume down\", \"Silent phone\"",
                            "💡 Hardware" to "\"Flashlight on/off\", \"Open camera\"",
                            "⏰ Clock" to "\"What time is it\", \"Timer 5 minutes\"",
                            "📞 Calling" to "\"Call +88017...\", \"Emergency SOS\"",
                            "🛑 Standby" to "\"Stop listening\" or \"Goodbye\""
                        )
                    }

                    commandExamples.forEach { (category, example) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SlateCardDark.copy(alpha = 0.5f))
                                .border(1.dp, SlateBorderDark, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan,
                                modifier = Modifier.width(84.dp)
                            )
                            Text(
                                text = example,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimaryDark
                            )
                        }
                    }
                }
            }
        }

        // 1. Hardware Toggles Grid (Flashlight + Camera + WiFi + Bluetooth)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Flashlight Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isFlashlightOn) WarningGold.copy(alpha = 0.15f) else ObsidianDark
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (uiState.isFlashlightOn) WarningGold else SlateBorderDark,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onToggleFlashlight() }
                        .testTag("toggle_flashlight_btn")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isFlashlightOn) WarningGold else SlateCardDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                contentDescription = "Flashlight",
                                tint = if (uiState.isFlashlightOn) SpaceBlack else TextSecondaryDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isBn) "টর্চ / ফ্ল্যাশ" else "Flashlight",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = if (uiState.isFlashlightOn) "ON" else "OFF",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isFlashlightOn) WarningGold else TextMutedDark
                        )
                    }
                }

                // Camera Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, SlateBorderDark, RoundedCornerShape(20.dp))
                        .clickable { onOpenCamera() }
                        .testTag("open_camera_btn")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SlateCardDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = CyberCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isBn) "ক্যামেরা" else "Camera",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = if (isBn) "ওপেন করুন" else "Launch",
                            fontSize = 12.sp,
                            color = CyberCyan
                        )
                    }
                }
            }
        }

        // Secondary Hardware Shortcuts (WiFi & Bluetooth)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // WiFi Settings
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, SlateBorderDark, RoundedCornerShape(16.dp))
                        .clickable { onOpenWiFi() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "WiFi",
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WiFi Settings",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimaryDark
                        )
                    }
                }

                // Bluetooth Settings
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, SlateBorderDark, RoundedCornerShape(16.dp))
                        .clickable { onOpenBluetooth() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = "Bluetooth",
                            tint = NeonViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bluetooth",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimaryDark
                        )
                    }
                }
            }
        }

        // 2. Audio & Volume Control
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Volume",
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "ভলিউম কন্ট্রোল" else "Media & Ring Volume",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }
                        Text(
                            text = "${uiState.mediaVolumePercent}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = uiState.mediaVolumePercent.toFloat(),
                        onValueChange = { onSetVolume(it.toInt()) },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = SlateBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Preset buttons (Mute, 50%, 100%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onSetVolume(0) },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateCardDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mute (0%)", fontSize = 11.sp, color = DangerAmber)
                        }
                        Button(
                            onClick = { onSetVolume(50) },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateCardDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("50%", fontSize = 11.sp, color = TextPrimaryDark)
                        }
                        Button(
                            onClick = { onSetVolume(100) },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateCardDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Max (100%)", fontSize = 11.sp, color = AmbientMint)
                        }
                    }
                }
            }
        }

        // 3. Timers & Alarms Presets
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timers",
                            tint = NeonViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "কুইক টাইমার ও অ্যালার্ম" else "Quick Timers & Alarms",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 30).forEach { mins ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SlateCardDark),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetTimer(mins) }
                                    .border(1.dp, SlateBorderDark, RoundedCornerShape(14.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${mins}m",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                    Text(
                                        text = if (isBn) "টাইমার" else "Timer",
                                        fontSize = 10.sp,
                                        color = TextMutedDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Morning Alarm
                    Button(
                        onClick = { onSetAlarm(7, 0) },
                        colors = ButtonDefaults.buttonColors(containerColor = SlateCardDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Morning Alarm",
                            tint = WarningGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "সকাল ৭:০০ টার অ্যালার্ম সেট করুন" else "Set 7:00 AM Morning Alarm",
                            fontSize = 12.sp,
                            color = TextPrimaryDark
                        )
                    }
                }
            }
        }

        // 4. Quick Call, SMS & WhatsApp Dispatcher
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (isBn) "কল ও মেসেজ পাঠানো" else "Call, SMS & WhatsApp Composer",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dialNumber,
                        onValueChange = { dialNumber = it },
                        label = { Text("Phone Number / Name", color = TextSecondaryDark) },
                        placeholder = { Text("+8801700000000 / 999", color = TextMutedDark) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateBorderDark,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = { Text("Message Text (Optional)", color = TextSecondaryDark) },
                        placeholder = { Text("Hello! How are you?", color = TextMutedDark) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateBorderDark,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onMakeCall(dialNumber.ifEmpty { "999" }) },
                            colors = ButtonDefaults.buttonColors(containerColor = AmbientMint),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = SpaceBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", fontSize = 12.sp, color = SpaceBlack, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onSendSms(dialNumber, messageText.ifEmpty { "Hello!" }) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Message, contentDescription = "SMS", tint = SpaceBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SMS", fontSize = 12.sp, color = SpaceBlack, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onSendWhatsApp(dialNumber, messageText.ifEmpty { "Hello from AI Assistant!" }) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "WhatsApp", tint = SpaceBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 12.sp, color = SpaceBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. Call Screening & Busy Auto-Reply Assistant
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhoneCallback,
                                contentDescription = "Screening",
                                tint = AmbientMint,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isBn) "অচেনা নম্বর স্ক্রিনিং ও অটো-রিপ্লাই" else "Call Screening & Auto-Reply",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = if (isBn) "ব্যস্ত থাকলে এআই দিয়ে স্বয়ংক্রিয় জবাব" else "AI Auto-attendant for unknown callers",
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark
                                )
                            }
                        }

                        Switch(
                            checked = autoReplyEnabled,
                            onCheckedChange = { autoReplyEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SpaceBlack,
                                checkedTrackColor = AmbientMint
                            )
                        )
                    }

                    if (autoReplyEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Auto-Reply Template:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondaryDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val templates = listOf(
                            "I'm in a meeting right now, will call back soon.",
                            "আমি এখন মিটিংয়ে আছি, পরে কথা বলছি।",
                            "Driving right now, please drop a text."
                        )

                        templates.forEach { t ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedAutoReplyTemplate == t) SlateCardDark else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (selectedAutoReplyTemplate == t) AmbientMint else SlateBorderDark,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedAutoReplyTemplate = t }
                                    .padding(10.dp)
                            ) {
                                Text(text = t, fontSize = 12.sp, color = TextPrimaryDark)
                            }
                        }
                    }
                }
            }
        }
    }
}
