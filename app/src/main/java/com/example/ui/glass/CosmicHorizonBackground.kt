package com.example.ui.glass

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ambient Theme choices for the Living Liquid Glass backdrop.
 * Updated to feature Option A (Midnight Sage), Option B (Nordic Twilight), and Option C (Warm Linen & Olive),
 * engineered with C1-continuous harmonic motion, braided volumetric silk waves, and specular caustic
 * filaments that complement real-time Liquid Glass lens refraction, reflection, and chromatic dispersion.
 */
enum class AmbientTheme(val label: String, val subtitle: String) {
    AURORA_SILK("NT", "Nordic Twilight: Abyssal navy, moonlight blue & soft dusk lavender"),
    FERRIS_WHEEL("FW", "Night Ferris Wheel: Cinemagraph of an illuminated Ferris wheel against a pure black sky"),
    TIME_DYNAMIC("TD", "Time Dynamic: Adaptive morning, afternoon & evening glass waves"),
    LOOPING_TRIOS("LT", "Looping Trios: Deep obsidian dark with animated geometry")
}

private const val TWO_PI = (2.0 * PI).toFloat()

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

    // Primary harmonic phase (0..2π over 24s). Every downstream wave uses integer multiples of
    // phase1 and phase2 so that wrapping from 2π -> 0 is 100% seamless with zero snap.
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    // Secondary counter-flowing harmonic phase (0..2π over 16s)
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    // Slow breathing luminance pulse for caustic blooms and refractive highlights
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = FastOutSlowInEasing),
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
            AmbientTheme.LOOPING_TRIOS -> Unit
        }

        if (theme != AmbientTheme.LOOPING_TRIOS) {
            drawFloatingLightMotes(w, h, phase1, phase2, pulse)
        }
    }
}

/**
 * Palette specification for the braided volumetric Liquid Silk engine.
 */
private data class SilkAuroraPalette(
    val backdropColors: List<Color>,
    val primaryBloomCore: Color,
    val primaryBloomHalo: Color,
    val secondaryBloomCore: Color,
    val secondaryBloomHalo: Color,
    val tertiaryBloomCore: Color,
    val mainRibbonColors: List<Color>,
    val counterRibbonColors: List<Color>,
    val canopyRibbonColors: List<Color>,
    val causticHighlight: Color,
    val causticSecondary: Color,
    val isLightMode: Boolean = false
)

/**
 * Renders a multi-layered Liquid Silk backdrop specifically tuned for Liquid Glass shaders:
 * - Animated edge anchor points and travelling S-curve control points ensure the waves continuously
 *   glide across the entire screen rather than staying pinned to static corners.
 * - Multi-envelope braided bezier curtains replace flat single-path polygons, creating graduated
 *   soft-feathered edges and rich internal folds that refract smoothly inside glass cards.
 * - Luminous caustic filaments along wave crests provide high-contrast specular ridges that trigger
 *   prism dispersion (chromatic aberration) around glass card borders.
 */
private fun DrawScope.drawLiquidSilkScene(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float,
    palette: SilkAuroraPalette
) {
    val additiveMode = if (palette.isLightMode) BlendMode.SrcOver else BlendMode.Plus

    // 1. Breathing Base Mesh Gradient
    val shiftX = w * 0.10f * sin(p1)
    val shiftY = h * 0.10f * cos(p2)
    drawRect(
        brush = Brush.linearGradient(
            colors = palette.backdropColors,
            start = Offset(shiftX.coerceAtLeast(0f), shiftY.coerceAtLeast(0f)),
            end = Offset(w + shiftX.coerceAtMost(0f), h + shiftY.coerceAtMost(0f))
        )
    )

    // 2. Three Lissajous-Drifting Caustic Orbs (top-left, mid-right, bottom-left)
    val upperBloom = Offset(
        x = w * (0.24f + 0.16f * sin(p1) + 0.06f * cos(p2)),
        y = h * (0.18f + 0.08f * cos(p1) + 0.04f * sin(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.primaryBloomCore.copy(alpha = palette.primaryBloomCore.alpha * pulse),
                palette.primaryBloomHalo,
                Color.Transparent
            ),
            center = upperBloom,
            radius = w * 0.90f
        ),
        center = upperBloom,
        radius = w * 0.90f
    )

    val midBloom = Offset(
        x = w * (0.76f - 0.16f * cos(p1) + 0.07f * sin(p2)),
        y = h * (0.46f + 0.10f * sin(p1) - 0.05f * cos(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.secondaryBloomCore.copy(alpha = palette.secondaryBloomCore.alpha * pulse),
                palette.secondaryBloomHalo,
                Color.Transparent
            ),
            center = midBloom,
            radius = w * 0.80f
        ),
        center = midBloom,
        radius = w * 0.80f
    )

    val lowerBloom = Offset(
        x = w * (0.34f + 0.18f * cos(p2) - 0.07f * sin(p1)),
        y = h * (0.80f - 0.09f * sin(p2) + 0.05f * cos(p1))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.tertiaryBloomCore.copy(alpha = palette.tertiaryBloomCore.alpha * pulse),
                palette.primaryBloomHalo.copy(alpha = palette.primaryBloomHalo.alpha * 0.75f),
                Color.Transparent
            ),
            center = lowerBloom,
            radius = w * 0.85f
        ),
        center = lowerBloom,
        radius = w * 0.85f
    )

    // 3. Upper Canopy Aurora Veil (sweeps across top header & reflection card)
    drawBraidedSilkCurtain(
        w = w,
        h = h,
        leftTopY = h * (0.10f + 0.07f * sin(p1 + 0.9f)),
        leftBottomY = h * (0.34f + 0.08f * cos(p2 + 0.4f)),
        ctrl1Top = Offset(w * (0.30f + 0.06f * sin(p2)), h * (0.03f + 0.09f * cos(p1))),
        ctrl2Top = Offset(w * (0.68f - 0.06f * cos(p1)), h * (0.28f + 0.09f * sin(p2))),
        rightTopY = h * (0.08f + 0.07f * cos(p1 - p2)),
        rightBottomY = h * (0.30f + 0.07f * sin(p1 + p2)),
        ctrl1Bottom = Offset(w * (0.66f + 0.05f * cos(p2)), h * (0.44f + 0.08f * cos(p2))),
        ctrl2Bottom = Offset(w * (0.32f - 0.05f * sin(p1)), h * (0.18f + 0.08f * sin(p1))),
        ribbonColors = palette.canopyRibbonColors,
        crestColor = palette.causticSecondary,
        phase = p1,
        pulse = pulse,
        additiveMode = additiveMode
    )

    // 4. Primary Sweeping Liquid Aurora Wave (true undulating S-curve with large harmonic travel)
    drawBraidedSilkCurtain(
        w = w,
        h = h,
        leftTopY = h * (0.52f + 0.11f * sin(p1)),
        leftBottomY = h * (0.82f + 0.09f * cos(p2)),
        ctrl1Top = Offset(w * (0.28f + 0.07f * cos(p2)), h * (0.30f + 0.14f * sin(p1))),
        ctrl2Top = Offset(w * (0.66f - 0.07f * sin(p1)), h * (0.46f - 0.14f * cos(p2))),
        rightTopY = h * (0.12f + 0.10f * sin(p2)),
        rightBottomY = h * (0.44f + 0.11f * cos(p1)),
        ctrl1Bottom = Offset(w * (0.70f + 0.06f * sin(p2)), h * (0.68f - 0.12f * sin(p2))),
        ctrl2Bottom = Offset(w * (0.34f - 0.06f * cos(p1)), h * (0.58f + 0.13f * cos(p1))),
        ribbonColors = palette.mainRibbonColors,
        crestColor = palette.causticHighlight,
        phase = p1 + p2,
        pulse = pulse,
        additiveMode = additiveMode
    )

    // 5. Secondary Counter-Flowing Silk Wave (glides from mid-left to lower-right for bottom cards)
    drawBraidedSilkCurtain(
        w = w,
        h = h,
        leftTopY = h * (0.36f + 0.09f * cos(p2)),
        leftBottomY = h * (0.62f + 0.09f * sin(p1)),
        ctrl1Top = Offset(w * (0.34f - 0.06f * sin(p1)), h * (0.58f + 0.11f * cos(p1 - p2))),
        ctrl2Top = Offset(w * (0.68f + 0.06f * cos(p2)), h * (0.50f - 0.11f * sin(p1))),
        rightTopY = h * (0.56f + 0.10f * cos(p1)),
        rightBottomY = h * (0.88f + 0.08f * sin(p2)),
        ctrl1Bottom = Offset(w * (0.64f - 0.05f * cos(p1)), h * (0.86f + 0.09f * sin(p2))),
        ctrl2Bottom = Offset(w * (0.32f + 0.05f * sin(p2)), h * (0.72f - 0.09f * cos(p1))),
        ribbonColors = palette.counterRibbonColors,
        crestColor = palette.causticSecondary,
        phase = p2,
        pulse = pulse,
        additiveMode = additiveMode
    )
}

/**
 * Draws a 4-layer braided volumetric silk wave curtain:
 * - Layer 1 (Wide Outer Veil): Expanded bezier envelope at 28% opacity for a soft, feathered transition.
 * - Layer 2 (Mid Silk Body): Primary flowing wave at 52% opacity.
 * - Layer 3 (Inner Luminous Core): Phase-braided inner fold at 68% opacity for 3D depth.
 * - Layer 4 (Specular Caustic Filaments): Dual glowing bezier threads along the wave crest & trough
 *   that feed crisp refractive highlights and chromatic dispersion into Liquid Glass cards.
 */
private fun DrawScope.drawBraidedSilkCurtain(
    w: Float,
    h: Float,
    leftTopY: Float,
    leftBottomY: Float,
    ctrl1Top: Offset,
    ctrl2Top: Offset,
    rightTopY: Float,
    rightBottomY: Float,
    ctrl1Bottom: Offset,
    ctrl2Bottom: Offset,
    ribbonColors: List<Color>,
    crestColor: Color,
    phase: Float,
    pulse: Float,
    additiveMode: BlendMode
) {
    val padX = w * 0.05f
    val brushStart = Offset(0f, leftBottomY)
    val brushEnd = Offset(w, rightTopY)
    val mainBrush = Brush.linearGradient(
        colors = ribbonColors,
        start = brushStart,
        end = brushEnd
    )

    // Layer 1: Wide Outer Atmospheric Veil (softens boundary so glass refraction has no harsh cliff)
    val outerSpread = h * 0.055f
    val outerPath = Path().apply {
        moveTo(-padX, leftTopY - outerSpread)
        cubicTo(
            ctrl1Top.x, ctrl1Top.y - outerSpread * 1.2f,
            ctrl2Top.x, ctrl2Top.y - outerSpread * 0.9f,
            w + padX, rightTopY - outerSpread
        )
        lineTo(w + padX, rightBottomY + outerSpread)
        cubicTo(
            ctrl1Bottom.x, ctrl1Bottom.y + outerSpread * 1.1f,
            ctrl2Bottom.x, ctrl2Bottom.y + outerSpread * 1.2f,
            -padX, leftBottomY + outerSpread
        )
        close()
    }
    drawPath(path = outerPath, brush = mainBrush, alpha = 0.28f)

    // Layer 2: Mid Silk Body
    val midPath = Path().apply {
        moveTo(-padX, leftTopY)
        cubicTo(ctrl1Top.x, ctrl1Top.y, ctrl2Top.x, ctrl2Top.y, w + padX, rightTopY)
        lineTo(w + padX, rightBottomY)
        cubicTo(ctrl1Bottom.x, ctrl1Bottom.y, ctrl2Bottom.x, ctrl2Bottom.y, -padX, leftBottomY)
        close()
    }
    drawPath(path = midPath, brush = mainBrush, alpha = 0.54f)

    // Layer 3: Inner Braided Luminous Core (creates internal silk fold and rich refraction gradients)
    val innerInset = h * 0.038f
    val braidShift = h * 0.025f * sin(phase)
    val innerPath = Path().apply {
        moveTo(-padX, leftTopY + innerInset)
        cubicTo(
            ctrl1Top.x, ctrl1Top.y + innerInset + braidShift,
            ctrl2Top.x, ctrl2Top.y + innerInset - braidShift,
            w + padX, rightTopY + innerInset
        )
        lineTo(w + padX, rightBottomY - innerInset)
        cubicTo(
            ctrl1Bottom.x, ctrl1Bottom.y - innerInset + braidShift,
            ctrl2Bottom.x, ctrl2Bottom.y - innerInset - braidShift,
            -padX, leftBottomY - innerInset
        )
        close()
    }
    drawPath(
        path = innerPath,
        brush = mainBrush,
        alpha = 0.65f,
        blendMode = additiveMode
    )

    // Layer 4: Specular Caustic Filaments along upper crest and internal braided spine
    val crestPath = Path().apply {
        moveTo(-padX, leftTopY)
        cubicTo(ctrl1Top.x, ctrl1Top.y, ctrl2Top.x, ctrl2Top.y, w + padX, rightTopY)
    }

    val spinePath = Path().apply {
        moveTo(-padX, (leftTopY + leftBottomY) * 0.5f)
        cubicTo(
            (ctrl1Top.x + ctrl2Bottom.x) * 0.5f,
            (ctrl1Top.y + ctrl2Bottom.y) * 0.5f + braidShift * 1.3f,
            (ctrl2Top.x + ctrl1Bottom.x) * 0.5f,
            (ctrl2Top.y + ctrl1Bottom.y) * 0.5f - braidShift * 1.3f,
            w + padX,
            (rightTopY + rightBottomY) * 0.5f
        )
    }

    val causticGlowBrush = Brush.horizontalGradient(
        colors = listOf(
            Color.Transparent,
            crestColor.copy(alpha = 0.18f * pulse),
            crestColor.copy(alpha = 0.42f * pulse),
            crestColor.copy(alpha = 0.22f * pulse),
            Color.Transparent
        )
    )

    val causticCoreBrush = Brush.horizontalGradient(
        colors = listOf(
            Color.Transparent,
            crestColor.copy(alpha = 0.35f * pulse),
            Color.White.copy(alpha = (if (additiveMode == BlendMode.Plus) 0.42f else 0.22f) * pulse),
            crestColor.copy(alpha = 0.35f * pulse),
            Color.Transparent
        )
    )

    // Soft wide crest bloom
    drawPath(
        path = crestPath,
        brush = causticGlowBrush,
        style = Stroke(width = w * 0.032f, cap = StrokeCap.Round),
        blendMode = additiveMode
    )
    // Fine luminous crest filament
    drawPath(
        path = crestPath,
        brush = causticCoreBrush,
        style = Stroke(width = w * 0.0055f, cap = StrokeCap.Round),
        blendMode = additiveMode
    )
    // Subtle internal braided caustic spine
    drawPath(
        path = spinePath,
        brush = causticGlowBrush,
        style = Stroke(width = w * 0.016f, cap = StrokeCap.Round),
        blendMode = additiveMode
    )
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
    drawLiquidSilkScene(
        w = w,
        h = h,
        p1 = p1,
        p2 = p2,
        pulse = pulse,
        palette = SilkAuroraPalette(
            backdropColors = listOf(
                Color(0xFF0A110F),
                Color(0xFF0F1715),
                Color(0xFF16221E),
                Color(0xFF182420),
                Color(0xFF0C1311)
            ),
            primaryBloomCore = Color(0xFFA3C1AD).copy(alpha = 0.30f),
            primaryBloomHalo = Color(0xFF8FA89B).copy(alpha = 0.14f),
            secondaryBloomCore = Color(0xFFD4B499).copy(alpha = 0.25f),
            secondaryBloomHalo = Color(0xFF8FA89B).copy(alpha = 0.12f),
            tertiaryBloomCore = Color(0xFFE6D7C3).copy(alpha = 0.20f),
            mainRibbonColors = listOf(
                Color(0xFF8FA89B).copy(alpha = 0.34f),
                Color(0xFFA3C1AD).copy(alpha = 0.30f),
                Color(0xFFD4B499).copy(alpha = 0.26f),
                Color(0xFFE6D7C3).copy(alpha = 0.20f)
            ),
            counterRibbonColors = listOf(
                Color(0xFF1E312B).copy(alpha = 0.42f),
                Color(0xFF8FA89B).copy(alpha = 0.24f),
                Color(0xFFD4B499).copy(alpha = 0.22f),
                Color(0xFFA3C1AD).copy(alpha = 0.18f)
            ),
            canopyRibbonColors = listOf(
                Color(0xFFA3C1AD).copy(alpha = 0.22f),
                Color(0xFF8FA89B).copy(alpha = 0.18f),
                Color(0xFFE6D7C3).copy(alpha = 0.14f)
            ),
            causticHighlight = Color(0xFFE6D7C3),
            causticSecondary = Color(0xFFA3C1AD)
        )
    )
}

/**
 * Option B: Nordic Twilight (Default AURORA_SILK Theme)
 * Background Mesh Gradient: Abyssal Navy (#0B101B) into Dusty Indigo (#1B263B).
 * Primary Accent: Moonlight Blue (#7C98B3), Soft Dusk Lavender (#C5B4E3), Pale Iris (#D8C5E8).
 */
private fun DrawScope.drawNordicTwilight(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float
) {
    drawLiquidSilkScene(
        w = w,
        h = h,
        p1 = p1,
        p2 = p2,
        pulse = pulse,
        palette = SilkAuroraPalette(
            backdropColors = listOf(
                Color(0xFF080C15), // Deep Abyssal Midnight
                Color(0xFF0E1525), // Abyssal Navy
                Color(0xFF172236), // Dusty Indigo
                Color(0xFF1E2942), // Twilight Slate
                Color(0xFF0B101C)  // Deep Horizon Navy
            ),
            primaryBloomCore = Color(0xFF7C98B3).copy(alpha = 0.34f),
            primaryBloomHalo = Color(0xFFC5B4E3).copy(alpha = 0.16f),
            secondaryBloomCore = Color(0xFFC5B4E3).copy(alpha = 0.28f),
            secondaryBloomHalo = Color(0xFF7C98B3).copy(alpha = 0.14f),
            tertiaryBloomCore = Color(0xFF94B0CC).copy(alpha = 0.24f),
            mainRibbonColors = listOf(
                Color(0xFF7C98B3).copy(alpha = 0.36f), // Moonlight Blue
                Color(0xFF94A8C4).copy(alpha = 0.32f), // Mist Periwinkle
                Color(0xFFC5B4E3).copy(alpha = 0.28f), // Soft Dusk Lavender
                Color(0xFFD8C5E8).copy(alpha = 0.22f)  // Pale Iris Highlight
            ),
            counterRibbonColors = listOf(
                Color(0xFF243452).copy(alpha = 0.42f), // Deep Nordic Fjord
                Color(0xFF7C98B3).copy(alpha = 0.26f), // Moonlight Blue
                Color(0xFFB3A1D6).copy(alpha = 0.24f), // Dusk Violet
                Color(0xFF8B9CB6).copy(alpha = 0.20f)  // Silver Mist
            ),
            canopyRibbonColors = listOf(
                Color(0xFF7C98B3).copy(alpha = 0.24f),
                Color(0xFFC5B4E3).copy(alpha = 0.20f),
                Color(0xFF94A8C4).copy(alpha = 0.15f)
            ),
            causticHighlight = Color(0xFFDCD0F2),
            causticSecondary = Color(0xFF9EC0E0)
        )
    )
}

/**
 * Option C: Warm Linen & Olive (Best for a "Classy" Light Mode)
 * Background: Warm Oat (#F4F1EA) to Soft Stone (#E8E2D7).
 * Accents: Olive Grove (#6B705C), Muted Terracotta (#C87D65).
 */
private fun DrawScope.drawWarmLinen(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float
) {
    drawLiquidSilkScene(
        w = w,
        h = h,
        p1 = p1,
        p2 = p2,
        pulse = pulse,
        palette = SilkAuroraPalette(
            backdropColors = listOf(
                Color(0xFFF6F3EC),
                Color(0xFFEEEAE1),
                Color(0xFFE8E2D7),
                Color(0xFFDFD8CC)
            ),
            primaryBloomCore = Color(0xFF6B705C).copy(alpha = 0.16f),
            primaryBloomHalo = Color(0xFF8F937D).copy(alpha = 0.09f),
            secondaryBloomCore = Color(0xFFC87D65).copy(alpha = 0.14f),
            secondaryBloomHalo = Color(0xFF6B705C).copy(alpha = 0.08f),
            tertiaryBloomCore = Color(0xFFD69F7E).copy(alpha = 0.12f),
            mainRibbonColors = listOf(
                Color(0xFF6B705C).copy(alpha = 0.20f),
                Color(0xFF8F937D).copy(alpha = 0.16f),
                Color(0xFFC87D65).copy(alpha = 0.14f),
                Color(0xFFE8E2D7).copy(alpha = 0.10f)
            ),
            counterRibbonColors = listOf(
                Color(0xFFC87D65).copy(alpha = 0.14f),
                Color(0xFF8F937D).copy(alpha = 0.14f),
                Color(0xFF6B705C).copy(alpha = 0.12f)
            ),
            canopyRibbonColors = listOf(
                Color(0xFF8F937D).copy(alpha = 0.12f),
                Color(0xFFC87D65).copy(alpha = 0.10f),
                Color(0xFFE8E2D7).copy(alpha = 0.08f)
            ),
            causticHighlight = Color(0xFFC87D65),
            causticSecondary = Color(0xFF6B705C),
            isLightMode = true
        )
    )
}

private fun DrawScope.drawCosmicDusk(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float
) {
    drawLiquidSilkScene(
        w = w,
        h = h,
        p1 = p1,
        p2 = p2,
        pulse = pulse,
        palette = SilkAuroraPalette(
            backdropColors = listOf(
                Color(0xFF090716),
                Color(0xFF180D2C),
                Color(0xFF281144),
                Color(0xFF1D0B35),
                Color(0xFF0C081B)
            ),
            primaryBloomCore = Color(0xFFC084FC).copy(alpha = 0.30f),
            primaryBloomHalo = Color(0xFFA855F7).copy(alpha = 0.15f),
            secondaryBloomCore = Color(0xFFFB923C).copy(alpha = 0.24f),
            secondaryBloomHalo = Color(0xFFF43F5E).copy(alpha = 0.14f),
            tertiaryBloomCore = Color(0xFFE879F9).copy(alpha = 0.20f),
            mainRibbonColors = listOf(
                Color(0xFFC084FC).copy(alpha = 0.34f),
                Color(0xFF7E22CE).copy(alpha = 0.30f),
                Color(0xFFFB923C).copy(alpha = 0.25f),
                Color(0xFFF43F5E).copy(alpha = 0.20f)
            ),
            counterRibbonColors = listOf(
                Color(0xFF4C1D95).copy(alpha = 0.36f),
                Color(0xFFA855F7).copy(alpha = 0.22f),
                Color(0xFFF43F5E).copy(alpha = 0.20f),
                Color(0xFFFB923C).copy(alpha = 0.16f)
            ),
            canopyRibbonColors = listOf(
                Color(0xFFC084FC).copy(alpha = 0.20f),
                Color(0xFFE879F9).copy(alpha = 0.15f),
                Color(0xFFFB923C).copy(alpha = 0.12f)
            ),
            causticHighlight = Color(0xFFFDBA74),
            causticSecondary = Color(0xFFD8B4FE)
        )
    )
}

private fun DrawScope.drawOceanAbyss(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float
) {
    drawLiquidSilkScene(
        w = w,
        h = h,
        p1 = p1,
        p2 = p2,
        pulse = pulse,
        palette = SilkAuroraPalette(
            backdropColors = listOf(
                Color(0xFF141236),
                Color(0xFF3B1578),
                Color(0xFF831843),
                Color(0xFF0C4A6E),
                Color(0xFF062038)
            ),
            primaryBloomCore = Color(0xFFFEF08A).copy(alpha = 0.45f),
            primaryBloomHalo = Color(0xFFF97316).copy(alpha = 0.22f),
            secondaryBloomCore = Color(0xFF38BDF8).copy(alpha = 0.30f),
            secondaryBloomHalo = Color(0xFF0284C7).copy(alpha = 0.16f),
            tertiaryBloomCore = Color(0xFFFDE047).copy(alpha = 0.26f),
            mainRibbonColors = listOf(
                Color(0xFF06B6D4).copy(alpha = 0.42f),
                Color(0xFF0284C7).copy(alpha = 0.38f),
                Color(0xFF0369A1).copy(alpha = 0.32f),
                Color(0xFF082F49).copy(alpha = 0.28f)
            ),
            counterRibbonColors = listOf(
                Color(0xFF0EA5E9).copy(alpha = 0.32f),
                Color(0xFF38BDF8).copy(alpha = 0.24f),
                Color(0xFFF59E0B).copy(alpha = 0.20f),
                Color(0xFF0369A1).copy(alpha = 0.22f)
            ),
            canopyRibbonColors = listOf(
                Color(0xFFFDE047).copy(alpha = 0.24f),
                Color(0xFFF97316).copy(alpha = 0.18f),
                Color(0xFFEC4899).copy(alpha = 0.14f)
            ),
            causticHighlight = Color(0xFFFEF08A),
            causticSecondary = Color(0xFF7DD3FC)
        )
    )
}

/**
 * Multi-depth refractive bokeh orbs & twinkling light motes.
 * Uses strictly integer harmonic multipliers of [phase1] and [phase2] so particles
 * drift continuously without ever snapping at animation loop boundaries.
 */
private fun DrawScope.drawFloatingLightMotes(
    w: Float,
    h: Float,
    phase1: Float,
    phase2: Float = phase1,
    pulse: Float = 1f
) {
    // Layer 1: Soft out-of-focus refractive bokeh discs (feed smooth color orbs into glass cards)
    val bokehSeeds = listOf(
        Triple(0.18f, 0.16f, 28f),
        Triple(0.78f, 0.24f, 34f),
        Triple(0.30f, 0.46f, 24f),
        Triple(0.74f, 0.58f, 30f),
        Triple(0.22f, 0.78f, 32f),
        Triple(0.68f, 0.86f, 26f)
    )

    bokehSeeds.forEachIndexed { idx, (nx, ny, baseRadius) ->
        val x = w * (nx + 0.05f * sin(phase1 + idx * 1.1f))
        val y = h * (ny + 0.04f * cos(phase2 + idx * 0.9f))
        val r = baseRadius * pulse
        val bokehAlpha = 0.05f + 0.04f * ((sin(phase1 + phase2 + idx) + 1f) * 0.5f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = bokehAlpha),
                    Color(0xFFC5B4E3).copy(alpha = bokehAlpha * 0.55f),
                    Color.Transparent
                ),
                center = Offset(x, y),
                radius = r
            ),
            center = Offset(x, y),
            radius = r,
            blendMode = BlendMode.Plus
        )
    }

    // Layer 2: Crisp foreground starlight motes with integer-harmonic orbital drift
    val moteSeeds = listOf(
        Pair(0.12f, 0.10f) to 2.1f,
        Pair(0.85f, 0.08f) to 2.6f,
        Pair(0.72f, 0.22f) to 1.8f,
        Pair(0.28f, 0.26f) to 1.5f,
        Pair(0.92f, 0.35f) to 2.4f,
        Pair(0.08f, 0.40f) to 1.9f,
        Pair(0.48f, 0.44f) to 1.6f,
        Pair(0.82f, 0.52f) to 2.2f,
        Pair(0.15f, 0.58f) to 2.0f,
        Pair(0.64f, 0.63f) to 1.4f,
        Pair(0.36f, 0.70f) to 2.3f,
        Pair(0.88f, 0.76f) to 2.5f,
        Pair(0.22f, 0.82f) to 1.8f,
        Pair(0.76f, 0.88f) to 2.1f
    )

    moteSeeds.forEachIndexed { idx, (pos, radius) ->
        val xDrift = sin(phase1 + idx * 0.8f) * (w * 0.022f)
        val yDrift = cos(phase2 + idx * 0.6f) * (h * 0.018f)
        val moteX = (pos.first * w + xDrift).coerceIn(0f, w)
        val moteY = (pos.second * h + yDrift).coerceIn(0f, h)

        val twinkleAlpha = 0.22f + 0.52f * ((sin(phase1 * 2f + idx * 1.3f) + 1f) * 0.5f)

        drawCircle(
            color = Color.White.copy(alpha = twinkleAlpha * 0.24f),
            radius = radius * 2.6f,
            center = Offset(moteX, moteY),
            blendMode = BlendMode.Plus
        )
        drawCircle(
            color = Color.White.copy(alpha = twinkleAlpha * 0.80f),
            radius = radius,
            center = Offset(moteX, moteY)
        )
    }
}

private fun DrawScope.drawForestRain(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float
) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A0F0D),
                Color(0xFF16201C),
                Color(0xFF213029),
                Color(0xFF101714)
            )
        )
    )

    val fogCenter = Offset(
        x = w * (0.42f + 0.12f * sin(p1)),
        y = h * (0.34f + 0.06f * cos(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF5A7266).copy(alpha = 0.26f * pulse),
                Color(0xFF28362D).copy(alpha = 0.12f),
                Color.Transparent
            ),
            center = fogCenter,
            radius = w * 0.92f
        ),
        center = fogCenter,
        radius = w * 0.92f
    )

    drawBraidedSilkCurtain(
        w = w,
        h = h,
        leftTopY = h * (0.66f + 0.04f * sin(p1)),
        leftBottomY = h * 0.96f,
        ctrl1Top = Offset(w * 0.35f, h * (0.60f + 0.05f * sin(p1))),
        ctrl2Top = Offset(w * 0.65f, h * (0.72f + 0.05f * cos(p2))),
        rightTopY = h * (0.64f + 0.04f * cos(p1)),
        rightBottomY = h * 0.96f,
        ctrl1Bottom = Offset(w * 0.65f, h * 0.92f),
        ctrl2Bottom = Offset(w * 0.35f, h * 0.92f),
        ribbonColors = listOf(
            Color(0xFF33473C).copy(alpha = 0.45f),
            Color(0xFF1D2922).copy(alpha = 0.55f),
            Color(0xFF0F1713).copy(alpha = 0.70f)
        ),
        crestColor = Color(0xFF8BA698),
        phase = p1,
        pulse = pulse,
        additiveMode = BlendMode.Plus
    )

    // Seamless Rain Streaks (integer speed multipliers 1, 2, 3 prevent loop jump)
    val numDrops = 18
    val normPhase = p1 / TWO_PI
    for (i in 0 until numDrops) {
        val startX = (w * 0.055f * i + w * 0.29f * (i % 3)) % w
        val speedMultiplier = 1 + (i % 3)
        val rawY = (normPhase * speedMultiplier + (i * 0.17f)) % 1f
        val dropY = h * rawY
        val streakLength = h * 0.055f + (i % 3) * h * 0.018f

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF9DB8AA).copy(alpha = 0.18f + 0.06f * (i % 2)),
                    Color(0xFFD2E3DA).copy(alpha = 0.28f)
                ),
                startY = dropY,
                endY = dropY + streakLength
            ),
            start = Offset(startX, dropY),
            end = Offset(startX, dropY + streakLength),
            strokeWidth = 1.4f + (i % 2) * 0.8f,
            cap = StrokeCap.Round
        )

        if (rawY > 0.65f) {
            val rippleProgress = (rawY - 0.65f) / 0.35f
            drawOval(
                color = Color(0xFF9DB8AA).copy(alpha = 0.18f * (1f - rippleProgress)),
                topLeft = Offset(
                    startX - w * 0.045f * rippleProgress,
                    dropY + streakLength - h * 0.01f * rippleProgress
                ),
                size = Size(w * 0.09f * rippleProgress, h * 0.022f * rippleProgress),
                style = Stroke(width = 1.6f)
            )
        }
    }
}

private fun DrawScope.drawBioluminescentWake(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float
) {
    drawRect(Color(0xFF030509))

    val centerX = w * 0.5f
    val boatY = h * 0.25f
    val boatWidth = w * 0.05f
    val boatHeight = h * 0.08f

    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF80D8FF).copy(alpha = 0.18f * pulse),
                Color(0xFF0066FF).copy(alpha = 0.06f),
                Color.Transparent
            ),
            center = Offset(centerX, boatY + boatHeight * 0.5f),
            radius = w * 0.24f
        ),
        topLeft = Offset(centerX - w * 0.24f, boatY - h * 0.06f),
        size = Size(w * 0.48f, h * 0.28f)
    )

    val wakeLength = h * 0.78f
    val wakeMaxSpread = w * 0.38f
    val trailCount = 14

    for (i in 0 until trailCount) {
        val trailPath = Path()
        trailPath.moveTo(centerX, boatY + boatHeight * 0.7f)

        val phaseOffset = p1 + i * 0.45f
        val side = if (i % 2 == 0) 1f else -1f
        val spreadFactor = (i / trailCount.toFloat()) * wakeMaxSpread * side

        val ctrl1X = centerX + spreadFactor * 0.3f + sin(phaseOffset) * w * 0.022f
        val ctrl1Y = boatY + wakeLength * 0.3f

        val ctrl2X = centerX + spreadFactor * 0.7f + cos(p2 + i * 0.6f) * w * 0.032f
        val ctrl2Y = boatY + wakeLength * 0.7f

        val endX = centerX + spreadFactor + sin(p1 - p2 + i * 0.3f) * w * 0.04f
        val endY = boatY + wakeLength

        trailPath.cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, endX, endY)

        val trailAlpha = 0.46f - (i / trailCount.toFloat()) * 0.30f
        val strokeWidth = w * 0.009f + (i % 3) * w * 0.005f

        drawPath(
            path = trailPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE0F7FA).copy(alpha = trailAlpha * pulse),
                    Color(0xFF00B0FF).copy(alpha = trailAlpha * 0.72f),
                    Color(0xFF0038D6).copy(alpha = trailAlpha * 0.22f),
                    Color.Transparent
                ),
                startY = boatY,
                endY = boatY + wakeLength
            ),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            blendMode = BlendMode.Plus
        )
    }

    val centerWakePath = Path().apply {
        moveTo(centerX, boatY + boatHeight * 0.8f)
        cubicTo(
            centerX + sin(p1 * 2f) * w * 0.012f, boatY + wakeLength * 0.3f,
            centerX + cos(p2 * 2f) * w * 0.016f, boatY + wakeLength * 0.7f,
            centerX + sin(p1) * w * 0.022f, boatY + wakeLength
        )
    }

    drawPath(
        path = centerWakePath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f * pulse),
                Color(0xFF80D8FF).copy(alpha = 0.52f),
                Color(0xFF0066FF).copy(alpha = 0.12f),
                Color.Transparent
            ),
            startY = boatY,
            endY = boatY + wakeLength
        ),
        style = Stroke(width = w * 0.016f, cap = StrokeCap.Round),
        blendMode = BlendMode.Plus
    )

    val boatPath = Path().apply {
        moveTo(centerX, boatY)
        quadraticTo(
            centerX + boatWidth * 0.5f,
            boatY + boatHeight * 0.3f,
            centerX + boatWidth * 0.5f,
            boatY + boatHeight * 0.8f
        )
        lineTo(centerX - boatWidth * 0.5f, boatY + boatHeight * 0.8f)
        quadraticTo(
            centerX - boatWidth * 0.5f,
            boatY + boatHeight * 0.3f,
            centerX,
            boatY
        )
        close()
    }
    drawPath(path = boatPath, color = Color(0xFFE2E8F0))

    val cockpitPath = Path().apply {
        moveTo(centerX, boatY + boatHeight * 0.35f)
        lineTo(centerX + boatWidth * 0.35f, boatY + boatHeight * 0.45f)
        lineTo(centerX + boatWidth * 0.35f, boatY + boatHeight * 0.65f)
        lineTo(centerX - boatWidth * 0.35f, boatY + boatHeight * 0.65f)
        lineTo(centerX - boatWidth * 0.35f, boatY + boatHeight * 0.45f)
        close()
    }
    drawPath(path = cockpitPath, color = Color(0xFF0F172A))

    drawRect(
        color = Color(0xFF92400E),
        topLeft = Offset(centerX - boatWidth * 0.4f, boatY + boatHeight * 0.8f),
        size = Size(boatWidth * 0.8f, boatHeight * 0.15f)
    )
}

/**
 * Night Ferris Wheel Theme (FW):
 * - Seamless 4-spoke sector rotation (`p1 / 6f`) guarantees zero loop snap across both the 24-spoke
 *   wheel structure and the alternating 4-spoke violet/warm-white cabins.
 * - Soft lunar atmospheric halo, subtle night-sky mist, and glowing neon rim/cabin blooms feed
 *   lush bokeh highlights into Liquid Glass cards.
 */
private fun DrawScope.drawFerrisWheel(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float
) {
    // 1. Deep OLED night sky with subtle vertical midnight-indigo horizon depth
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF010103),
                Color(0xFF050710),
                Color(0xFF090C1A),
                Color(0xFF020205)
            )
        )
    )

    // 2. Full Moon with soft refractive atmospheric halo
    val moonCenter = Offset(w * 0.82f, h * 0.12f)
    val moonRadius = w * 0.068f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFCBD5E1).copy(alpha = 0.24f * pulse),
                Color(0xFF818CF8).copy(alpha = 0.08f),
                Color.Transparent
            ),
            center = moonCenter,
            radius = moonRadius * 4.8f
        ),
        center = moonCenter,
        radius = moonRadius * 4.8f,
        blendMode = BlendMode.Plus
    )

    drawCircle(Color(0xFFF8F8FA), radius = moonRadius, center = moonCenter)
    drawCircle(
        color = Color(0xFFE4E4E7).copy(alpha = 0.5f),
        radius = moonRadius * 0.25f,
        center = Offset(moonCenter.x - moonRadius * 0.35f, moonCenter.y + moonRadius * 0.25f)
    )
    drawCircle(
        color = Color(0xFFD4D4D8).copy(alpha = 0.4f),
        radius = moonRadius * 0.35f,
        center = Offset(moonCenter.x + moonRadius * 0.2f, moonCenter.y - moonRadius * 0.15f)
    )
    drawCircle(
        color = Color(0xFFE4E4E7).copy(alpha = 0.6f),
        radius = moonRadius * 0.15f,
        center = Offset(moonCenter.x + moonRadius * 0.1f, moonCenter.y + moonRadius * 0.45f)
    )

    // 3. Ferris Wheel geometry
    val wheelCenter = Offset(w * 1.15f, h * 0.68f)
    val outerRadius = w * 1.05f
    val innerRadius1 = w * 0.95f
    val innerRadius2 = w * 0.70f

    // 24 spokes with cabin colors repeating every 4 spokes (6 identical 4-spoke sectors per 2π).
    // Rotating by 1 full 4-spoke sector (p1 / 6f) per 0..2π cycle makes the loop 100% seamless.
    val numSpokes = 24
    val sectorStep = TWO_PI / numSpokes
    val rotationAngle = p1 / 6f

    // Soft neon rim aura behind the wheel rings so Liquid Glass cards refract a dreamy rim glow
    drawCircle(
        color = Color(0xFFD8B4E2).copy(alpha = 0.11f * pulse),
        radius = outerRadius,
        center = wheelCenter,
        style = Stroke(width = w * 0.065f),
        blendMode = BlendMode.Plus
    )
    drawCircle(
        color = Color(0xFFFFF7ED).copy(alpha = 0.12f * pulse),
        radius = innerRadius1,
        center = wheelCenter,
        style = Stroke(width = w * 0.038f),
        blendMode = BlendMode.Plus
    )

    // 4. Spokes and Truss Webbing
    for (i in 0 until numSpokes) {
        val angle = rotationAngle + i * sectorStep
        val cosA = cos(angle)
        val sinA = sin(angle)
        val outerX = wheelCenter.x + outerRadius * cosA
        val outerY = wheelCenter.y + outerRadius * sinA

        drawLine(
            color = Color(0xFFD4D4D8).copy(alpha = 0.78f),
            start = wheelCenter,
            end = Offset(outerX, outerY),
            strokeWidth = 2.6f
        )

        val nextAngle = angle + sectorStep
        val nextInnerX = wheelCenter.x + innerRadius2 * cos(nextAngle)
        val nextInnerY = wheelCenter.y + innerRadius2 * sin(nextAngle)
        val currOuterX = wheelCenter.x + innerRadius1 * cosA
        val currOuterY = wheelCenter.y + innerRadius1 * sinA

        drawLine(
            color = Color(0xFFA1A1AA).copy(alpha = 0.55f),
            start = Offset(currOuterX, currOuterY),
            end = Offset(nextInnerX, nextInnerY),
            strokeWidth = 1.8f
        )
    }

    // Wheel Structural Rings
    drawCircle(
        color = Color(0xFFE4E4E7).copy(alpha = 0.92f),
        radius = outerRadius,
        center = wheelCenter,
        style = Stroke(width = 5.5f)
    )
    drawCircle(
        color = Color(0xFFD4D4D8).copy(alpha = 0.78f),
        radius = innerRadius1,
        center = wheelCenter,
        style = Stroke(width = 3.8f)
    )
    drawCircle(
        color = Color(0xFFA1A1AA).copy(alpha = 0.58f),
        radius = innerRadius2,
        center = wheelCenter,
        style = Stroke(width = 2.8f)
    )

    // 5. Glowing Gondola Cabins with gentle gravity-leveled sway
    val cabinWidth = w * 0.115f
    val cabinHeight = h * 0.066f
    for (i in 0 until numSpokes) {
        val angle = rotationAngle + i * sectorStep
        val cabinAttachX = wheelCenter.x + outerRadius * cos(angle)
        val cabinAttachY = wheelCenter.y + outerRadius * sin(angle)

        if (cabinAttachX > -cabinWidth * 2f &&
            cabinAttachX < w + cabinWidth &&
            cabinAttachY > -cabinHeight * 2f &&
            cabinAttachY < h + cabinHeight * 2f
        ) {
            val swayX = sin(p2 + i * 0.5f) * 2.2f
            val left = cabinAttachX - cabinWidth / 2f + swayX
            val top = cabinAttachY + cabinHeight * 0.18f
            val right = left + cabinWidth
            val cabinCenter = Offset((left + right) * 0.5f, top + cabinHeight * 0.5f)

            val isViolet = i % 4 == 0
            val windowColor = if (isViolet) Color(0xFFD8B4E2) else Color(0xFFFFF7ED)

            // Soft radial light bloom around each cabin
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        windowColor.copy(alpha = 0.32f * pulse),
                        windowColor.copy(alpha = 0.10f * pulse),
                        Color.Transparent
                    ),
                    center = cabinCenter,
                    radius = cabinWidth * 1.15f
                ),
                center = cabinCenter,
                radius = cabinWidth * 1.15f,
                blendMode = BlendMode.Plus
            )

            // Suspension struts
            drawLine(
                color = Color(0xFFD4D4D8),
                start = Offset(cabinAttachX, cabinAttachY),
                end = Offset(left + cabinWidth * 0.18f, top),
                strokeWidth = 2.5f
            )
            drawLine(
                color = Color(0xFFD4D4D8),
                start = Offset(cabinAttachX, cabinAttachY),
                end = Offset(right - cabinWidth * 0.18f, top),
                strokeWidth = 2.5f
            )
            drawCircle(
                color = Color(0xFFE4E4E7),
                radius = 4.5f,
                center = Offset(cabinAttachX, cabinAttachY)
            )

            // Cabin body
            drawRoundRect(
                color = Color(0xFF27272A),
                topLeft = Offset(left, top),
                size = Size(cabinWidth, cabinHeight),
                cornerRadius = CornerRadius(10f, 10f)
            )

            // Illuminated dual cabin windows
            drawRoundRect(
                color = windowColor.copy(alpha = 0.95f),
                topLeft = Offset(left + cabinWidth * 0.11f, top + cabinHeight * 0.16f),
                size = Size(cabinWidth * 0.34f, cabinHeight * 0.58f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = windowColor.copy(alpha = 0.95f),
                topLeft = Offset(left + cabinWidth * 0.55f, top + cabinHeight * 0.16f),
                size = Size(cabinWidth * 0.34f, cabinHeight * 0.58f),
                cornerRadius = CornerRadius(5f, 5f)
            )
        }
    }
}

/**
 * Time Dynamic Theme (TD): Adaptive Morning, Afternoon, and Evening/Night Fluted Glass Wave.
 * Renders 9 cylindrical fluted glass columns with dual counter-propagating harmonic light arcs
 * (primary lower-mid parabolic wave + secondary upper-canopy wave) so Liquid Glass cards at every
 * vertical position refract rich, time-adaptive light bands.
 */
private fun DrawScope.drawTimeDynamic(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    val baseDark: Color
    val panelDark: Color
    val midGlow: Color
    val brightCore: Color
    val highlightEdge: Color
    val coreWhite = Color.White

    when (hour) {
        in 5..11 -> { // Morning / Dawn (Crisp Celadon Green & Eucalyptus)
            baseDark = Color(0xFF030D08)
            panelDark = Color(0xFF081C12)
            midGlow = Color(0xFF2C5E3B)
            brightCore = Color(0xFF8FD870)
            highlightEdge = Color(0xFFE2FFB0)
        }
        in 12..17 -> { // Afternoon / Dusk (Crisp Golden Amber & Warm Sun)
            baseDark = Color(0xFF0D0703)
            panelDark = Color(0xFF1E1007)
            midGlow = Color(0xFF5E3C21)
            brightCore = Color(0xFFD89B58)
            highlightEdge = Color(0xFFFFF1C6)
        }
        else -> { // Night / Evening (Crisp Deep Indigo & Moonlight Violet)
            baseDark = Color(0xFF04040E)
            panelDark = Color(0xFF0B0B20)
            midGlow = Color(0xFF2B295B)
            brightCore = Color(0xFF7B76D8)
            highlightEdge = Color(0xFFDCDAEE)
        }
    }

    drawRect(color = baseDark)

    val numPanels = 9
    val panelWidth = w / numPanels
    val primaryWaveOffset = sin(p1) * (h * 0.045f)
    val secondaryWaveOffset = cos(p2) * (h * 0.035f)

    val primaryArcY = FloatArray(numPanels)
    val secondaryArcY = FloatArray(numPanels)

    for (i in 0 until numPanels) {
        val panelCenterX = (i + 0.5f) * panelWidth
        val normDist = (panelCenterX - (w / 2f)) / (w * 0.48f)

        // Primary lower-mid parabolic wave with travelling ripple
        val center1 = h * 0.60f + primaryWaveOffset + sin(p2 + i * 0.42f) * (h * 0.024f)
        primaryArcY[i] = center1 + (normDist * normDist) * (h * 0.22f)

        // Secondary upper-canopy inverted wave (feeds refractive light to top cards)
        val center2 = h * 0.24f + secondaryWaveOffset + cos(p1 + i * 0.36f) * (h * 0.020f)
        secondaryArcY[i] = center2 - (normDist * normDist) * (h * 0.10f)
    }

    // 1. Render 9 Fluted Glass Columns with internal light beams
    for (i in 0 until numPanels) {
        val left = i * panelWidth
        val arcY1 = primaryArcY[i]
        val arcY2 = secondaryArcY[i]

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    baseDark,
                    panelDark,
                    midGlow.copy(alpha = 0.32f),
                    panelDark,
                    baseDark
                ),
                startY = 0f,
                endY = h
            ),
            topLeft = Offset(left, 0f),
            size = Size(panelWidth, h)
        )

        // Upper Canopy Light Beam inside column
        val upperTop = (arcY2 - h * 0.12f * pulse).coerceAtLeast(0f)
        val upperBottom = (arcY2 + h * 0.12f * pulse).coerceAtMost(h)
        if (upperBottom > upperTop) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        midGlow.copy(alpha = 0.22f),
                        brightCore.copy(alpha = 0.50f),
                        highlightEdge.copy(alpha = 0.68f),
                        brightCore.copy(alpha = 0.44f),
                        midGlow.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    startY = upperTop,
                    endY = upperBottom
                ),
                topLeft = Offset(left, upperTop),
                size = Size(panelWidth, upperBottom - upperTop)
            )
        }

        // Primary Parabolic Light Beam inside column
        val bandTop = (arcY1 - h * 0.14f * pulse).coerceAtLeast(0f)
        val bandBottom = (arcY1 + h * 0.15f * pulse).coerceAtMost(h)
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
                size = Size(panelWidth, bandBottom - bandTop)
            )
        }

        // Cylindrical 3D fluted-glass horizontal shading across each reed column
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.30f),
                    highlightEdge.copy(alpha = 0.09f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.24f)
                ),
                startX = left,
                endX = left + panelWidth
            ),
            topLeft = Offset(left, 0f),
            size = Size(panelWidth, h)
        )
    }

    // 2. Trace smooth continuous parabolic curves across panel centers
    fun buildSmoothArcPath(yPoints: FloatArray): Path {
        val path = Path()
        for (i in 0 until numPanels) {
            val x = (i + 0.5f) * panelWidth
            val y = yPoints[i]
            if (i == 0) {
                path.moveTo(0f, y)
                path.lineTo(x, y)
            } else {
                val prevX = (i - 0.5f) * panelWidth
                val prevY = yPoints[i - 1]
                val midX = (prevX + x) * 0.5f
                val midY = (prevY + y) * 0.5f
                path.quadraticTo(prevX, prevY, midX, midY)
            }
        }
        path.lineTo(w, yPoints[numPanels - 1])
        return path
    }

    val upperArcPath = buildSmoothArcPath(secondaryArcY)
    val primaryArcPath = buildSmoothArcPath(primaryArcY)

    // Upper Canopy Glowing Arc Filament
    drawPath(
        path = upperArcPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                brightCore.copy(alpha = 0.15f),
                highlightEdge.copy(alpha = 0.55f),
                coreWhite.copy(alpha = 0.72f),
                highlightEdge.copy(alpha = 0.55f),
                brightCore.copy(alpha = 0.15f)
            )
        ),
        style = Stroke(width = 2.0f, cap = StrokeCap.Round)
    )

    // Primary Glowing Parabolic Arc Rim
    drawPath(
        path = primaryArcPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                brightCore.copy(alpha = 0.30f),
                highlightEdge.copy(alpha = 0.55f),
                coreWhite.copy(alpha = 0.75f),
                highlightEdge.copy(alpha = 0.55f),
                brightCore.copy(alpha = 0.30f)
            )
        ),
        style = Stroke(width = 10f, cap = StrokeCap.Round),
        blendMode = BlendMode.Plus
    )
    drawPath(
        path = primaryArcPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                brightCore.copy(alpha = 0.55f),
                highlightEdge.copy(alpha = 0.95f),
                coreWhite,
                highlightEdge.copy(alpha = 0.95f),
                brightCore.copy(alpha = 0.55f)
            )
        ),
        style = Stroke(width = 2.4f, cap = StrokeCap.Round)
    )

    // 3. Fluted Glass Partition Seam Lines
    for (i in 1 until numPanels) {
        val seamX = i * panelWidth
        val seamY1 = (primaryArcY[i - 1] + primaryArcY[i]) * 0.5f

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    highlightEdge.copy(alpha = 0.06f),
                    highlightEdge.copy(alpha = 0.38f),
                    coreWhite.copy(alpha = 0.78f),
                    highlightEdge.copy(alpha = 0.38f),
                    highlightEdge.copy(alpha = 0.06f)
                ),
                startY = 0f,
                endY = h
            ),
            start = Offset(seamX, 0f),
            end = Offset(seamX, h),
            strokeWidth = 1.6f
        )

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    coreWhite.copy(alpha = 0.30f),
                    coreWhite.copy(alpha = 0.95f),
                    coreWhite.copy(alpha = 0.30f),
                    Color.Transparent
                ),
                startY = (seamY1 - h * 0.12f).coerceAtLeast(0f),
                endY = (seamY1 + h * 0.12f).coerceAtMost(h)
            ),
            start = Offset(seamX, 0f),
            end = Offset(seamX, h),
            strokeWidth = 1.0f
        )
    }
}

/**
 * Looping Trios Theme (LT):
 * - Drifting ambient refraction blooms beneath the obsidian geometric lattice give Liquid Glass cards
 *   soft tonal depth and chromatic edge dispersion.
 * - Spatial wave phase propagation (`motionEnvelope`) makes the triangle trios ripple across the
 *   screen during rotation and settle into exact 90° crystalline alignment during pause intervals.
 */
@Composable
fun LoopingTriosBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF0B0E14),
    solidTriangleColor: Color = Color(0xFF1A2230),
    stripeTriangleColor: Color = Color(0xFF283447),
    cellSizeDp: Float = 150f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LoopingTrios")
    val silkyStepEasing = remember { CubicBezierEasing(0.32f, 0.0f, 0.20f, 1.0f) }

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 8000
                0f at 0 using silkyStepEasing
                0f at 400 using silkyStepEasing
                1f at 2000 using silkyStepEasing
                1f at 2400 using silkyStepEasing
                2f at 4000 using silkyStepEasing
                2f at 4400 using silkyStepEasing
                3f at 6000 using silkyStepEasing
                3f at 6400 using silkyStepEasing
                4f at 8000 using silkyStepEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "PhaseAnimation"
    )

    val ambientPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AmbientGlowPhase"
    )

    val reusablePath = remember { Path() }
    val density = LocalDensity.current.density

    Box(modifier = modifier.fillMaxSize().background(backgroundColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Subtle Ambient Refraction Under-Glow
            val glow1 = Offset(
                x = w * (0.30f + 0.15f * sin(ambientPhase)),
                y = h * (0.28f + 0.10f * cos(ambientPhase))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF384B6B).copy(alpha = 0.24f),
                        Color(0xFF1E293B).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = glow1,
                    radius = w * 0.85f
                ),
                center = glow1,
                radius = w * 0.85f
            )

            val glow2 = Offset(
                x = w * (0.72f - 0.14f * cos(ambientPhase)),
                y = h * (0.70f + 0.10f * sin(ambientPhase))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF473B6B).copy(alpha = 0.20f),
                        Color(0xFF1E293B).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = glow2,
                    radius = w * 0.80f
                ),
                center = glow2,
                radius = w * 0.80f
            )

            // 2. Staggered Geometric Trio Lattice
            val cellW = cellSizeDp * density
            val cellH = cellSizeDp * 1.15f * density
            val triR = cellW * 0.18f

            val cols = ceil((w / cellW).toDouble()).toInt() + 2
            val rows = ceil((h / cellH).toDouble()).toInt() + 2

            // Vanishes at integer stops (0, 1, 2, 3, 4) so triangles lock into exact 90° alignment at rest
            val motionEnvelope = (1f - cos(phase * TWO_PI)) * 0.5f

            for (col in -1 until cols) {
                for (row in -1 until rows) {
                    val baseX = col * cellW
                    val baseY = row * cellH
                    val spatialOffset = (col * 0.35f + row * 0.25f)
                    val localRipple = sin(ambientPhase + spatialOffset) * 8f * motionEnvelope

                    val primaryAngle = phase * 90f + localRipple
                    val secondaryAngle = -phase * 90f - localRipple

                    drawTrioUnit(
                        centerX = baseX + cellW * 0.28f,
                        centerY = baseY + cellH * 0.58f,
                        r = triR,
                        primaryAngle = primaryAngle,
                        secondaryAngle = secondaryAngle,
                        solidColor = solidTriangleColor,
                        stripeColor = stripeTriangleColor,
                        path = reusablePath,
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
                        path = reusablePath,
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

    withTransform({
        translate(centerX, centerY - verticalOffset)
        rotate(primaryAngle, pivot = Offset.Zero)
    }) {
        drawStripedTriangle(
            path = path,
            p1 = Offset(0f, -r),
            p2 = Offset(-r * 1.1f, -r * 0.35f),
            p3 = Offset(r * 0.85f, 0f),
            color = stripeColor,
            stripeAngle = -primaryAngle,
            density = density
        )
        drawSolidTriangle(
            path = path,
            p1 = Offset(0f, -r),
            p2 = Offset(-r * 0.95f, 0f),
            p3 = Offset(r * 0.95f, 0f),
            color = solidColor,
            density = density
        )
    }

    withTransform({
        translate(centerX, centerY + verticalOffset)
        rotate(primaryAngle, pivot = Offset.Zero)
    }) {
        drawStripedTriangle(
            path = path,
            p1 = Offset(0f, r),
            p2 = Offset(-r * 1.1f, r * 0.35f),
            p3 = Offset(r * 0.85f, 0f),
            color = stripeColor,
            stripeAngle = -primaryAngle,
            density = density
        )
        drawSolidTriangle(
            path = path,
            p1 = Offset(0f, r),
            p2 = Offset(-r * 0.95f, 0f),
            p3 = Offset(r * 0.95f, 0f),
            color = solidColor,
            density = density
        )
    }
}

private fun DrawScope.drawSolidTriangle(
    path: Path,
    p1: Offset,
    p2: Offset,
    p3: Offset,
    color: Color,
    density: Float = 1f
) {
    path.reset()
    path.moveTo(p1.x, p1.y)
    path.lineTo(p2.x, p2.y)
    path.lineTo(p3.x, p3.y)
    path.close()

    val highlightTint = Color(
        red = (color.red * 1.35f).coerceAtMost(1f),
        green = (color.green * 1.35f).coerceAtMost(1f),
        blue = (color.blue * 1.40f).coerceAtMost(1f),
        alpha = color.alpha
    )

    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(highlightTint, color),
            start = p1,
            end = Offset((p2.x + p3.x) * 0.5f, (p2.y + p3.y) * 0.5f)
        )
    )

    drawPath(
        path = path,
        color = highlightTint.copy(alpha = 0.45f),
        style = Stroke(width = 0.9f * density, join = StrokeJoin.Round)
    )
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
    val maxExtent = 120f * density
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
                    strokeWidth = 1.5f * density,
                    cap = StrokeCap.Round
                )
                y += step
            }
        }
    }
}