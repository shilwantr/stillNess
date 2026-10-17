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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ambient Theme choices for the Living Liquid Glass backdrop.
 * Updated to feature Option A (Midnight Sage), Option B (Nordic Twilight), and Option C (Warm Linen & Olive).
 */
enum class AmbientTheme(val label: String, val subtitle: String) {
    NIGHT_FERRIS_WHEEL("Midnight Sage", "Option A: Earthy forest slate, celadon sage & warm sand sanctuary"),
    AURORA_SILK("Nordic Twilight", "Option B: Abyssal navy, moonlight blue & soft dusk lavender"),
    DAYLIGHT_ETHEREAL("Warm Linen", "Option C: Paper-like warm oat, soft stone & olive grove light mode"),
    COSMIC_DUSK("Cosmic Dusk", "Deep ultraviolet nebula and twilight amber glow"),
    OCEAN_ABYSS("Ocean Dawn", "Animated ocean waves with a moving sunrise"),
    FOREST_RAIN("Forest Rain", "Cinematic mist, soft rain, and deep forest sage"),
    BIOLUMINESCENT_WAKE("Deep Abyssal Wake", "A hypnotic top-down view of an illuminated boat cutting through pitch-black water"),
    FERRIS_WHEEL("Night Ferris Wheel", "Cinemagraph of an illuminated Ferris wheel against a pure black sky")
}

@Composable
fun CosmicHorizonCanvas(
    modifier: Modifier = Modifier,
    theme: AmbientTheme = AmbientTheme.NIGHT_FERRIS_WHEEL
) {
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
            AmbientTheme.NIGHT_FERRIS_WHEEL -> drawMidnightSage(w, h, phase1, phase2, pulse)
            AmbientTheme.AURORA_SILK -> drawNordicTwilight(w, h, phase1, phase2, pulse)
            AmbientTheme.DAYLIGHT_ETHEREAL -> drawWarmLinen(w, h, phase1, phase2, pulse)
            AmbientTheme.COSMIC_DUSK -> drawCosmicDusk(w, h, phase1, phase2, pulse)
            AmbientTheme.OCEAN_ABYSS -> drawOceanAbyss(w, h, phase1, phase2, pulse)
            AmbientTheme.FOREST_RAIN -> drawForestRain(w, h, phase1, phase2, pulse)
            AmbientTheme.BIOLUMINESCENT_WAKE -> drawBioluminescentWake(w, h, phase1, phase2, pulse)
            AmbientTheme.FERRIS_WHEEL -> drawFerrisWheel(w, h, phase1, phase2, pulse)
        }

        drawFloatingLightMotes(w, h, phase1)
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
