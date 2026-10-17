package com.example.ui.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GlassBorderActiveCyan
import com.example.ui.theme.GlassBorderGradient
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite

import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class MoodState(val label: String, val testId: String) {
    CALM("Calm", "mood_calm"),
    OKAY("Okay", "mood_okay"),
    LOW("Low", "mood_low"),
    ANXIOUS("Anxious", "mood_anxious"),
    TIRED("Tired", "mood_tired")
}

@Composable
fun MoodSelectorCard(
    selectedMood: MoodState,
    onMoodSelected: (MoodState) -> Unit,
    onMewMewClick: () -> Unit,
    onGamesClick: () -> Unit = {},
    onEmoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        testTag = "card_mood_selector"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoodState.values().forEach { mood ->
                    val isSelected = mood == selectedMood
                    MoodItem(
                        mood = mood,
                        isSelected = isSelected,
                        onClick = { onMoodSelected(mood) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onMewMewClick() }
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Mew Mew", color = TextWhite, fontSize = 12.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onGamesClick() }
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFD4AF5A).copy(alpha = 0.24f),
                                    Color(0xFFFFD54F).copy(alpha = 0.16f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFFD54F).copy(alpha = 0.6f),
                                    Color(0xFFD4AF5A).copy(alpha = 0.4f)
                                )
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("🎮 Games", color = Color(0xFFFFD54F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onEmoClick() }
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.22f),
                                    Color(0xFF00F5D4).copy(alpha = 0.14f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.6f),
                                    Color(0xFF00F5D4).copy(alpha = 0.4f)
                                )
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("🤖 EMO", color = Color(0xFF00F5D4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun MoodItem(
    mood: MoodState,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val ringColor by animateColorAsState(
        targetValue = if (isSelected) CyanBright else Color.Transparent,
        label = "MoodRingColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) CyanBright else TextMuted,
        label = "MoodTextColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(mood.testId)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, color = CyanGlow),
                onClick = onClick
            )
            .padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        Brush.radialGradient(
                            colors = listOf(
                                CyanGlow.copy(alpha = 0.45f),
                                Color(0x33102450)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x2E162B5E),
                                Color(0x18101E42)
                            )
                        )
                    }
                )
                .border(
                    width = if (isSelected) 1.8.dp else 1.dp,
                    brush = if (isSelected) GlassBorderActiveCyan else GlassBorderGradient,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Stylized vector face expression
            MoodFaceCanvas(mood = mood, isSelected = isSelected)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = mood.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun MoodFaceCanvas(
    mood: MoodState,
    isSelected: Boolean
) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val w = size.width
        val h = size.height
        val faceColor = if (isSelected) CyanBright else Color.White.copy(alpha = 0.85f)
        val strokeWidth = 1.8.dp.toPx()

        when (mood) {
            MoodState.CALM -> {
                // Serene curved closed smiling eyes
                val leftEye = Path().apply {
                    moveTo(w * 0.22f, h * 0.40f)
                    quadraticTo(w * 0.32f, h * 0.30f, w * 0.42f, h * 0.40f)
                }
                val rightEye = Path().apply {
                    moveTo(w * 0.58f, h * 0.40f)
                    quadraticTo(w * 0.68f, h * 0.30f, w * 0.78f, h * 0.40f)
                }
                drawPath(leftEye, faceColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                drawPath(rightEye, faceColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))

                // Gentle smile
                val mouth = Path().apply {
                    moveTo(w * 0.35f, h * 0.62f)
                    quadraticTo(w * 0.50f, h * 0.78f, w * 0.65f, h * 0.62f)
                }
                drawPath(mouth, faceColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))
            }

            MoodState.OKAY -> {
                // Open dot eyes
                drawCircle(faceColor, radius = 2.dp.toPx(), center = Offset(w * 0.32f, h * 0.38f))
                drawCircle(faceColor, radius = 2.dp.toPx(), center = Offset(w * 0.68f, h * 0.38f))

                // Open happy smile
                val mouth = Path().apply {
                    moveTo(w * 0.32f, h * 0.60f)
                    quadraticTo(w * 0.50f, h * 0.76f, w * 0.68f, h * 0.60f)
                }
                drawPath(mouth, faceColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))
            }

            MoodState.LOW -> {
                // Open dot eyes
                drawCircle(faceColor, radius = 2.dp.toPx(), center = Offset(w * 0.32f, h * 0.40f))
                drawCircle(faceColor, radius = 2.dp.toPx(), center = Offset(w * 0.68f, h * 0.40f))

                // Flat / slight frown mouth
                val mouth = Path().apply {
                    moveTo(w * 0.34f, h * 0.68f)
                    quadraticTo(w * 0.50f, h * 0.62f, w * 0.66f, h * 0.68f)
                }
                drawPath(mouth, faceColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))
            }

            MoodState.ANXIOUS -> {
                // Wide eyes
                drawCircle(faceColor, radius = 2.5.dp.toPx(), center = Offset(w * 0.30f, h * 0.38f))
                drawCircle(faceColor, radius = 2.5.dp.toPx(), center = Offset(w * 0.70f, h * 0.38f))

                // Wavy nervous mouth
                val mouth = Path().apply {
                    moveTo(w * 0.30f, h * 0.66f)
                    quadraticTo(w * 0.40f, h * 0.60f, w * 0.50f, h * 0.66f)
                    quadraticTo(w * 0.60f, h * 0.72f, w * 0.70f, h * 0.66f)
                }
                drawPath(mouth, faceColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))
            }

            MoodState.TIRED -> {
                // Sleepy downward angled closed eyes
                val leftEye = Path().apply {
                    moveTo(w * 0.22f, h * 0.45f)
                    quadraticTo(w * 0.32f, h * 0.50f, w * 0.42f, h * 0.45f)
                }
                val rightEye = Path().apply {
                    moveTo(w * 0.58f, h * 0.45f)
                    quadraticTo(w * 0.68f, h * 0.50f, w * 0.78f, h * 0.45f)
                }
                drawPath(leftEye, faceColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                drawPath(rightEye, faceColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))

                // Little z Z marks
                drawLine(faceColor, Offset(w * 0.72f, h * 0.20f), Offset(w * 0.82f, h * 0.20f), strokeWidth * 0.7f)
                drawLine(faceColor, Offset(w * 0.82f, h * 0.20f), Offset(w * 0.72f, h * 0.27f), strokeWidth * 0.7f)
                drawLine(faceColor, Offset(w * 0.72f, h * 0.27f), Offset(w * 0.82f, h * 0.27f), strokeWidth * 0.7f)

                // Soft relaxed mouth
                val mouth = Path().apply {
                    moveTo(w * 0.38f, h * 0.68f)
                    quadraticTo(w * 0.50f, h * 0.72f, w * 0.62f, h * 0.68f)
                }
                drawPath(mouth, faceColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))
            }
        }
    }
}
