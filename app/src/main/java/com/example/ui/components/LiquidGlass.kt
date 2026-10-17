package com.example.ui.components

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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.glass.EnhancedLiquidGlassOrb
import com.example.ui.theme.BlueDroplet
import com.example.ui.theme.BlueWater
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.GlassBackgroundSubtle
import com.example.ui.theme.GlassBorderActiveCyan
import com.example.ui.theme.LiquidAzure
import com.example.ui.theme.LiquidCobalt
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VioletBright
import kotlin.math.PI
import kotlin.math.sin

/**
 * Unified Liquid Glass Card in com.example.ui.components — delegates to the
 * refractive AGSL-backed GlassCard in com.example.ui.glass so both packages share identical optics.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    borderBrush: Brush = FrostedGlassBorder,
    borderWidth: Dp = 1.2.dp,
    backgroundColor: Color = GlassBackgroundSubtle,
    onClick: (() -> Unit)? = null,
    testTag: String = "glass_card",
    content: @Composable BoxScope.() -> Unit
) {
    com.example.ui.glass.GlassCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        borderBrush = borderBrush,
        borderWidth = borderWidth,
        onClick = onClick,
        testTag = testTag,
        content = content
    )
}

/**
 * Pill-shaped minimal Liquid Glass action button with refractive active/inactive states.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: UnitFunction = {},
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    testTag: String = "glass_button"
) {
    val shape = RoundedCornerShape(50)
    val bgColor = if (isActive) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
    val borderColor = if (isActive) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minHeight = 40.dp)
            .shadow(
                elevation = if (isActive) 8.dp else 3.dp,
                shape = shape,
                ambientColor = Color(0xFF001F6B).copy(alpha = 0.35f),
                spotColor = CyanBright.copy(alpha = if (isActive) 0.35f else 0.15f)
            )
            .clip(shape)
            .background(bgColor)
            .border(1.1.dp, borderColor, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.25f)),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

typealias UnitFunction = () -> Unit

/**
 * Circular Refractive Liquid Glass Icon Action Button.
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    isActive: Boolean = false,
    testTag: String = "glass_icon_button",
    content: @Composable () -> Unit
) {
    val shape = CircleShape
    Box(
        modifier = modifier
            .testTag(testTag)
            .size(size)
            .shadow(
                elevation = if (isActive) 8.dp else 4.dp,
                shape = shape,
                ambientColor = Color(0xFF001F6B).copy(alpha = 0.35f),
                spotColor = CyanBright.copy(alpha = 0.25f)
            )
            .clip(shape)
            .background(
                if (isActive) {
                    Brush.radialGradient(
                        colors = listOf(
                            CyanBright.copy(alpha = 0.55f),
                            LiquidCobalt.copy(alpha = 0.50f),
                            VioletBright.copy(alpha = 0.40f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.20f),
                            LiquidAzure.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    )
                }
            )
            .border(
                1.1.dp,
                if (isActive) GlassBorderActiveCyan else FrostedGlassBorder,
                shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.25f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Signature Animated 3D Liquid Glass Bubble for Breathing and Meditation.
 * Delegates to EnhancedLiquidGlassOrb so all callers render the smooth, polished glass bubble.
 */
@Composable
fun LiquidGlassOrb(
    modifier: Modifier = Modifier,
    expansionFraction: Float = 0.5f,
    isBreathingActive: Boolean = true
) {
    EnhancedLiquidGlassOrb(
        modifier = modifier,
        expansionFraction = expansionFraction,
        isBreathingActive = isBreathingActive
    )
}

/**
 * Animated Refractive Water Glass Vessel Indicator.
 */
@Composable
fun GlassWaterIndicator(
    currentMl: Int,
    goalMl: Int,
    modifier: Modifier = Modifier
) {
    val progress = (currentMl.toFloat() / goalMl.coerceAtLeast(1)).coerceIn(0f, 1f)
    val infiniteTransition = rememberInfiniteTransition(label = "WaterLevelWave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaterWave"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val glassPath = Path().apply {
            moveTo(w * 0.18f, h * 0.1f)
            lineTo(w * 0.82f, h * 0.1f)
            lineTo(w * 0.74f, h * 0.92f)
            quadraticTo(w * 0.72f, h * 0.98f, w * 0.65f, h * 0.98f)
            lineTo(w * 0.35f, h * 0.98f)
            quadraticTo(w * 0.28f, h * 0.98f, w * 0.26f, h * 0.92f)
            close()
        }

        drawPath(
            path = glassPath,
            color = Color.White.copy(alpha = 0.08f)
        )

        val waterTopY = h * (0.92f - (0.80f * progress))
        val liquidPath = Path().apply {
            val leftX = w * (0.26f - (0.08f * progress))
            val rightX = w * (0.74f + (0.08f * progress))
            moveTo(leftX, waterTopY)
            val waveSteps = 24
            for (step in 0..waveSteps) {
                val frac = step.toFloat() / waveSteps
                val x = leftX + (rightX - leftX) * frac
                val y = waterTopY + sin(frac * 2 * PI.toFloat() + waveOffset) * (h * 0.025f)
                lineTo(x, y)
            }
            lineTo(w * 0.74f, h * 0.92f)
            quadraticTo(w * 0.72f, h * 0.98f, w * 0.65f, h * 0.98f)
            lineTo(w * 0.35f, h * 0.98f)
            quadraticTo(w * 0.28f, h * 0.98f, w * 0.26f, h * 0.92f)
            close()
        }

        drawPath(
            path = liquidPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    CyanBright.copy(alpha = 0.85f),
                    BlueDroplet.copy(alpha = 0.75f),
                    LiquidCobalt.copy(alpha = 0.85f)
                ),
                startY = waterTopY,
                endY = h
            )
        )

        // Left specular reflection line on the glass tumbler
        drawLine(
            color = Color.White.copy(alpha = 0.55f),
            start = Offset(w * 0.25f, h * 0.18f),
            end = Offset(w * 0.31f, h * 0.86f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawPath(
            path = glassPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.75f),
                    CyanGlow.copy(alpha = 0.45f),
                    Color.White.copy(alpha = 0.40f)
                )
            ),
            style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
