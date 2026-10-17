package com.example.ui.glass

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.PinkGlow
import com.example.ui.theme.VioletBright
import com.example.ui.theme.VioletGlow
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Animated audio waveform visualizer for Calm Sounds and ambient audio.
 */
@Composable
fun AudioWaveformVisualizer(
    modifier: Modifier = Modifier.fillMaxWidth().height(42.dp),
    isPlaying: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnimation")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AudioPhase"
    )

    Canvas(modifier = modifier) {
        val barCount = 38
        val spacing = size.width / barCount
        val barWidth = 2.2.dp.toPx()
        val centerY = size.height / 2f

        for (i in 0 until barCount) {
            val frac = i.toFloat() / barCount
            // Bell curve envelope centered in the middle
            val envelope = sin(frac * PI.toFloat())
            val oscillation = if (isPlaying) {
                sin(frac * 4 * PI.toFloat() + wavePhase)
            } else 0.2f

            val heightFactor = (0.25f + 0.75f * abs(oscillation)) * envelope
            val barHeight = (size.height * 0.90f * heightFactor).coerceAtLeast(3.dp.toPx())
            val x = i * spacing + spacing / 2f

            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CyanBright,
                        VioletBright,
                        PinkGlow.copy(alpha = 0.8f)
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
