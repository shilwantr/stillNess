package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassIconButton
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.GlassBackgroundBright
import com.example.ui.theme.GlassBorderActiveCyan
import com.example.ui.theme.GlassBorderGradient
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.PinkGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VioletBright
import com.example.ui.theme.VioletGlow
import com.example.ui.viewmodel.WellnessViewModel
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun VoiceCallScreen(
    viewModel: WellnessViewModel,
    onCallEnded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val callState by viewModel.callState.collectAsState()

    val durationMin = callState.durationSeconds / 60
    val durationSec = callState.durationSeconds % 60
    val durationStr = String.format("%02d:%02d", durationMin, durationSec)

    val infiniteTransition = rememberInfiniteTransition(label = "VoiceVisualizer")
    val visualizerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "VisualizerPhase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark)
            .statusBarsPadding()
            .padding(24.dp)
            .testTag("voice_call_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Security & Encrypted Session Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(0.8.dp, FrostedGlassBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Encrypted",
                    tint = CyanBright,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = callState.connectionQuality,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (!callState.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PinkGlow.copy(alpha = 0.20f))
                        .border(1.dp, PinkGlow.copy(alpha = 0.60f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = PinkGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = callState.errorMessage ?: "",
                        color = Color(0xFFFECDD3),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Therapist Details & Avatar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Pulsing Audio Avatar Halo
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    CyanGlow.copy(alpha = 0.4f),
                                    VioletGlow.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(1.5.dp, GlassBorderActiveCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Therapist Voice",
                        tint = TextWhite,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = callState.therapistName.ifEmpty { "Dr. Elena Vance, Psy.D." },
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = callState.therapistTitle.ifEmpty { "Licensed Clinical Psychologist" },
                    color = TextMuted,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Call Duration Counter
                Text(
                    text = durationStr,
                    color = CyanBright,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }

            // Real-Time Audio Frequency Wave Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barCount = 28
                    val barWidth = 3.dp.toPx()
                    val totalWidth = barCount * barWidth * 2f
                    val startX = (size.width - totalWidth) / 2f
                    val centerY = size.height / 2f

                    for (i in 0 until barCount) {
                        val x = startX + i * barWidth * 2f
                        val wave = if (callState.isMuted) 0.1f else {
                            val freq = sin(i * 0.35f + visualizerPhase)
                            (0.2f + 0.8f * kotlin.math.abs(freq)).coerceIn(0.1f, 1f)
                        }
                        val barHeight = size.height * 0.7f * wave

                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    CyanBright,
                                    VioletBright,
                                    PinkGlow
                                ),
                                startY = centerY - barHeight / 2f,
                                endY = centerY + barHeight / 2f
                            ),
                            start = Offset(x, centerY - barHeight / 2f),
                            end = Offset(x, centerY + barHeight / 2f),
                            strokeWidth = barWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Controls Row (Mute, End Call, Speaker)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute Mic Button
                GlassIconButton(
                    onClick = { viewModel.toggleCallMute() },
                    size = 56.dp,
                    isActive = callState.isMuted,
                    testTag = "btn_call_mute"
                ) {
                    Icon(
                        imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (callState.isMuted) PinkGlow else TextWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // End Call Button (Large Red/Pink glowing circle)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFEF4444),
                                    Color(0xFFB91C1C)
                                )
                            )
                        )
                        .clickable {
                            viewModel.endCall()
                            onCallEnded()
                        }
                        .testTag("btn_end_call"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Voice Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Speakerphone Toggle
                GlassIconButton(
                    onClick = { viewModel.toggleCallSpeaker() },
                    size = 56.dp,
                    isActive = callState.isSpeakerOn,
                    testTag = "btn_call_speaker"
                ) {
                    Icon(
                        imageVector = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = "Speaker",
                        tint = if (callState.isSpeakerOn) CyanBright else TextWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
