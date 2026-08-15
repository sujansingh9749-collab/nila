package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmbientMint
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerAmber
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SunsetRose
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VoiceOrb(
    isListening: Boolean,
    isSpeaking: Boolean,
    isLoading: Boolean,
    audioRms: Float,
    isWakeWordGlow: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isWakeWordGlow) 1.38f else if (isListening) 1.28f else if (isSpeaking) 1.18f else 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isWakeWordGlow) 420 else if (isListening) 550 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isWakeWordGlow) 1400 else if (isLoading) 1800 else 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isWakeWordGlow) 650 else 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    val primaryColor = when {
        isWakeWordGlow -> AmbientMint
        isListening -> CyberCyan
        isSpeaking -> AmbientMint
        isLoading -> NeonViolet
        else -> ElectricBlue
    }

    val secondaryColor = when {
        isWakeWordGlow -> CyberCyan
        isListening -> NeonViolet
        isSpeaking -> CyberCyan
        isLoading -> SunsetRose
        else -> NeonViolet
    }

    val glowColor = when {
        isWakeWordGlow -> AmbientMint.copy(alpha = 0.8f)
        isListening -> CyberCyan.copy(alpha = 0.5f)
        isSpeaking -> AmbientMint.copy(alpha = 0.5f)
        isLoading -> NeonViolet.copy(alpha = 0.55f)
        else -> ElectricBlue.copy(alpha = 0.28f)
    }

    val haptic = LocalHapticFeedback.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(200.dp)
            .testTag("voice_orb_button")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 100.dp),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
    ) {
        // Dynamic pulsating Canvas ripples, soundwaves, and equalizer orbit
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.54f

            // Outer reactive glow ring
            val rmsBoost = (audioRms * 38f).coerceIn(0f, 45f)
            val outerRadius = (baseRadius * pulseScale) + rmsBoost + (if (isWakeWordGlow) 18.dp.toPx() else 0f)

            // Multi-layered Outer Soft Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor,
                        if (isWakeWordGlow) CyberCyan.copy(alpha = 0.4f) else secondaryColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = outerRadius * (if (isWakeWordGlow) 1.6f else 1.35f)
                ),
                radius = outerRadius * (if (isWakeWordGlow) 1.6f else 1.35f),
                center = center
            )

            // Dynamic Radiant Shockwave Rings
            if (isWakeWordGlow) {
                val wakeGlowRadius = baseRadius + (outerRadius * 1.35f - baseRadius) * waveOffset
                val wakeGlowAlpha = (1f - waveOffset).coerceIn(0f, 1f) * 0.95f
                drawCircle(
                    color = AmbientMint.copy(alpha = wakeGlowAlpha),
                    radius = wakeGlowRadius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )

                val wakeGlowRadius2 = baseRadius + (outerRadius * 1.2f - baseRadius) * ((waveOffset + 0.5f) % 1f)
                val wakeGlowAlpha2 = (1f - ((waveOffset + 0.5f) % 1f)).coerceIn(0f, 1f) * 0.75f
                drawCircle(
                    color = CyberCyan.copy(alpha = wakeGlowAlpha2),
                    radius = wakeGlowRadius2,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            // Radial Equalizer Rays around Orb when listening / speaking
            val rayCount = if (isWakeWordGlow) 32 else 24
            for (i in 0 until rayCount) {
                val angleDeg = (i * (360f / rayCount) + (rotationAngle * (if (i % 2 == 0) 1 else -1)))
                val angleRad = angleDeg * (Math.PI / 180f)
                val dynamicHeight = if (isListening || isSpeaking || isWakeWordGlow) {
                    8.dp.toPx() + (sin((i * 15f + rotationAngle * 3f) * (Math.PI / 180f)).toFloat() * 6.dp.toPx()) + (audioRms * 16.dp.toPx())
                } else {
                    4.dp.toPx() + (sin((i * 15f) * (Math.PI / 180f)).toFloat() * 2.dp.toPx())
                }

                val startDist = baseRadius + 4.dp.toPx()
                val endDist = startDist + dynamicHeight

                val startX = center.x + (cos(angleRad) * startDist).toFloat()
                val startY = center.y + (sin(angleRad) * startDist).toFloat()
                val endX = center.x + (cos(angleRad) * endDist).toFloat()
                val endY = center.y + (sin(angleRad) * endDist).toFloat()

                val rayAlpha = if (isListening || isSpeaking || isWakeWordGlow) 0.85f else 0.35f
                drawLine(
                    color = (if (i % 2 == 0) primaryColor else secondaryColor).copy(alpha = rayAlpha),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = (if (isWakeWordGlow) 2.5f else 2f).dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Dynamic concentric wave rings when listening or speaking
            if (isListening || isSpeaking || isWakeWordGlow) {
                val waveRadius1 = baseRadius + (outerRadius - baseRadius) * waveOffset
                val alpha1 = (1f - waveOffset).coerceIn(0f, 1f) * (if (isWakeWordGlow) 0.9f else 0.6f)
                drawCircle(
                    color = primaryColor.copy(alpha = alpha1),
                    radius = waveRadius1,
                    center = center,
                    style = Stroke(width = if (isWakeWordGlow) 3.5.dp.toPx() else 2.5.dp.toPx())
                )

                val waveRadius2 = baseRadius + (outerRadius - baseRadius) * ((waveOffset + 0.5f) % 1f)
                val alpha2 = (1f - ((waveOffset + 0.5f) % 1f)).coerceIn(0f, 1f) * (if (isWakeWordGlow) 0.8f else 0.6f)
                drawCircle(
                    color = (if (isWakeWordGlow) CyberCyan else secondaryColor).copy(alpha = alpha2),
                    radius = waveRadius2,
                    center = center,
                    style = Stroke(width = if (isWakeWordGlow) 2.5.dp.toPx() else 1.5.dp.toPx())
                )
            }
        }

        // Inner glowing holographic glass core orb
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(112.dp)
                .shadow(
                    elevation = if (isWakeWordGlow) 32.dp else if (isListening) 24.dp else 12.dp,
                    shape = CircleShape,
                    ambientColor = if (isWakeWordGlow) AmbientMint else primaryColor,
                    spotColor = if (isWakeWordGlow) CyberCyan else secondaryColor
                )
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = if (isWakeWordGlow) {
                            listOf(AmbientMint, CyberCyan, NeonViolet, AmbientMint)
                        } else {
                            listOf(primaryColor, secondaryColor, primaryColor, ElectricBlue, primaryColor)
                        }
                    )
                )
        ) {
            // Dark inner glass chamber with radial depth
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                SpaceBlack.copy(alpha = 0.75f),
                                SpaceBlack
                            )
                        )
                    )
            ) {
                val icon = when {
                    isListening || isWakeWordGlow -> Icons.Default.Mic
                    isSpeaking -> Icons.AutoMirrored.Filled.VolumeUp
                    isLoading -> Icons.Default.Stop
                    else -> Icons.Default.Mic
                }

                Icon(
                    imageVector = icon,
                    contentDescription = if (isListening) "Listening" else "Mic Trigger",
                    tint = if (isWakeWordGlow) AmbientMint else primaryColor,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
    }
}

