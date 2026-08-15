package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ConversationMessage
import com.example.ui.AssistantUiState
import com.example.ui.components.VoiceOrb
import com.example.ui.theme.AmbientMint
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerAmber
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SlateBorderDark
import com.example.ui.theme.SlateCardDark
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SunsetRose
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssistantHomeScreen(
    uiState: AssistantUiState,
    conversations: List<ConversationMessage>,
    onMicClick: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onQuickPrompt: (String) -> Unit,
    onSpeakResponse: (String) -> Unit,
    onDeleteConversation: (Long) -> Unit,
    onClearAll: () -> Unit,
    onToggleWakeWord: (Boolean) -> Unit,
    onTriggerSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBn = uiState.selectedLanguage.startsWith("bn")
    val clipboardManager = LocalClipboardManager.current

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingText = if (isBn) {
        when (currentHour) {
            in 4..11 -> "শুভ সকাল"
            in 12..16 -> "শুভ দুপুর"
            in 17..20 -> "শুভ সন্ধ্যা"
            else -> "শুভ রাত্রি"
        }
    } else {
        when (currentHour) {
            in 4..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Personalized Header with AI Avatar & Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Greeting & AI Identifier
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(CyberCyan, NeonViolet)
                                )
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(ObsidianDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Nila AI",
                                tint = CyberCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = greetingText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = if (isBn) "নীলা এআই সহকারী" else "Nila AI Assistant",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                }

                // Language Selector Glass Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(SlateCardDark)
                        .border(1.dp, SlateBorderDark, RoundedCornerShape(22.dp))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isBn) CyberCyan else Color.Transparent)
                            .clickable { onLanguageChange("bn-BD") }
                            .padding(horizontal = 11.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "বাংলা",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBn) SpaceBlack else TextSecondaryDark
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (!isBn) ElectricBlue else Color.Transparent)
                            .clickable { onLanguageChange("en-US") }
                            .padding(horizontal = 11.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "EN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isBn) SpaceBlack else TextSecondaryDark
                        )
                    }
                }
            }
        }

        // 2. Central Voice Interaction Section with Hero Orb & Glowing Wake Indicator
        item {
            val isGlow = uiState.isWakeWordGlowActive
            val infiniteTransition = rememberInfiniteTransition(label = "pulse_badge")
            val badgeAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "badge_alpha"
            )

            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ObsidianDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        if (isGlow) 2.dp else 1.dp,
                        if (isGlow) AmbientMint else SlateBorderDark,
                        RoundedCornerShape(32.dp)
                    )
                    .shadow(
                        elevation = if (isGlow) 24.dp else 8.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = if (isGlow) AmbientMint else CyberCyan.copy(alpha = 0.2f),
                        spotColor = if (isGlow) AmbientMint else CyberCyan.copy(alpha = 0.2f)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Wake Status Bar inside Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isGlow) AmbientMint.copy(alpha = 0.2f) else SlateCardDark)
                                .border(
                                    1.dp,
                                    if (isGlow) AmbientMint else if (uiState.isWakeWordActive) AmbientMint.copy(alpha = 0.6f) else SlateBorderDark,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onToggleWakeWord(!uiState.isWakeWordActive) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("wake_word_badge")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (uiState.isWakeWordActive) AmbientMint.copy(alpha = badgeAlpha) else TextMutedDark
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isGlow) "✨ 'নীলা' সাড়া দিচ্ছে!" else if (uiState.isWakeWordActive) "Wake: '${uiState.wakeWordPhrase}'" else "Wake Word Off",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isGlow || uiState.isWakeWordActive) AmbientMint else TextSecondaryDark
                            )
                        }

                        // Small SOS Quick Trigger on top right
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(DangerAmber.copy(alpha = 0.12f))
                                .border(1.dp, DangerAmber.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .clickable { onTriggerSos() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("quick_sos_pill")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "SOS",
                                tint = DangerAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SOS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DangerAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Glowing Wake-Word Banner Trigger Indicator
                    AnimatedVisibility(
                        visible = uiState.isWakeWordGlowActive,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            AmbientMint.copy(alpha = 0.25f),
                                            CyberCyan.copy(alpha = 0.35f),
                                            AmbientMint.copy(alpha = 0.25f)
                                        )
                                    )
                                )
                                .border(
                                    1.5.dp,
                                    AmbientMint,
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Wake Word Active",
                                    tint = AmbientMint,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.wakeWordDetectedPrompt ?: if (isBn) "✨ 'নীলা' ওয়েক-ওয়ার্ড শনাক্ত হয়েছে!" else "✨ 'Nila' wake word detected!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmbientMint
                                )
                            }
                        }
                    }

                    Text(
                        text = when {
                            uiState.isWakeWordGlowActive -> if (isBn) "✨ 'নীলা' সাড়া দিয়েছে! বলুন..." else "✨ 'Nila' is listening..."
                            uiState.isListening -> if (isBn) "🎙️ শুনছি... বলুন" else "🎙️ Listening... Speak now"
                            uiState.isGeminiLoading -> if (isBn) "🧠 চিন্তা করছি..." else "🧠 Gemini is thinking..."
                            uiState.isSpeaking -> if (isBn) "🔊 উত্তর দিচ্ছি..." else "🔊 Speaking..."
                            else -> if (isBn) "মাইকে ট্যাপ করুন বা বলুন \"${uiState.wakeWordPhrase}\"" else "Tap orb or say \"${uiState.wakeWordPhrase}\""
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            uiState.isWakeWordGlowActive -> AmbientMint
                            uiState.isListening -> CyberCyan
                            uiState.isSpeaking -> AmbientMint
                            uiState.isGeminiLoading -> NeonViolet
                            else -> TextSecondaryDark
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Animated Futuristic Voice Orb with Wake Word Glowing Aura
                    VoiceOrb(
                        isListening = uiState.isListening,
                        isSpeaking = uiState.isSpeaking,
                        isLoading = uiState.isGeminiLoading,
                        audioRms = uiState.audioRms,
                        isWakeWordGlow = uiState.isWakeWordGlowActive,
                        onClick = onMicClick
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Live Speech Transcript Box
                    AnimatedVisibility(
                        visible = uiState.liveTranscript.isNotEmpty(),
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(SlateCardDark)
                                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Transcript",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "\"${uiState.liveTranscript}\"",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimaryDark
                                )
                            }
                        }
                    }

                    // Last AI Response Bubble (if available)
                    if (uiState.lastResponse.isNotEmpty() && !uiState.isListening) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            SlateCardDark,
                                            ObsidianDark
                                        )
                                    )
                                )
                                .border(1.dp, NeonViolet.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "AI",
                                            tint = NeonViolet,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = uiState.persona,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonViolet
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { clipboardManager.setText(AnnotatedString(uiState.lastResponse)) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy Text",
                                                tint = TextMutedDark,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onSpeakResponse(uiState.lastResponse) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = "Replay Audio",
                                                tint = CyberCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = uiState.lastResponse,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    color = TextPrimaryDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Action Voice Command Chips
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "দ্রুত ভয়েস কমান্ড" else "Quick Voice Commands",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = if (isBn) "ট্যাপ করে শুনুন" else "Tap to speak",
                        fontSize = 11.sp,
                        color = TextMutedDark
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                val chips = if (isBn) {
                    listOf(
                        "👁️ স্ক্রিন পড়ে শোনাও",
                        "🖱️ স্ক্রিনে ক্লিক করো",
                        "✍️ টেক্সট বক্সে টাইপ করো",
                        "📜 নিচে স্ক্রোল করো",
                        "🔋 ব্যাটারি চার্জ কত",
                        "🕒 কয়টা বাজে",
                        "🎵 ইউটিউবে গান চালাও",
                        "📱 হোয়াটসঅ্যাপ খোলো",
                        "🌐 গুগলে সার্চ করো",
                        "💡 ফ্ল্যাশলাইট জ্বালাও",
                        "🔊 ভলিউম বাড়াও",
                        "⏰ ৫ মিনিটের টাইমার",
                        "📝 নোট: বাজারে যেতে হবে",
                        "💰 খরচ ৫০০ টাকা খাবার",
                        "🎭 একটি বাংলা কবিতা শোনাও",
                        "🚨 বিপদ / SOS"
                    )
                } else {
                    listOf(
                        "👁️ Read current screen",
                        "🖱️ Click on screen target",
                        "✍️ Type in text box",
                        "📜 Scroll down",
                        "🔋 Battery percentage",
                        "🕒 What time is it",
                        "🎵 Play music on YouTube",
                        "📱 Open WhatsApp",
                        "🌐 Search on Google",
                        "💡 Turn on flashlight",
                        "🔊 Volume up",
                        "⏰ Set timer 5 minutes",
                        "📝 Note down buy milk",
                        "💰 Spent 300 on food",
                        "🎭 Tell me an inspiring poem",
                        "🚨 Emergency SOS"
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chips.forEach { chipText ->
                        val isSos = chipText.contains("SOS") || chipText.contains("বিপদ")
                        SuggestionChip(
                            onClick = {
                                if (isSos) {
                                    onTriggerSos()
                                } else {
                                    onQuickPrompt(chipText.replace(Regex("""^[^\w\s\u0980-\u09FF]+"""), "").trim())
                                }
                            },
                            label = {
                                Text(
                                    text = chipText,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSos) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSos) DangerAmber else TextPrimaryDark
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSos) DangerAmber.copy(alpha = 0.15f) else SlateCardDark
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = if (isSos) DangerAmber else SlateBorderDark
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("quick_chip_${chipText.take(6)}")
                        )
                    }
                }
            }
        }

        // 4. Conversation History Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "সাম্প্রতিক কথোপকথন (${conversations.size})" else "Recent Interactions (${conversations.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryDark
                )

                if (conversations.isNotEmpty()) {
                    Text(
                        text = if (isBn) "সব মুছুন" else "Clear All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = DangerAmber,
                        modifier = Modifier
                            .clickable { onClearAll() }
                            .padding(4.dp)
                            .testTag("clear_history_btn")
                    )
                }
            }
        }

        // Conversation history items
        if (conversations.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SlateCardDark.copy(alpha = 0.5f))
                        .border(1.dp, SlateBorderDark, RoundedCornerShape(20.dp))
                        .padding(vertical = 28.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = TextMutedDark,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "এখনো কোনো কথোপকথন হয়নি। শুরু করতে মাইকে চাপ দিন!" else "No history yet. Tap the orb to ask a question or issue a command!",
                            fontSize = 13.sp,
                            color = TextMutedDark
                        )
                    }
                }
            }
        } else {
            items(conversations, key = { it.id }) { msg ->
                ConversationItemCard(
                    message = msg,
                    onSpeak = { onSpeakResponse(msg.response) },
                    onDelete = { onDeleteConversation(msg.id) }
                )
            }
        }
    }
}

@Composable
fun ConversationItemCard(
    message: ConversationMessage,
    onSpeak: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    val clipboardManager = LocalClipboardManager.current

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianDark),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorderDark, RoundedCornerShape(22.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // User Query Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "User",
                            tint = ElectricBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = message.query,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryDark
                    )
                }

                Text(
                    text = timeStr,
                    fontSize = 11.sp,
                    color = TextMutedDark
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Response Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(NeonViolet.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = NeonViolet,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = message.response,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = TextSecondaryDark
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(message.response)) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = TextMutedDark,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Play",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TextMutedDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Action Tag (if any)
            if (!message.intentAction.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlateCardDark)
                        .border(1.dp, AmbientMint.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = AmbientMint,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = message.intentAction,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = AmbientMint
                        )
                    }
                }
            }
        }
    }
}

