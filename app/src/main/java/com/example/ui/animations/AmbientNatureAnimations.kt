package com.example.ui.animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// ============================================================================
// ENUMS & CONFIGURATION MODELS
// ============================================================================

enum class AmbientSceneType(
    val title: String,
    val subtitle: String,
    val badgeColor: Color
) {
    HARBOR_CATS_DOCK(
        title = "1. Harbor Cats & Boat",
        subtitle = "Headspace-inspired night pier, sleeping cyan cats, porthole cat & bobbing boat",
        badgeColor = Color(0xFF5CE1E6)
    ),
    NIGHT_COURTYARD_FOUNTAIN(
        title = "2. Courtyard Fountain & Cat",
        subtitle = "Headspace-inspired 3-tier purple fountain, cascading water ripples & prowling cat",
        badgeColor = Color(0xFF8B65D4)
    ),
    CLOUD_AND_RAIN(
        title = "3. Cloud & Rain",
        subtitle = "Slow drifting clouds with straight rain streaks, water bubbles & ripples",
        badgeColor = Color(0xFF64B5F6)
    ),
    WIND_AND_TREE(
        title = "4. Wind & Tree",
        subtitle = "Swaying tree, infinite blowing leaves & time-of-day sky/cloud cycle",
        badgeColor = Color(0xFF81C784)
    ),
    FOREST_GRASS(
        title = "5. Forest & Bustling Grass",
        subtitle = "Multi-layered lush meadow grass blades rippling in wind waves",
        badgeColor = Color(0xFFA5D6A7)
    ),
    NIGHT_CAMPFIRE(
        title = "6. Night Campfire & Meteors",
        subtitle = "Half moon, glittering stars, shooting meteors, clouds & flickering fire",
        badgeColor = Color(0xFFFFB74D)
    ),
    OCEAN_WAVE_BOAT(
        title = "7. Ocean Waves & Sailboat",
        subtitle = "Layered cyan waves with foam bubbles, pitching boat, lighthouse & weather",
        badgeColor = Color(0xFF4DD0E1)
    )
}

enum class HeadspaceSubScene(val label: String) {
    HARBOR_DOCK("Harbor Cats & Boat"),
    NIGHT_FOUNTAIN("Night Courtyard Fountain"),
    AUTO_CYCLE("Auto-Blend Both")
}

enum class RainStyleMode(val label: String) {
    STREAKS_AND_BUBBLES("Rain Lines + Water Bubbles"),
    STRAIGHT_LINES("Straight Rain Lines"),
    WATER_BUBBLES("Water Bubble Drops")
}

enum class TimeOfDayCycle(val label: String) {
    AUTO_CYCLE("Auto Time Cycle"),
    SUNSHINE_MORNING("Sunshine Morning"),
    AFTERNOON_WARM("Afternoon Sun"),
    CLOUDY_OVERCAST("Cloudy Sky"),
    MOONLIT_NIGHT("Moonlit Night")
}

enum class WaveWeatherState(val label: String) {
    AUTO_CYCLE("Auto Weather"),
    SUNNY_CALM("Sunny & Calm"),
    CLOUDY_PEACEFUL("Cloudy & Peaceful"),
    WINDY_SWELL("Windy Swell")
}

// ============================================================================
// CONTINUOUS FRAME TIME HOOK (60/120 FPS SMOOTH INFINITE LOOP)
// ============================================================================

@Composable
fun rememberContinuousTimeSeconds(): Float {
    var timeSeconds by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val startNanos = withFrameNanos { it }
        while (true) {
            withFrameNanos { frameNanos ->
                timeSeconds = ((frameNanos - startNanos) / 1_000_000_000.0).toFloat()
            }
        }
    }
    return timeSeconds
}

// ============================================================================
// MAIN FULLSCREEN SECTION COMPOSABLE (DROP-IN ENTRY POINT FOR YOUR APP)
// ============================================================================

/**
 * Fullscreen 2D Animations Section containing all 6 scenes (including both Headspace
 * reference scenes and the 5 nature animations: Cloud & Rain, Wind & Tree, Forest Grass,
 * Night Campfire & Meteors, and Ocean Waves & Boat).
 *
 * Tap anywhere on the fullscreen canvas to toggle the sleek glassmorphic scene & weather HUD.
 */
@Composable
fun AmbientNatureAnimationsSection(
    modifier: Modifier = Modifier,
    initialScene: AmbientSceneType = AmbientSceneType.HARBOR_CATS_DOCK,
    onBackClick: (() -> Unit)? = null
) {
    val scenes = remember { AmbientSceneType.entries }
    val pagerState = rememberPagerState(
        initialPage = scenes.indexOf(initialScene).coerceAtLeast(0),
        pageCount = { scenes.size }
    )
    val coroutineScope = rememberCoroutineScope()
    var showHud by remember { mutableStateOf(true) }

    // Per-scene interactive state controls
    var headspaceSubScene by remember { mutableStateOf(HeadspaceSubScene.HARBOR_DOCK) }
    var rainStyleMode by remember { mutableStateOf(RainStyleMode.STREAKS_AND_BUBBLES) }
    var windTimeOfDay by remember { mutableStateOf(TimeOfDayCycle.AUTO_CYCLE) }
    var waveWeather by remember { mutableStateOf(WaveWeatherState.AUTO_CYCLE) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0E26))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { showHud = !showHud })
            }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            when (scenes[pageIndex]) {
                AmbientSceneType.HARBOR_CATS_DOCK -> HeadspaceCatsAndFountainScene(
                    subScene = HeadspaceSubScene.HARBOR_DOCK,
                    modifier = Modifier.fillMaxSize()
                )
                AmbientSceneType.NIGHT_COURTYARD_FOUNTAIN -> HeadspaceCatsAndFountainScene(
                    subScene = HeadspaceSubScene.NIGHT_FOUNTAIN,
                    modifier = Modifier.fillMaxSize()
                )
                AmbientSceneType.CLOUD_AND_RAIN -> CloudAndRainFullscreenScene(
                    rainMode = rainStyleMode,
                    modifier = Modifier.fillMaxSize()
                )
                AmbientSceneType.WIND_AND_TREE -> WindTreeLeavesFullscreenScene(
                    timeOfDayMode = windTimeOfDay,
                    modifier = Modifier.fillMaxSize()
                )
                AmbientSceneType.FOREST_GRASS -> ForestBustlingGrassFullscreenScene(
                    modifier = Modifier.fillMaxSize()
                )
                AmbientSceneType.NIGHT_CAMPFIRE -> NightCampfireMeteorsFullscreenScene(
                    modifier = Modifier.fillMaxSize()
                )
                AmbientSceneType.OCEAN_WAVE_BOAT -> OceanWaveBoatFullscreenScene(
                    weatherMode = waveWeather,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Sleek floating HUD (tap fullscreen anywhere to hide/show for pure immersion)
        AnimatedVisibility(
            visible = showHud,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            val currentScene = scenes[pagerState.currentPage]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Scene Header & Sub-Mode Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x88101426))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (onBackClick != null) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .clickable { onBackClick() }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentScene.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentScene.subtitle,
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 11.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(currentScene.badgeColor.copy(alpha = 0.35f))
                                    .border(1.dp, currentScene.badgeColor, RoundedCornerShape(50))
                                    .clickable {
                                        coroutineScope.launch {
                                            val nextPg = (pagerState.currentPage + 1) % scenes.size
                                            pagerState.animateScrollToPage(nextPg)
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Next Theme ➔",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Contextual sub-controls for the active animation
                    Spacer(modifier = Modifier.height(10.dp))
                    when (currentScene) {
                        AmbientSceneType.HARBOR_CATS_DOCK -> {
                            ModeChipRow(
                                options = HeadspaceSubScene.entries,
                                selected = headspaceSubScene,
                                labelSelector = { it.label },
                                accentColor = currentScene.badgeColor,
                                onSelect = { headspaceSubScene = it }
                            )
                        }
                        AmbientSceneType.CLOUD_AND_RAIN -> {
                            ModeChipRow(
                                options = RainStyleMode.entries,
                                selected = rainStyleMode,
                                labelSelector = { it.label },
                                accentColor = currentScene.badgeColor,
                                onSelect = { rainStyleMode = it }
                            )
                        }
                        AmbientSceneType.WIND_AND_TREE -> {
                            ModeChipRow(
                                options = TimeOfDayCycle.entries,
                                selected = windTimeOfDay,
                                labelSelector = { it.label },
                                accentColor = currentScene.badgeColor,
                                onSelect = { windTimeOfDay = it }
                            )
                        }
                        AmbientSceneType.OCEAN_WAVE_BOAT -> {
                            ModeChipRow(
                                options = WaveWeatherState.entries,
                                selected = waveWeather,
                                labelSelector = { it.label },
                                accentColor = currentScene.badgeColor,
                                onSelect = { waveWeather = it }
                            )
                        }
                        else -> {
                            Text(
                                text = "Swipe left/right or tap pills below to switch fullscreen scenes",
                                color = currentScene.badgeColor.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Bottom Scene Selector Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0x99101426))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(24.dp))
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    scenes.forEachIndexed { index, scene ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isSelected) scene.badgeColor.copy(alpha = 0.28f)
                                    else Color.White.copy(alpha = 0.07f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) scene.badgeColor else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = scene.title,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> ModeChipRow(
    options: List<T>,
    selected: T,
    labelSelector: (T) -> String,
    accentColor: Color,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Surface(
                color = if (isSelected) accentColor.copy(alpha = 0.26f) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) accentColor else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onSelect(option) }
            ) {
                Text(
                    text = labelSelector(option),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
    }
}

// ============================================================================
// SCENE 1 (INCLUDES BOTH UPLOADED HEADSPACE SCENES):
// 1A: NIGHT HARBOR DOCK WITH SLEEPING CYAN CATS & BOBBING BOAT
// 1B: NIGHT COURTYARD FOUNTAIN WITH PROWLING BLUE CAT & WATER RIPPLES
// ============================================================================

@Composable
fun HeadspaceCatsAndFountainScene(
    subScene: HeadspaceSubScene = HeadspaceSubScene.HARBOR_DOCK,
    modifier: Modifier = Modifier
) {
    val t = rememberContinuousTimeSeconds()
    Canvas(modifier = modifier.fillMaxSize()) {
        val activeMode = when (subScene) {
            HeadspaceSubScene.HARBOR_DOCK -> HeadspaceSubScene.HARBOR_DOCK
            HeadspaceSubScene.NIGHT_FOUNTAIN -> HeadspaceSubScene.NIGHT_FOUNTAIN
            HeadspaceSubScene.AUTO_CYCLE -> {
                if ((t / 14f).toInt() % 2 == 0) HeadspaceSubScene.HARBOR_DOCK
                else HeadspaceSubScene.NIGHT_FOUNTAIN
            }
        }
        if (activeMode == HeadspaceSubScene.HARBOR_DOCK) {
            drawHeadspaceHarborCats(t)
        } else {
            drawHeadspaceNightFountain(t)
        }
    }
}

private fun DrawScope.drawHeadspaceHarborCats(t: Float) {
    val w = size.width
    val h = size.height

    // Deep indigo-violet night sky matching image_ae3a5d.jpg
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF282268),
                Color(0xFF332B7A),
                Color(0xFF3E348A)
            )
        )
    )

    // Twinkling 5-point stars and small night dots
    val starSpots = listOf(
        Triple(0.06f, 0.08f, 1.0f),
        Triple(0.11f, 0.17f, 0.6f),
        Triple(0.18f, 0.19f, 1.3f),
        Triple(0.09f, 0.27f, 1.1f),
        Triple(0.73f, 0.19f, 0.6f),
        Triple(0.78f, 0.26f, 0.7f),
        Triple(0.82f, 0.21f, 1.2f),
        Triple(0.92f, 0.22f, 1.15f)
    )
    starSpots.forEachIndexed { index, (sx, sy, scale) ->
        val twinkle = 0.55f + 0.45f * sin(t * 1.8f + index * 1.3f)
        val cx = w * sx
        val cy = h * sy
        if (scale > 0.9f) {
            drawFivePointStar(
                center = Offset(cx, cy),
                outerRadius = 9f * scale * (0.9f + 0.1f * twinkle),
                innerRadius = 4.2f * scale,
                color = Color(0xFF564D9A).copy(alpha = twinkle)
            )
        } else {
            drawCircle(
                color = Color(0xFF665DB0).copy(alpha = twinkle),
                radius = 5f * scale,
                center = Offset(cx, cy)
            )
        }
    }

    // Back billowing purple cloud bank
    drawPuffyCloudBank(
        baseY = h * 0.73f,
        color = Color(0xFF43398B),
        driftOffset = sin(t * 0.18f) * 18f,
        puffs = listOf(
            Triple(0.03f, 0.44f, 0.22f),
            Triple(0.18f, 0.40f, 0.25f),
            Triple(0.35f, 0.55f, 0.18f),
            Triple(0.54f, 0.59f, 0.16f),
            Triple(0.71f, 0.45f, 0.22f),
            Triple(0.88f, 0.36f, 0.26f),
            Triple(1.02f, 0.28f, 0.25f)
        )
    )

    // Mid-layer lighter lavender-purple cloud bank
    drawPuffyCloudBank(
        baseY = h * 0.75f,
        color = Color(0xFF554A9E),
        driftOffset = cos(t * 0.24f) * 24f,
        puffs = listOf(
            Triple(0.12f, 0.58f, 0.15f),
            Triple(0.62f, 0.52f, 0.19f),
            Triple(0.78f, 0.42f, 0.23f),
            Triple(0.95f, 0.35f, 0.24f)
        )
    )

    val seaTopY = h * 0.74f

    // Distant cruise/cargo ship silhouette slowly gliding on the horizon
    val shipX = ((w * 0.28f + t * 8f) % (w * 0.65f)) + w * 0.08f
    val shipBob = sin(t * 1.4f) * 2.5f
    val shipY = seaTopY + h * 0.02f + shipBob
    val shipHull = Path().apply {
        moveTo(shipX, shipY)
        lineTo(shipX + w * 0.105f, shipY)
        lineTo(shipX + w * 0.092f, shipY + h * 0.032f)
        lineTo(shipX + w * 0.01f, shipY + h * 0.032f)
        close()
    }
    drawPath(shipHull, color = Color(0xFF473D96))
    drawRoundRect(
        color = Color(0xFF473D96),
        topLeft = Offset(shipX + w * 0.014f, shipY - h * 0.024f),
        size = Size(w * 0.055f, h * 0.026f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    for (p in 0..2) {
        drawCircle(
            color = Color(0xFF2B226E),
            radius = 3.5f,
            center = Offset(shipX + w * 0.024f + p * w * 0.015f, shipY - h * 0.011f)
        )
    }

    // Deep indigo harbor sea
    drawRect(
        color = Color(0xFF2B226E),
        topLeft = Offset(0f, seaTopY),
        size = Size(w, h - seaTopY)
    )

    // Slow undulating dark water current ribbons
    val waveRibbonPositions = listOf(
        Offset(w * 0.03f, h * 0.79f) to w * 0.09f,
        Offset(w * 0.22f, h * 0.89f) to w * 0.26f,
        Offset(w * 0.66f, h * 0.84f) to w * 0.13f,
        Offset(w * 0.55f, h * 0.94f) to w * 0.18f,
        Offset(w * 0.84f, h * 0.95f) to w * 0.15f
    )
    waveRibbonPositions.forEachIndexed { idx, (pos, len) ->
        val shift = sin(t * 1.2f + idx * 1.1f) * 14f
        val path = Path().apply {
            val y0 = pos.y
            moveTo(pos.x + shift, y0)
            cubicTo(
                pos.x + len * 0.33f + shift, y0 - 8f * sin(t + idx),
                pos.x + len * 0.66f + shift, y0 + 8f * cos(t + idx),
                pos.x + len + shift, y0
            )
        }
        drawPath(
            path = path,
            color = Color(0xFF211A5A),
            style = Stroke(width = 12f, cap = StrokeCap.Round)
        )
    }

    // Wooden Pier & Piling Post on the left-center
    val pierY = h * 0.605f
    val postX = w * 0.422f
    val postW = w * 0.042f
    val postTopY = h * 0.50f
    val postBottomY = h * 0.92f

    // Support diagonal struts under pier
    val leftStrut = Path().apply {
        moveTo(postX - w * 0.055f, pierY + h * 0.035f)
        lineTo(postX - w * 0.025f, pierY + h * 0.035f)
        lineTo(postX + postW * 0.2f, pierY + h * 0.16f)
        lineTo(postX, pierY + h * 0.16f)
        close()
    }
    val rightStrut = Path().apply {
        moveTo(postX + postW + w * 0.055f, pierY + h * 0.035f)
        lineTo(postX + postW + w * 0.025f, pierY + h * 0.035f)
        lineTo(postX + postW * 0.8f, pierY + h * 0.16f)
        lineTo(postX + postW, pierY + h * 0.16f)
        close()
    }
    drawPath(leftStrut, color = Color(0xFF3E4098))
    drawPath(rightStrut, color = Color(0xFF3E4098))

    // Horizontal pier planks
    val plankSpecs = listOf(
        0.0f to 0.045f,
        0.052f to 0.145f,
        0.152f to 0.200f,
        0.207f to 0.352f,
        0.358f to 0.505f
    )
    plankSpecs.forEach { (startFrac, endFrac) ->
        drawRoundRect(
            color = Color(0xFF4749A6),
            topLeft = Offset(w * startFrac, pierY),
            size = Size(w * (endFrac - startFrac), h * 0.045f),
            cornerRadius = CornerRadius(6f, 6f)
        )
    }

    // Vertical wooden post
    drawRoundRect(
        color = Color(0xFF4749A6),
        topLeft = Offset(postX, postTopY),
        size = Size(postW, postBottomY - postTopY),
        cornerRadius = CornerRadius(postW * 0.35f, postW * 0.35f)
    )
    // Post top cap oval
    drawOval(
        color = Color(0xFF6B73C9),
        topLeft = Offset(postX, postTopY - h * 0.012f),
        size = Size(postW, h * 0.024f)
    )
    // Post wood grain highlights
    drawRoundRect(
        color = Color(0xFF7B84DB),
        topLeft = Offset(postX + postW * 0.22f, postTopY + h * 0.24f),
        size = Size(postW * 0.14f, h * 0.032f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = Color(0xFF7B84DB),
        topLeft = Offset(postX + postW * 0.55f, postTopY + h * 0.29f),
        size = Size(postW * 0.14f, h * 0.045f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Water ripple rings around the base of the post
    val postRippleScale = 1f + 0.08f * sin(t * 2.1f)
    drawArc(
        color = Color(0xFF211A5A),
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(postX - postW * 0.45f * postRippleScale, postBottomY - h * 0.03f),
        size = Size(postW * 1.9f * postRippleScale, h * 0.05f),
        style = Stroke(width = 9f, cap = StrokeCap.Round)
    )

    // LEFT SLEEPING CHUNKY CYAN CAT ON THE PIER
    val breath = 1f + 0.025f * sin(t * 1.9f)
    val catBaseX = w * 0.205f
    val catBaseY = pierY
    // Swaying curved tail
    val tailSway = sin(t * 1.4f) * 10f
    val tailPath = Path().apply {
        moveTo(catBaseX - w * 0.062f, catBaseY - h * 0.01f)
        cubicTo(
            catBaseX - w * 0.085f, catBaseY - h * 0.08f,
            catBaseX - w * 0.075f, catBaseY - h * 0.17f,
            catBaseX - w * 0.102f + tailSway * 0.4f, catBaseY - h * 0.195f + tailSway * 0.2f
        )
    }
    drawPath(
        path = tailPath,
        color = Color(0xFF5CE1E6),
        style = Stroke(width = w * 0.022f, cap = StrokeCap.Round)
    )

    // Pear-shaped breathing cat body + pointed ears
    val leftCatPath = Path().apply {
        moveTo(catBaseX - w * 0.064f, catBaseY)
        cubicTo(
            catBaseX - w * 0.068f, catBaseY - h * 0.09f * breath,
            catBaseX - w * 0.042f, catBaseY - h * 0.145f * breath,
            catBaseX - w * 0.035f, catBaseY - h * 0.165f * breath
        )
        // Left ear
        lineTo(catBaseX - w * 0.026f, catBaseY - h * 0.198f * breath)
        lineTo(catBaseX - w * 0.009f, catBaseY - h * 0.172f * breath)
        // Forehead
        lineTo(catBaseX + w * 0.012f, catBaseY - h * 0.172f * breath)
        // Right ear
        lineTo(catBaseX + w * 0.029f, catBaseY - h * 0.198f * breath)
        lineTo(catBaseX + w * 0.038f, catBaseY - h * 0.165f * breath)
        // Right body curve
        cubicTo(
            catBaseX + w * 0.052f, catBaseY - h * 0.135f * breath,
            catBaseX + w * 0.070f, catBaseY - h * 0.08f * breath,
            catBaseX + w * 0.065f, catBaseY
        )
        close()
    }
    drawPath(leftCatPath, color = Color(0xFF5CE1E6))

    // Left cat peaceful closed eyes & nose/mouth
    val faceY = catBaseY - h * 0.145f * breath
    drawSleepingCatFace(
        center = Offset(catBaseX + w * 0.002f, faceY),
        scale = w * 0.001f,
        strokeColor = Color(0xFF282268)
    )

    // RIGHT BOBBING BOAT WITH PORTHOLE CAT & ROOF SLEEPING CAT
    val boatBobY = sin(t * 1.55f) * (h * 0.014f)
    val boatPitchDeg = cos(t * 1.55f) * 1.8f

    withTransform({
        translate(left = 0f, top = boatBobY)
        rotate(degrees = boatPitchDeg, pivot = Offset(w * 0.86f, h * 0.72f))
    }) {
        val cabinLeft = w * 0.82f
        val cabinTop = h * 0.485f
        val cabinRight = w * 1.03f
        val cabinBottom = h * 0.73f

        // Boat cabin housing
        drawRoundRect(
            color = Color(0xFF5054B5),
            topLeft = Offset(cabinLeft, cabinTop),
            size = Size(cabinRight - cabinLeft, cabinBottom - cabinTop),
            cornerRadius = CornerRadius(24f, 24f)
        )
        // Cabin roof overhang
        drawRoundRect(
            color = Color(0xFF3E4098),
            topLeft = Offset(cabinLeft - w * 0.016f, cabinTop - h * 0.015f),
            size = Size(cabinRight - cabinLeft + w * 0.025f, h * 0.055f),
            cornerRadius = CornerRadius(28f, 28f)
        )

        // Circular Porthole Window
        val portholeCenter = Offset(w * 0.922f, h * 0.635f)
        val portholeOuterR = min(w, h) * 0.095f
        drawCircle(
            color = Color(0xFF282268),
            radius = portholeOuterR,
            center = portholeCenter
        )
        drawCircle(
            color = Color(0xFF7B84DB),
            radius = portholeOuterR * 0.88f,
            center = portholeCenter
        )
        drawCircle(
            color = Color(0xFF2B226E),
            radius = portholeOuterR * 0.72f,
            center = portholeCenter
        )

        // Porthole bolts
        for (b in 0 until 8) {
            val ang = (b * 45.0) * PI / 180.0
            val bx = portholeCenter.x + cos(ang).toFloat() * portholeOuterR * 0.80f
            val by = portholeCenter.y + sin(ang).toFloat() * portholeOuterR * 0.80f
            drawCircle(
                color = Color(0xFF3E4098),
                radius = 3.5f,
                center = Offset(bx, by)
            )
        }

        // Cute cyan cat inside the porthole
        val innerCatCenter = Offset(portholeCenter.x, portholeCenter.y + portholeOuterR * 0.16f)
        val innerR = portholeOuterR * 0.52f
        drawCircle(
            color = Color(0xFF5CE1E6),
            radius = innerR,
            center = innerCatCenter
        )
        // Porthole cat ears
        val earPath = Path().apply {
            moveTo(innerCatCenter.x - innerR * 0.85f, innerCatCenter.y - innerR * 0.35f)
            lineTo(innerCatCenter.x - innerR * 0.65f, innerCatCenter.y - innerR * 1.15f)
            lineTo(innerCatCenter.x - innerR * 0.18f, innerCatCenter.y - innerR * 0.82f)
            moveTo(innerCatCenter.x + innerR * 0.85f, innerCatCenter.y - innerR * 0.35f)
            lineTo(innerCatCenter.x + innerR * 0.65f, innerCatCenter.y - innerR * 1.15f)
            lineTo(innerCatCenter.x + innerR * 0.18f, innerCatCenter.y - innerR * 0.82f)
        }
        drawPath(earPath, color = Color(0xFF5CE1E6))
        drawSleepingCatFace(
            center = Offset(innerCatCenter.x, innerCatCenter.y - innerR * 0.25f),
            scale = w * 0.00075f,
            strokeColor = Color(0xFF282268)
        )

        // Sleeping cyan cat lounging on top of the cabin roof with dangling paws & tail
        val roofCatX = w * 0.90f
        val roofCatY = cabinTop - h * 0.01f
        // Body oval
        drawRoundRect(
            color = Color(0xFF5CE1E6),
            topLeft = Offset(roofCatX - w * 0.052f, roofCatY - h * 0.085f),
            size = Size(w * 0.135f, h * 0.085f),
            cornerRadius = CornerRadius(45f, 45f)
        )
        // Subtle darker teal spot on back
        drawOval(
            color = Color(0xFF38B6C4),
            topLeft = Offset(roofCatX + w * 0.035f, roofCatY - h * 0.085f),
            size = Size(w * 0.038f, h * 0.042f)
        )
        // Dangling front paws & tail
        drawLine(
            color = Color(0xFF5CE1E6),
            start = Offset(roofCatX - w * 0.045f, roofCatY - h * 0.015f),
            end = Offset(roofCatX - w * 0.062f, roofCatY + h * 0.032f),
            strokeWidth = w * 0.016f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF5CE1E6),
            start = Offset(roofCatX - w * 0.008f, roofCatY - h * 0.015f),
            end = Offset(roofCatX - w * 0.005f, roofCatY + h * 0.042f),
            strokeWidth = w * 0.016f,
            cap = StrokeCap.Round
        )
        // Swaying dangling tail on the right
        val roofTailSway = sin(t * 1.7f) * 8f
        drawLine(
            color = Color(0xFF5CE1E6),
            start = Offset(roofCatX + w * 0.075f, roofCatY - h * 0.025f),
            end = Offset(roofCatX + w * 0.072f + roofTailSway, roofCatY + h * 0.075f),
            strokeWidth = w * 0.017f,
            cap = StrokeCap.Round
        )
        // Roof cat head & ears
        val roofHeadCenter = Offset(roofCatX - w * 0.026f, roofCatY - h * 0.052f)
        val roofEarPath = Path().apply {
            moveTo(roofHeadCenter.x - w * 0.025f, roofHeadCenter.y - h * 0.025f)
            lineTo(roofHeadCenter.x - w * 0.022f, roofHeadCenter.y - h * 0.065f)
            lineTo(roofHeadCenter.x - w * 0.004f, roofHeadCenter.y - h * 0.038f)
            moveTo(roofHeadCenter.x + w * 0.006f, roofHeadCenter.y - h * 0.038f)
            lineTo(roofHeadCenter.x + w * 0.028f, roofHeadCenter.y - h * 0.065f)
            lineTo(roofHeadCenter.x + w * 0.030f, roofHeadCenter.y - h * 0.025f)
        }
        drawPath(roofEarPath, color = Color(0xFF5CE1E6))
        drawSleepingCatFace(
            center = Offset(roofHeadCenter.x + w * 0.002f, roofHeadCenter.y - h * 0.005f),
            scale = w * 0.00075f,
            strokeColor = Color(0xFF282268)
        )

        // Boat Hull curving up toward the bow
        val hullPath = Path().apply {
            moveTo(w * 0.70f, h * 0.68f)
            quadraticBezierTo(w * 0.85f, h * 0.75f, w * 1.04f, h * 0.735f)
            lineTo(w * 1.04f, h * 0.89f)
            lineTo(w * 0.75f, h * 0.885f)
            close()
        }
        drawPath(hullPath, color = Color(0xFF5860C2))

        // Top gunwale rim along the hull
        val rimPath = Path().apply {
            moveTo(w * 0.695f, h * 0.678f)
            quadraticBezierTo(w * 0.85f, h * 0.748f, w * 1.04f, h * 0.733f)
        }
        drawPath(
            path = rimPath,
            color = Color(0xFF4246A2),
            style = Stroke(width = h * 0.032f, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawHeadspaceNightFountain(t: Float) {
    val w = size.width
    val h = size.height

    // Deep indigo background wall & diagonal courtyard floor matching image_ae3d5e.jpg
    drawRect(color = Color(0xFF231768))

    val courtyardFloor = Path().apply {
        moveTo(0f, h * 0.29f)
        lineTo(w, h * 0.08f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(courtyardFloor, color = Color(0xFF4B2E99))

    // Prowling blue cat walking across the upper-left courtyard with long dramatic shadow
    val walkCycle = (t * 0.065f) % 1f
    val catX = w * (0.12f + 0.36f * sin(walkCycle * PI.toFloat()))
    val catY = h * (0.33f - 0.05f * sin(walkCycle * PI.toFloat()))
    val stepPhase = t * 5.2f

    // Long angled shadow cast to the bottom-left
    val shadowPath = Path().apply {
        moveTo(catX - w * 0.045f, catY + h * 0.025f)
        lineTo(catX + w * 0.055f, catY + h * 0.012f)
        lineTo(0f, catY + h * 0.14f)
        lineTo(0f, catY + h * 0.065f)
        close()
    }
    drawPath(shadowPath, color = Color(0xFF28196E))

    // Walking cat legs, arched body, raised tail, and lowered curious head
    drawWalkingCourtyardCat(
        center = Offset(catX, catY),
        scale = min(w, h) * 0.0014f,
        stepPhase = stepPhase
    )

    // 3-Tier Courtyard Fountain on the right
    val fCenterX = w * 0.705f
    val baseCenterY = h * 0.64f
    val baseRx = w * 0.34f
    val baseRy = h * 0.24f

    // Fountain cast shadow on the purple ground
    drawOval(
        color = Color(0xFF28196E),
        topLeft = Offset(fCenterX - baseRx * 1.32f, baseCenterY - baseRy * 0.45f),
        size = Size(baseRx * 2.15f, baseRy * 1.95f)
    )

    // Lower basin outer cylinder wall
    val basinWallPath = Path().apply {
        moveTo(fCenterX - baseRx * 0.94f, baseCenterY)
        lineTo(fCenterX - baseRx * 0.86f, baseCenterY + baseRy * 0.95f)
        quadraticBezierTo(
            fCenterX, baseCenterY + baseRy * 1.55f,
            fCenterX + baseRx * 0.86f, baseCenterY + baseRy * 0.95f
        )
        lineTo(fCenterX + baseRx * 0.94f, baseCenterY)
        close()
    }
    drawPath(basinWallPath, color = Color(0xFF5D3C9E))

    // Lower basin rim outer ellipse
    drawOval(
        color = Color(0xFF7D58C2),
        topLeft = Offset(fCenterX - baseRx, baseCenterY - baseRy * 0.85f),
        size = Size(baseRx * 2f, baseRy * 1.7f)
    )

    // Lower basin vibrant blue water pool
    val poolRx = baseRx * 0.84f
    val poolRy = baseRy * 0.68f
    drawOval(
        color = Color(0xFF0076F5),
        topLeft = Offset(fCenterX - poolRx, baseCenterY - poolRy),
        size = Size(poolRx * 2f, poolRy * 2f)
    )

    // Animated expanding cyan foam ripples in the main pool
    val splashPulse = 1f + 0.06f * sin(t * 3.4f)
    drawOval(
        color = Color(0xFF25B7FF),
        topLeft = Offset(
            fCenterX - poolRx * 0.48f * splashPulse,
            baseCenterY - poolRy * 0.38f * splashPulse
        ),
        size = Size(poolRx * 0.96f * splashPulse, poolRy * 0.72f * splashPulse)
    )
    // Secondary foam lobes around central column
    for (i in 0 until 5) {
        val ang = i * 1.25f + 0.3f
        val lobePulse = 1f + 0.12f * sin(t * 4f + i)
        val lx = fCenterX - poolRx * 0.08f + cos(ang) * poolRx * 0.28f
        val ly = baseCenterY + sin(ang) * poolRy * 0.24f
        drawOval(
            color = Color(0xFF25B7FF),
            topLeft = Offset(lx - 42f * lobePulse, ly - 22f * lobePulse),
            size = Size(84f * lobePulse, 44f * lobePulse)
        )
    }

    // Floating animated water droplets/ripples in the pool
    val rippleBubbles = listOf(
        Triple(-0.62f, -0.05f, 1.3f),
        Triple(-0.46f, -0.32f, 0.55f),
        Triple(0.64f, -0.18f, 0.6f),
        Triple(0.75f, 0.22f, 1.1f),
        Triple(-0.18f, 0.52f, 0.5f)
    )
    rippleBubbles.forEachIndexed { idx, (rx, ry, rScale) ->
        val bob = sin(t * 2.5f + idx) * 5f
        drawOval(
            color = Color(0xFF25B7FF).copy(alpha = 0.85f),
            topLeft = Offset(
                fCenterX + poolRx * rx - 24f * rScale,
                baseCenterY + poolRy * ry - 12f * rScale + bob
            ),
            size = Size(48f * rScale, 24f * rScale)
        )
    }

    // Center pedestal column between Tier 1 (bottom) and Tier 2 (middle)
    val midCenterY = h * 0.31f
    val colPath = Path().apply {
        moveTo(fCenterX - w * 0.032f, midCenterY + h * 0.09f)
        lineTo(fCenterX + w * 0.032f, midCenterY + h * 0.09f)
        lineTo(fCenterX + w * 0.045f, baseCenterY - h * 0.02f)
        quadraticBezierTo(
            fCenterX, baseCenterY + h * 0.01f,
            fCenterX - w * 0.045f, baseCenterY - h * 0.02f
        )
        close()
    }
    drawPath(colPath, color = Color(0xFF7D58C2))

    // Middle tier bowl
    val midRx = w * 0.115f
    val midRy = h * 0.068f
    drawArc(
        color = Color(0xFF6B48B0),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(fCenterX - midRx, midCenterY - midRy * 0.4f),
        size = Size(midRx * 2f, midRy * 2.6f)
    )
    drawOval(
        color = Color(0xFF8B65D4),
        topLeft = Offset(fCenterX - midRx, midCenterY - midRy),
        size = Size(midRx * 2f, midRy * 1.75f)
    )
    drawOval(
        color = Color(0xFF25B7FF),
        topLeft = Offset(fCenterX - midRx * 0.84f, midCenterY - midRy * 0.78f),
        size = Size(midRx * 1.68f, midRy * 1.38f)
    )

    // Upper pedestal column between Tier 2 and Tier 3 (top)
    val topCenterY = h * 0.145f
    drawRect(
        color = Color(0xFF6B48B0),
        topLeft = Offset(fCenterX - w * 0.018f, topCenterY + h * 0.03f),
        size = Size(w * 0.036f, midCenterY - topCenterY - h * 0.03f)
    )

    // Top tier bowl
    val topRx = w * 0.065f
    val topRy = h * 0.038f
    drawArc(
        color = Color(0xFF6B48B0),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(fCenterX - topRx, topCenterY - topRy * 0.3f),
        size = Size(topRx * 2f, topRy * 2.5f)
    )
    drawOval(
        color = Color(0xFF8B65D4),
        topLeft = Offset(fCenterX - topRx, topCenterY - topRy),
        size = Size(topRx * 2f, topRy * 1.7f)
    )
    drawOval(
        color = Color(0xFF25B7FF),
        topLeft = Offset(fCenterX - topRx * 0.8f, topCenterY - topRy * 0.72f),
        size = Size(topRx * 1.6f, topRy * 1.25f)
    )
    // Top nozzle spout
    drawOval(
        color = Color(0xFF8B65D4),
        topLeft = Offset(fCenterX - w * 0.015f, topCenterY - topRy * 1.15f),
        size = Size(w * 0.03f, h * 0.025f)
    )

    // Animated cascading water streams (Top -> Middle, and Middle -> Main Basin)
    val streamShimmer = sin(t * 6f) * 3.5f
    val upperCascade = Path().apply {
        moveTo(fCenterX - topRx * 0.62f, topCenterY + topRy * 0.25f)
        lineTo(fCenterX - topRx * 0.32f, topCenterY + topRy * 0.38f)
        cubicTo(
            fCenterX - topRx * 0.38f, (topCenterY + midCenterY) * 0.5f,
            fCenterX - topRx * 0.45f + streamShimmer, midCenterY - midRy * 0.1f,
            fCenterX - topRx * 0.40f + streamShimmer, midCenterY + midRy * 0.2f
        )
        lineTo(fCenterX - topRx * 0.76f - streamShimmer, midCenterY + midRy * 0.1f)
        close()
    }
    drawPath(upperCascade, color = Color(0xFF25B7FF))

    val lowerCascade = Path().apply {
        moveTo(fCenterX - midRx * 0.78f, midCenterY + midRy * 0.05f)
        lineTo(fCenterX - midRx * 0.35f, midCenterY + midRy * 0.35f)
        cubicTo(
            fCenterX - midRx * 0.52f + streamShimmer, (midCenterY + baseCenterY) * 0.48f,
            fCenterX - midRx * 0.65f - streamShimmer, baseCenterY - baseRy * 0.2f,
            fCenterX - midRx * 0.58f + streamShimmer, baseCenterY + baseRy * 0.05f
        )
        lineTo(fCenterX - midRx * 1.08f - streamShimmer, baseCenterY - baseRy * 0.04f)
        close()
    }
    drawPath(lowerCascade, color = Color(0xFF25B7FF))

    // Animated vertical water shimmer streaks inside the cascades
    for (s in 0..3) {
        val prog = ((t * 1.8f + s * 0.25f) % 1f)
        val sy1 = midCenterY + midRy * 0.3f + prog * (baseCenterY - midCenterY - baseRy * 0.25f)
        val sx1 = fCenterX - midRx * (0.62f + 0.18f * prog) + (s - 1.5f) * 6f
        drawLine(
            color = Color(0xFF7CE0FF).copy(alpha = 0.75f * (1f - prog)),
            start = Offset(sx1, sy1),
            end = Offset(sx1 - 3f, sy1 + 18f),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawWalkingCourtyardCat(
    center: Offset,
    scale: Float,
    stepPhase: Float
) {
    val bodyBlue = Color(0xFF1A4BFF)
    val headLightBlue = Color(0xFF2585FF)
    val legSwing1 = sin(stepPhase) * 14f * scale
    val legSwing2 = cos(stepPhase) * 14f * scale

    // Back legs
    drawLine(
        color = bodyBlue,
        start = Offset(center.x - 28f * scale, center.y - 8f * scale),
        end = Offset(center.x - 42f * scale + legSwing1, center.y + 34f * scale),
        strokeWidth = 9f * scale,
        cap = StrokeCap.Round
    )
    // Front legs
    drawLine(
        color = headLightBlue,
        start = Offset(center.x + 22f * scale, center.y - 4f * scale),
        end = Offset(center.x + 12f * scale - legSwing1, center.y + 32f * scale),
        strokeWidth = 8.5f * scale,
        cap = StrokeCap.Round
    )
    drawLine(
        color = headLightBlue,
        start = Offset(center.x + 30f * scale, center.y - 4f * scale),
        end = Offset(center.x + 48f * scale + legSwing2, center.y + 32f * scale),
        strokeWidth = 8.5f * scale,
        cap = StrokeCap.Round
    )

    // Perky curved tail
    val tailPath = Path().apply {
        moveTo(center.x - 28f * scale, center.y - 14f * scale)
        cubicTo(
            center.x - 22f * scale, center.y - 52f * scale,
            center.x - 16f * scale, center.y - 78f * scale,
            center.x - 36f * scale + sin(stepPhase * 0.7f) * 6f * scale, center.y - 75f * scale
        )
    }
    drawPath(
        path = tailPath,
        color = bodyBlue,
        style = Stroke(width = 8f * scale, cap = StrokeCap.Round)
    )

    // Arched torso (two-tone blue like image_ae3d5e.jpg)
    val torsoPath = Path().apply {
        moveTo(center.x - 34f * scale, center.y - 6f * scale)
        quadraticBezierTo(
            center.x - 5f * scale, center.y - 34f * scale,
            center.x + 32f * scale, center.y - 8f * scale
        )
        quadraticBezierTo(
            center.x + 2f * scale, center.y + 8f * scale,
            center.x - 34f * scale, center.y - 6f * scale
        )
        close()
    }
    drawPath(torsoPath, color = bodyBlue)

    // Lowered curious head & shoulders
    drawCircle(
        color = headLightBlue,
        radius = 16f * scale,
        center = Offset(center.x + 36f * scale, center.y - 4f * scale)
    )
    val earPath = Path().apply {
        moveTo(center.x + 30f * scale, center.y - 16f * scale)
        lineTo(center.x + 36f * scale, center.y - 26f * scale)
        lineTo(center.x + 44f * scale, center.y - 16f * scale)
        close()
    }
    drawPath(earPath, color = headLightBlue)
}

// ============================================================================
// SCENE 2: CLOUD & RAIN FULLSCREEN ANIMATION
// - Clouds moving slowly across the sky in multi-depth layers
// - Raining with small water-like bubbles AND/OR straight-line rain streaks
//   (also includes atmospheric bokeh particles inspired by the uploaded video)
// - Ground puddle surface with expanding rain splash ripples
// ============================================================================

private data class RainParticleSpec(
    val xSeed: Float,
    val ySeed: Float,
    val speed: Float,
    val sizeFactor: Float,
    val isBubble: Boolean,
    val depthLayer: Int
)

@Composable
fun CloudAndRainFullscreenScene(
    rainMode: RainStyleMode = RainStyleMode.STREAKS_AND_BUBBLES,
    modifier: Modifier = Modifier
) {
    val t = rememberContinuousTimeSeconds()
    val particles = remember {
        List(150) { i ->
            val r1 = ((i * 73 + 19) % 100) / 100f
            val r2 = ((i * 37 + 43) % 100) / 100f
            val r3 = ((i * 53 + 11) % 100) / 100f
            RainParticleSpec(
                xSeed = r1,
                ySeed = r2,
                speed = 0.38f + r3 * 0.55f,
                sizeFactor = 0.6f + ((i * 29) % 100) / 100f * 0.9f,
                isBubble = i % 2 == 0,
                depthLayer = i % 3
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Moody atmospheric rain sky gradient with soft top-left light beam (inspired by video)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F1B2D),
                    Color(0xFF1B2E47),
                    Color(0xFF274261),
                    Color(0xFF1A2C42)
                )
            )
        )

        // Subtle volumetric light glow in the upper-left corner
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x55B3E5FC),
                    Color(0x2281D4FA),
                    Color.Transparent
                ),
                center = Offset(w * 0.12f, h * 0.08f),
                radius = max(w, h) * 0.55f
            ),
            radius = max(w, h) * 0.55f,
            center = Offset(w * 0.12f, h * 0.08f)
        )

        // Back layer slow-drifting rain clouds
        drawDriftingCloudCluster(
            cx = ((w * 0.18f + t * 11f) % (w * 1.35f)) - w * 0.18f,
            cy = h * 0.16f,
            scale = w * 0.0012f,
            color = Color(0xFF324B6B).copy(alpha = 0.85f)
        )
        drawDriftingCloudCluster(
            cx = ((w * 0.72f + t * 9f) % (w * 1.35f)) - w * 0.18f,
            cy = h * 0.21f,
            scale = w * 0.00145f,
            color = Color(0xFF2B425E).copy(alpha = 0.9f)
        )

        // Mid layer slow-drifting clouds
        drawDriftingCloudCluster(
            cx = ((w * 0.45f + t * 17f) % (w * 1.4f)) - w * 0.2f,
            cy = h * 0.13f,
            scale = w * 0.0016f,
            color = Color(0xFF47668A).copy(alpha = 0.92f)
        )
        drawDriftingCloudCluster(
            cx = ((w * 0.95f + t * 15f) % (w * 1.4f)) - w * 0.2f,
            cy = h * 0.19f,
            scale = w * 0.00135f,
            color = Color(0xFF3E5B7D).copy(alpha = 0.9f)
        )

        // Calm wet reflection ground / water plane at the bottom
        val puddleTop = h * 0.84f
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF132235),
                    Color(0xFF0B1522)
                ),
                startY = puddleTop,
                endY = h
            ),
            topLeft = Offset(0f, puddleTop),
            size = Size(w, h - puddleTop)
        )

        // Rain particles: Straight rain streaks & small water-like bubble droplets
        val windSlant = w * 0.065f
        particles.forEachIndexed { idx, p ->
            val progress = (p.ySeed + t * p.speed) % 1f
            val curY = h * 0.14f + progress * (puddleTop - h * 0.12f)
            val baseX = ((p.xSeed * w + progress * windSlant + t * 12f) % (w * 1.1f)) - w * 0.05f

            val drawAsBubble = when (rainMode) {
                RainStyleMode.STRAIGHT_LINES -> false
                RainStyleMode.WATER_BUBBLES -> true
                RainStyleMode.STREAKS_AND_BUBBLES -> p.isBubble
            }

            val alphaByDepth = when (p.depthLayer) {
                0 -> 0.35f
                1 -> 0.62f
                else -> 0.88f
            }

            if (drawAsBubble) {
                // Water-like translucent bubble droplet with specular highlight
                val radius = (3.2f + p.sizeFactor * 5.2f) * (0.75f + p.depthLayer * 0.2f)
                val wobbleX = sin(t * 4f + idx) * 2.5f
                val bubbleCenter = Offset(baseX + wobbleX, curY)

                drawCircle(
                    color = Color(0xFF90CAF9).copy(alpha = alphaByDepth * 0.42f),
                    radius = radius,
                    center = bubbleCenter
                )
                drawCircle(
                    color = Color(0xFFE3F2FD).copy(alpha = alphaByDepth * 0.85f),
                    radius = radius,
                    center = bubbleCenter,
                    style = Stroke(width = 1.4f)
                )
                // Inner water bubble specular glint
                drawCircle(
                    color = Color.White.copy(alpha = alphaByDepth),
                    radius = radius * 0.28f,
                    center = Offset(bubbleCenter.x - radius * 0.32f, bubbleCenter.y - radius * 0.32f)
                )
            } else {
                // Straight slanted rain line streak
                val streakLen = (18f + p.sizeFactor * 26f) * (0.8f + p.depthLayer * 0.22f)
                val dx = streakLen * 0.14f
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0f),
                            Color(0xFFB3E5FC).copy(alpha = alphaByDepth)
                        ),
                        start = Offset(baseX - dx, curY - streakLen),
                        end = Offset(baseX, curY)
                    ),
                    start = Offset(baseX - dx, curY - streakLen),
                    end = Offset(baseX, curY),
                    strokeWidth = 1.6f + p.depthLayer * 0.65f,
                    cap = StrokeCap.Round
                )
            }

            // Splash rings & tiny water bead bounce when drops hit the puddle surface
            if (idx % 5 == 0) {
                val rippleAge = ((t * p.speed * 1.4f + p.xSeed * 3f) % 1f)
                val rx = (p.xSeed * w * 0.92f) + w * 0.04f
                val ry = puddleTop + (idx % 7) * (h * 0.018f)
                val maxRippleW = 28f * p.sizeFactor
                drawOval(
                    color = Color(0xFFB3E5FC).copy(alpha = (1f - rippleAge) * 0.55f),
                    topLeft = Offset(
                        rx - maxRippleW * rippleAge,
                        ry - maxRippleW * 0.28f * rippleAge
                    ),
                    size = Size(
                        maxRippleW * 2f * rippleAge,
                        maxRippleW * 0.56f * rippleAge
                    ),
                    style = Stroke(width = 1.6f)
                )
                if (rippleAge < 0.35f) {
                    val bounceH = sin(rippleAge / 0.35f * PI.toFloat()) * 10f
                    drawCircle(
                        color = Color(0xFFE1F5FE).copy(alpha = 0.8f),
                        radius = 2.4f,
                        center = Offset(rx, ry - bounceH)
                    )
                }
            }
        }

        // Foreground fluffy dark-blue cloud bank at the very top
        drawDriftingCloudCluster(
            cx = ((w * 0.25f + t * 24f) % (w * 1.45f)) - w * 0.22f,
            cy = h * 0.09f,
            scale = w * 0.0018f,
            color = Color(0xFF5A7CA3).copy(alpha = 0.95f)
        )
        drawDriftingCloudCluster(
            cx = ((w * 0.82f + t * 21f) % (w * 1.45f)) - w * 0.22f,
            cy = h * 0.11f,
            scale = w * 0.00165f,
            color = Color(0xFF517299).copy(alpha = 0.95f)
        )
    }
}

// ============================================================================
// SCENE 3: WIND & WAVING TREE WITH INFINITE FALLING LEAVES & TIME-OF-DAY SKY
// - One expressive tree waving in the wind on a rolling hill
// - Leaves detach at intervals and blow in the wind out of the screen to the
//   right in a continuous infinite loop
// - Sky and cloud density smoothly transition across Sunshine, Afternoon,
//   Cloudy, and Moonlit Night (or can be pinned via the HUD selector)
// ============================================================================

private data class BlowingLeafSpec(
    val startBranchIndex: Int,
    val phaseOffset: Float,
    val speedMultiplier: Float,
    val flutterFreq: Float,
    val sizeScale: Float,
    val verticalSpread: Float
)

@Composable
fun WindTreeLeavesFullscreenScene(
    timeOfDayMode: TimeOfDayCycle = TimeOfDayCycle.AUTO_CYCLE,
    modifier: Modifier = Modifier
) {
    val t = rememberContinuousTimeSeconds()
    val leaves = remember {
        List(28) { i ->
            BlowingLeafSpec(
                startBranchIndex = i % 5,
                phaseOffset = i / 28f,
                speedMultiplier = 0.82f + (i % 7) * 0.08f,
                flutterFreq = 2.4f + (i % 5) * 0.65f,
                sizeScale = 0.78f + (i % 4) * 0.18f,
                verticalSpread = ((i * 37) % 100) / 100f - 0.5f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Continuous time-of-day cycle (0..4):
        // 0 = Sunshine Morning, 1 = Warm Afternoon, 2 = Overcast Cloudy, 3 = Moonlit Night
        val cyclePos = when (timeOfDayMode) {
            TimeOfDayCycle.SUNSHINE_MORNING -> 0f
            TimeOfDayCycle.AFTERNOON_WARM -> 1f
            TimeOfDayCycle.CLOUDY_OVERCAST -> 2f
            TimeOfDayCycle.MOONLIT_NIGHT -> 3f
            TimeOfDayCycle.AUTO_CYCLE -> (t * 0.09f) % 4f
        }
        val stageIndex = floor(cyclePos).toInt() % 4
        val stageFraction = cyclePos - floor(cyclePos)

        val skyTopColors = listOf(
            Color(0xFF4FA8FF), // Sunshine Morning
            Color(0xFFF57C51), // Warm Afternoon
            Color(0xFF607D8B), // Cloudy Overcast
            Color(0xFF14183E)  // Moonlit Night
        )
        val skyBottomColors = listOf(
            Color(0xFFB3E5FC),
            Color(0xFFFFD584),
            Color(0xFFB0BEC5),
            Color(0xFF2B326B)
        )
        val hillColors = listOf(
            Color(0xFF43A047),
            Color(0xFF558B2F),
            Color(0xFF3E6B48),
            Color(0xFF1C352D)
        )
        val leafPalettes = listOf(
            Color(0xFF66BB6A),
            Color(0xFFFFB300),
            Color(0xFF4CAF50),
            Color(0xFF4DB6AC)
        )

        val nextStage = (stageIndex + 1) % 4
        val topSky = lerpColor(skyTopColors[stageIndex], skyTopColors[nextStage], stageFraction)
        val bottomSky = lerpColor(skyBottomColors[stageIndex], skyBottomColors[nextStage], stageFraction)
        val hillColor = lerpColor(hillColors[stageIndex], hillColors[nextStage], stageFraction)
        val leafBaseColor = lerpColor(leafPalettes[stageIndex], leafPalettes[nextStage], stageFraction)

        // Sky background gradient
        drawRect(
            brush = Brush.verticalGradient(colors = listOf(topSky, bottomSky))
        )

        // Celestial bodies: Sun in Morning/Afternoon, Moon & Stars at Night
        val sunAlpha = when (stageIndex) {
            0 -> 1f
            1 -> 1f - stageFraction * 0.7f
            3 -> stageFraction
            else -> 0.25f
        }
        val nightAlpha = when (stageIndex) {
            2 -> stageFraction
            3 -> 1f - stageFraction
            else -> 0f
        }

        if (sunAlpha > 0.05f) {
            val sunY = h * (0.20f + 0.12f * (1f - sunAlpha))
            val sunCenter = Offset(w * 0.76f, sunY)
            drawCircle(
                color = Color(0xFFFFF59D).copy(alpha = 0.28f * sunAlpha),
                radius = min(w, h) * 0.13f,
                center = sunCenter
            )
            drawCircle(
                color = Color(0xFFFFEE58).copy(alpha = 0.92f * sunAlpha),
                radius = min(w, h) * 0.072f,
                center = sunCenter
            )
        }

        if (nightAlpha > 0.05f) {
            // Twinkling night stars & crescent moon
            for (s in 0 until 18) {
                val sx = ((s * 67 + 13) % 95) / 100f * w
                val sy = ((s * 41 + 7) % 45) / 100f * h
                val tw = (0.4f + 0.6f * sin(t * 2.5f + s)) * nightAlpha
                drawCircle(
                    color = Color.White.copy(alpha = tw),
                    radius = if (s % 3 == 0) 3f else 2f,
                    center = Offset(sx, sy)
                )
            }
            drawCrescentMoon(
                center = Offset(w * 0.78f, h * 0.19f),
                radius = min(w, h) * 0.065f,
                color = Color(0xFFFFF9C4).copy(alpha = nightAlpha),
                shadowColor = topSky
            )
        }

        // Dynamic cloud count & opacity depending on time/weather (extra cloudy in stage 2)
        val cloudDensityAlpha = when (stageIndex) {
            0 -> 0.65f
            1 -> 0.55f + 0.4f * stageFraction
            2 -> 0.95f
            else -> 0.45f
        }
        val cloudTint = lerpColor(Color.White, Color(0xFF90A4AE), if (stageIndex == 2) 0.55f else 0.15f)
            .copy(alpha = cloudDensityAlpha)

        val cloudCount = if (stageIndex == 2) 6 else 4
        for (c in 0 until cloudCount) {
            val speed = 16f + c * 5f
            val cx = ((w * (c * 0.24f) + t * speed) % (w * 1.45f)) - w * 0.22f
            val cy = h * (0.12f + (c % 3) * 0.085f)
            drawDriftingCloudCluster(
                cx = cx,
                cy = cy,
                scale = w * (0.0011f + (c % 3) * 0.00025f),
                color = cloudTint
            )
        }

        // Visible sweeping wind gust ribbons across the sky
        for (g in 0..3) {
            val gustProg = ((t * 0.38f + g * 0.25f) % 1f)
            val gx = -w * 0.2f + gustProg * (w * 1.3f)
            val gy = h * (0.28f + g * 0.11f) + sin(t * 2f + g) * 14f
            val gustAlpha = sin(gustProg * PI.toFloat()) * 0.32f
            val gustPath = Path().apply {
                moveTo(gx, gy)
                cubicTo(
                    gx + w * 0.08f, gy - 18f,
                    gx + w * 0.16f, gy + 18f,
                    gx + w * 0.24f, gy - 4f
                )
            }
            drawPath(
                path = gustPath,
                color = Color.White.copy(alpha = gustAlpha),
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }

        // Distant & foreground rolling hills
        val backHill = Path().apply {
            moveTo(0f, h * 0.76f)
            quadraticBezierTo(w * 0.35f, h * 0.67f, w * 0.75f, h * 0.74f)
            quadraticBezierTo(w * 0.90f, h * 0.77f, w, h * 0.73f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(backHill, color = hillColor.copy(alpha = 0.7f))

        val frontHill = Path().apply {
            moveTo(0f, h * 0.78f)
            quadraticBezierTo(w * 0.28f, h * 0.68f, w * 0.62f, h * 0.77f)
            quadraticBezierTo(w * 0.84f, h * 0.82f, w, h * 0.79f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(frontHill, color = hillColor)

        // Waving Tree positioned on the left-center crest so leaves have a long path to blow right
        val treeBaseX = w * 0.26f
        val treeBaseY = h * 0.76f
        val treeHeight = min(w, h) * 0.44f

        // Always blowing right with natural rhythmic wind gusts
        val windBend = 16f + 9f * sin(t * 1.85f) + 4f * sin(t * 3.9f)
        val canopyAnchors = drawSwayingWindTree(
            base = Offset(treeBaseX, treeBaseY),
            trunkHeight = treeHeight,
            windAngleDeg = windBend,
            time = t,
            canopyBaseColor = leafBaseColor
        )

        // Infinite loop of detaching leaves blowing out of the screen to the right
        leaves.forEach { leaf ->
            val life = (t * 0.22f * leaf.speedMultiplier + leaf.phaseOffset) % 1f
            val origin = canopyAnchors[leaf.startBranchIndex % canopyAnchors.size]

            // Travel from canopy anchor all the way past the right edge of the screen (w * 1.12f)
            val travelX = origin.x + life * (w * 1.15f - origin.x)
            val gravityDrop = (life * life) * (h * 0.26f)
            val windLift = sin(life * PI.toFloat() * leaf.flutterFreq + t * 2f) * (h * 0.045f)
            val travelY = origin.y + leaf.verticalSpread * (h * 0.14f) + gravityDrop + windLift

            val leafRotation = (t * 140f * leaf.speedMultiplier + leaf.phaseOffset * 360f) +
                    sin(t * 4f + leaf.phaseOffset * 10f) * 35f

            val fadeInAlpha = (life / 0.08f).coerceIn(0f, 1f)
            val leafSize = 11f * leaf.sizeScale

            withTransform({
                translate(left = travelX, top = travelY)
                rotate(degrees = leafRotation, pivot = Offset.Zero)
            }) {
                val leafPath = Path().apply {
                    moveTo(-leafSize, 0f)
                    quadraticBezierTo(0f, -leafSize * 0.65f, leafSize, 0f)
                    quadraticBezierTo(0f, leafSize * 0.65f, -leafSize, 0f)
                    close()
                }
                drawPath(
                    path = leafPath,
                    color = leafBaseColor.copy(alpha = fadeInAlpha * 0.92f)
                )
                drawLine(
                    color = Color(0xFF2E572C).copy(alpha = fadeInAlpha * 0.7f),
                    start = Offset(-leafSize * 0.8f, 0f),
                    end = Offset(leafSize * 0.8f, 0f),
                    strokeWidth = 1.4f
                )
            }
        }
    }
}

private fun DrawScope.drawSwayingWindTree(
    base: Offset,
    trunkHeight: Float,
    windAngleDeg: Float,
    time: Float,
    canopyBaseColor: Color
): List<Offset> {
    val rad = (windAngleDeg - 90f) * (PI / 180.0).toFloat()
    val midRad = (windAngleDeg * 0.55f - 90f) * (PI / 180.0).toFloat()

    val midPoint = Offset(
        base.x + cos(midRad) * (trunkHeight * 0.52f),
        base.y + sin(midRad) * (trunkHeight * 0.52f)
    )
    val topPoint = Offset(
        midPoint.x + cos(rad) * (trunkHeight * 0.50f),
        midPoint.y + sin(rad) * (trunkHeight * 0.50f)
    )

    // Tapered organic tree trunk curving in the wind
    val trunkW = trunkHeight * 0.13f
    val trunkPath = Path().apply {
        moveTo(base.x - trunkW * 0.6f, base.y)
        quadraticBezierTo(
            midPoint.x - trunkW * 0.35f, midPoint.y,
            topPoint.x - trunkW * 0.14f, topPoint.y
        )
        lineTo(topPoint.x + trunkW * 0.14f, topPoint.y)
        quadraticBezierTo(
            midPoint.x + trunkW * 0.35f, midPoint.y,
            base.x + trunkW * 0.6f, base.y
        )
        close()
    }
    drawPath(trunkPath, color = Color(0xFF4E342E))

    // Primary swaying branches & foliage clusters
    val branchConfigs = listOf(
        Triple(-32f, 0.42f, 0.78f),
        Triple(-12f, 0.48f, 0.92f),
        Triple(8f, 0.52f, 1.0f),
        Triple(26f, 0.46f, 0.88f),
        Triple(42f, 0.38f, 0.74f)
    )

    val canopyTips = mutableListOf<Offset>()
    branchConfigs.forEachIndexed { idx, (angleOffset, lenFactor, foliageScale) ->
        val localFlutter = sin(time * 2.8f + idx * 1.2f) * 4.5f
        val bAngleRad = (windAngleDeg * 1.15f + angleOffset + localFlutter - 90f) * (PI / 180.0).toFloat()
        val bStart = if (idx % 2 == 0) topPoint else Offset(
            (midPoint.x + topPoint.x) * 0.5f,
            (midPoint.y + topPoint.y) * 0.5f
        )
        val bLen = trunkHeight * lenFactor
        val bTip = Offset(
            bStart.x + cos(bAngleRad) * bLen,
            bStart.y + sin(bAngleRad) * bLen
        )
        canopyTips.add(bTip)

        drawLine(
            color = Color(0xFF4E342E),
            start = bStart,
            end = bTip,
            strokeWidth = trunkW * 0.25f,
            cap = StrokeCap.Round
        )

        // Layered leafy canopy puffs stretched slightly toward the wind direction
        val clusterR = trunkHeight * 0.24f * foliageScale
        val shadeColor = lerpColor(canopyBaseColor, Color(0xFF1B5E20), 0.32f)
        val highlightColor = lerpColor(canopyBaseColor, Color(0xFFFFF59D), 0.22f)

        drawCircle(
            color = shadeColor,
            radius = clusterR * 0.95f,
            center = Offset(bTip.x - clusterR * 0.15f, bTip.y + clusterR * 0.12f)
        )
        drawCircle(
            color = canopyBaseColor,
            radius = clusterR,
            center = bTip
        )
        drawCircle(
            color = highlightColor,
            radius = clusterR * 0.72f,
            center = Offset(bTip.x + clusterR * 0.28f, bTip.y - clusterR * 0.12f)
        )
    }

    return canopyTips
}

// ============================================================================
// SCENE 4: FOREST & BUSTLING GRASS FULLSCREEN ANIMATION
// - Inspired by uploaded images (image_aea2f6.png, image_aea2f8.png, image_aea2fa.png)
// - Rolling sunlit forest meadow with hundreds of individual curved grass blades
//   organized in depth layers, bustling and rippling in traveling wind waves
// ============================================================================

private data class GrassBladeSpec(
    val xFraction: Float,
    val baseYFraction: Float,
    val heightFraction: Float,
    val widthPx: Float,
    val naturalLean: Float,
    val phaseSeed: Float,
    val layer: Int
)

@Composable
fun ForestBustlingGrassFullscreenScene(
    modifier: Modifier = Modifier
) {
    val t = rememberContinuousTimeSeconds()
    val grassBlades = remember {
        val list = mutableListOf<GrassBladeSpec>()
        // 4 depth rows of dense lush grass blades from mid-ground to foreground
        val countsPerLayer = listOf(85, 95, 110, 125)
        countsPerLayer.forEachIndexed { layerIdx, count ->
            for (i in 0 until count) {
                val xFrac = (i.toFloat() / count) + (((i * 37 + layerIdx * 19) % 100) / 100f - 0.5f) * 0.02f
                val hVar = 0.75f + ((i * 53 + layerIdx * 29) % 100) / 100f * 0.55f
                val baseYFrac = when (layerIdx) {
                    0 -> 0.58f + ((i * 17) % 100) / 100f * 0.08f
                    1 -> 0.68f + ((i * 23) % 100) / 100f * 0.09f
                    2 -> 0.80f + ((i * 31) % 100) / 100f * 0.09f
                    else -> 0.93f + ((i * 43) % 100) / 100f * 0.08f
                }
                val baseHeightFrac = when (layerIdx) {
                    0 -> 0.11f
                    1 -> 0.15f
                    2 -> 0.20f
                    else -> 0.27f
                }
                list.add(
                    GrassBladeSpec(
                        xFraction = xFrac.coerceIn(-0.05f, 1.05f),
                        baseYFraction = baseYFrac,
                        heightFraction = baseHeightFrac * hVar,
                        widthPx = (5.5f + layerIdx * 3.2f) * (0.8f + (i % 5) * 0.1f),
                        naturalLean = ((i * 41) % 100 / 100f - 0.5f) * 28f,
                        phaseSeed = (i * 0.37f) + layerIdx * 1.3f,
                        layer = layerIdx
                    )
                )
            }
        }
        list
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Warm sunlit forest canopy background gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFDCE775),
                    Color(0xFFAED581),
                    Color(0xFF7CB342),
                    Color(0xFF33691E)
                )
            )
        )

        // Soft sunbeams filtering into the forest meadow from the upper left
        for (ray in 0..3) {
            val rayShift = sin(t * 0.45f + ray) * (w * 0.03f)
            val rayPath = Path().apply {
                moveTo(w * (0.08f + ray * 0.18f) + rayShift, 0f)
                lineTo(w * (0.18f + ray * 0.18f) + rayShift, 0f)
                lineTo(w * (0.42f + ray * 0.22f) + rayShift, h * 0.75f)
                lineTo(w * (0.24f + ray * 0.22f) + rayShift, h * 0.75f)
                close()
            }
            drawPath(
                path = rayPath,
                color = Color(0x24FFFDE7)
            )
        }

        // Rolling layered green meadow hills matching image_aea2f6.png
        val meadowLayers = listOf(
            Triple(0.50f, Color(0xFF8BC34A), 0.03f),
            Triple(0.62f, Color(0xFF7CB342), -0.025f),
            Triple(0.75f, Color(0xFF558B2F), 0.02f),
            Triple(0.88f, Color(0xFF2E5722), -0.015f)
        )

        meadowLayers.forEachIndexed { idx, (yFrac, hillCol, arch) ->
            val hillPath = Path().apply {
                moveTo(0f, h * yFrac)
                quadraticBezierTo(
                    w * 0.32f, h * (yFrac + arch),
                    w * 0.65f, h * (yFrac - arch * 0.6f)
                )
                quadraticBezierTo(
                    w * 0.85f, h * (yFrac + arch * 0.5f),
                    w, h * yFrac
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(hillPath, color = hillCol)

            // Draw all grass blades belonging to this depth layer
            val bladePalette = when (idx) {
                0 -> listOf(Color(0xFF9CCC65), Color(0xFF7CB342), Color(0xFFAED581))
                1 -> listOf(Color(0xFF7CB342), Color(0xFF558B2F), Color(0xFF8BC34A))
                2 -> listOf(Color(0xFF43A047), Color(0xFF2E7D32), Color(0xFF66BB6A))
                else -> listOf(Color(0xFF1B5E20), Color(0xFF0D3B10), Color(0xFF2E7D32))
            }

            grassBlades.filter { it.layer == idx }.forEachIndexed { bIdx, blade ->
                val bx = blade.xFraction * w
                val by = blade.baseYFraction * h
                val bHeight = blade.heightFraction * h

                // Traveling wind wave across the field + rapid bustling micro-flutter
                val waveSweep = sin(t * 2.3f - blade.xFraction * 6.5f + blade.phaseSeed * 0.3f)
                val gustFlutter = sin(t * 5.2f + blade.phaseSeed) * 0.3f
                val totalSwayPx = blade.naturalLean + (waveSweep * 32f + gustFlutter * 14f) * (0.65f + idx * 0.2f)

                val tipX = bx + totalSwayPx
                val tipY = by - bHeight
                val ctrlX = bx + totalSwayPx * 0.38f
                val ctrlY = by - bHeight * 0.55f

                val bladePath = Path().apply {
                    moveTo(bx - blade.widthPx * 0.5f, by)
                    quadraticBezierTo(ctrlX - blade.widthPx * 0.18f, ctrlY, tipX, tipY)
                    quadraticBezierTo(ctrlX + blade.widthPx * 0.18f, ctrlY, bx + blade.widthPx * 0.5f, by)
                    close()
                }
                val color = bladePalette[bIdx % bladePalette.size]
                drawPath(bladePath, color = color)
            }
        }

        // Drifting golden dandelion / forest pollen motes in the breeze
        for (m in 0 until 24) {
            val mx = ((w * ((m * 47) % 100 / 100f) + t * (22f + m % 9 * 4f)) % (w * 1.1f)) - w * 0.05f
            val my = h * (0.25f + ((m * 61) % 65) / 100f) + sin(t * 1.8f + m) * 16f
            val alpha = 0.45f + 0.45f * sin(t * 2.4f + m)
            drawCircle(
                color = Color(0xFFFFF9C4).copy(alpha = alpha),
                radius = 3.2f + (m % 3) * 1.2f,
                center = Offset(mx, my)
            )
        }
    }
}

// ============================================================================
// SCENE 5: FIRE & NIGHT CAMP WITH HALF MOON, GLITTERING STARS & METEORS
// - Inspired by image_aea65e.png
// - Deep purple night sky with crescent half-moon, glittering stars & 4-point
//   sparkles, slow night clouds, and periodic shooting meteors
// - Mountain silhouettes, pine trees, glowing tent, backpack, sleeping bags,
//   and a warm stone-ring campfire with flickering flames and rising embers
// ============================================================================

@Composable
fun NightCampfireMeteorsFullscreenScene(
    modifier: Modifier = Modifier
) {
    val t = rememberContinuousTimeSeconds()

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Deep violet-indigo night sky gradient matching image_aea65e.png
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF24134E),
                    Color(0xFF2D1963),
                    Color(0xFF392373),
                    Color(0xFF2A1D54)
                )
            )
        )

        // Glittering stars (both circular stars and 4-point diamond sparkles)
        for (s in 0 until 32) {
            val sx = ((s * 67 + 11) % 94 + 3) / 100f * w
            val sy = ((s * 37 + 5) % 44 + 3) / 100f * h
            val twinkle = 0.35f + 0.65f * abs(sin(t * (1.6f + (s % 5) * 0.4f) + s * 0.9f))
            if (s % 5 == 0) {
                drawFourPointSparkle(
                    center = Offset(sx, sy),
                    radius = (6f + (s % 3) * 2.5f) * (0.7f + 0.3f * twinkle),
                    color = Color.White.copy(alpha = twinkle)
                )
            } else {
                drawCircle(
                    color = Color.White.copy(alpha = twinkle),
                    radius = 2.2f + (s % 3) * 0.9f,
                    center = Offset(sx, sy)
                )
            }
        }

        // Crescent Half-Moon with soft atmospheric halo
        val moonCenter = Offset(w * 0.44f, h * 0.155f)
        val moonRadius = min(w, h) * 0.085f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x44FFF9C4),
                    Color(0x18FFF9C4),
                    Color.Transparent
                ),
                center = moonCenter,
                radius = moonRadius * 2.6f
            ),
            radius = moonRadius * 2.6f,
            center = moonCenter
        )
        drawCrescentMoon(
            center = moonCenter,
            radius = moonRadius,
            color = Color(0xFFFFFFFF),
            shadowColor = Color(0xFF28165B)
        )

        // Periodic Shooting Meteors streaking across the night sky
        for (m in 0..1) {
            val meteorPeriod = 5.5f
            val localClock = (t + m * 2.75f) % meteorPeriod
            if (localClock < 1.15f) {
                val p = localClock / 1.15f
                val startX = w * (0.82f - m * 0.25f)
                val startY = h * (0.07f + m * 0.08f)
                val headX = startX - p * (w * 0.45f)
                val headY = startY + p * (h * 0.24f)
                val tailLenX = w * 0.14f * (1f - p * 0.4f)
                val tailLenY = h * 0.075f * (1f - p * 0.4f)
                val alpha = sin(p * PI.toFloat())

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = alpha),
                            Color(0xFF80D8FF).copy(alpha = alpha * 0.5f),
                            Color.Transparent
                        ),
                        start = Offset(headX, headY),
                        end = Offset(headX + tailLenX, headY - tailLenY)
                    ),
                    start = Offset(headX, headY),
                    end = Offset(headX + tailLenX, headY - tailLenY),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
                drawFourPointSparkle(
                    center = Offset(headX, headY),
                    radius = 7f * alpha,
                    color = Color.White.copy(alpha = alpha)
                )
            }
        }

        // Slow drifting flat-bottomed night clouds matching image_aea65e.png
        val cloud1X = ((w * 0.08f + t * 8f) % (w * 1.3f)) - w * 0.15f
        val cloud2X = ((w * 0.34f + t * 11f) % (w * 1.3f)) - w * 0.15f
        val cloud3X = ((w * 0.85f + t * 6.5f) % (w * 1.3f)) - w * 0.15f
        drawFlatNightCloud(Offset(cloud1X, h * 0.19f), w * 0.28f, h * 0.038f, Color(0xFF6B5B9E).copy(alpha = 0.72f))
        drawFlatNightCloud(Offset(cloud2X, h * 0.31f), w * 0.24f, h * 0.032f, Color(0xFF6B5B9E).copy(alpha = 0.68f))
        drawFlatNightCloud(Offset(cloud3X, h * 0.26f), w * 0.18f, h * 0.025f, Color(0xFF6B5B9E).copy(alpha = 0.62f))

        // Layered Purple Mountain Peaks in the background
        val leftMountain = Path().apply {
            moveTo(0f, h * 0.41f)
            lineTo(w * 0.48f, h * 0.62f)
            lineTo(0f, h * 0.72f)
            close()
        }
        drawPath(leftMountain, color = Color(0xFF42285C))

        val rightMountain = Path().apply {
            moveTo(w * 0.35f, h * 0.72f)
            lineTo(w * 0.68f, h * 0.43f)
            lineTo(w * 0.76f, h * 0.47f)
            lineTo(w * 0.96f, h * 0.33f)
            lineTo(w, h * 0.36f)
            lineTo(w, h * 0.72f)
            close()
        }
        drawPath(rightMountain, color = Color(0xFF35204F))

        // Mountain ridge highlight facets
        val ridgeHighlight = Path().apply {
            moveTo(w * 0.68f, h * 0.43f)
            lineTo(w * 0.72f, h * 0.45f)
            lineTo(w * 0.56f, h * 0.65f)
            lineTo(w * 0.46f, h * 0.65f)
            close()
        }
        drawPath(ridgeHighlight, color = Color(0xFF4D326E))

        // Pine tree silhouettes along the mountain slope
        val pinePositions = listOf(
            Triple(0.05f, 0.51f, 0.065f),
            Triple(0.13f, 0.53f, 0.060f),
            Triple(0.18f, 0.54f, 0.055f),
            Triple(0.28f, 0.55f, 0.052f),
            Triple(0.50f, 0.59f, 0.055f),
            Triple(0.54f, 0.60f, 0.048f)
        )
        pinePositions.forEach { (px, py, ph) ->
            drawPineTreeSilhouette(
                base = Offset(w * px, h * py),
                treeHeight = h * ph,
                treeWidth = w * 0.036f,
                color = Color(0xFF15242C)
            )
        }

        // Mossy green campsite ground
        val groundTopY = h * 0.71f
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF3A5236),
                    Color(0xFF4B683B),
                    Color(0xFF33492B)
                ),
                startY = groundTopY,
                endY = h
            ),
            topLeft = Offset(0f, groundTopY),
            size = Size(w, h - groundTopY)
        )

        // Warm pulsing firelight glow on the ground around the campfire
        val fireCenter = Offset(w * 0.62f, h * 0.825f)
        val firePulse = 1f + 0.07f * sin(t * 7.5f) + 0.04f * cos(t * 13f)
        drawOval(
            color = Color(0x44FFB74D),
            topLeft = Offset(
                fireCenter.x - w * 0.25f * firePulse,
                fireCenter.y - h * 0.075f * firePulse
            ),
            size = Size(w * 0.50f * firePulse, h * 0.16f * firePulse)
        )
        drawOval(
            color = Color(0x55FFA726),
            topLeft = Offset(
                fireCenter.x - w * 0.16f * firePulse,
                fireCenter.y - h * 0.045f * firePulse
            ),
            size = Size(w * 0.32f * firePulse, h * 0.105f * firePulse)
        )

        // Glowing Yellow-Orange Camping Tent on the left
        val tentApex = Offset(w * 0.295f, h * 0.555f)
        val tentLeft = Offset(w * -0.02f, h * 0.74f)
        val tentFrontCenter = Offset(w * 0.225f, h * 0.79f)
        val tentRight = Offset(w * 0.46f, h * 0.735f)

        // Tent side slope (warm golden yellow)
        val tentSide = Path().apply {
            moveTo(tentLeft.x, tentLeft.y)
            lineTo(tentApex.x, tentApex.y)
            lineTo(tentFrontCenter.x, tentFrontCenter.y)
            lineTo(w * -0.02f, h * 0.775f)
            close()
        }
        drawPath(tentSide, color = Color(0xFFE6A12E))

        // Tent front gable
        val tentFront = Path().apply {
            moveTo(tentFrontCenter.x, tentFrontCenter.y)
            lineTo(tentApex.x, tentApex.y)
            lineTo(tentRight.x, tentRight.y)
            close()
        }
        drawPath(tentFront, color = Color(0xFFF5B841))

        // Tent dark interior doorway flap
        val tentDoor = Path().apply {
            moveTo(w * 0.265f, h * 0.78f)
            lineTo(tentApex.x, h * 0.585f)
            lineTo(w * 0.39f, h * 0.752f)
            close()
        }
        drawPath(tentDoor, color = Color(0xFF1A1E36))

        // Tent guy-line rope & peg
        drawLine(
            color = Color(0xFFD7CCC8).copy(alpha = 0.75f),
            start = tentApex,
            end = Offset(w * 0.545f, h * 0.765f),
            strokeWidth = 2f
        )

        // Backpack resting beside the tent
        drawRoundRect(
            color = Color(0xFF332940),
            topLeft = Offset(w * 0.095f, h * 0.765f),
            size = Size(w * 0.105f, h * 0.082f),
            cornerRadius = CornerRadius(18f, 18f)
        )
        drawRoundRect(
            color = Color(0xFF4A3B5C),
            topLeft = Offset(w * 0.102f, h * 0.762f),
            size = Size(w * 0.09f, h * 0.035f),
            cornerRadius = CornerRadius(14f, 14f)
        )

        // Rolled sleeping mats on the right of the fire
        drawRoundRect(
            color = Color(0xFF423A5A),
            topLeft = Offset(w * 0.72f, h * 0.755f),
            size = Size(w * 0.19f, h * 0.048f),
            cornerRadius = CornerRadius(24f, 24f)
        )
        drawRoundRect(
            color = Color(0xFF9C7A54),
            topLeft = Offset(w * 0.83f, h * 0.818f),
            size = Size(w * 0.17f, h * 0.052f),
            cornerRadius = CornerRadius(26f, 26f)
        )

        // Campfire logs & circle of stones
        drawLine(
            color = Color(0xFF6D3B24),
            start = Offset(fireCenter.x - w * 0.065f, fireCenter.y + h * 0.015f),
            end = Offset(fireCenter.x + w * 0.065f, fireCenter.y - h * 0.012f),
            strokeWidth = 14f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF5D2F1B),
            start = Offset(fireCenter.x - w * 0.055f, fireCenter.y - h * 0.012f),
            end = Offset(fireCenter.x + w * 0.055f, fireCenter.y + h * 0.015f),
            strokeWidth = 14f,
            cap = StrokeCap.Round
        )

        // Animated 3-layer flickering campfire flames
        drawFlickeringCampfire(
            baseCenter = Offset(fireCenter.x, fireCenter.y + h * 0.008f),
            flameWidth = w * 0.15f,
            flameHeight = h * 0.135f,
            time = t
        )

        // Ring of stones around the campfire base
        val stoneOffsets = listOf(
            Offset(-0.11f, 0.005f),
            Offset(-0.085f, 0.022f),
            Offset(-0.04f, 0.032f),
            Offset(0.015f, 0.034f),
            Offset(0.068f, 0.026f),
            Offset(0.105f, 0.010f)
        )
        stoneOffsets.forEachIndexed { idx, (ox, oy) ->
            drawOval(
                color = if (idx % 2 == 0) Color(0xFF7B6B63) else Color(0xFF62544E),
                topLeft = Offset(fireCenter.x + w * ox - w * 0.028f, fireCenter.y + h * oy - h * 0.014f),
                size = Size(w * 0.056f, h * 0.028f)
            )
        }

        // Rising glowing fire sparks / embers + soft smoke plume
        for (e in 0 until 14) {
            val eProg = ((t * 0.55f + e * 0.075f) % 1f)
            val ex = fireCenter.x + sin(t * 3.2f + e * 1.7f) * (w * 0.045f * eProg)
            val ey = fireCenter.y - h * 0.05f - eProg * (h * 0.19f)
            val eAlpha = (1f - eProg) * 0.9f
            drawCircle(
                color = if (e % 2 == 0) Color(0xFFFFD54F).copy(alpha = eAlpha)
                else Color(0xFFFF7043).copy(alpha = eAlpha),
                radius = (1f - eProg * 0.5f) * 3.8f,
                center = Offset(ex, ey)
            )
        }
    }
}

private fun DrawScope.drawFlickeringCampfire(
    baseCenter: Offset,
    flameWidth: Float,
    flameHeight: Float,
    time: Float
) {
    val sway1 = sin(time * 6.5f) * (flameWidth * 0.14f)
    val sway2 = cos(time * 8.2f + 1.1f) * (flameWidth * 0.12f)
    val stretch = 1f + 0.09f * sin(time * 9.5f)

    // Outer orange-red flame
    val outerFlame = Path().apply {
        moveTo(baseCenter.x - flameWidth * 0.5f, baseCenter.y)
        cubicTo(
            baseCenter.x - flameWidth * 0.62f, baseCenter.y - flameHeight * 0.45f,
            baseCenter.x - flameWidth * 0.15f + sway1, baseCenter.y - flameHeight * 0.85f * stretch,
            baseCenter.x + sway1 * 0.8f, baseCenter.y - flameHeight * stretch
        )
        cubicTo(
            baseCenter.x + flameWidth * 0.25f + sway2, baseCenter.y - flameHeight * 0.78f * stretch,
            baseCenter.x + flameWidth * 0.62f, baseCenter.y - flameHeight * 0.40f,
            baseCenter.x + flameWidth * 0.5f, baseCenter.y
        )
        close()
    }
    drawPath(outerFlame, color = Color(0xFFFF6D00))

    // Middle golden-amber flame
    val midFlame = Path().apply {
        moveTo(baseCenter.x - flameWidth * 0.36f, baseCenter.y)
        cubicTo(
            baseCenter.x - flameWidth * 0.42f, baseCenter.y - flameHeight * 0.35f,
            baseCenter.x - flameWidth * 0.10f + sway2, baseCenter.y - flameHeight * 0.68f * stretch,
            baseCenter.x + sway2 * 0.6f, baseCenter.y - flameHeight * 0.78f * stretch
        )
        cubicTo(
            baseCenter.x + flameWidth * 0.18f + sway1, baseCenter.y - flameHeight * 0.60f * stretch,
            baseCenter.x + flameWidth * 0.42f, baseCenter.y - flameHeight * 0.32f,
            baseCenter.x + flameWidth * 0.36f, baseCenter.y
        )
        close()
    }
    drawPath(midFlame, color = Color(0xFFFFAB00))

    // Inner bright yellow-white core flame
    val coreFlame = Path().apply {
        moveTo(baseCenter.x - flameWidth * 0.20f, baseCenter.y)
        quadraticBezierTo(
            baseCenter.x - flameWidth * 0.22f + sway1 * 0.4f, baseCenter.y - flameHeight * 0.38f,
            baseCenter.x + sway1 * 0.3f, baseCenter.y - flameHeight * 0.52f * stretch
        )
        quadraticBezierTo(
            baseCenter.x + flameWidth * 0.22f + sway2 * 0.4f, baseCenter.y - flameHeight * 0.35f,
            baseCenter.x + flameWidth * 0.20f, baseCenter.y
        )
        close()
    }
    drawPath(coreFlame, color = Color(0xFFFFF59D))
}

// ============================================================================
// SCENE 6: OCEAN WAVES & SAILBOAT WITH CALM, CLOUDY, WINDY & SUNNY WEATHERS
// - Inspired by uploaded images (image_aeaa80.jpg, image_aeaa97.png, image_aeaa99.png)
// - Multi-layered rolling cyan/blue ocean waves with white foam ribbons &
//   translucent water bubbles
// - Sailboat that dynamically rides the exact wave surface height & slope angle
// - Distant coastal lighthouse on a rocky headland + gliding seagulls
// - Smoothly transitions across Sunny/Calm, Cloudy/Peaceful, and Windy Swell
// ============================================================================

@Composable
fun OceanWaveBoatFullscreenScene(
    weatherMode: WaveWeatherState = WaveWeatherState.AUTO_CYCLE,
    modifier: Modifier = Modifier
) {
    val t = rememberContinuousTimeSeconds()

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Determine active weather state (0 = Sunny Calm, 1 = Cloudy Peaceful, 2 = Windy Swell)
        val weatherCycle = when (weatherMode) {
            WaveWeatherState.SUNNY_CALM -> 0f
            WaveWeatherState.CLOUDY_PEACEFUL -> 1f
            WaveWeatherState.WINDY_SWELL -> 2f
            WaveWeatherState.AUTO_CYCLE -> (t * 0.08f) % 3f
        }
        val wIdx = floor(weatherCycle).toInt() % 3
        val wFrac = weatherCycle - floor(weatherCycle)
        val nextWIdx = (wIdx + 1) % 3

        val skyTops = listOf(
            Color(0xFF64B5F6), // Sunny Calm
            Color(0xFF78909C), // Cloudy Peaceful
            Color(0xFF455A64)  // Windy Swell
        )
        val skyBottoms = listOf(
            Color(0xFFE1F5FE),
            Color(0xFFCFD8DC),
            Color(0xFF90A4AE)
        )
        val waveSpeedMultipliers = listOf(1.0f, 0.85f, 1.65f)
        val waveAmpMultipliers = listOf(0.85f, 0.75f, 1.35f)

        val skyTop = lerpColor(skyTops[wIdx], skyTops[nextWIdx], wFrac)
        val skyBottom = lerpColor(skyBottoms[wIdx], skyBottoms[nextWIdx], wFrac)
        val speedMul = waveSpeedMultipliers[wIdx] * (1f - wFrac) + waveSpeedMultipliers[nextWIdx] * wFrac
        val ampMul = waveAmpMultipliers[wIdx] * (1f - wFrac) + waveAmpMultipliers[nextWIdx] * wFrac

        // Sky background
        drawRect(
            brush = Brush.verticalGradient(colors = listOf(skyTop, skyBottom))
        )

        // Warm sun in Sunny/Calm weather
        val sunVis = if (wIdx == 0) 1f - wFrac * 0.6f else if (nextWIdx == 0) wFrac else 0.25f
        if (sunVis > 0.05f) {
            val sunPos = Offset(w * 0.20f, h * 0.16f)
            drawCircle(
                color = Color(0xFFFFF59D).copy(alpha = 0.35f * sunVis),
                radius = min(w, h) * 0.12f,
                center = sunPos
            )
            drawCircle(
                color = Color(0xFFFFEB3B).copy(alpha = 0.9f * sunVis),
                radius = min(w, h) * 0.068f,
                center = sunPos
            )
        }

        // Layered clouds (soft white in Sunny, fuller grey-white in Cloudy/Windy)
        val cloudShade = lerpColor(
            Color.White,
            Color(0xFFB0BEC5),
            if (wIdx == 0) 0.15f else 0.55f
        )
        for (c in 0 until 4) {
            val cx = ((w * (0.15f + c * 0.28f) + t * (12f + c * 4f) * speedMul) % (w * 1.4f)) - w * 0.2f
            val cy = h * (0.11f + (c % 2) * 0.07f)
            drawDriftingCloudCluster(
                cx = cx,
                cy = cy,
                scale = w * (0.00125f + (c % 2) * 0.0003f),
                color = cloudShade.copy(alpha = 0.9f)
            )
        }

        // Gliding Seagulls in the distance (inspired by image_aeaa80.jpg)
        for (bird in 0 until 7) {
            val bx = ((w * (0.30f + bird * 0.08f) + t * (14f + bird * 2f)) % (w * 0.9f)) + w * 0.05f
            val by = h * (0.24f + (bird % 4) * 0.055f) + sin(t * 2.2f + bird) * 6f
            val wingFlap = sin(t * 5.5f + bird * 1.3f) * 6f
            val birdScale = 0.7f + (bird % 3) * 0.22f
            val gullPath = Path().apply {
                moveTo(bx - 12f * birdScale, by + wingFlap * birdScale)
                quadraticBezierTo(bx - 5f * birdScale, by - 6f * birdScale, bx, by)
                quadraticBezierTo(bx + 5f * birdScale, by - 6f * birdScale, bx + 12f * birdScale, by + wingFlap * birdScale)
            }
            drawPath(
                path = gullPath,
                color = Color.White.copy(alpha = 0.85f),
                style = Stroke(width = 2.2f * birdScale, cap = StrokeCap.Round)
            )
        }

        // Distant Rocky Headland & Lighthouse on the right (from image_aeaa80.jpg)
        val cliffBaseY = h * 0.60f
        val lighthouseX = w * 0.88f
        val towerBottomY = h * 0.53f
        val towerTopY = h * 0.30f

        // Lighthouse tower
        val towerPath = Path().apply {
            moveTo(lighthouseX - w * 0.028f, towerBottomY)
            lineTo(lighthouseX - w * 0.018f, towerTopY)
            lineTo(lighthouseX + w * 0.018f, towerTopY)
            lineTo(lighthouseX + w * 0.028f, towerBottomY)
            close()
        }
        drawPath(towerPath, color = Color(0xFFECEFF1))
        // Lantern room & warm glowing beacon
        drawRect(
            color = Color(0xFF455A64),
            topLeft = Offset(lighthouseX - w * 0.020f, towerTopY - h * 0.032f),
            size = Size(w * 0.040f, h * 0.032f)
        )
        val beaconGlow = 0.65f + 0.35f * sin(t * 3.2f)
        drawCircle(
            color = Color(0xFFFFB300).copy(alpha = beaconGlow),
            radius = min(w, h) * 0.014f,
            center = Offset(lighthouseX, towerTopY - h * 0.016f)
        )
        // Lighthouse roof cap
        val capPath = Path().apply {
            moveTo(lighthouseX - w * 0.024f, towerTopY - h * 0.032f)
            lineTo(lighthouseX, towerTopY - h * 0.052f)
            lineTo(lighthouseX + w * 0.024f, towerTopY - h * 0.032f)
            close()
        }
        drawPath(capPath, color = Color(0xFF263238))

        // Keeper's cottage & rocky cliff base
        drawRect(
            color = Color(0xFFCFD8DC),
            topLeft = Offset(lighthouseX + w * 0.01f, towerBottomY - h * 0.055f),
            size = Size(w * 0.085f, h * 0.055f)
        )
        val cliffPath = Path().apply {
            moveTo(w * 0.72f, cliffBaseY)
            quadraticBezierTo(w * 0.76f, h * 0.51f, w * 0.85f, h * 0.51f)
            lineTo(w, h * 0.50f)
            lineTo(w, cliffBaseY)
            close()
        }
        drawPath(cliffPath, color = Color(0xFF455A64))

        // 4 Layered Rolling Ocean Waves (combining image_aeaa97.png & image_aeaa99.png)
        // Wave Layer 1 (Back pale sky-blue swell)
        drawSineWaveLayer(
            baseY = h * 0.57f,
            amplitude = h * 0.022f * ampMul,
            wavelength = w * 0.52f,
            phase = t * 1.2f * speedMul,
            color = Color(0xFF81D4FA),
            foamColor = null
        )

        // Wave Layer 2 (Mid-back turquoise swell — the Sailboat rides on this wave!)
        val boatWaveBaseY = h * 0.63f
        val boatWaveAmp = h * 0.030f * ampMul
        val boatWaveLen = w * 0.48f
        val boatWavePhase = -t * 1.65f * speedMul

        // Compute exact wave height and tangent slope angle at the boat's X coordinate
        val boatX = w * 0.26f + sin(t * 0.55f) * (w * 0.04f)
        val k = (2.0 * PI / boatWaveLen).toFloat()
        val angleArg = k * boatX + boatWavePhase
        val boatWaveY = boatWaveBaseY + sin(angleArg) * boatWaveAmp +
                sin(angleArg * 1.8f + 1.2f) * (boatWaveAmp * 0.25f)
        val dYdx = cos(angleArg) * boatWaveAmp * k +
                cos(angleArg * 1.8f + 1.2f) * (boatWaveAmp * 0.25f * 1.8f * k)
        val boatTiltDeg = (atan2(dYdx, 1f) * (180.0 / PI)).toFloat().coerceIn(-18f, 18f)

        // Draw the Sailboat pitching and bobbing realistically with the waves
        drawWaveRidingSailboat(
            pivot = Offset(boatX, boatWaveY + h * 0.008f),
            tiltDegrees = boatTiltDeg,
            scale = min(w, h) * 0.0016f,
            windIntensity = ampMul,
            time = t
        )

        // Draw Wave Layer 2 over the bottom of the boat hull so the boat sits naturally in the water
        drawSineWaveLayer(
            baseY = boatWaveBaseY,
            amplitude = boatWaveAmp,
            wavelength = boatWaveLen,
            phase = boatWavePhase,
            color = Color(0xFF29B6F6),
            foamColor = Color.White.copy(alpha = 0.55f)
        )

        // Wave Layer 3 (Mid-front rich cyan-blue wave)
        drawSineWaveLayer(
            baseY = h * 0.73f,
            amplitude = h * 0.036f * ampMul,
            wavelength = w * 0.56f,
            phase = t * 1.95f * speedMul + 1.4f,
            color = Color(0xFF0288D1),
            foamColor = Color.White.copy(alpha = 0.72f)
        )

        // Wave Layer 4 (Foreground deep ocean blue wave with sparkling foam bubbles from image_aeaa99.png)
        val frontWaveBaseY = h * 0.83f
        val frontWaveAmp = h * 0.038f * ampMul
        val frontWaveLen = w * 0.62f
        val frontWavePhase = -t * 2.25f * speedMul + 2.5f

        drawSineWaveLayer(
            baseY = frontWaveBaseY,
            amplitude = frontWaveAmp,
            wavelength = frontWaveLen,
            phase = frontWavePhase,
            color = Color(0xFF01579B),
            foamColor = Color.White.copy(alpha = 0.85f)
        )

        // Sparkling sea-foam bubbles & white water swirls inside the foreground waves (image_aeaa99.png)
        for (b in 0 until 26) {
            val bx = ((w * ((b * 43) % 100 / 100f) + t * (28f + (b % 5) * 8f) * speedMul) % (w * 1.1f)) - w * 0.05f
            val localWaveY = frontWaveBaseY + sin((2f * PI.toFloat() / frontWaveLen) * bx + frontWavePhase) * frontWaveAmp
            val by = localWaveY + h * 0.025f + (b % 6) * (h * 0.022f) + sin(t * 3f + b) * 5f
            val br = (4f + (b % 4) * 3.8f)

            drawCircle(
                color = Color.White.copy(alpha = 0.28f),
                radius = br,
                center = Offset(bx, by)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.78f),
                radius = br,
                center = Offset(bx, by),
                style = Stroke(width = 1.6f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = br * 0.28f,
                center = Offset(bx - br * 0.3f, by - br * 0.3f)
            )
        }
    }
}

private fun DrawScope.drawSineWaveLayer(
    baseY: Float,
    amplitude: Float,
    wavelength: Float,
    phase: Float,
    color: Color,
    foamColor: Color?
) {
    val w = size.width
    val h = size.height
    val k = (2.0 * PI / wavelength).toFloat()
    val steps = 64
    val dx = w / steps

    val wavePath = Path().apply {
        moveTo(0f, h)
        for (i in 0..steps) {
            val x = i * dx
            val y = baseY + sin(k * x + phase) * amplitude +
                    sin(k * x * 1.8f + phase + 1.2f) * (amplitude * 0.25f)
            lineTo(x, y)
        }
        lineTo(w, h)
        close()
    }
    drawPath(wavePath, color = color)

    if (foamColor != null) {
        val crestPath = Path().apply {
            for (i in 0..steps) {
                val x = i * dx
                val y = baseY + sin(k * x + phase) * amplitude +
                        sin(k * x * 1.8f + phase + 1.2f) * (amplitude * 0.25f)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(
            path = crestPath,
            color = foamColor,
            style = Stroke(width = 4.5f, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawWaveRidingSailboat(
    pivot: Offset,
    tiltDegrees: Float,
    scale: Float,
    windIntensity: Float,
    time: Float
) {
    withTransform({
        translate(left = pivot.x, top = pivot.y)
        rotate(degrees = tiltDegrees, pivot = Offset.Zero)
    }) {
        // Mast
        val mastHeight = 165f * scale
        drawLine(
            color = Color(0xFF455A64),
            start = Offset(0f, 0f),
            end = Offset(0f, -mastHeight),
            strokeWidth = 4.5f * scale,
            cap = StrokeCap.Round
        )

        val billow = (12f + 6f * sin(time * 3.2f)) * windIntensity * scale

        // Left Rear Mainsail
        val leftSail = Path().apply {
            moveTo(-4f * scale, -mastHeight * 0.95f)
            quadraticBezierTo(
                -28f * scale + billow * 0.5f, -mastHeight * 0.50f,
                -46f * scale, -18f * scale
            )
            lineTo(-4f * scale, -18f * scale)
            close()
        }
        drawPath(leftSail, color = Color(0xFFCFD8DC))

        // Right Front Jib Sail billowing in the breeze
        val rightSail = Path().apply {
            moveTo(3f * scale, -mastHeight * 0.92f)
            quadraticBezierTo(
                44f * scale + billow, -mastHeight * 0.45f,
                52f * scale + billow * 0.4f, -15f * scale
            )
            quadraticBezierTo(
                24f * scale, -12f * scale,
                3f * scale, -15f * scale
            )
            close()
        }
        drawPath(rightSail, color = Color.White)

        // Boat Hull
        val hullPath = Path().apply {
            moveTo(-58f * scale, -10f * scale)
            quadraticBezierTo(0f, -4f * scale, 62f * scale, -12f * scale)
            lineTo(46f * scale, 16f * scale)
            quadraticBezierTo(0f, 22f * scale, -48f * scale, 14f * scale)
            close()
        }
        drawPath(hullPath, color = Color(0xFF37474F))
        // Hull accent stripe
        drawLine(
            color = Color(0xFFECEFF1),
            start = Offset(-50f * scale, -4f * scale),
            end = Offset(52f * scale, -5f * scale),
            strokeWidth = 3f * scale,
            cap = StrokeCap.Round
        )
    }
}

// ============================================================================
// SHARED DRAWING HELPERS FOR CLOUDS, STARS, FACES & TREES
// ============================================================================

private fun DrawScope.drawSleepingCatFace(
    center: Offset,
    scale: Float,
    strokeColor: Color
) {
    val sw = max(2.5f, 3.2f * scale)
    // Closed peaceful U-shaped eyes
    drawArc(
        color = strokeColor,
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(center.x - 16f * scale, center.y - 8f * scale),
        size = Size(12f * scale, 10f * scale),
        style = Stroke(width = sw, cap = StrokeCap.Round)
    )
    drawArc(
        color = strokeColor,
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(center.x + 4f * scale, center.y - 8f * scale),
        size = Size(12f * scale, 10f * scale),
        style = Stroke(width = sw, cap = StrokeCap.Round)
    )
    // Cute nose & inverted-Y mouth
    drawLine(
        color = strokeColor,
        start = Offset(center.x, center.y + 2f * scale),
        end = Offset(center.x, center.y + 14f * scale),
        strokeWidth = sw,
        cap = StrokeCap.Round
    )
    drawLine(
        color = strokeColor,
        start = Offset(center.x, center.y + 14f * scale),
        end = Offset(center.x - 6f * scale, center.y + 19f * scale),
        strokeWidth = sw,
        cap = StrokeCap.Round
    )
    drawLine(
        color = strokeColor,
        start = Offset(center.x, center.y + 14f * scale),
        end = Offset(center.x + 6f * scale, center.y + 19f * scale),
        strokeWidth = sw,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawPuffyCloudBank(
    baseY: Float,
    color: Color,
    driftOffset: Float,
    puffs: List<Triple<Float, Float, Float>>
) {
    val w = size.width
    val h = size.height
    drawRect(
        color = color,
        topLeft = Offset(0f, baseY - h * 0.12f),
        size = Size(w, h * 0.20f)
    )
    puffs.forEach { (xFrac, yFrac, rFrac) ->
        drawCircle(
            color = color,
            radius = min(w, h) * rFrac,
            center = Offset(w * xFrac + driftOffset, h * yFrac)
        )
    }
}

private fun DrawScope.drawDriftingCloudCluster(
    cx: Float,
    cy: Float,
    scale: Float,
    color: Color
) {
    val w = 130f * scale
    val h = 44f * scale
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - w * 0.5f, cy),
        size = Size(w, h),
        cornerRadius = CornerRadius(h * 0.5f, h * 0.5f)
    )
    drawCircle(
        color = color,
        radius = 34f * scale,
        center = Offset(cx - 24f * scale, cy - 4f * scale)
    )
    drawCircle(
        color = color,
        radius = 42f * scale,
        center = Offset(cx + 12f * scale, cy - 12f * scale)
    )
    drawCircle(
        color = color,
        radius = 28f * scale,
        center = Offset(cx + 42f * scale, cy + 2f * scale)
    )
}

private fun DrawScope.drawFlatNightCloud(
    center: Offset,
    width: Float,
    height: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x - width * 0.5f, center.y + height * 0.5f)
        quadraticBezierTo(
            center.x - width * 0.42f, center.y - height * 0.4f,
            center.x - width * 0.22f, center.y - height * 0.2f
        )
        quadraticBezierTo(
            center.x - width * 0.08f, center.y - height * 0.95f,
            center.x + width * 0.12f, center.y - height * 0.25f
        )
        quadraticBezierTo(
            center.x + width * 0.28f, center.y - height * 0.65f,
            center.x + width * 0.5f, center.y + height * 0.5f
        )
        close()
    }
    drawPath(path, color = color)
}

private fun DrawScope.drawCrescentMoon(
    center: Offset,
    radius: Float,
    color: Color,
    shadowColor: Color
) {
    drawCircle(
        color = color,
        radius = radius,
        center = center
    )
    drawCircle(
        color = shadowColor,
        radius = radius * 0.88f,
        center = Offset(center.x + radius * 0.42f, center.y - radius * 0.18f)
    )
}

private fun DrawScope.drawFourPointSparkle(
    center: Offset,
    radius: Float,
    color: Color
) {
    val inner = radius * 0.24f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + inner, center.y - inner)
        lineTo(center.x + radius, center.y)
        lineTo(center.x + inner, center.y + inner)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - inner, center.y + inner)
        lineTo(center.x - radius, center.y)
        lineTo(center.x - inner, center.y - inner)
        close()
    }
    drawPath(path, color = color)
}

private fun DrawScope.drawFivePointStar(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    color: Color
) {
    val path = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (i * 36.0 - 90.0) * PI / 180.0
        val px = center.x + cos(angle).toFloat() * r
        val py = center.y + sin(angle).toFloat() * r
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, color = color)
}

private fun DrawScope.drawPineTreeSilhouette(
    base: Offset,
    treeHeight: Float,
    treeWidth: Float,
    color: Color
) {
    for (tier in 0..2) {
        val tierBottomY = base.y - tier * (treeHeight * 0.26f)
        val tierTopY = tierBottomY - treeHeight * 0.48f
        val halfW = treeWidth * (0.55f - tier * 0.11f)
        val path = Path().apply {
            moveTo(base.x - halfW, tierBottomY)
            lineTo(base.x, tierTopY)
            lineTo(base.x + halfW, tierBottomY)
            close()
        }
        drawPath(path, color = color)
    }
}

private fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (stop.red - start.red) * f,
        green = start.green + (stop.green - start.green) * f,
        blue = start.blue + (stop.blue - start.blue) * f,
        alpha = start.alpha + (stop.alpha - start.alpha) * f
    )
}