package com.example.ui.glass

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.remember
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ambient Theme choices for the Living Liquid Glass backdrop.
 * Updated to feature Option A (Midnight Sage), Option B (Nordic Twilight), and Option C (Warm Linen & Olive).
 */
enum class AmbientTheme(val label: String, val subtitle: String) {
    AURORA_SILK("NT", "Nordic Twilight: Abyssal navy, moonlight blue & soft dusk lavender"),
    FERRIS_WHEEL("FW", "Night Ferris Wheel: Cinemagraph of an illuminated Ferris wheel against a pure black sky"),
    TIME_DYNAMIC("TD", "Time Dynamic: Adaptive morning, afternoon & evening glass waves"),
    LOOPING_TRIOS("LT", "Looping Trios: Deep obsidian dark with animated geometry")
}

@Composable
fun CosmicHorizonCanvas(
    modifier: Modifier = Modifier,
    theme: AmbientTheme = AmbientTheme.AURORA_SILK
) {
    if (theme == AmbientTheme.LOOPING_TRIOS) {
        LoopingTriosBackground(modifier = modifier)
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "LiquidAuroraMotion")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (theme) {
            AmbientTheme.AURORA_SILK -> drawNordicTwilight(w, h, phase1, phase2, pulse)
            AmbientTheme.FERRIS_WHEEL -> drawFerrisWheel(w, h, phase1, phase2, pulse)
            AmbientTheme.TIME_DYNAMIC -> drawTimeDynamic(w, h, phase1, phase2, pulse)
            AmbientTheme.LOOPING_TRIOS -> Unit // Handled via separate Composable call if needed, or by Canvas draw directly
        }

        if (theme != AmbientTheme.LOOPING_TRIOS) {
            drawFloatingLightMotes(w, h, phase1)
        }
    }
}

/**
 * Option A: Midnight Sage & Warm Sand (Best All-Around Sanctuary)
 * Background Mesh Gradient: Deep Forest Slate (#0F1715) blending softly into Muted Pine (#182420).
 * Accents: Celadon Sage (#8FA89B), Eucalyptus (#A3C1AD), Warm Sand (#D4B499), Pale Champagne (#E6D7C3).
 */
private fun DrawScope.drawMidnightSage(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float
) {
    // 1. Deep Forest Slate (#0F1715) into Muted Pine (#182420)
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF0F1715),
                Color(0xFF131F1C),
                Color(0xFF182420),
                Color(0xFF0D1412)
            ),
            start = Offset.Zero,
            end = Offset(w, h)
        )
    )

    // 2. Top-left Eucalyptus & Celadon Sage ambient light bloom
    val sageCenter = Offset(
        x = w * (0.22f + 0.06f * sin(p1)),
        y = h * (0.18f + 0.04f * cos(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFA3C1AD).copy(alpha = 0.32f * pulse), // Eucalyptus
                Color(0xFF8FA89B).copy(alpha = 0.18f),         // Celadon Sage
                Color.Transparent
            ),
            center = sageCenter,
            radius = w * 0.85f
        ),
        center = sageCenter,
        radius = w * 0.85f
    )

    // 3. Sweeping Warm Sand & Sage Organic Wave Ribbon
    val sageWave = Path().apply {
        moveTo(0f, h * 0.68f)
        cubicTo(
            w * 0.28f,
            h * (0.52f + 0.05f * sin(p1)),
            w * 0.62f,
            h * (0.18f + 0.05f * cos(p2)),
            w,
            h * 0.06f
        )
        lineTo(w, h * 0.64f)
        cubicTo(
            w * 0.68f,
            h * (0.54f - 0.04f * sin(p2)),
            w * 0.34f,
            h * (0.82f + 0.04f * cos(p1)),
            0f,
            h * 0.94f
        )
        close()
    }

    drawPath(
        path = sageWave,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF8FA89B).copy(alpha = 0.45f), // Celadon Sage
                Color(0xFFA3C1AD).copy(alpha = 0.40f), // Eucalyptus
                Color(0xFFD4B499).copy(alpha = 0.32f), // Warm Sand
                Color(0xFFE6D7C3).copy(alpha = 0.25f)  // Pale Champagne
            ),
            start = Offset(0f, h * 0.8f),
            end = Offset(w, h * 0.1f)
        )
    )

    // 4. Secondary Warm Sand Crest Highlight
    val sandCenter = Offset(
        x = w * (0.72f - 0.07f * cos(p1)),
        y = h * (0.38f + 0.05f * sin(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFD4B499).copy(alpha = 0.28f * pulse), // Warm Sand
                Color(0xFF8FA89B).copy(alpha = 0.15f),
                Color.Transparent
            ),
            center = sandCenter,
            radius = w * 0.65f
        ),
        center = sandCenter,
        radius = w * 0.65f
    )
}

/**
 * Option B: Nordic Twilight (If You Want to Keep a Blue Identity)
 * Background Mesh Gradient: Abyssal Navy (#0B101B) into Dusty Indigo (#1B263B).
 * Primary Accent: Moonlight Blue (#7C98B3), Soft Dusk Lavender (#C5B4E3).
 */
private fun DrawScope.drawNordicTwilight(w: Float, h: Float, p1: Float, p2: Float, pulse: Float) {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF0B101B), // Abyssal Navy
                Color(0xFF131B2A),
                Color(0xFF1B263B), // Dusty Indigo
                Color(0xFF0F1624)
            ),
            start = Offset.Zero,
            end = Offset(w, h)
        )
    )

    val bloomCenter = Offset(
        x = w * (0.22f + 0.06f * sin(p1)),
        y = h * (0.18f + 0.04f * cos(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF7C98B3).copy(alpha = 0.35f * pulse), // Moonlight Blue / Mist Blue
                Color(0xFFC5B4E3).copy(alpha = 0.22f), // Soft Dusk Lavender
                Color.Transparent
            ),
            center = bloomCenter,
            radius = w * 0.85f
        ),
        center = bloomCenter,
        radius = w * 0.85f
    )

    val twilightWave = Path().apply {
        moveTo(0f, h * 0.68f)
        cubicTo(
            w * 0.28f, h * (0.52f + 0.05f * sin(p1)),
            w * 0.62f, h * (0.18f + 0.05f * cos(p2)),
            w, h * 0.06f
        )
        lineTo(w, h * 0.64f)
        cubicTo(
            w * 0.68f, h * (0.54f - 0.04f * sin(p2)),
            w * 0.34f, h * (0.82f + 0.04f * cos(p1)),
            0f, h * 0.94f
        )
        close()
    }

    drawPath(
        path = twilightWave,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF7C98B3).copy(alpha = 0.45f),
                Color(0xFF8B9CB6).copy(alpha = 0.40f),
                Color(0xFFC5B4E3).copy(alpha = 0.32f),
                Color(0xFFB3A1D6).copy(alpha = 0.25f)
            ),
            start = Offset(0f, h * 0.8f),
            end = Offset(w, h * 0.1f)
        )
    )

    val crestCenter = Offset(
        x = w * (0.72f - 0.07f * cos(p1)),
        y = h * (0.38f + 0.05f * sin(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFC5B4E3).copy(alpha = 0.28f * pulse),
                Color(0xFF7C98B3).copy(alpha = 0.15f),
                Color.Transparent
            ),
            center = crestCenter,
            radius = w * 0.65f
        ),
        center = crestCenter,
        radius = w * 0.65f
    )
}

/**
 * Option C: Warm Linen & Olive (Best for a "Classy" Light Mode)
 * Background: Warm Oat (#F4F1EA) to Soft Stone (#E8E2D7).
 * Accents: Olive Grove (#6B705C), Muted Terracotta (#C87D65).
 */
private fun DrawScope.drawWarmLinen(w: Float, h: Float, p1: Float, p2: Float, pulse: Float) {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFF4F1EA), // Warm Oat
                Color(0xFFEEEAE2),
                Color(0xFFE8E2D7), // Soft Stone
                Color(0xFFDFD9CE)
            ),
            start = Offset.Zero,
            end = Offset(w, h)
        )
    )

    val bloomCenter = Offset(
        x = w * (0.22f + 0.06f * sin(p1)),
        y = h * (0.18f + 0.04f * cos(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF6B705C).copy(alpha = 0.18f * pulse), // Olive Grove
                Color(0xFF8F937D).copy(alpha = 0.10f),
                Color.Transparent
            ),
            center = bloomCenter,
            radius = w * 0.85f
        ),
        center = bloomCenter,
        radius = w * 0.85f
    )

    val linenWave = Path().apply {
        moveTo(0f, h * 0.68f)
        cubicTo(
            w * 0.28f, h * (0.52f + 0.05f * sin(p1)),
            w * 0.62f, h * (0.18f + 0.05f * cos(p2)),
            w, h * 0.06f
        )
        lineTo(w, h * 0.64f)
        cubicTo(
            w * 0.68f, h * (0.54f - 0.04f * sin(p2)),
            w * 0.34f, h * (0.82f + 0.04f * cos(p1)),
            0f, h * 0.94f
        )
        close()
    }

    drawPath(
        path = linenWave,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF6B705C).copy(alpha = 0.25f),
                Color(0xFF8F937D).copy(alpha = 0.20f),
                Color(0xFFC87D65).copy(alpha = 0.15f),
                Color(0xFFE8E2D7).copy(alpha = 0.10f)
            ),
            start = Offset(0f, h * 0.8f),
            end = Offset(w, h * 0.1f)
        )
    )

    val crestCenter = Offset(
        x = w * (0.72f - 0.07f * cos(p1)),
        y = h * (0.38f + 0.05f * sin(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFC87D65).copy(alpha = 0.14f * pulse), // Muted Terracotta
                Color(0xFF6B705C).copy(alpha = 0.08f), // Olive Grove
                Color.Transparent
            ),
            center = crestCenter,
            radius = w * 0.65f
        ),
        center = crestCenter,
        radius = w * 0.65f
    )
}

private fun DrawScope.drawCosmicDusk(w: Float, h: Float, p1: Float, p2: Float, pulse: Float) {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF0B091A),
                Color(0xFF1E1035),
                Color(0xFF2E134D),
                Color(0xFF220D3D)
            ),
            start = Offset.Zero,
            end = Offset(w, h)
        )
    )

    val bloomCenter = Offset(
        x = w * (0.22f + 0.06f * sin(p1)),
        y = h * (0.18f + 0.04f * cos(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFC084FC).copy(alpha = 0.36f * pulse),
                Color(0xFFA855F7).copy(alpha = 0.20f),
                Color.Transparent
            ),
            center = bloomCenter,
            radius = w * 0.85f
        ),
        center = bloomCenter,
        radius = w * 0.85f
    )

    val duskWave = Path().apply {
        moveTo(0f, h * 0.68f)
        cubicTo(
            w * 0.28f, h * (0.52f + 0.05f * sin(p1)),
            w * 0.62f, h * (0.18f + 0.05f * cos(p2)),
            w, h * 0.06f
        )
        lineTo(w, h * 0.64f)
        cubicTo(
            w * 0.68f, h * (0.54f - 0.04f * sin(p2)),
            w * 0.34f, h * (0.82f + 0.04f * cos(p1)),
            0f, h * 0.94f
        )
        close()
    }

    drawPath(
        path = duskWave,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFC084FC).copy(alpha = 0.45f),
                Color(0xFF6B21A8).copy(alpha = 0.40f),
                Color(0xFFFB923C).copy(alpha = 0.32f),
                Color(0xFFF43F5E).copy(alpha = 0.25f)
            ),
            start = Offset(0f, h * 0.8f),
            end = Offset(w, h * 0.1f)
        )
    )

    val crestCenter = Offset(
        x = w * (0.72f - 0.07f * cos(p1)),
        y = h * (0.38f + 0.05f * sin(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFB923C).copy(alpha = 0.28f * pulse),
                Color(0xFFF43F5E).copy(alpha = 0.15f),
                Color.Transparent
            ),
            center = crestCenter,
            radius = w * 0.65f
        ),
        center = crestCenter,
        radius = w * 0.65f
    )
}

private fun DrawScope.drawOceanAbyss(w: Float, h: Float, p1: Float, p2: Float, pulse: Float) {
    // 1. Dawn Sky Background (Deep Indigo down to Golden Peach)
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF1E1B4B), // Deep Indigo Night Sky
                Color(0xFF4C1D95), // Purple Twilight
                Color(0xFFBE185D), // Magenta Morning
                Color(0xFFF59E0B)  // Golden Dawn Horizon
            ),
            start = Offset.Zero,
            end = Offset(w, h)
        )
    )

    // 2. Animated Rising/Moving Dawn Sun
    // The sun moves horizontally (p1) and bobs vertically (p2)
    val sunCenter = Offset(
        x = w * (0.25f + 0.15f * sin(p1)),
        y = h * (0.25f + 0.10f * cos(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFEF08A).copy(alpha = 0.85f * pulse), // Sun core (bright yellow)
                Color(0xFFF97316).copy(alpha = 0.45f),         // Sun corona (warm orange)
                Color(0xFFF59E0B).copy(alpha = 0.15f),         // Outer soft gold glow
                Color.Transparent
            ),
            center = sunCenter,
            radius = w * 0.75f
        ),
        center = sunCenter,
        radius = w * 0.75f
    )

    // 3. Sweeping Ocean Wave Ribbon (Deep Teals and Cyans)
    val oceanWave = Path().apply {
        moveTo(0f, h * 0.68f)
        cubicTo(
            w * 0.28f, h * (0.52f + 0.05f * sin(p1)),
            w * 0.62f, h * (0.18f + 0.05f * cos(p2)),
            w, h * 0.06f
        )
        lineTo(w, h * 0.64f)
        cubicTo(
            w * 0.68f, h * (0.54f - 0.04f * sin(p2)),
            w * 0.34f, h * (0.82f + 0.04f * cos(p1)),
            0f, h * 0.94f
        )
        close()
    }

    drawPath(
        path = oceanWave,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF06B6D4).copy(alpha = 0.65f), // Bright Cyan surface
                Color(0xFF0284C7).copy(alpha = 0.60f), // Ocean Blue
                Color(0xFF0369A1).copy(alpha = 0.50f), // Deep Ocean
                Color(0xFF082F49).copy(alpha = 0.45f)  // Navy depths
            ),
            start = Offset(0f, h * 0.8f),
            end = Offset(w, h * 0.1f)
        )
    )

    // 4. Secondary Sun Reflection / Water Glint on the wave
    val reflectionCenter = Offset(
        x = w * (0.72f - 0.07f * cos(p1)),
        y = h * (0.38f + 0.05f * sin(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFDE047).copy(alpha = 0.35f * pulse), // Sun glint on water
                Color(0xFF0EA5E9).copy(alpha = 0.20f),         // Cyan water reflection
                Color.Transparent
            ),
            center = reflectionCenter,
            radius = w * 0.65f
        ),
        center = reflectionCenter,
        radius = w * 0.65f
    )
}

private fun DrawScope.drawFloatingLightMotes(w: Float, h: Float, phase: Float) {
    val moteSeeds = listOf(
        Pair(0.12f, 0.10f) to 2.2f,
        Pair(0.85f, 0.08f) to 2.8f,
        Pair(0.72f, 0.22f) to 1.8f,
        Pair(0.28f, 0.26f) to 1.5f,
        Pair(0.92f, 0.35f) to 2.5f,
        Pair(0.08f, 0.40f) to 1.9f,
        Pair(0.48f, 0.44f) to 1.6f,
        Pair(0.82f, 0.52f) to 2.2f,
        Pair(0.15f, 0.58f) to 2.0f,
        Pair(0.64f, 0.63f) to 1.4f,
        Pair(0.36f, 0.70f) to 2.4f,
        Pair(0.88f, 0.76f) to 2.7f,
        Pair(0.22f, 0.82f) to 1.8f,
        Pair(0.76f, 0.88f) to 2.1f
    )

    moteSeeds.forEachIndexed { idx, (pos, radius) ->
        val xDrift = sin(phase + idx * 0.8f) * (w * 0.018f)
        val yDrift = cos(phase * 0.7f + idx * 0.5f) * (h * 0.015f)
        val moteX = (pos.first * w + xDrift).coerceIn(0f, w)
        val moteY = (pos.second * h + yDrift).coerceIn(0f, h)

        val twinkleAlpha = 0.25f + 0.50f * ((sin(phase * 2f + idx) + 1f) / 2f)

        drawCircle(
            color = Color.White.copy(alpha = twinkleAlpha * 0.28f),
            radius = radius * 2.2f,
            center = Offset(moteX, moteY)
        )
        drawCircle(
            color = Color.White.copy(alpha = twinkleAlpha * 0.82f),
            radius = radius,
            center = Offset(moteX, moteY)
        )
    }
}

private fun DrawScope.drawForestRain(w: Float, h: Float, p1: Float, p2: Float, pulse: Float) {
    // 1. Mist-covered forest lake background (Deep charcoal to dark moss)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0D1210), // Dark canopy
                Color(0xFF1B2420), // Mist-covered trees
                Color(0xFF26332C), // Fog layer
                Color(0xFF131A17)  // Deep lake water
            )
        )
    )

    // 2. Soft atmospheric fog bloom (moving slowly)
    val fogCenter = Offset(
        x = w * (0.4f + 0.1f * sin(p1)),
        y = h * (0.35f + 0.05f * cos(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF4A5C53).copy(alpha = 0.22f * pulse),
                Color(0xFF28362D).copy(alpha = 0.10f),
                Color.Transparent
            ),
            center = fogCenter,
            radius = w * 0.9f
        ),
        center = fogCenter,
        radius = w * 0.9f
    )

    // 3. Leaf-covered muddy bank / Rippling water edge
    val bankWave = Path().apply {
        moveTo(0f, h * 0.72f)
        cubicTo(
            w * 0.35f, h * (0.65f + 0.03f * sin(p1)),
            w * 0.65f, h * (0.75f + 0.03f * cos(p2)),
            w, h * 0.68f
        )
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = bankWave,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF2A3A31).copy(alpha = 0.55f),
                Color(0xFF1D2922).copy(alpha = 0.65f),
                Color(0xFF0F1713).copy(alpha = 0.85f)
            ),
            start = Offset(0f, h * 0.7f),
            end = Offset(0f, h)
        )
    )
    
    // Secondary reflection layer
    val reflectionCenter = Offset(
        x = w * (0.6f - 0.05f * cos(p1)),
        y = h * (0.65f + 0.02f * sin(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF5A6E63).copy(alpha = 0.18f * pulse),
                Color(0xFF2B3A32).copy(alpha = 0.05f),
                Color.Transparent
            ),
            center = reflectionCenter,
            radius = w * 0.7f
        ),
        center = reflectionCenter,
        radius = w * 0.7f
    )

    // 4. Subtle Rain Streaks
    val numDrops = 18
    for (i in 0 until numDrops) {
        val startX = (w * 0.05f * i + w * 0.3f * (i % 3)) % w
        val speedMultiplier = 1f + (i % 4) * 0.4f
        // Phase goes 0 to 2PI, map to 0..1
        val rawY = (p1 / (2 * Math.PI.toFloat()) * speedMultiplier + (i * 0.17f)) % 1f
        val dropY = h * rawY

        val streakLength = h * 0.06f + (i % 3) * h * 0.02f
        
        drawLine(
            color = Color(0xFF8BA698).copy(alpha = 0.12f + 0.05f * (i % 2)),
            start = Offset(startX, dropY),
            end = Offset(startX, dropY + streakLength),
            strokeWidth = 1.5f + (i % 2) * 1f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        // Tiny expanding ripple when drop is in the lower portion
        if (rawY > 0.65f) {
            val rippleProgress = (rawY - 0.65f) / 0.35f
            drawOval(
                color = Color(0xFF8BA698).copy(alpha = 0.15f * (1f - rippleProgress)),
                topLeft = Offset(startX - w * 0.04f * rippleProgress, dropY + streakLength - h * 0.01f * rippleProgress),
                size = androidx.compose.ui.geometry.Size(w * 0.08f * rippleProgress, h * 0.02f * rippleProgress),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
        }
    }
}

private fun DrawScope.drawBioluminescentWake(w: Float, h: Float, p1: Float, p2: Float, pulse: Float) {
    // 1. Pure OLED Abyss Background
    drawRect(Color(0xFF030509))

    val centerX = w * 0.5f

    // 2. The boat (stationary in the upper third)
    val boatY = h * 0.25f
    val boatWidth = w * 0.05f
    val boatHeight = h * 0.08f

    // Subtle boat glow/reflection on the dark water
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF80D8FF).copy(alpha = 0.15f * pulse),
                Color(0xFF0066FF).copy(alpha = 0.05f),
                Color.Transparent
            ),
            center = Offset(centerX, boatY + boatHeight * 0.5f),
            radius = w * 0.2f
        ),
        topLeft = Offset(centerX - w * 0.2f, boatY - h * 0.05f),
        size = androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.25f)
    )

    // The glowing bioluminescent Kelvin wake
    val wakeLength = h * 0.8f
    val wakeMaxSpread = w * 0.35f
    
    // We draw multiple overlapping trails to create the fluid turbulence
    val trailCount = 14
    for (i in 0 until trailCount) {
        val trailPath = Path()
        trailPath.moveTo(centerX, boatY + boatHeight * 0.7f)
        
        // Use phase for smooth downward streaming motion
        val phaseOffset = (p1 * 0.5f + i * 0.4f) % (Math.PI.toFloat() * 2)
        val side = if (i % 2 == 0) 1f else -1f
        
        // Spread varies based on the trail index to create the V-shape
        val spreadFactor = (i / trailCount.toFloat()) * wakeMaxSpread * side
        
        val ctrl1X = centerX + spreadFactor * 0.3f + sin(phaseOffset) * w * 0.02f
        val ctrl1Y = boatY + wakeLength * 0.3f
        
        val ctrl2X = centerX + spreadFactor * 0.7f + cos(phaseOffset * 1.5f) * w * 0.03f
        val ctrl2Y = boatY + wakeLength * 0.7f
        
        val endX = centerX + spreadFactor + sin(phaseOffset * 0.8f) * w * 0.04f
        val endY = boatY + wakeLength
        
        trailPath.cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, endX, endY)
        
        // Fade the trails as they spread outward and downward
        val trailAlpha = 0.45f - (i / trailCount.toFloat()) * 0.3f
        val strokeWidth = w * 0.008f + (i % 3) * w * 0.005f
        
        drawPath(
            path = trailPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE0F7FA).copy(alpha = trailAlpha * pulse), // Bright cyan core
                    Color(0xFF00B0FF).copy(alpha = trailAlpha * 0.7f),  // Deep blue mid
                    Color(0xFF0038D6).copy(alpha = trailAlpha * 0.2f),  // Dispersing edge
                    Color.Transparent
                ),
                startY = boatY,
                endY = boatY + wakeLength
            ),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }

    // 3. Central glowing engine thrust/main wake
    val centerWakePath = Path().apply {
        moveTo(centerX, boatY + boatHeight * 0.8f)
        cubicTo(
            centerX + sin(p1 * 2f) * w * 0.01f, boatY + wakeLength * 0.3f,
            centerX + cos(p1 * 1.5f) * w * 0.015f, boatY + wakeLength * 0.7f,
            centerX + sin(p1) * w * 0.02f, boatY + wakeLength
        )
    }
    
    drawPath(
        path = centerWakePath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f * pulse),
                Color(0xFF80D8FF).copy(alpha = 0.5f),
                Color(0xFF0066FF).copy(alpha = 0.1f),
                Color.Transparent
            ),
            startY = boatY,
            endY = boatY + wakeLength
        ),
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = w * 0.015f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    )

    // 4. The Boat Hull (Minimalist geometric shape)
    val boatPath = Path().apply {
        moveTo(centerX, boatY) // Bow
        quadraticTo(centerX + boatWidth * 0.5f, boatY + boatHeight * 0.3f, centerX + boatWidth * 0.5f, boatY + boatHeight * 0.8f)
        lineTo(centerX - boatWidth * 0.5f, boatY + boatHeight * 0.8f)
        quadraticTo(centerX - boatWidth * 0.5f, boatY + boatHeight * 0.3f, centerX, boatY)
        close()
    }
    
    // Boat Body
    drawPath(
        path = boatPath,
        color = Color(0xFFE2E8F0) // Light grey/white hull
    )
    
    // Boat Cockpit/Windshield (Dark)
    val cockpitPath = Path().apply {
        moveTo(centerX, boatY + boatHeight * 0.35f)
        lineTo(centerX + boatWidth * 0.35f, boatY + boatHeight * 0.45f)
        lineTo(centerX + boatWidth * 0.35f, boatY + boatHeight * 0.65f)
        lineTo(centerX - boatWidth * 0.35f, boatY + boatHeight * 0.65f)
        lineTo(centerX - boatWidth * 0.35f, boatY + boatHeight * 0.45f)
        close()
    }
    drawPath(
        path = cockpitPath,
        color = Color(0xFF0F172A)
    )
    
    // Wooden rear deck
    drawRect(
        color = Color(0xFF92400E),
        topLeft = Offset(centerX - boatWidth * 0.4f, boatY + boatHeight * 0.8f),
        size = androidx.compose.ui.geometry.Size(boatWidth * 0.8f, boatHeight * 0.15f)
    )
}

private fun DrawScope.drawFerrisWheel(w: Float, h: Float, p1: Float, p2: Float, pulse: Float) {
    // 1. Pure OLED pitch-black sky
    drawRect(Color(0xFF010101))

    // 2. Crisp, plain white full moon with subtle lunar surface texture
    val moonCenter = Offset(w * 0.82f, h * 0.12f)
    val moonRadius = w * 0.07f
    drawCircle(Color(0xFFF8F8FA), radius = moonRadius, center = moonCenter)
    // Subtle, sharp craters
    drawCircle(Color(0xFFE4E4E7).copy(alpha = 0.5f), radius = moonRadius * 0.25f, center = Offset(moonCenter.x - moonRadius * 0.35f, moonCenter.y + moonRadius * 0.25f))
    drawCircle(Color(0xFFD4D4D8).copy(alpha = 0.4f), radius = moonRadius * 0.35f, center = Offset(moonCenter.x + moonRadius * 0.2f, moonCenter.y - moonRadius * 0.15f))
    drawCircle(Color(0xFFE4E4E7).copy(alpha = 0.6f), radius = moonRadius * 0.15f, center = Offset(moonCenter.x + moonRadius * 0.1f, moonCenter.y + moonRadius * 0.45f))

    // 3. Ferris Wheel parameters
    // Center is off-screen to the bottom-right so the wheel arcs nicely across the screen
    val wheelCenter = Offset(w * 1.15f, h * 0.68f)
    val outerRadius = w * 1.05f
    val innerRadius1 = w * 0.95f
    val innerRadius2 = w * 0.70f

    // 4. Ultra-slow clockwise rotation
    val rotationAngle = p1 * 0.3f 
    
    // Draw Spokes and Truss
    val numSpokes = 24
    for (i in 0 until numSpokes) {
        val angle = rotationAngle + i * (2 * Math.PI.toFloat() / numSpokes)
        val outerX = wheelCenter.x + outerRadius * Math.cos(angle.toDouble()).toFloat()
        val outerY = wheelCenter.y + outerRadius * Math.sin(angle.toDouble()).toFloat()
        
        // Main Spoke
        drawLine(
            color = Color(0xFFD4D4D8).copy(alpha = 0.85f),
            start = wheelCenter,
            end = Offset(outerX, outerY),
            strokeWidth = 3f
        )
        
        // Truss zig-zags
        val nextAngle = rotationAngle + (i + 1) * (2 * Math.PI.toFloat() / numSpokes)
        val nextInnerX = wheelCenter.x + innerRadius2 * Math.cos(nextAngle.toDouble()).toFloat()
        val nextInnerY = wheelCenter.y + innerRadius2 * Math.sin(nextAngle.toDouble()).toFloat()
        val currOuterX = wheelCenter.x + innerRadius1 * Math.cos(angle.toDouble()).toFloat()
        val currOuterY = wheelCenter.y + innerRadius1 * Math.sin(angle.toDouble()).toFloat()
        
        drawLine(
            color = Color(0xFFA1A1AA).copy(alpha = 0.6f),
            start = Offset(currOuterX, currOuterY),
            end = Offset(nextInnerX, nextInnerY),
            strokeWidth = 2f
        )
    }

    // Draw Wheel Rings
    drawCircle(
        color = Color(0xFFE4E4E7).copy(alpha = 0.95f),
        radius = outerRadius,
        center = wheelCenter,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
    )
    drawCircle(
        color = Color(0xFFD4D4D8).copy(alpha = 0.8f),
        radius = innerRadius1,
        center = wheelCenter,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
    )
    drawCircle(
        color = Color(0xFFA1A1AA).copy(alpha = 0.6f),
        radius = innerRadius2,
        center = wheelCenter,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
    )

    // 5. Glowing Cabins (Smoothly leveled and upright)
    val cabinWidth = w * 0.12f
    val cabinHeight = h * 0.07f
    for (i in 0 until numSpokes) {
        val angle = rotationAngle + i * (2 * Math.PI.toFloat() / numSpokes)
        val cabinAttachX = wheelCenter.x + outerRadius * Math.cos(angle.toDouble()).toFloat()
        val cabinAttachY = wheelCenter.y + outerRadius * Math.sin(angle.toDouble()).toFloat()
        
        // Only draw cabins if they are within visible bounds (with some margin)
        if (cabinAttachX > -cabinWidth * 2 && cabinAttachX < w + cabinWidth && cabinAttachY > -cabinHeight * 2 && cabinAttachY < h + cabinHeight * 2) {
            
            val left = cabinAttachX - cabinWidth / 2
            val top = cabinAttachY
            val right = cabinAttachX + cabinWidth / 2
            val bottom = cabinAttachY + cabinHeight
            
            // Central pivot connector
            drawCircle(
                color = Color(0xFF71717A),
                radius = 5f,
                center = Offset(cabinAttachX, cabinAttachY)
            )

            // Triangle suspension
            drawLine(
                color = Color(0xFFD4D4D8),
                start = Offset(cabinAttachX, cabinAttachY),
                end = Offset(left + cabinWidth * 0.15f, top),
                strokeWidth = 3f
            )
            drawLine(
                color = Color(0xFFD4D4D8),
                start = Offset(cabinAttachX, cabinAttachY),
                end = Offset(right - cabinWidth * 0.15f, top),
                strokeWidth = 3f
            )
            
            // Cabin body outline
            drawRoundRect(
                color = Color(0xFF27272A),
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(cabinWidth, cabinHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
            )
            
            // Warm-white & faint violet LEDs on the cabins
            val isViolet = i % 4 == 0
            val windowColor = if (isViolet) Color(0xFFD8B4E2) else Color(0xFFFFF7ED)
            val windowGlow = if (isViolet) Color(0x33D8B4E2) else Color(0x33FFF7ED)
            
            // Inner window pane glow
            drawRoundRect(
                color = windowColor.copy(alpha = 0.95f),
                topLeft = Offset(left + cabinWidth * 0.1f, top + cabinHeight * 0.15f),
                size = androidx.compose.ui.geometry.Size(cabinWidth * 0.35f, cabinHeight * 0.6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
            drawRoundRect(
                color = windowColor.copy(alpha = 0.95f),
                topLeft = Offset(left + cabinWidth * 0.55f, top + cabinHeight * 0.15f),
                size = androidx.compose.ui.geometry.Size(cabinWidth * 0.35f, cabinHeight * 0.6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )

            // LED glow effect around the cabin
            drawRoundRect(
                color = windowGlow,
                topLeft = Offset(left - 4f, top - 4f),
                size = androidx.compose.ui.geometry.Size(cabinWidth + 8f, cabinHeight + 8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
            )
        }
    }
}

/**
 * Time Dynamic Theme (TD): Adaptive Morning, Afternoon, and Evening/Night Glass Pillar Wave
 * Renders vertical glass panels with a sharp, high-definition sweeping U-shaped parabolic arc of light
 * that dynamically changes color based on the time of day (Morning / Afternoon / Evening).
 */
private fun DrawScope.drawTimeDynamic(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float
) {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

    val baseDark: Color
    val panelDark: Color
    val midGlow: Color
    val brightCore: Color
    val highlightEdge: Color
    val coreWhite: Color = Color.White

    when (hour) {
        in 5..11 -> { // Morning / Dawn (Crisp Celadon Green & Sage)
            baseDark = Color(0xFF030D08)
            panelDark = Color(0xFF081C12)
            midGlow = Color(0xFF2C5E3B)
            brightCore = Color(0xFF8FD870)
            highlightEdge = Color(0xFFE2FFB0)
        }
        in 12..17 -> { // Afternoon / Dusk (Crisp Golden Amber & Sun)
            baseDark = Color(0xFF0D0703)
            panelDark = Color(0xFF1E1007)
            midGlow = Color(0xFF5E3C21)
            brightCore = Color(0xFFD89B58)
            highlightEdge = Color(0xFFFFF1C6)
        }
        else -> { // Night / Evening (Crisp Deep Indigo & Violet)
            baseDark = Color(0xFF04040E)
            panelDark = Color(0xFF0B0B20)
            midGlow = Color(0xFF2B295B)
            brightCore = Color(0xFF7B76D8)
            highlightEdge = Color(0xFFDCDAEE)
        }
    }

    // 1. Deep High-Contrast Canvas
    drawRect(color = baseDark)

    val numPanels = 7
    val panelWidth = w / numPanels
    val waveOffset = sin(p1) * 8f

    // Pre-calculate parabolic arc Y coordinates across panels for crisp smooth continuity
    val arcYList = FloatArray(numPanels)
    for (i in 0 until numPanels) {
        val panelCenterX = (i + 0.5f) * panelWidth
        val normDist = (panelCenterX - (w / 2f)) / (w * 0.48f)
        val arcCenterY = h * 0.63f + waveOffset + (sin(p2 + i * 0.4f) * 6f)
        arcYList[i] = arcCenterY + (normDist * normDist) * (h * 0.24f)
    }

    // 2. Draw 7 Sharp Glass Column Panels
    for (i in 0 until numPanels) {
        val left = i * panelWidth
        val right = (i + 1) * panelWidth
        val panelCenterX = (left + right) / 2f
        val arcY = arcYList[i]

        // Sharp Vertical Column Background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    baseDark,
                    panelDark,
                    midGlow.copy(alpha = 0.35f),
                    baseDark
                ),
                startY = 0f,
                endY = h
            ),
            topLeft = Offset(left, 0f),
            size = androidx.compose.ui.geometry.Size(panelWidth, h)
        )

        // Focused Crisp Arc Light Beam inside Column
        val bandTop = (arcY - 100f * pulse).coerceAtLeast(0f)
        val bandBottom = (arcY + 110f * pulse).coerceAtMost(h)
        if (bandBottom > bandTop) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        midGlow.copy(alpha = 0.25f),
                        brightCore.copy(alpha = 0.75f),
                        highlightEdge.copy(alpha = 0.95f),
                        coreWhite.copy(alpha = 0.85f),
                        brightCore.copy(alpha = 0.65f),
                        midGlow.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    startY = bandTop,
                    endY = bandBottom
                ),
                topLeft = Offset(left, bandTop),
                size = androidx.compose.ui.geometry.Size(panelWidth, bandBottom - bandTop)
            )
        }

        // Inner Sharp Specular Edge Highlight on Column
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    highlightEdge.copy(alpha = 0.30f),
                    highlightEdge.copy(alpha = 0.75f),
                    Color.Transparent
                ),
                startY = (arcY - 80f).coerceAtLeast(0f),
                endY = (arcY + 80f).coerceAtMost(h)
            ),
            start = Offset(left + 1.5f, 0f),
            end = Offset(left + 1.5f, h),
            strokeWidth = 1f
        )
    }

    // 3. Crisp Continuous Parabolic Arc Curve Rim Line across screen
    val arcPath = Path()
    for (i in 0 until numPanels) {
        val x = (i + 0.5f) * panelWidth
        val y = arcYList[i]
        if (i == 0) {
            arcPath.moveTo(x - panelWidth / 2f, y)
            arcPath.lineTo(x, y)
        } else {
            val prevX = ((i - 1) + 0.5f) * panelWidth
            val prevY = arcYList[i - 1]
            val midX = (prevX + x) / 2f
            val midY = (prevY + y) / 2f
            arcPath.quadraticTo(prevX, prevY, midX, midY)
        }
    }
    val lastX = w
    val lastY = arcYList[numPanels - 1]
    arcPath.lineTo(lastX, lastY)

    // Draw Razor-Sharp Glowing Arc Line
    drawPath(
        path = arcPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                brightCore.copy(alpha = 0.50f),
                highlightEdge.copy(alpha = 0.95f),
                coreWhite.copy(alpha = 0.99f),
                highlightEdge.copy(alpha = 0.95f),
                brightCore.copy(alpha = 0.50f)
            )
        ),
        style = Stroke(width = 2.5f)
    )

    // 4. Sharp Razor Glass Partition Seam Lines (Vertical Panel Dividers)
    for (i in 1 until numPanels) {
        val seamX = i * panelWidth
        val arcYAtSeam = arcYList[i.coerceIn(0, numPanels - 1)]

        // Sharp Outer Glow Line (1.8px)
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    highlightEdge.copy(alpha = 0.08f),
                    highlightEdge.copy(alpha = 0.45f),
                    coreWhite.copy(alpha = 0.85f),
                    highlightEdge.copy(alpha = 0.45f),
                    highlightEdge.copy(alpha = 0.08f)
                ),
                startY = 0f,
                endY = h
            ),
            start = Offset(seamX, 0f),
            end = Offset(seamX, h),
            strokeWidth = 1.8f
        )

        // Super-sharp 1.0px Specular Core Line
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    coreWhite.copy(alpha = 0.30f),
                    coreWhite.copy(alpha = 0.95f),
                    coreWhite.copy(alpha = 0.30f),
                    Color.Transparent
                ),
                startY = (arcYAtSeam - 120f).coerceAtLeast(0f),
                endY = (arcYAtSeam + 120f).coerceAtMost(h)
            ),
            start = Offset(seamX, 0f),
            end = Offset(seamX, h),
            strokeWidth = 1.0f
        )
    }
}

@Composable
fun LoopingTriosBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF0B0E14), // Deep obsidian dark
    solidTriangleColor: Color = Color(0xFF1A2230), // Muted dark slate
    stripeTriangleColor: Color = Color(0xFF283447), // Subtle slate for hatched lines
    cellSizeDp: Float = 150f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LoopingTrios")

    // 8-second cycle split into 4 stepped 90-degree phases with brief pauses
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.keyframes {
                durationMillis = 8000
                0f at 0 using FastOutSlowInEasing
                0f at 450 using FastOutSlowInEasing
                1f at 2000 using FastOutSlowInEasing
                1f at 2450 using FastOutSlowInEasing
                2f at 4000 using FastOutSlowInEasing
                2f at 4450 using FastOutSlowInEasing
                3f at 6000 using FastOutSlowInEasing
                3f at 6450 using FastOutSlowInEasing
                4f at 8000 using FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "PhaseAnimation"
    )

    val ReusablePath = remember { Path() }
    val density = androidx.compose.ui.platform.LocalDensity.current.density

    Box(modifier = modifier.fillMaxSize().background(backgroundColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellW = cellSizeDp * density
            val cellH = cellSizeDp * 1.15f * density
            val triR = cellW * 0.18f // Base radius of each triangle

            val cols = kotlin.math.ceil((size.width / cellW).toDouble()).toInt() + 2
            val rows = kotlin.math.ceil((size.height / cellH).toDouble()).toInt() + 2

            // Angles driven by the 0..4 stepped phase
            val primaryAngle = phase * 90f
            val secondaryAngle = -phase * 90f

            for (col in -1 until cols) {
                for (row in -1 until rows) {
                    val baseX = col * cellW
                    val baseY = row * cellH

                    // Each grid cell renders a staggered pair of trios (left and shifted-up right)
                    drawTrioUnit(
                        centerX = baseX + cellW * 0.28f,
                        centerY = baseY + cellH * 0.58f,
                        r = triR,
                        primaryAngle = primaryAngle,
                        secondaryAngle = secondaryAngle,
                        solidColor = solidTriangleColor,
                        stripeColor = stripeTriangleColor,
                        path = ReusablePath,
                        density = density
                    )

                    drawTrioUnit(
                        centerX = baseX + cellW * 0.72f,
                        centerY = baseY + cellH * 0.32f,
                        r = triR,
                        primaryAngle = primaryAngle,
                        secondaryAngle = secondaryAngle,
                        solidColor = solidTriangleColor,
                        stripeColor = stripeTriangleColor,
                        path = ReusablePath,
                        density = density
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawTrioUnit(
    centerX: Float,
    centerY: Float,
    r: Float,
    primaryAngle: Float,
    secondaryAngle: Float,
    solidColor: Color,
    stripeColor: Color,
    path: Path,
    density: Float
) {
    val verticalOffset = r * 1.05f

    // 1. Center-connecting striped triangle
    withTransform({
        translate(centerX, centerY)
        rotate(secondaryAngle, pivot = Offset.Zero)
    }) {
        drawStripedTriangle(
            path = path,
            p1 = Offset(0f, -verticalOffset),
            p2 = Offset(0f, verticalOffset),
            p3 = Offset(-r * 1.1f, 0f),
            color = stripeColor,
            stripeAngle = primaryAngle,
            density = density
        )
    }

    // 2. Top solid triangle + top-cap striped triangle
    withTransform({
        translate(centerX, centerY - verticalOffset)
        rotate(primaryAngle, pivot = Offset.Zero)
    }) {
        // Top-cap striped accent
        drawStripedTriangle(
            path = path,
            p1 = Offset(0f, -r),
            p2 = Offset(-r * 1.1f, -r * 0.35f),
            p3 = Offset(r * 0.85f, 0f),
            color = stripeColor,
            stripeAngle = -primaryAngle,
            density = density
        )
        // Top solid triangle (pointing up)
        drawSolidTriangle(
            path = path,
            p1 = Offset(0f, -r),
            p2 = Offset(-r * 0.95f, 0f),
            p3 = Offset(r * 0.95f, 0f),
            color = solidColor
        )
    }

    // 3. Bottom solid triangle + bottom-cap striped triangle
    withTransform({
        translate(centerX, centerY + verticalOffset)
        rotate(primaryAngle, pivot = Offset.Zero)
    }) {
        // Bottom-cap striped accent
        drawStripedTriangle(
            path = path,
            p1 = Offset(0f, r),
            p2 = Offset(-r * 1.1f, r * 0.35f),
            p3 = Offset(r * 0.85f, 0f),
            color = stripeColor,
            stripeAngle = -primaryAngle,
            density = density
        )
        // Bottom solid triangle (pointing down)
        drawSolidTriangle(
            path = path,
            p1 = Offset(0f, r),
            p2 = Offset(-r * 0.95f, 0f),
            p3 = Offset(r * 0.95f, 0f),
            color = solidColor
        )
    }
}

private fun DrawScope.drawSolidTriangle(
    path: Path,
    p1: Offset,
    p2: Offset,
    p3: Offset,
    color: Color
) {
    path.reset()
    path.moveTo(p1.x, p1.y)
    path.lineTo(p2.x, p2.y)
    path.lineTo(p3.x, p3.y)
    path.close()
    drawPath(path = path, color = color)
}

private fun DrawScope.drawStripedTriangle(
    path: Path,
    p1: Offset,
    p2: Offset,
    p3: Offset,
    color: Color,
    stripeAngle: Float,
    density: Float
) {
    path.reset()
    path.moveTo(p1.x, p1.y)
    path.lineTo(p2.x, p2.y)
    path.lineTo(p3.x, p3.y)
    path.close()

    val cx = (p1.x + p2.x + p3.x) / 3f
    val cy = (p1.y + p2.y + p3.y) / 3f
    val maxExtent = 120f
    val step = 4.5f * density

    clipPath(path) {
        withTransform({
            rotate(stripeAngle, pivot = Offset(cx, cy))
        }) {
            var y = cy - maxExtent
            while (y <= cy + maxExtent) {
                drawLine(
                    color = color,
                    start = Offset(cx - maxExtent, y),
                    end = Offset(cx + maxExtent, y),
                    strokeWidth = 1.6f * density
                )
                y += step
            }
        }
    }
}
