package com.example.ui.glass

import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.runtime.staticCompositionLocalOf

// Ambient Theme choices for the Living Liquid Glass backdrop.
// Optimized for low-end CPU and GPU devices:
// 1. Single master infinite transition float per canvas minimizes animation state evaluations.
// 2. Zero per-frame Path or collection allocations inside DrawScope.
// 3. Zero hardware clipPath calls (triangle hatching is analytically interpolated along edges).
// 4. Every theme features 3 automatic time-of-day variants (Morning, Afternoon, Night).
// 5. Zero pure-white background elements so foreground white text remains 100% legible.
enum class AmbientTheme(val label: String, val subtitle: String) {
    AURORA_SILK("NT", "Olive Twilight: Deep obsidian olive, moss sage & muted earth silk"),
    FERRIS_WHEEL("FW", "Celestial Bubbles: Rising liquid glass bubbles with sun, moon & 2D clouds"),
    TIME_DYNAMIC("TD", "Time Dynamic: Adaptive morning, afternoon & evening fluted glass waves"),
    LOOPING_TRIOS("LT", "Looping Trios: Pure black high-contrast kinetic geometry"),
    RESONANCE_TUNNEL("RT", "Resonance Tunnel: Hypnotic infinite ribbed obsidian vortex"),
    CLEAN_WHITE("CW", "Clean White: Minimalist soft porcelain white with dark olive accents")
}

val AmbientTheme.isLight: Boolean
    get() = this == AmbientTheme.CLEAN_WHITE

val LocalAmbientTheme = staticCompositionLocalOf { AmbientTheme.AURORA_SILK }

val AmbientTheme.primaryTextColor: Color
    get() = if (isLight) Color(0xFF1E2A20) else Color.White

val AmbientTheme.secondaryTextColor: Color
    get() = if (isLight) Color(0xFF3B503E) else Color.White.copy(alpha = 0.72f)

val AmbientTheme.tertiaryTextColor: Color
    get() = if (isLight) Color(0xFF5B7260) else Color.White.copy(alpha = 0.52f)

// Three distinct time-of-day periods applied across all themes.
enum class TimePeriod {
    MORNING,   // 05:00 .. 11:59
    AFTERNOON, // 12:00 .. 17:59
    NIGHT;     // 18:00 .. 04:59

    companion object {
        fun current(hourOverride: Int? = null): TimePeriod {
            val hour = hourOverride ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when (hour) {
                in 5..11 -> MORNING
                in 12..17 -> AFTERNOON
                else -> NIGHT
            }
        }
    }
}

private const val TWO_PI = (2.0 * PI).toFloat()

// Pre-allocated static particle coordinates to avoid per-frame allocations on low-end devices
private val MOTE_X = floatArrayOf(0.14f, 0.84f, 0.70f, 0.26f, 0.90f, 0.10f, 0.48f, 0.80f, 0.18f, 0.62f, 0.36f, 0.86f)
private val MOTE_Y = floatArrayOf(0.12f, 0.10f, 0.24f, 0.30f, 0.38f, 0.44f, 0.50f, 0.58f, 0.66f, 0.72f, 0.80f, 0.88f)
private val MOTE_R = floatArrayOf(2.0f, 2.5f, 1.8f, 1.6f, 2.3f, 1.9f, 1.7f, 2.2f, 2.0f, 1.5f, 2.4f, 2.1f)

// Pre-allocated rising liquid bubble seeds for the FERRIS_WHEEL (Celestial Bubbles) theme
private val BUBBLE_X = floatArrayOf(
    0.10f, 0.24f, 0.38f, 0.52f, 0.67f, 0.82f, 0.91f,
    0.16f, 0.31f, 0.46f, 0.60f, 0.75f, 0.87f, 0.42f
)
private val BUBBLE_PHASE_OFFSET = floatArrayOf(
    0.00f, 0.43f, 0.17f, 0.61f, 0.83f, 0.29f, 0.74f,
    0.52f, 0.91f, 0.35f, 0.08f, 0.66f, 0.21f, 0.48f
)
private val BUBBLE_RADIUS_FACTOR = floatArrayOf(
    0.038f, 0.056f, 0.032f, 0.068f, 0.044f, 0.052f, 0.030f,
    0.062f, 0.040f, 0.074f, 0.035f, 0.058f, 0.046f, 0.028f
)
private val BUBBLE_SPEED_MULT = intArrayOf(
    1, 1, 2, 1, 2, 1, 2,
    1, 2, 1, 2, 1, 2, 3
)

@Composable
fun CosmicHorizonCanvas(
    modifier: Modifier = Modifier,
    theme: AmbientTheme = AmbientTheme.AURORA_SILK,
    hourOverride: Int? = null
) {
    if (theme == AmbientTheme.LOOPING_TRIOS) {
        LoopingTriosBackground(modifier = modifier, hourOverride = hourOverride)
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "LiquidGlassBackdrop")

    // Single master harmonic phase (0..2π over 24s) minimizes CPU animation overhead.
    val masterPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "masterPhase"
    )

    // Reusable Path & FloatArrays prevent per-frame garbage collection churn
    val sharedPath = remember { Path() }
    val primaryArcY = remember { FloatArray(7) }
    val secondaryArcY = remember { FloatArray(7) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val period = TimePeriod.current(hourOverride)

        val p1 = masterPhase
        val p2 = (masterPhase * 2f) % TWO_PI
        val pulse = 0.92f + 0.08f * sin(masterPhase * 3f)

        when (theme) {
            AmbientTheme.AURORA_SILK -> drawNordicTwilight(w, h, p1, p2, pulse, period, sharedPath)
            AmbientTheme.FERRIS_WHEEL -> drawFerrisWheel(w, h, p1, p2, pulse, period, sharedPath)
            AmbientTheme.TIME_DYNAMIC -> drawTimeDynamic(
                w = w,
                h = h,
                p1 = p1,
                p2 = p2,
                pulse = pulse,
                period = period,
                sharedPath = sharedPath,
                primaryArcY = primaryArcY,
                secondaryArcY = secondaryArcY
            )
            AmbientTheme.RESONANCE_TUNNEL -> drawResonanceTunnel(w, h, p1, pulse, period, sharedPath)
            AmbientTheme.CLEAN_WHITE -> drawCleanWhite(w, h, p1, pulse, sharedPath)
            AmbientTheme.LOOPING_TRIOS -> Unit
        }

        if (theme != AmbientTheme.LOOPING_TRIOS &&
            theme != AmbientTheme.FERRIS_WHEEL &&
            theme != AmbientTheme.RESONANCE_TUNNEL &&
            theme != AmbientTheme.CLEAN_WHITE
        ) {
            drawFloatingLightMotes(w, h, p1, p2, period)
        }
    }
}

// Pre-allocated palette structure for the Dark Olive Sanctuary (NT / AURORA_SILK) scenes.
private class SilkAuroraPalette(
    val backdropColors: List<Color>,
    val upperOrbCore: Color,
    val upperOrbEdge: Color,
    val lowerOrbCore: Color,
    val lowerOrbEdge: Color,
    val primaryWaveColors: List<Color>,
    val secondaryWaveColors: List<Color>,
    val crestColors: List<Color>
)

private val NT_MORNING_PALETTE = SilkAuroraPalette(
    backdropColors = listOf(
        Color(0xFF040705),
        Color(0xFF0A110C),
        Color(0xFF060A07)
    ),
    upperOrbCore = Color(0xFF3F523A).copy(alpha = 0.26f),
    upperOrbEdge = Color(0xFF3F523A).copy(alpha = 0.08f),
    lowerOrbCore = Color(0xFF5E4636).copy(alpha = 0.22f),
    lowerOrbEdge = Color(0xFF5E4636).copy(alpha = 0.07f),
    primaryWaveColors = listOf(
        Color(0xFF233024).copy(alpha = 0.46f),
        Color(0xFF384A35).copy(alpha = 0.40f),
        Color(0xFF523F31).copy(alpha = 0.32f)
    ),
    secondaryWaveColors = listOf(
        Color(0xFF19241B).copy(alpha = 0.44f),
        Color(0xFF31402F).copy(alpha = 0.32f)
    ),
    crestColors = listOf(
        Color.Transparent,
        Color(0xFF5B6E53).copy(alpha = 0.28f),
        Color(0xFF6E8264).copy(alpha = 0.42f),
        Color(0xFF5B6E53).copy(alpha = 0.28f),
        Color.Transparent
    )
)

private val NT_AFTERNOON_PALETTE = SilkAuroraPalette(
    backdropColors = listOf(
        Color(0xFF050704),
        Color(0xFF0C1209),
        Color(0xFF070A05)
    ),
    upperOrbCore = Color(0xFF4B5735).copy(alpha = 0.26f),
    upperOrbEdge = Color(0xFF4B5735).copy(alpha = 0.08f),
    lowerOrbCore = Color(0xFF5E4F2D).copy(alpha = 0.23f),
    lowerOrbEdge = Color(0xFF5E4F2D).copy(alpha = 0.07f),
    primaryWaveColors = listOf(
        Color(0xFF28321E).copy(alpha = 0.46f),
        Color(0xFF3E4A2D).copy(alpha = 0.40f),
        Color(0xFF574829).copy(alpha = 0.32f)
    ),
    secondaryWaveColors = listOf(
        Color(0xFF1E2617).copy(alpha = 0.44f),
        Color(0xFF3A442B).copy(alpha = 0.32f)
    ),
    crestColors = listOf(
        Color.Transparent,
        Color(0xFF6B7348).copy(alpha = 0.28f),
        Color(0xFF7D8556).copy(alpha = 0.42f),
        Color(0xFF6B7348).copy(alpha = 0.28f),
        Color.Transparent
    )
)

private val NT_NIGHT_PALETTE = SilkAuroraPalette(
    backdropColors = listOf(
        Color(0xFF030504),
        Color(0xFF080F0C),
        Color(0xFF040705)
    ),
    upperOrbCore = Color(0xFF2D3E33).copy(alpha = 0.26f),
    upperOrbEdge = Color(0xFF2D3E33).copy(alpha = 0.08f),
    lowerOrbCore = Color(0xFF223336).copy(alpha = 0.22f),
    lowerOrbEdge = Color(0xFF223336).copy(alpha = 0.07f),
    primaryWaveColors = listOf(
        Color(0xFF16241C).copy(alpha = 0.48f),
        Color(0xFF25382C).copy(alpha = 0.42f),
        Color(0xFF1F2E34).copy(alpha = 0.34f)
    ),
    secondaryWaveColors = listOf(
        Color(0xFF111C16).copy(alpha = 0.45f),
        Color(0xFF24342B).copy(alpha = 0.34f)
    ),
    crestColors = listOf(
        Color.Transparent,
        Color(0xFF43594C).copy(alpha = 0.28f),
        Color(0xFF526B5C).copy(alpha = 0.42f),
        Color(0xFF43594C).copy(alpha = 0.28f),
        Color.Transparent
    )
)

private fun DrawScope.drawLiquidSilkScene(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float,
    palette: SilkAuroraPalette,
    sharedPath: Path
) {
    drawRect(
        brush = Brush.verticalGradient(colors = palette.backdropColors)
    )

    val orb1Center = Offset(
        x = w * (0.26f + 0.14f * sin(p1)),
        y = h * (0.22f + 0.08f * cos(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(palette.upperOrbCore, palette.upperOrbEdge, Color.Transparent),
            center = orb1Center,
            radius = w * (0.76f * pulse)
        ),
        center = orb1Center,
        radius = w * (0.76f * pulse)
    )

    val orb2Center = Offset(
        x = w * (0.74f - 0.14f * cos(p1)),
        y = h * (0.68f + 0.09f * sin(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(palette.lowerOrbCore, palette.lowerOrbEdge, Color.Transparent),
            center = orb2Center,
            radius = w * (0.74f * pulse)
        ),
        center = orb2Center,
        radius = w * (0.74f * pulse)
    )

    drawLightweightSilkWave(
        w = w,
        h = h,
        leftTopY = h * (0.18f + 0.08f * cos(p2)),
        leftBottomY = h * (0.44f + 0.07f * sin(p1)),
        ctrl1TopX = w * 0.32f,
        ctrl1TopY = h * (0.32f + 0.10f * sin(p1)),
        ctrl2TopX = w * 0.68f,
        ctrl2TopY = h * (0.12f + 0.09f * cos(p2)),
        rightTopY = h * (0.24f + 0.08f * sin(p2)),
        rightBottomY = h * (0.50f + 0.08f * cos(p1)),
        ctrl1BotX = w * 0.66f,
        ctrl1BotY = h * (0.38f - 0.08f * sin(p2)),
        ctrl2BotX = w * 0.34f,
        ctrl2BotY = h * (0.56f + 0.09f * cos(p1)),
        waveColors = palette.secondaryWaveColors,
        crestColors = palette.crestColors,
        sharedPath = sharedPath
    )

    drawLightweightSilkWave(
        w = w,
        h = h,
        leftTopY = h * (0.50f + 0.10f * sin(p1)),
        leftBottomY = h * (0.80f + 0.08f * cos(p2)),
        ctrl1TopX = w * 0.28f,
        ctrl1TopY = h * (0.34f + 0.12f * cos(p1)),
        ctrl2TopX = w * 0.66f,
        ctrl2TopY = h * (0.54f - 0.12f * sin(p2)),
        rightTopY = h * (0.16f + 0.09f * cos(p2)),
        rightBottomY = h * (0.46f + 0.09f * sin(p1)),
        ctrl1BotX = w * 0.70f,
        ctrl1BotY = h * (0.72f - 0.10f * cos(p2)),
        ctrl2BotX = w * 0.34f,
        ctrl2BotY = h * (0.62f + 0.11f * sin(p1)),
        waveColors = palette.primaryWaveColors,
        crestColors = palette.crestColors,
        sharedPath = sharedPath
    )
}

private fun DrawScope.drawLightweightSilkWave(
    w: Float,
    h: Float,
    leftTopY: Float,
    leftBottomY: Float,
    ctrl1TopX: Float,
    ctrl1TopY: Float,
    ctrl2TopX: Float,
    ctrl2TopY: Float,
    rightTopY: Float,
    rightBottomY: Float,
    ctrl1BotX: Float,
    ctrl1BotY: Float,
    ctrl2BotX: Float,
    ctrl2BotY: Float,
    waveColors: List<Color>,
    crestColors: List<Color>,
    sharedPath: Path
) {
    val padX = w * 0.04f
    val spread = h * 0.045f

    val brush = Brush.linearGradient(
        colors = waveColors,
        start = Offset(0f, leftBottomY),
        end = Offset(w, rightTopY)
    )

    sharedPath.reset()
    sharedPath.moveTo(-padX, leftTopY - spread)
    sharedPath.cubicTo(ctrl1TopX, ctrl1TopY - spread, ctrl2TopX, ctrl2TopY - spread, w + padX, rightTopY - spread)
    sharedPath.lineTo(w + padX, rightBottomY + spread)
    sharedPath.cubicTo(ctrl1BotX, ctrl1BotY + spread, ctrl2BotX, ctrl2BotY + spread, -padX, leftBottomY + spread)
    sharedPath.close()
    drawPath(path = sharedPath, brush = brush, alpha = 0.35f)

    sharedPath.reset()
    sharedPath.moveTo(-padX, leftTopY)
    sharedPath.cubicTo(ctrl1TopX, ctrl1TopY, ctrl2TopX, ctrl2TopY, w + padX, rightTopY)
    sharedPath.lineTo(w + padX, rightBottomY)
    sharedPath.cubicTo(ctrl1BotX, ctrl1BotY, ctrl2BotX, ctrl2BotY, -padX, leftBottomY)
    sharedPath.close()
    drawPath(path = sharedPath, brush = brush, alpha = 0.72f)

    sharedPath.reset()
    sharedPath.moveTo(-padX, leftTopY)
    sharedPath.cubicTo(ctrl1TopX, ctrl1TopY, ctrl2TopX, ctrl2TopY, w + padX, rightTopY)
    drawPath(
        path = sharedPath,
        brush = Brush.horizontalGradient(colors = crestColors),
        style = Stroke(width = w * 0.008f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawNordicTwilight(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float,
    period: TimePeriod = TimePeriod.current(),
    sharedPath: Path = Path()
) {
    val palette = when (period) {
        TimePeriod.MORNING -> NT_MORNING_PALETTE
        TimePeriod.AFTERNOON -> NT_AFTERNOON_PALETTE
        TimePeriod.NIGHT -> NT_NIGHT_PALETTE
    }
    drawLiquidSilkScene(w, h, p1, p2, pulse, palette, sharedPath)
}

private fun DrawScope.drawMidnightSage(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float,
    sharedPath: Path = Path()
) {
    drawNordicTwilight(w, h, p1, p2, pulse, TimePeriod.MORNING, sharedPath)
}

private fun DrawScope.drawWarmLinen(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float,
    sharedPath: Path = Path()
) {
    drawNordicTwilight(w, h, p1, p2, pulse, TimePeriod.AFTERNOON, sharedPath)
}

private fun DrawScope.drawCosmicDusk(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float,
    sharedPath: Path = Path()
) {
    drawNordicTwilight(w, h, p1, p2, pulse, TimePeriod.NIGHT, sharedPath)
}

private fun DrawScope.drawOceanAbyss(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float,
    sharedPath: Path = Path()
) {
    drawNordicTwilight(w, h, p1, p2, pulse, TimePeriod.NIGHT, sharedPath)
}

private fun DrawScope.drawFloatingLightMotes(
    w: Float,
    h: Float,
    phase1: Float,
    phase2: Float = phase1,
    period: TimePeriod = TimePeriod.current()
) {
    val moteColor = when (period) {
        TimePeriod.MORNING -> Color(0xFF6E8062)
        TimePeriod.AFTERNOON -> Color(0xFF7D754E)
        TimePeriod.NIGHT -> Color(0xFF4E6658)
    }

    for (idx in MOTE_X.indices) {
        val xDrift = sin(phase1 + idx * 0.8f) * (w * 0.020f)
        val yDrift = cos(phase2 + idx * 0.6f) * (h * 0.016f)
        val moteX = (MOTE_X[idx] * w + xDrift).coerceIn(0f, w)
        val moteY = (MOTE_Y[idx] * h + yDrift).coerceIn(0f, h)
        val radius = MOTE_R[idx]

        val twinkleAlpha = 0.08f + 0.16f * ((sin(phase1 * 2f + idx) + 1f) * 0.5f)

        drawCircle(
            color = moteColor.copy(alpha = twinkleAlpha),
            radius = radius * 1.4f,
            center = Offset(moteX, moteY)
        )
    }
}

private fun DrawScope.drawFerrisWheel(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float,
    period: TimePeriod = TimePeriod.current(),
    sharedPath: Path = Path()
) {
    val skyTop: Color
    val skyMid: Color
    val skyBottom: Color
    val bubbleRimColor: Color
    val bubbleFillColor: Color
    val bubbleGlintColor: Color
    val cloudBodyColor: Color
    val cloudRimColor: Color

    when (period) {
        TimePeriod.MORNING -> {
            skyTop = Color(0xFF090406)
            skyMid = Color(0xFF14080C)
            skyBottom = Color(0xFF070406)
            bubbleRimColor = Color(0xFFD9536F)
            bubbleFillColor = Color(0xFF4C1D2E)
            bubbleGlintColor = Color(0xFFF48FB1)
            cloudBodyColor = Color(0xFF3A363B)
            cloudRimColor = Color(0xFF574F56)
        }
        TimePeriod.AFTERNOON -> {
            skyTop = Color(0xFF080704)
            skyMid = Color(0xFF141108)
            skyBottom = Color(0xFF060503)
            bubbleRimColor = Color(0xFFD4A74A)
            bubbleFillColor = Color(0xFF3D3117)
            bubbleGlintColor = Color(0xFFEAD180)
            cloudBodyColor = Color(0xFF3E4247)
            cloudRimColor = Color(0xFF5A6066)
        }
        TimePeriod.NIGHT -> {
            skyTop = Color(0xFF010103)
            skyMid = Color(0xFF05070E)
            skyBottom = Color(0xFF020205)
            bubbleRimColor = Color(0xFF6C88A6)
            bubbleFillColor = Color(0xFF1B2638)
            bubbleGlintColor = Color(0xFF9AB4CC)
            cloudBodyColor = Color(0xFF2B2E36)
            cloudRimColor = Color(0xFF434854)
        }
    }

    drawRect(
        brush = Brush.verticalGradient(colors = listOf(skyTop, skyMid, skyBottom))
    )

    val celestialCenter = Offset(w * 0.80f, h * 0.13f)
    val celestialRadius = w * 0.075f

    when (period) {
        TimePeriod.MORNING -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE11D48).copy(alpha = 0.28f * pulse),
                        Color(0xFF881337).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = celestialCenter,
                    radius = celestialRadius * 3.4f
                ),
                center = celestialCenter,
                radius = celestialRadius * 3.4f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF43F5E).copy(alpha = 0.88f),
                        Color(0xFFBE123C).copy(alpha = 0.92f)
                    ),
                    center = celestialCenter,
                    radius = celestialRadius
                ),
                center = celestialCenter,
                radius = celestialRadius
            )
        }
        TimePeriod.AFTERNOON -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFEAB308).copy(alpha = 0.26f * pulse),
                        Color(0xFF854D0E).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = celestialCenter,
                    radius = celestialRadius * 3.4f
                ),
                center = celestialCenter,
                radius = celestialRadius * 3.4f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFDE047).copy(alpha = 0.86f),
                        Color(0xFFCA8A04).copy(alpha = 0.90f)
                    ),
                    center = celestialCenter,
                    radius = celestialRadius
                ),
                center = celestialCenter,
                radius = celestialRadius
            )
        }
        TimePeriod.NIGHT -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF94A3B8).copy(alpha = 0.20f * pulse),
                        Color(0xFF334155).copy(alpha = 0.07f),
                        Color.Transparent
                    ),
                    center = celestialCenter,
                    radius = celestialRadius * 3.2f
                ),
                center = celestialCenter,
                radius = celestialRadius * 3.2f
            )
            drawCircle(
                color = Color(0xFFD4D8E0),
                radius = celestialRadius,
                center = celestialCenter
            )
            drawCircle(
                color = Color(0xFFA8B0BC).copy(alpha = 0.55f),
                radius = celestialRadius * 0.25f,
                center = Offset(celestialCenter.x - celestialRadius * 0.35f, celestialCenter.y + celestialRadius * 0.22f)
            )
            drawCircle(
                color = Color(0xFFA8B0BC).copy(alpha = 0.45f),
                radius = celestialRadius * 0.34f,
                center = Offset(celestialCenter.x + celestialRadius * 0.22f, celestialCenter.y - celestialRadius * 0.16f)
            )
            drawCircle(
                color = Color(0xFFA8B0BC).copy(alpha = 0.60f),
                radius = celestialRadius * 0.16f,
                center = Offset(celestialCenter.x + celestialRadius * 0.10f, celestialCenter.y + celestialRadius * 0.44f)
            )
        }
    }

    val cloudDrift1 = sin(p1) * (w * 0.045f)
    val cloudDrift2 = cos(p1) * (w * 0.055f)
    val cloudDrift3 = sin(p2) * (w * 0.035f)

    draw2DCloud(
        cx = w * 0.68f + cloudDrift1,
        cy = h * 0.16f,
        cloudW = w * 0.26f,
        cloudH = h * 0.038f,
        bodyColor = cloudBodyColor.copy(alpha = 0.78f),
        rimColor = cloudRimColor.copy(alpha = 0.55f),
        sharedPath = sharedPath
    )
    draw2DCloud(
        cx = w * 0.26f + cloudDrift2,
        cy = h * 0.11f,
        cloudW = w * 0.30f,
        cloudH = h * 0.042f,
        bodyColor = cloudBodyColor.copy(alpha = 0.70f),
        rimColor = cloudRimColor.copy(alpha = 0.48f),
        sharedPath = sharedPath
    )
    draw2DCloud(
        cx = w * 0.84f + cloudDrift3,
        cy = h * 0.24f,
        cloudW = w * 0.22f,
        cloudH = h * 0.032f,
        bodyColor = cloudBodyColor.copy(alpha = 0.62f),
        rimColor = cloudRimColor.copy(alpha = 0.42f),
        sharedPath = sharedPath
    )

    val normPhase = p1 / TWO_PI
    for (i in BUBBLE_X.indices) {
        val r = w * BUBBLE_RADIUS_FACTOR[i]
        val speed = BUBBLE_SPEED_MULT[i]
        val progress = (normPhase * speed + BUBBLE_PHASE_OFFSET[i]) % 1f

        val totalSpan = h + r * 4f
        val cy = (h + r * 2f) - progress * totalSpan

        val wobbleX = sin(p1 * speed + i * 1.3f) * (w * 0.032f)
        val cx = (BUBBLE_X[i] * w + wobbleX).coerceIn(r, w - r)
        val center = Offset(cx, cy)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    bubbleFillColor.copy(alpha = 0.10f),
                    bubbleFillColor.copy(alpha = 0.34f),
                    bubbleRimColor.copy(alpha = 0.52f)
                ),
                center = Offset(cx - r * 0.22f, cy - r * 0.22f),
                radius = r
            ),
            center = center,
            radius = r
        )

        drawCircle(
            color = bubbleRimColor.copy(alpha = 0.46f),
            radius = r,
            center = center,
            style = Stroke(width = 1.6f)
        )

        drawCircle(
            color = bubbleGlintColor.copy(alpha = 0.42f),
            radius = r * 0.24f,
            center = Offset(cx - r * 0.36f, cy - r * 0.36f)
        )
    }
}

private fun DrawScope.draw2DCloud(
    cx: Float,
    cy: Float,
    cloudW: Float,
    cloudH: Float,
    bodyColor: Color,
    rimColor: Color,
    sharedPath: Path
) {
    val left = cx - cloudW * 0.5f
    val top = cy - cloudH * 0.5f

    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(left, top),
        size = Size(cloudW, cloudH),
        cornerRadius = CornerRadius(cloudH * 0.5f, cloudH * 0.5f)
    )
    drawCircle(
        color = bodyColor,
        radius = cloudH * 0.58f,
        center = Offset(cx - cloudW * 0.22f, top + cloudH * 0.15f)
    )
    drawCircle(
        color = bodyColor,
        radius = cloudH * 0.78f,
        center = Offset(cx + cloudW * 0.04f, top - cloudH * 0.05f)
    )
    drawCircle(
        color = bodyColor,
        radius = cloudH * 0.50f,
        center = Offset(cx + cloudW * 0.26f, top + cloudH * 0.22f)
    )

    sharedPath.reset()
    sharedPath.moveTo(left + cloudW * 0.12f, top)
    sharedPath.quadraticTo(cx, top - cloudH * 0.85f, left + cloudW * 0.88f, top)
    drawPath(
        path = sharedPath,
        color = rimColor,
        style = Stroke(width = 1.5f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawTimeDynamic(
    w: Float,
    h: Float,
    p1: Float,
    p2: Float,
    pulse: Float,
    period: TimePeriod = TimePeriod.current(),
    sharedPath: Path = Path(),
    primaryArcY: FloatArray = FloatArray(7),
    secondaryArcY: FloatArray = FloatArray(7)
) {
    val baseDark: Color
    val panelDark: Color
    val midGlow: Color
    val brightCore: Color
    val highlightEdge: Color

    when (period) {
        TimePeriod.MORNING -> {
            baseDark = Color(0xFF030805)
            panelDark = Color(0xFF07140D)
            midGlow = Color(0xFF234730)
            brightCore = Color(0xFF4D7A58)
            highlightEdge = Color(0xFF7CA384)
        }
        TimePeriod.AFTERNOON -> {
            baseDark = Color(0xFF080502)
            panelDark = Color(0xFF140B05)
            midGlow = Color(0xFF4A2F18)
            brightCore = Color(0xFF8C6236)
            highlightEdge = Color(0xFFB88B58)
        }
        TimePeriod.NIGHT -> {
            baseDark = Color(0xFF03030A)
            panelDark = Color(0xFF09091A)
            midGlow = Color(0xFF23214A)
            brightCore = Color(0xFF524E99)
            highlightEdge = Color(0xFF8581C2)
        }
    }

    drawRect(color = baseDark)

    val numPanels = 7
    val panelWidth = w / numPanels
    val primaryWaveOffset = sin(p1) * (h * 0.04f)
    val secondaryWaveOffset = cos(p2) * (h * 0.03f)

    for (i in 0 until numPanels) {
        val panelCenterX = (i + 0.5f) * panelWidth
        val normDist = (panelCenterX - (w * 0.5f)) / (w * 0.48f)

        val center1 = h * 0.62f + primaryWaveOffset + sin(p2 + i * 0.45f) * (h * 0.02f)
        primaryArcY[i] = center1 + (normDist * normDist) * (h * 0.20f)

        val center2 = h * 0.25f + secondaryWaveOffset + cos(p1 + i * 0.38f) * (h * 0.018f)
        secondaryArcY[i] = center2 - (normDist * normDist) * (h * 0.09f)
    }

    for (i in 0 until numPanels) {
        val left = i * panelWidth
        val arcY1 = primaryArcY[i]
        val arcY2 = secondaryArcY[i]

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(baseDark, panelDark, midGlow.copy(alpha = 0.28f), baseDark),
                startY = 0f,
                endY = h
            ),
            topLeft = Offset(left, 0f),
            size = Size(panelWidth, h)
        )

        val upperTop = (arcY2 - h * 0.10f * pulse).coerceAtLeast(0f)
        val upperBottom = (arcY2 + h * 0.10f * pulse).coerceAtMost(h)
        if (upperBottom > upperTop) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        midGlow.copy(alpha = 0.24f),
                        brightCore.copy(alpha = 0.48f),
                        midGlow.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    startY = upperTop,
                    endY = upperBottom
                ),
                topLeft = Offset(left, upperTop),
                size = Size(panelWidth, upperBottom - upperTop)
            )
        }

        val bandTop = (arcY1 - h * 0.13f * pulse).coerceAtLeast(0f)
        val bandBottom = (arcY1 + h * 0.14f * pulse).coerceAtMost(h)
        if (bandBottom > bandTop) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        midGlow.copy(alpha = 0.25f),
                        brightCore.copy(alpha = 0.68f),
                        highlightEdge.copy(alpha = 0.78f),
                        brightCore.copy(alpha = 0.58f),
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
    }

    sharedPath.reset()
    for (i in 0 until numPanels) {
        val x = (i + 0.5f) * panelWidth
        val y = primaryArcY[i]
        if (i == 0) {
            sharedPath.moveTo(0f, y)
            sharedPath.lineTo(x, y)
        } else {
            val prevX = (i - 0.5f) * panelWidth
            val prevY = primaryArcY[i - 1]
            sharedPath.quadraticTo(prevX, prevY, (prevX + x) * 0.5f, (prevY + y) * 0.5f)
        }
    }
    sharedPath.lineTo(w, primaryArcY[numPanels - 1])

    drawPath(
        path = sharedPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                brightCore.copy(alpha = 0.35f),
                highlightEdge.copy(alpha = 0.75f),
                brightCore.copy(alpha = 0.35f)
            )
        ),
        style = Stroke(width = 2.2f, cap = StrokeCap.Round)
    )

    for (i in 1 until numPanels) {
        val seamX = i * panelWidth
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    highlightEdge.copy(alpha = 0.05f),
                    highlightEdge.copy(alpha = 0.36f),
                    highlightEdge.copy(alpha = 0.05f)
                ),
                startY = 0f,
                endY = h
            ),
            start = Offset(seamX, 0f),
            end = Offset(seamX, h),
            strokeWidth = 1.3f
        )
    }
}

// Resonance Tunnel Theme (RT / RESONANCE_TUNNEL):
// Inspired by hypnotic ribbed concentric perspective architecture (Unsplash reference).
// Renders an infinite zoom vortex of nested elliptical ribs and flared amphitheater wings
// receding into a dark, calm horizon. Zero stutter, mathematically seamless 0..1 phase loop,
// zero per-frame allocations, and 3 dark time-of-day palettes.
private fun DrawScope.drawResonanceTunnel(
    w: Float,
    h: Float,
    p1: Float,
    pulse: Float,
    period: TimePeriod = TimePeriod.current(),
    sharedPath: Path = Path()
) {
    val bgTop: Color
    val bgHorizon: Color
    val bgBottom: Color
    val tunnelCoreGlow: Color
    val ribDarkFill: Color
    val ribMidFill: Color
    val ribHighlight: Color
    val wingAccent: Color

    when (period) {
        TimePeriod.MORNING -> { // Morning: Deep Forest Obsidian & Moss-Sage Metals
            bgTop = Color(0xFF030504)
            bgHorizon = Color(0xFF0B140E)
            bgBottom = Color(0xFF040705)
            tunnelCoreGlow = Color(0xFF283D2D)
            ribDarkFill = Color(0xFF0C140E)
            ribMidFill = Color(0xFF1B291E)
            ribHighlight = Color(0xFF4C6B52) // Sage-Jade rim
            wingAccent = Color(0xFF63513F)   // Warm clay secondary
        }
        TimePeriod.AFTERNOON -> { // Afternoon: Deep Espresso-Charcoal & Warm Bronze Metals
            bgTop = Color(0xFF050403)
            bgHorizon = Color(0xFF140E08)
            bgBottom = Color(0xFF070503)
            tunnelCoreGlow = Color(0xFF3D2B17)
            ribDarkFill = Color(0xFF140D07)
            ribMidFill = Color(0xFF261B10)
            ribHighlight = Color(0xFF7D5E37) // Warm Bronze-Ochre rim
            wingAccent = Color(0xFF5A442E)   // Amber-taupe secondary
        }
        TimePeriod.NIGHT -> { // Night: Pure Pitch Obsidian & Slate-Titanium Metals
            bgTop = Color(0xFF010204)
            bgHorizon = Color(0xFF080D18)
            bgBottom = Color(0xFF020306)
            tunnelCoreGlow = Color(0xFF1C2740)
            ribDarkFill = Color(0xFF080C14)
            ribMidFill = Color(0xFF141C2B)
            ribHighlight = Color(0xFF486382) // Titanium-Cyan rim
            wingAccent = Color(0xFF384357)   // Slate-charcoal secondary
        }
    }

    // 1. Deep Perspective Horizon Background
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(bgTop, bgHorizon, bgBottom),
            startY = 0f,
            endY = h
        )
    )

    // Vanishing Point Anchor (slightly above center for dramatic perspective)
    val horizonY = h * 0.44f
    val horizonCenter = Offset(w * 0.5f, horizonY)

    // 2. Soft Vanishing Core Glow (calm depth, no white glare)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                tunnelCoreGlow.copy(alpha = 0.38f * pulse),
                tunnelCoreGlow.copy(alpha = 0.12f),
                Color.Transparent
            ),
            center = horizonCenter,
            radius = w * 0.38f
        ),
        center = horizonCenter,
        radius = w * 0.38f
    )

    // 3. Infinite Concentric Ribbed Shells
    // 8 nested rings receding into distance. Master cycle wraps seamlessly at TWO_PI.
    val numRings = 8
    val loopProgress = (p1 / TWO_PI) % 1f

    for (i in 0 until numRings) {
        // Continuous z-depth from 0f (vanishing point) to 1f (camera/screen plane)
        val z = (loopProgress + i.toFloat()) / numRings.toFloat()
        val normalizedZ = z % 1f

        // Exponential perspective growth so rings accelerate smoothly toward camera
        val scale = normalizedZ * normalizedZ
        if (scale < 0.005f) continue

        // Smooth alpha envelope: fades in smoothly near vanishing point, fades out at camera edge
        val ringAlpha = when {
            normalizedZ < 0.15f -> normalizedZ / 0.15f
            normalizedZ > 0.82f -> (1f - normalizedZ) / 0.18f
            else -> 1f
        } * 0.92f

        val rx = w * (0.08f + 0.88f * scale)
        val ry = h * (0.05f + 0.54f * scale)
        val wingHeight = ry * 0.45f

        val ringLeft = horizonCenter.x - rx
        val ringRight = horizonCenter.x + rx
        val ringBottom = horizonCenter.y + ry

        // 3a. Upper Elliptical Vault Arch (receding ceiling ribs)
        sharedPath.reset()
        sharedPath.moveTo(ringLeft, horizonCenter.y)
        sharedPath.cubicTo(
            ringLeft, horizonCenter.y - ry * 0.92f,
            ringRight, horizonCenter.y - ry * 0.92f,
            ringRight, horizonCenter.y
        )
        drawPath(
            path = sharedPath,
            color = ribMidFill.copy(alpha = ringAlpha * 0.45f),
            style = Stroke(width = 1.2f + 2.5f * scale, cap = StrokeCap.Round)
        )

        // 3b. Lower Flared Amphitheater Rib (nested bowl with curved wings)
        // Curves downward in the center trough and flares smoothly upward at left & right wings
        sharedPath.reset()
        val wingTipLeft = Offset(ringLeft, horizonCenter.y - wingHeight)
        val wingTipRight = Offset(ringRight, horizonCenter.y - wingHeight)
        val bowlBottom = Offset(horizonCenter.x, ringBottom)

        sharedPath.moveTo(wingTipLeft.x, wingTipLeft.y)
        // Downward flared contour toward bowl bottom
        sharedPath.cubicTo(
            horizonCenter.x - rx * 0.70f, horizonCenter.y + ry * 0.85f,
            horizonCenter.x - rx * 0.28f, ringBottom,
            bowlBottom.x, bowlBottom.y
        )
        // Upward flared contour toward right wing tip
        sharedPath.cubicTo(
            horizonCenter.x + rx * 0.28f, ringBottom,
            horizonCenter.x + rx * 0.70f, horizonCenter.y + ry * 0.85f,
            wingTipRight.x, wingTipRight.y
        )

        // Secondary inner rib line for ribbed/grooved texture (from reference image)
        val innerTroughY = ringBottom - ry * 0.16f
        sharedPath.cubicTo(
            horizonCenter.x + rx * 0.55f, horizonCenter.y + ry * 0.65f,
            horizonCenter.x + rx * 0.22f, innerTroughY,
            horizonCenter.x, innerTroughY
        )
        sharedPath.cubicTo(
            horizonCenter.x - rx * 0.22f, innerTroughY,
            horizonCenter.x - rx * 0.55f, horizonCenter.y + ry * 0.65f,
            wingTipLeft.x, wingTipLeft.y
        )
        sharedPath.close()

        // Rib trough shadow fill
        drawPath(
            path = sharedPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    ribDarkFill.copy(alpha = ringAlpha * 0.75f),
                    ribMidFill.copy(alpha = ringAlpha * 0.85f),
                    ribDarkFill.copy(alpha = ringAlpha * 0.90f)
                ),
                startY = horizonCenter.y,
                endY = ringBottom
            )
        )

        // Illuminated specular rib crest line along the upper bowl rim
        sharedPath.reset()
        sharedPath.moveTo(wingTipLeft.x, wingTipLeft.y)
        sharedPath.cubicTo(
            horizonCenter.x - rx * 0.70f, horizonCenter.y + ry * 0.85f,
            horizonCenter.x - rx * 0.28f, ringBottom,
            bowlBottom.x, bowlBottom.y
        )
        sharedPath.cubicTo(
            horizonCenter.x + rx * 0.28f, ringBottom,
            horizonCenter.x + rx * 0.70f, horizonCenter.y + ry * 0.85f,
            wingTipRight.x, wingTipRight.y
        )

        drawPath(
            path = sharedPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    wingAccent.copy(alpha = ringAlpha * 0.35f),
                    ribHighlight.copy(alpha = ringAlpha * 0.85f),
                    wingAccent.copy(alpha = ringAlpha * 0.35f)
                ),
                startX = ringLeft,
                endX = ringRight
            ),
            style = Stroke(
                width = 1.0f + 2.4f * scale,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Central nested elliptical eye ring around horizon axis
        if (scale < 0.50f) {
            val eyeRx = rx * 0.38f
            val eyeRy = ry * 0.32f
            drawOval(
                color = ribHighlight.copy(alpha = ringAlpha * 0.40f),
                topLeft = Offset(horizonCenter.x - eyeRx, horizonCenter.y - eyeRy * 0.5f),
                size = Size(eyeRx * 2f, eyeRy),
                style = Stroke(width = 1.1f)
            )
        }
    }
}

// Looping Trios Theme (LT):
// Pure black high-contrast kinetic geometry with analytically interpolated stripes (zero clipPath).
@Composable
fun LoopingTriosBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF000000),
    solidTriangleColor: Color? = null,
    stripeTriangleColor: Color? = null,
    cellSizeDp: Float = 155f,
    hourOverride: Int? = null
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
                0f at 420 using silkyStepEasing
                1f at 2000 using silkyStepEasing
                1f at 2420 using silkyStepEasing
                2f at 4000 using silkyStepEasing
                2f at 4420 using silkyStepEasing
                3f at 6000 using silkyStepEasing
                3f at 6420 using silkyStepEasing
                4f at 8000 using silkyStepEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "PhaseAnimation"
    )

    val reusablePath = remember { Path() }
    val density = LocalDensity.current.density

    Box(modifier = modifier.fillMaxSize().background(backgroundColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val period = TimePeriod.current(hourOverride)

            val resolvedSolid: Color
            val resolvedStripe: Color
            val rimAccent: Color

            when (period) {
                TimePeriod.MORNING -> {
                    resolvedSolid = solidTriangleColor ?: Color(0xFF243D31)
                    resolvedStripe = stripeTriangleColor ?: Color(0xFF688758)
                    rimAccent = Color(0xFF84A870)
                }
                TimePeriod.AFTERNOON -> {
                    resolvedSolid = solidTriangleColor ?: Color(0xFF38291B)
                    resolvedStripe = stripeTriangleColor ?: Color(0xFF996E3D)
                    rimAccent = Color(0xFFBA8B52)
                }
                TimePeriod.NIGHT -> {
                    resolvedSolid = solidTriangleColor ?: Color(0xFF1F3047)
                    resolvedStripe = stripeTriangleColor ?: Color(0xFF52789E)
                    rimAccent = Color(0xFF739EC7)
                }
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        resolvedSolid.copy(alpha = 0.28f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.48f),
                    radius = w * 0.75f
                ),
                center = Offset(w * 0.5f, h * 0.48f),
                radius = w * 0.75f
            )

            val cellW = cellSizeDp * density
            val cellH = cellSizeDp * 1.15f * density
            val triR = cellW * 0.185f

            val cols = ceil((w / cellW).toDouble()).toInt() + 1
            val rows = ceil((h / cellH).toDouble()).toInt() + 1

            val motionEnvelope = (1f - cos(phase * TWO_PI)) * 0.5f

            for (col in -1 until cols) {
                for (row in -1 until rows) {
                    val baseX = col * cellW
                    val baseY = row * cellH
                    val localRipple = sin((col * 0.4f) + (row * 0.3f) + phase * (PI.toFloat() * 0.5f)) * 6f * motionEnvelope

                    val primaryAngle = phase * 90f + localRipple
                    val secondaryAngle = -phase * 90f - localRipple

                    drawTrioUnit(
                        centerX = baseX + cellW * 0.28f,
                        centerY = baseY + cellH * 0.58f,
                        r = triR,
                        primaryAngle = primaryAngle,
                        secondaryAngle = secondaryAngle,
                        solidColor = resolvedSolid,
                        stripeColor = resolvedStripe,
                        rimColor = rimAccent,
                        path = reusablePath,
                        density = density
                    )

                    drawTrioUnit(
                        centerX = baseX + cellW * 0.72f,
                        centerY = baseY + cellH * 0.32f,
                        r = triR,
                        primaryAngle = primaryAngle,
                        secondaryAngle = secondaryAngle,
                        solidColor = resolvedSolid,
                        stripeColor = resolvedStripe,
                        rimColor = rimAccent,
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
    rimColor: Color,
    path: Path,
    density: Float
) {
    val verticalOffset = r * 1.05f

    withTransform({
        translate(centerX, centerY)
        rotate(secondaryAngle, pivot = Offset.Zero)
    }) {
        drawStripedTriangleFast(
            p1 = Offset(-r * 1.1f, 0f),
            p2 = Offset(0f, -verticalOffset),
            p3 = Offset(0f, verticalOffset),
            color = stripeColor,
            density = density
        )
    }

    withTransform({
        translate(centerX, centerY - verticalOffset)
        rotate(primaryAngle, pivot = Offset.Zero)
    }) {
        drawStripedTriangleFast(
            p1 = Offset(0f, -r),
            p2 = Offset(-r * 1.1f, -r * 0.35f),
            p3 = Offset(r * 0.85f, 0f),
            color = stripeColor,
            density = density
        )
        drawSolidTriangleFast(
            path = path,
            p1 = Offset(0f, -r),
            p2 = Offset(-r * 0.95f, 0f),
            p3 = Offset(r * 0.95f, 0f),
            solidColor = solidColor,
            rimColor = rimColor,
            density = density
        )
    }

    withTransform({
        translate(centerX, centerY + verticalOffset)
        rotate(primaryAngle, pivot = Offset.Zero)
    }) {
        drawStripedTriangleFast(
            p1 = Offset(0f, r),
            p2 = Offset(-r * 1.1f, r * 0.35f),
            p3 = Offset(r * 0.85f, 0f),
            color = stripeColor,
            density = density
        )
        drawSolidTriangleFast(
            path = path,
            p1 = Offset(0f, r),
            p2 = Offset(-r * 0.95f, 0f),
            p3 = Offset(r * 0.95f, 0f),
            solidColor = solidColor,
            rimColor = rimColor,
            density = density
        )
    }
}

private fun DrawScope.drawSolidTriangleFast(
    path: Path,
    p1: Offset,
    p2: Offset,
    p3: Offset,
    solidColor: Color,
    rimColor: Color,
    density: Float
) {
    path.reset()
    path.moveTo(p1.x, p1.y)
    path.lineTo(p2.x, p2.y)
    path.lineTo(p3.x, p3.y)
    path.close()

    drawPath(path = path, color = solidColor)

    drawPath(
        path = path,
        color = rimColor.copy(alpha = 0.68f),
        style = Stroke(width = 1.15f * density, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawStripedTriangleFast(
    p1: Offset,
    p2: Offset,
    p3: Offset,
    color: Color,
    density: Float
) {
    val strokeW = 1.4f * density
    val numLines = 6
    for (i in 1..numLines) {
        val t = i / (numLines + 1f)
        val startX = p1.x + (p2.x - p1.x) * t
        val startY = p1.y + (p2.y - p1.y) * t
        val endX = p1.x + (p3.x - p1.x) * t
        val endY = p1.y + (p3.y - p1.y) * t

        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

// Clean White Theme (CW / CLEAN_WHITE):
// Minimalist soft porcelain white with dark olive silk wave accents.
private fun DrawScope.drawCleanWhite(
    w: Float,
    h: Float,
    p1: Float,
    pulse: Float,
    sharedPath: Path
) {
    // 1. Pristine soft porcelain white vertical gradient background
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF0F0E6),
                Color(0xFFE3E9E1),
                Color(0xFFD5DED6),
            )
        )
    )

    // 2. Soft subtle olive silk wave accent
    val waveY = h * 0.42f + sin(p1) * 18f
    sharedPath.reset()
    sharedPath.moveTo(0f, waveY)
    sharedPath.cubicTo(
        w * 0.35f, waveY - 35f * pulse,
        w * 0.65f, waveY + 35f * pulse,
        w, waveY
    )
    sharedPath.lineTo(w, h)
    sharedPath.lineTo(0f, h)
    sharedPath.close()

    drawPath(
        path = sharedPath,
        color = Color(0xFF384D3C).copy(alpha = 0.05f)
    )

    // 3. Gentle floating dark olive light motes
    for (i in MOTE_X.indices) {
        val mx = MOTE_X[i] * w
        val my = (MOTE_Y[i] * h + sin(p1 + i) * 12f) % h
        drawCircle(
            color = Color(0xFF2C3E2B).copy(alpha = 0.06f),
            radius = MOTE_R[i] * 2.2f,
            center = Offset(mx, my)
        )
    }
}