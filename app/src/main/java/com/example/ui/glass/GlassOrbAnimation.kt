package com.example.ui.glass

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.LiquidAzure
import com.example.ui.theme.LiquidCobalt
import com.example.ui.theme.LiquidCobaltDeep
import com.example.ui.theme.LiquidPrismCyan
import com.example.ui.theme.LiquidPrismPink
import com.example.ui.theme.PinkGlow
import com.example.ui.theme.VioletBright
import com.example.ui.theme.VioletGlow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class GlassStyle(val label: String) {
    BALLOON("Balloon"),
    TRINITY("Trinity Split"),
    LOTUS("Lotus Pulse")
}

@Composable
fun EnhancedLiquidGlassOrb(
    modifier: Modifier = Modifier,
    expansionFraction: Float = 0.5f,
    isBreathingActive: Boolean = true,
    style: GlassStyle = GlassStyle.BALLOON
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GlassBubblePhysics")

    // Lengthened durations for ultra-smooth, polished liquid easing
    val idleBreath by infiniteTransition.animateFloat(
        initialValue = 0.36f,
        targetValue = 0.68f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdleBubbleBreath"
    )

    val targetExpansion = if (isBreathingActive) {
        expansionFraction.coerceIn(0.22f, 1.0f)
    } else {
        idleBreath
    }

    // Switched to StiffnessVeryLow for a buttery, floating size transition
    val smoothExpansion by animateFloatAsState(
        targetValue = targetExpansion,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "SmoothBubbleExpansion"
    )

    val causticPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CausticWavePhase"
    )

    val wobblePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BubbleSurfaceWobble"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .liquidGlassBubbleShader(phase = causticPhase, expansion = smoothExpansion)
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxRadius = size.minDimension * 0.43f
        val minRadius = size.minDimension * 0.25f

        val density = 1f - smoothExpansion

        fun DrawScope.drawSingleBubble(
            cx: Float, cy: Float, radius: Float, bubbleDensity: Float,
            cPhase: Float, wPhase: Float, scaleX: Float = 1f, scaleY: Float = 1f
        ) {
            val bubbleExpansion = 1f - bubbleDensity
            val whiteAmt = bubbleExpansion * 0.35f
            val denseAmt = bubbleDensity * 0.40f

            withTransform({
                scale(scaleX, scaleY, Offset(cx, cy))
            }) {
                // OUTER GLOW COMPLETELY REMOVED HERE.
                // Only the crisp, polished glass bubble remains.

                // Organic 3D Bubble Contour Path - Increased steps to 120 for perfectly smooth edges
                val bubblePath = Path()
                val steps = 120
                for (i in 0..steps) {
                    val theta = (i.toFloat() / steps) * 2f * PI.toFloat()
                    val harmonic = sin(theta * 3f + wPhase) * 0.010f +
                            cos(theta * 2f + cPhase) * 0.008f
                    val r = radius * (1f + harmonic)
                    val px = cx + cos(theta) * r
                    val py = cy + sin(theta) * r
                    if (i == 0) bubblePath.moveTo(px, py) else bubblePath.lineTo(px, py)
                }
                bubblePath.close()

                clipPath(bubblePath) {
                    // Refractive Core Base
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.0f to Color.White.copy(alpha = 0.04f + whiteAmt),
                            0.52f to LiquidAzure.copy(alpha = 0.16f + whiteAmt),
                            0.78f to LiquidCobalt.copy(alpha = 0.38f + denseAmt),
                            0.92f to LiquidPrismCyan.copy(alpha = 0.50f + whiteAmt),
                            1.0f to Color.White.copy(alpha = 0.75f),
                            center = Offset(
                                cx + sin(cPhase) * radius * 0.08f,
                                cy + cos(cPhase) * radius * 0.08f
                            ),
                            radius = radius
                        ),
                        radius = radius * 1.04f,
                        center = Offset(cx, cy)
                    )

                    // Internal Liquid Caustic Ribbon
                    val wavePath = Path().apply {
                        val waterLevelY = cy + radius * (0.22f - 0.18f * bubbleExpansion)
                        moveTo(cx - radius * 1.2f, waterLevelY)
                        val waveSegments = 36 // Smoother internal wave
                        for (s in 0..waveSegments) {
                            val frac = s.toFloat() / waveSegments
                            val wx = (cx - radius * 1.2f) + (radius * 2.4f) * frac
                            val wy = waterLevelY +
                                    sin(frac * 2.5f * PI.toFloat() + cPhase) * (radius * 0.07f) +
                                    cos(frac * 1.5f * PI.toFloat() - wPhase) * (radius * 0.04f)
                            lineTo(wx, wy)
                        }
                        lineTo(cx + radius * 1.2f, cy + radius * 1.2f)
                        lineTo(cx - radius * 1.2f, cy + radius * 1.2f)
                        close()
                    }

                    drawPath(
                        path = wavePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CyanBright.copy(alpha = 0.34f + whiteAmt),
                                LiquidCobaltDeep.copy(alpha = 0.44f + denseAmt),
                                VioletBright.copy(alpha = 0.30f + whiteAmt)
                            ),
                            startY = cy - radius * 0.2f,
                            endY = cy + radius
                        )
                    )

                    // Secondary Prism Refraction
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                LiquidPrismPink.copy(alpha = 0.30f),
                                VioletGlow.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(cx + radius * 0.55f, cy - radius * 0.45f),
                            radius = radius * 0.75f
                        ),
                        radius = radius * 0.75f,
                        center = Offset(cx + radius * 0.55f, cy - radius * 0.45f)
                    )

                    // Micro-bubbles (Polished to be slightly softer)
                    val microBubbles = listOf(
                        Triple(-0.32f, 0.25f, 0.065f),
                        Triple(0.28f, 0.38f, 0.048f),
                        Triple(-0.12f, 0.52f, 0.038f),
                        Triple(0.35f, -0.10f, 0.052f),
                        Triple(-0.42f, -0.15f, 0.042f)
                    )
                    microBubbles.forEachIndexed { idx, (bxFrac, byFrac, rFrac) ->
                        val riseOffset = ((cPhase / (2f * PI.toFloat()) + idx * 0.2f) % 1f) * (radius * 0.45f)
                        val mx = cx + bxFrac * radius + sin(cPhase + idx) * (radius * 0.04f)
                        val my = cy + byFrac * radius - riseOffset
                        val mr = radius * rFrac

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.30f), CyanBright.copy(alpha = 0.45f)),
                                center = Offset(mx, my),
                                radius = mr
                            ),
                            radius = mr,
                            center = Offset(mx, my)
                        )
                        drawCircle(color = Color.White.copy(alpha = 0.75f), radius = mr * 0.28f, center = Offset(mx - mr * 0.32f, my - mr * 0.32f))
                    }

                    // Top-Left Bloom
                    val highlightCenter = Offset(cx - radius * 0.36f, cy - radius * 0.38f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.25f), Color.Transparent),
                            center = highlightCenter,
                            radius = radius * 0.44f
                        ),
                        radius = radius * 0.44f,
                        center = highlightCenter
                    )

                    // Specular Arcs
                    val arcInset = radius * 0.14f
                    val arcRect = Rect(left = cx - radius + arcInset, top = cy - radius + arcInset, right = cx + radius - arcInset, bottom = cy + radius - arcInset)

                    val topArcPath = Path().apply { addArc(arcRect, 198f, 58f) }
                    drawPath(
                        path = topArcPath,
                        brush = Brush.linearGradient(
                            listOf(Color.Transparent, Color.White.copy(alpha = 0.88f), CyanBright.copy(alpha = 0.60f), Color.Transparent),
                            start = Offset(arcRect.left, arcRect.top), end = Offset(arcRect.center.x, arcRect.top)
                        ),
                        style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    val bottomArcPath = Path().apply { addArc(arcRect, 18f, 52f) }
                    drawPath(
                        path = bottomArcPath,
                        brush = Brush.linearGradient(
                            listOf(Color.Transparent, LiquidPrismPink.copy(alpha = 0.45f), CyanBright.copy(alpha = 0.60f), Color.White.copy(alpha = 0.50f), Color.Transparent),
                            start = Offset(arcRect.center.x, arcRect.bottom), end = Offset(arcRect.right, arcRect.bottom)
                        ),
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Chromatic Rim (Crisper without glow)
                drawPath(
                    path = bubblePath,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.88f), LiquidPrismCyan.copy(alpha = 0.85f),
                            LiquidCobalt.copy(alpha = 0.70f), LiquidPrismPink.copy(alpha = 0.78f),
                            VioletBright.copy(alpha = 0.70f), CyanBright.copy(alpha = 0.85f), Color.White.copy(alpha = 0.88f)
                        ),
                        center = Offset(cx, cy)
                    ),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        when (style) {
            GlassStyle.BALLOON -> {
                val baseRadius = minRadius + (maxRadius - minRadius) * smoothExpansion
                val bounceY = sin(wobblePhase * 3.5f) * (8.dp.toPx() * density)
                val squeezeX = 1f - (0.22f * density)
                val squeezeY = 1f + (0.12f * density)

                drawSingleBubble(
                    cx = centerX, cy = centerY + bounceY, radius = baseRadius,
                    bubbleDensity = density, cPhase = causticPhase, wPhase = wobblePhase,
                    scaleX = squeezeX, scaleY = squeezeY
                )
            }
            GlassStyle.TRINITY -> {
                val splitDist = maxRadius * 0.6f * density
                val baseRadius = minRadius + (maxRadius * 0.82f - minRadius) * smoothExpansion

                for (i in 0 until 3) {
                    val angle = (i * 120f) * (PI.toFloat() / 180f) + wobblePhase * 0.4f
                    val bx = centerX + cos(angle) * splitDist * 1.3f
                    val by = centerY + sin(angle) * splitDist * 0.8f

                    drawSingleBubble(
                        cx = bx, cy = by, radius = baseRadius, bubbleDensity = density,
                        cPhase = causticPhase + i, wPhase = wobblePhase + i
                    )
                }
            }
            GlassStyle.LOTUS -> {
                val splitDist = maxRadius * 0.45f * density
                val baseRadius = minRadius + (maxRadius * 0.65f - minRadius) * smoothExpansion

                for (i in 0 until 5) {
                    val angle = (i * 72f) * (PI.toFloat() / 180f) - wobblePhase * 0.3f
                    val bx = centerX + cos(angle) * splitDist
                    val by = centerY + sin(angle) * splitDist

                    drawSingleBubble(
                        cx = bx, cy = by, radius = baseRadius, bubbleDensity = density * 0.5f,
                        cPhase = causticPhase - i, wPhase = wobblePhase + i
                    )
                }
            }
        }
    }
}