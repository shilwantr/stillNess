package com.example.ui.screens

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.glass.LocalLiquidState
import com.example.ui.glass.liquidGlassCardShader
import com.example.ui.theme.CyanBright
import io.github.fletchmckee.liquid.liquid
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val GoldAccent = Color(0xFFD4AF5A)

enum class StillMiniGame(val tabTitle: String) {
    REFLEX("REFLEX"),
    GAME_2048("2048"),
    BURST("BURST")
}

@Composable
private fun Modifier.liquidGlassEffect(
    shape: Shape,
    cornerRadius: Dp,
    frost: Dp = 0.dp,
    refraction: Float = 0.16f,
    curve: Float = 0.55f,
    dispersion: Float = 0.03f,
    saturation: Float = 1.35f,
    tint: Color = Color.White.copy(alpha = 0.04f)
): Modifier {
    val liquidState = LocalLiquidState.current
    return if (liquidState != null) {
        this.liquid(liquidState) {
            this.shape = shape
            this.frost = frost
            this.refraction = refraction
            this.curve = curve
            this.edge = 0.0f
            this.tint = tint
            this.saturation = saturation
            this.dispersion = dispersion
        }
    } else {
        this.liquidGlassCardShader(
            cornerRadius = cornerRadius,
            refractionAmount = (refraction * 42f).dp,
            aberration = (dispersion * 5f).coerceIn(0.1f, 0.45f)
        )
    }
}

@Composable
fun StillGamesScreen(
    modifier: Modifier = Modifier
) {
    var selectedGame by remember { mutableStateOf(StillMiniGame.REFLEX) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StillGameTopBar(
            selectedGame = selectedGame,
            onGameSelected = { selectedGame = it }
        )

        val arenaShape = RoundedCornerShape(34.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .liquidGlassEffect(
                    shape = arenaShape,
                    cornerRadius = 34.dp,
                    frost = 2.dp,
                    refraction = 0.12f,
                    curve = 0.48f,
                    dispersion = 0.02f
                )
                .clip(arenaShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.02f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = selectedGame,
                transitionSpec = {
                    fadeIn(tween(260)) togetherWith fadeOut(tween(200))
                },
                label = "gameSwitcher"
            ) { game ->
                when (game) {
                    StillMiniGame.REFLEX -> ReflexCoreGame()
                    StillMiniGame.GAME_2048 -> TileShift2048Game()
                    StillMiniGame.BURST -> BubbleBurstGame()
                }
            }
        }
    }
}

// ============================================================================
// TOP SEGMENTED LIQUID GLASS BAR
// ============================================================================
@Composable
private fun StillGameTopBar(
    selectedGame: StillMiniGame,
    onGameSelected: (StillMiniGame) -> Unit
) {
    val tabBounds = remember { mutableStateMapOf<StillMiniGame, Rect>() }
    val bubbleLeft = remember { Animatable(0f) }
    val bubbleRight = remember { Animatable(0f) }
    val isInitialized = remember { mutableStateOf(false) }

    val targetRect = tabBounds[selectedGame]

    LaunchedEffect(targetRect) {
        if (targetRect == null) return@LaunchedEffect
        if (!isInitialized.value) {
            bubbleLeft.snapTo(targetRect.left)
            bubbleRight.snapTo(targetRect.right)
            isInitialized.value = true
        } else {
            val movingRight = targetRect.left >= bubbleLeft.value
            val spec = spring<Float>(dampingRatio = 0.52f, stiffness = 55f)
            if (movingRight) {
                launch { bubbleRight.animateTo(targetRect.right, spec) }
                launch {
                    delay(65.milliseconds)
                    bubbleLeft.animateTo(targetRect.left, spec)
                }
            } else {
                launch { bubbleLeft.animateTo(targetRect.left, spec) }
                launch {
                    delay(65.milliseconds)
                    bubbleRight.animateTo(targetRect.right, spec)
                }
            }
        }
    }

    val barShape = RoundedCornerShape(28.dp)
    val pillShape = RoundedCornerShape(50.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassEffect(
                shape = barShape,
                cornerRadius = 28.dp,
                refraction = 0.12f,
                curve = 0.50f
            )
            .clip(barShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.05f),
                        Color.White.copy(alpha = 0.02f)
                    )
                )
            )
            .padding(6.dp),
        contentAlignment = Alignment.TopStart
    ) {
        if (targetRect != null && isInitialized.value) {
            Box(
                modifier = Modifier
                    .layout { measurable, _ ->
                        val left = bubbleLeft.value
                        val right = bubbleRight.value
                        val currentWidth = (right - left).coerceAtLeast(1f)
                        val baseWidth = targetRect.width.coerceAtLeast(1f)
                        val stretch = ((currentWidth - baseWidth) / baseWidth).coerceIn(-0.15f, 1.8f)
                        val squash = stretch * 3.dp.toPx()

                        val top = targetRect.top + squash
                        val height = (targetRect.height - squash * 2f).coerceAtLeast(2f)

                        val wPx = currentWidth.roundToInt().coerceAtLeast(1)
                        val hPx = height.roundToInt().coerceAtLeast(1)
                        val placeable = measurable.measure(Constraints.fixed(wPx, hPx))
                        layout(wPx, hPx) {
                            placeable.place(left.roundToInt(), top.roundToInt())
                        }
                    }
                    .liquidGlassEffect(
                        shape = pillShape,
                        cornerRadius = 50.dp,
                        refraction = 0.28f,
                        curve = 0.80f,
                        dispersion = 0.06f,
                        tint = CyanBright.copy(alpha = 0.06f)
                    )
                    .clip(pillShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.16f),
                                CyanBright.copy(alpha = 0.06f),
                                Color.White.copy(alpha = 0.03f)
                            )
                        )
                    )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StillMiniGame.entries.forEach { game ->
                val isSelected = game == selectedGame
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.55f, stiffness = 80f),
                    label = "topTabScale"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                    label = "topTabColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { tabBounds[game] = it.boundsInParent() }
                        .clip(pillShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                            onClick = { onGameSelected(game) }
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = game.tabTitle,
                        color = textColor,
                        fontSize = 13.sp,
                        letterSpacing = 2.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.scale(scale)
                    )
                }
            }
        }
    }
}

// ============================================================================
// GAME 1: REFLEX CORE
// ============================================================================
private enum class ReflexState {
    IDLE,
    FROSTED_WAITING,
    CLEAR_TAP_NOW,
    TOO_EARLY,
    RESULT
}

@Composable
private fun ReflexCoreGame() {
    var state by remember { mutableStateOf(ReflexState.IDLE) }
    var lastMs by remember { mutableStateOf<Long?>(null) }
    var bestMs by remember { mutableStateOf<Long?>(null) }
    var attempts by remember { mutableIntStateOf(0) }
    var clearTimestamp by remember { mutableLongStateOf(0L) }
    var roundKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(state, roundKey) {
        if (state == ReflexState.FROSTED_WAITING) {
            val waitTime = Random.nextLong(1600L, 4200L)
            delay(waitTime.milliseconds)
            if (state == ReflexState.FROSTED_WAITING) {
                clearTimestamp = SystemClock.elapsedRealtime()
                state = ReflexState.CLEAR_TAP_NOW
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "reflexBreathing")
    val breath by infiniteTransition.animateFloat(
        initialValue = -0.025f,
        targetValue = 0.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    val targetFrost = when (state) {
        ReflexState.FROSTED_WAITING -> 24f
        ReflexState.CLEAR_TAP_NOW -> 0f
        else -> 5f
    }
    val frostDp by animateFloatAsState(
        targetValue = targetFrost,
        animationSpec = if (state == ReflexState.CLEAR_TAP_NOW) {
            spring(dampingRatio = 0.45f, stiffness = 380f)
        } else {
            spring(dampingRatio = 0.7f, stiffness = 80f)
        },
        label = "frostAnim"
    )

    val bubbleBaseScale by animateFloatAsState(
        targetValue = when (state) {
            ReflexState.FROSTED_WAITING -> 0.94f
            ReflexState.CLEAR_TAP_NOW -> 1.08f
            ReflexState.TOO_EARLY -> 0.88f
            else -> 1.0f
        },
        animationSpec = spring(dampingRatio = 0.42f, stiffness = 180f),
        label = "reflexScale"
    )

    val handleBubbleTap = {
        when (state) {
            ReflexState.IDLE, ReflexState.RESULT, ReflexState.TOO_EARLY -> {
                roundKey++
                state = ReflexState.FROSTED_WAITING
            }
            ReflexState.FROSTED_WAITING -> {
                state = ReflexState.TOO_EARLY
            }
            ReflexState.CLEAR_TAP_NOW -> {
                val reaction = (SystemClock.elapsedRealtime() - clearTimestamp).coerceAtLeast(1L)
                lastMs = reaction
                bestMs = bestMs?.let { minOf(it, reaction) } ?: reaction
                attempts++
                state = ReflexState.RESULT
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GameHeaderTitle(firstWord = "REFLEX", secondWord = "CORE")
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatLabel(label = "LAST", value = lastMs?.let { "$it MS" } ?: "-- MS")
                StatLabel(label = "BEST", value = bestMs?.let { "$it MS" } ?: "-- MS")
                StatLabel(label = "ATTEMPTS", value = if (attempts > 0) "LOGGED ($attempts)" else "LOGGED")
            }
        }

        Box(
            modifier = Modifier
                .size(250.dp)
                .graphicsLayer {
                    scaleX = bubbleBaseScale + breath
                    scaleY = bubbleBaseScale - breath
                }
                .liquidGlassEffect(
                    shape = CircleShape,
                    cornerRadius = 125.dp,
                    frost = frostDp.dp,
                    refraction = if (state == ReflexState.CLEAR_TAP_NOW) 0.34f else 0.18f,
                    curve = 0.85f,
                    dispersion = if (state == ReflexState.CLEAR_TAP_NOW) 0.09f else 0.03f,
                    tint = when (state) {
                        ReflexState.FROSTED_WAITING -> Color.White.copy(alpha = 0.22f)
                        ReflexState.CLEAR_TAP_NOW -> CyanBright.copy(alpha = 0.16f)
                        ReflexState.TOO_EARLY -> Color(0xFFFF5252).copy(alpha = 0.15f)
                        else -> Color.White.copy(alpha = 0.06f)
                    }
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = when (state) {
                            ReflexState.FROSTED_WAITING -> listOf(
                                Color.White.copy(alpha = 0.28f),
                                Color.White.copy(alpha = 0.10f)
                            )
                            ReflexState.CLEAR_TAP_NOW -> listOf(
                                CyanBright.copy(alpha = 0.28f),
                                GoldAccent.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                            else -> listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.03f),
                                Color.Transparent
                            )
                        }
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (state == ReflexState.CLEAR_TAP_NOW) CyanBright.copy(alpha = 0.6f)
                    else Color.White.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = handleBubbleTap
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (state) {
                    ReflexState.IDLE -> {
                        Text(
                            text = "TAP TO",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 13.sp,
                            letterSpacing = 3.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "FROST",
                            color = GoldAccent,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 2.sp
                        )
                    }
                    ReflexState.FROSTED_WAITING -> {
                        Text(
                            text = "HOLD...",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 3.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "WAIT FOR FROST TO CLEAR",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            letterSpacing = 1.8.sp
                        )
                    }
                    ReflexState.CLEAR_TAP_NOW -> {
                        Text(
                            text = "STOP!",
                            color = CyanBright,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "FROST CLEARED",
                            color = Color.White,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp
                        )
                    }
                    ReflexState.TOO_EARLY -> {
                        Text(
                            text = "EARLY",
                            color = Color(0xFFFF6B6B),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "FROST HAD NOT SHIFTED",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                    ReflexState.RESULT -> {
                        Text(
                            text = "${lastMs ?: 0}",
                            color = GoldAccent,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "MILLISECONDS",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp,
                            letterSpacing = 3.sp
                        )
                    }
                }
            }
        }

        GlassActionButton(
            text = "NEW ATTEMPT",
            onClick = {
                roundKey++
                state = ReflexState.FROSTED_WAITING
            }
        )
    }
}

// ============================================================================
// GAME 2: TILE SHIFT (COALESCENT LIQUID BUBBLES)
// ============================================================================
private data class FluidTileModel(
    val id: Int,
    val value: Int,
    val row: Int,
    val col: Int,
    val mergeTrigger: Int = 0,
    val isDisappearing: Boolean = false
)

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
private fun TileShift2048Game() {
    var nextTileId by remember { mutableIntStateOf(1) }
    val tiles = remember { mutableStateListOf<FluidTileModel>() }
    var score by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(0) }
    var lastSwipeAxis by remember { mutableStateOf(Offset.Zero) }
    var isSliding by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var hasWon2048 by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    fun spawnRandomTile(currentTiles: MutableList<FluidTileModel>) {
        val occupied = currentTiles.filter { !it.isDisappearing }.map { it.row to it.col }.toSet()
        val empty = mutableListOf<Pair<Int, Int>>()
        for (r in 0..3) {
            for (c in 0..3) {
                if ((r to c) !in occupied) empty.add(r to c)
            }
        }
        if (empty.isNotEmpty()) {
            val (r, c) = empty.random()
            currentTiles.add(
                FluidTileModel(
                    id = nextTileId++,
                    value = if (Random.nextFloat() < 0.88f) 2 else 4,
                    row = r,
                    col = c
                )
            )
        }
    }

    fun canMove(activeTiles: List<FluidTileModel>): Boolean {
        val active = activeTiles.filter { !it.isDisappearing }
        if (active.size < 16) return true
        val grid = Array(4) { IntArray(4) }
        for (t in active) {
            if (t.row in 0..3 && t.col in 0..3) {
                grid[t.row][t.col] = t.value
            }
        }
        for (r in 0..3) {
            for (c in 0..3) {
                val v = grid[r][c]
                if (v == 0) return true
                if (r < 3 && grid[r + 1][c] == v) return true
                if (c < 3 && grid[r][c + 1] == v) return true
            }
        }
        return false
    }

    fun resetBoard() {
        tiles.clear()
        score = 0
        isGameOver = false
        hasWon2048 = false
        spawnRandomTile(tiles)
        spawnRandomTile(tiles)
    }

    LaunchedEffect(Unit) {
        if (tiles.isEmpty()) resetBoard()
    }

    fun slide(dr: Int, dc: Int) {
        if (isSliding || isGameOver) return
        lastSwipeAxis = Offset(dc.toFloat(), dr.toFloat())
        var moved = false
        var gainedScore = 0
        val intermediateTiles = mutableListOf<FluidTileModel>()
        val mergesToApply = mutableMapOf<Int, Int>()

        for (i in 0..3) {
            val line = tiles
                .filter { !it.isDisappearing && if (dr == 0) it.row == i else it.col == i }
                .let { list ->
                    when {
                        dc == -1 -> list.sortedBy { it.col }
                        dc == 1 -> list.sortedByDescending { it.col }
                        dr == -1 -> list.sortedBy { it.row }
                        else -> list.sortedByDescending { it.row }
                    }
                }

            var targetIndex = 0
            var idx = 0
            while (idx < line.size) {
                val current = line[idx]
                val next = line.getOrNull(idx + 1)

                val destRow = if (dr == 0) i else if (dr == -1) targetIndex else 3 - targetIndex
                val destCol = if (dc == 0) i else if (dc == -1) targetIndex else 3 - targetIndex

                if (next != null && next.value == current.value) {
                    val mergedVal = current.value * 2
                    gainedScore += mergedVal
                    moved = true

                    if (mergedVal >= 2048) {
                        hasWon2048 = true
                    }

                    intermediateTiles.add(current.copy(row = destRow, col = destCol))
                    intermediateTiles.add(
                        next.copy(
                            row = destRow,
                            col = destCol,
                            isDisappearing = true
                        )
                    )
                    mergesToApply[current.id] = mergedVal
                    idx += 2
                } else {
                    if (current.row != destRow || current.col != destCol) {
                        moved = true
                    }
                    intermediateTiles.add(current.copy(row = destRow, col = destCol))
                    idx += 1
                }
                targetIndex++
            }
        }

        if (moved) {
            isSliding = true
            tiles.clear()
            tiles.addAll(intermediateTiles)
            score += gainedScore
            if (score > bestScore) bestScore = score

            coroutineScope.launch {
                delay(165.milliseconds)
                val finalTiles = mutableListOf<FluidTileModel>()
                val activeTiles = tiles.filter { !it.isDisappearing }
                for (tile in activeTiles) {
                    val mergedVal = mergesToApply[tile.id]
                    if (mergedVal != null) {
                        finalTiles.add(
                            tile.copy(
                                value = mergedVal,
                                mergeTrigger = tile.mergeTrigger + 1
                            )
                        )
                    } else {
                        finalTiles.add(tile)
                    }
                }
                tiles.clear()
                tiles.addAll(finalTiles)

                spawnRandomTile(tiles)
                isSliding = false
                if (!canMove(tiles)) {
                    isGameOver = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var totalDrag = Offset.Zero
                        var swiped = false
                        val triggerThreshold = with(density) { 22.dp.toPx() }
                        val flickThreshold = with(density) { 12.dp.toPx() }

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (change.changedToUp() || change.isConsumed) {
                                if (!swiped) {
                                    val dx = totalDrag.x
                                    val dy = totalDrag.y
                                    if (abs(dx) >= flickThreshold || abs(dy) >= flickThreshold) {
                                        if (abs(dx) > abs(dy)) {
                                            slide(0, if (dx > 0) 1 else -1)
                                        } else {
                                            slide(if (dy > 0) 1 else -1, 0)
                                        }
                                    }
                                }
                                break
                            }

                            val dragDelta = change.position - change.previousPosition
                            totalDrag += dragDelta

                            if (!swiped) {
                                val dx = totalDrag.x
                                val dy = totalDrag.y
                                if (abs(dx) >= triggerThreshold || abs(dy) >= triggerThreshold) {
                                    swiped = true
                                    change.consume()
                                    if (abs(dx) > abs(dy)) {
                                        slide(0, if (dx > 0) 1 else -1)
                                    } else {
                                        slide(if (dy > 0) 1 else -1, 0)
                                    }
                                }
                            }
                        }
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GameHeaderTitle(firstWord = "TILE", secondWord = "SHIFT")
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatLabel(label = "SCORE", value = "$score")
                StatLabel(label = "BEST", value = "$bestScore")
                StatLabel(label = "TARGET", value = if (hasWon2048) "WON!" else "2048")
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.TopStart
        ) {
            val spacing = 10.dp
            val cellSize = (maxWidth - spacing * 3) / 4

            for (r in 0..3) {
                for (c in 0..3) {
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .offset(
                                x = (cellSize + spacing) * c,
                                y = (cellSize + spacing) * r
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.035f))
                            .border(
                                width = 0.8.dp,
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(18.dp)
                            )
                    )
                }
            }

            tiles.forEach { tile ->
                key(tile.id) {
                    Fluid2048Tile(
                        tile = tile,
                        cellSize = cellSize,
                        spacing = spacing,
                        swipeAxis = lastSwipeAxis
                    )
                }
            }

            if (isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF040B1A).copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "NO MORE MOVES",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Score: $score",
                            color = CyanBright,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        GlassActionButton(
                            text = "PLAY AGAIN",
                            onClick = { resetBoard() }
                        )
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArrowControlButton(symbol = "←", onClick = { slide(0, -1) })
                ArrowControlButton(symbol = "↑", onClick = { slide(-1, 0) })
                ArrowControlButton(symbol = "↓", onClick = { slide(1, 0) })
                ArrowControlButton(symbol = "→", onClick = { slide(0, 1) })
            }

            GlassActionButton(
                text = "RESET BOARD",
                onClick = { resetBoard() }
            )
        }
    }
}

@Composable
private fun ArrowControlButton(
    symbol: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(0.8.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = CyanBright,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun Fluid2048Tile(
    tile: FluidTileModel,
    cellSize: Dp,
    spacing: Dp,
    swipeAxis: Offset
) {
    val density = LocalDensity.current
    val stepPx = with(density) { (cellSize + spacing).toPx() }
    val targetX = stepPx * tile.col
    val targetY = stepPx * tile.row

    val xAnim = remember { Animatable(targetX) }
    val yAnim = remember { Animatable(targetY) }

    val squishX = remember { Animatable(if (tile.isDisappearing) 1f else 0.6f) }
    val squishY = remember { Animatable(if (tile.isDisappearing) 1f else 0.6f) }
    val alphaAnim = remember { Animatable(if (tile.isDisappearing) 1f else 0f) }
    val numberScale = remember { Animatable(1f) }

    val refractionAnim = remember { Animatable(0.28f) }
    val dispersionAnim = remember { Animatable(if (tile.value >= 16) 0.07f else 0.04f) }

    val infinite = rememberInfiniteTransition(label = "tileWobble_${tile.id}")
    val idleWobble by infinite.animateFloat(
        initialValue = -0.018f,
        targetValue = 0.018f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1350 + (tile.id % 4) * 180,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble"
    )

    LaunchedEffect(Unit) {
        if (!tile.isDisappearing) {
            launch { alphaAnim.animateTo(1f, tween(140, easing = LinearEasing)) }
            launch { squishX.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = 180f)) }
            launch { squishY.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = 180f)) }
        }
    }

    LaunchedEffect(tile.isDisappearing) {
        if (tile.isDisappearing) {
            launch { alphaAnim.animateTo(0f, tween(160, easing = FastOutSlowInEasing)) }
            launch { squishX.animateTo(0.5f, tween(160, easing = FastOutSlowInEasing)) }
            launch { squishY.animateTo(0.5f, tween(160, easing = FastOutSlowInEasing)) }
        }
    }

    LaunchedEffect(targetX, targetY) {
        if (xAnim.value != targetX || yAnim.value != targetY) {
            val movingHorizontally = abs(swipeAxis.x) >= abs(swipeAxis.y)
            launch {
                if (movingHorizontally) {
                    squishX.snapTo(1.18f)
                    squishY.snapTo(0.85f)
                } else {
                    squishX.snapTo(0.85f)
                    squishY.snapTo(1.18f)
                }
                squishX.animateTo(1f, spring(dampingRatio = 0.40f, stiffness = 160f))
            }
            launch {
                squishY.animateTo(1f, spring(dampingRatio = 0.40f, stiffness = 160f))
            }
            launch {
                xAnim.animateTo(targetX, spring(dampingRatio = 0.76f, stiffness = 320f))
            }
            launch {
                yAnim.animateTo(targetY, spring(dampingRatio = 0.76f, stiffness = 320f))
            }
        }
    }

    LaunchedEffect(tile.mergeTrigger) {
        if (tile.mergeTrigger > 0) {
            val movingHorizontally = abs(swipeAxis.x) >= abs(swipeAxis.y)

            launch {
                refractionAnim.snapTo(0.40f)
                refractionAnim.animateTo(0.28f, spring(dampingRatio = 0.45f, stiffness = 140f))
            }
            launch {
                dispersionAnim.snapTo(0.12f)
                dispersionAnim.animateTo(
                    if (tile.value >= 16) 0.07f else 0.04f,
                    spring(dampingRatio = 0.45f, stiffness = 140f)
                )
            }
            launch {
                numberScale.snapTo(1.32f)
                numberScale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 200f))
            }

            if (movingHorizontally) {
                squishX.snapTo(0.74f)
                squishY.snapTo(1.30f)
            } else {
                squishX.snapTo(1.30f)
                squishY.snapTo(0.74f)
            }
            launch {
                squishX.animateTo(1f, spring(dampingRatio = 0.34f, stiffness = 160f))
            }
            launch {
                squishY.animateTo(1f, spring(dampingRatio = 0.34f, stiffness = 160f))
            }
        }
    }

    val tileShape = RoundedCornerShape(22.dp)
    val accentTint = when {
        tile.value >= 2048 -> Color(0xFFFFD700).copy(alpha = 0.35f)
        tile.value >= 1024 -> Color(0xFFFF8A80).copy(alpha = 0.28f)
        tile.value >= 512 -> Color(0xFFBA68C8).copy(alpha = 0.26f)
        tile.value >= 256 -> Color(0xFF4FC3F7).copy(alpha = 0.24f)
        tile.value >= 128 -> Color(0xFF81C784).copy(alpha = 0.22f)
        tile.value >= 64 -> CyanBright.copy(alpha = 0.22f)
        tile.value >= 32 -> Color(0xFFFFB74D).copy(alpha = 0.20f)
        tile.value >= 16 -> GoldAccent.copy(alpha = 0.18f)
        tile.value >= 8 -> GoldAccent.copy(alpha = 0.14f)
        tile.value >= 4 -> Color.White.copy(alpha = 0.11f)
        else -> Color.White.copy(alpha = 0.06f)
    }

    Box(
        modifier = Modifier
            .size(cellSize)
            .offset { IntOffset(xAnim.value.roundToInt(), yAnim.value.roundToInt()) }
            .zIndex(if (tile.isDisappearing) 0f else 1f)
            .graphicsLayer {
                alpha = alphaAnim.value
                scaleX = squishX.value + idleWobble
                scaleY = squishY.value - idleWobble
            }
            .liquidGlassEffect(
                shape = tileShape,
                cornerRadius = 22.dp,
                refraction = refractionAnim.value,
                curve = 0.82f,
                dispersion = dispersionAnim.value,
                tint = accentTint
            )
            .clip(tileShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f),
                        accentTint,
                        Color.White.copy(alpha = 0.03f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${tile.value}",
            color = if (tile.value >= 4) GoldAccent else Color.White,
            fontSize = if (tile.value >= 1024) 18.sp else if (tile.value >= 128) 20.sp else 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.scale(numberScale.value)
        )
    }
}

// ============================================================================
// GAME 3: BUBBLE BURST
// ============================================================================
private data class FloatingBubble(
    val id: Int,
    var xFraction: Float,
    var yFraction: Float,
    val radiusDp: Float,
    val speedY: Float,
    val wobblePhase: Float,
    val tint: Color
)

private data class BurstEffect(
    val id: Int,
    val centerXFraction: Float,
    val centerYFraction: Float,
    val baseRadiusPx: Float,
    var progress: Float = 0f
)

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
private fun BubbleBurstGame() {
    val bubbles = remember { mutableStateListOf<FloatingBubble>() }
    val bursts = remember { mutableStateListOf<BurstEffect>() }
    var nextId by remember { mutableIntStateOf(1) }
    var poppedCount by remember { mutableIntStateOf(0) }
    var bestCount by remember { mutableIntStateOf(0) }
    var framePhase by remember { mutableFloatStateOf(0f) }

    fun createBubble(): FloatingBubble {
        val id = nextId++
        return FloatingBubble(
            id = id,
            xFraction = Random.nextFloat() * 0.72f + 0.14f,
            yFraction = Random.nextFloat() * 0.70f + 0.15f,
            radiusDp = Random.nextInt(34, 52).toFloat(),
            speedY = Random.nextFloat() * 0.08f + 0.04f,
            wobblePhase = Random.nextFloat() * 6.28f,
            tint = if (id % 2 == 0) CyanBright else GoldAccent
        )
    }

    LaunchedEffect(Unit) {
        if (bubbles.isEmpty()) {
            repeat(6) { bubbles.add(createBubble()) }
        }
        var lastNanos = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { now ->
                val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastNanos = now
                framePhase += dt * 3.2f

                for (i in bubbles.indices) {
                    val b = bubbles[i]
                    var newY = b.yFraction - b.speedY * dt
                    var newX = b.xFraction + sin(framePhase + b.wobblePhase) * 0.025f * dt

                    if (newY < 0.10f) {
                        newY = 0.88f
                        newX = Random.nextFloat() * 0.72f + 0.14f
                    }
                    bubbles[i] = b.copy(
                        xFraction = newX.coerceIn(0.12f, 0.88f),
                        yFraction = newY
                    )
                }

                val iterator = bursts.listIterator()
                while (iterator.hasNext()) {
                    val burst = iterator.next()
                    val nextProgress = burst.progress + dt * 2.4f
                    if (nextProgress >= 1f) {
                        iterator.remove()
                    } else {
                        iterator.set(burst.copy(progress = nextProgress))
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GameHeaderTitle(firstWord = "BUBBLE", secondWord = "BURST")
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatLabel(label = "POPPED", value = "$poppedCount")
                StatLabel(label = "BEST", value = "$bestCount")
                StatLabel(label = "ACTIVE", value = "${bubbles.size}")
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp)
        ) {
            val density = LocalDensity.current
            val arenaWidthPx = constraints.maxWidth.toFloat()
            val arenaHeightPx = constraints.maxHeight.toFloat()

            bubbles.forEach { bubble ->
                key(bubble.id) {
                    val radiusPx = with(density) { bubble.radiusDp.dp.toPx() }
                    val centerX = bubble.xFraction * arenaWidthPx
                    val centerY = bubble.yFraction * arenaHeightPx

                    val wobble = sin(framePhase * 1.4f + bubble.wobblePhase) * 0.06f

                    var isPopping by remember { mutableStateOf(false) }
                    val popScale by animateFloatAsState(
                        targetValue = if (isPopping) 1.48f else 1f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                        label = "popScale"
                    )

                    Box(
                        modifier = Modifier
                            .size((bubble.radiusDp * 2f).dp)
                            .offset {
                                IntOffset(
                                    (centerX - radiusPx).roundToInt(),
                                    (centerY - radiusPx).roundToInt()
                                )
                            }
                            .graphicsLayer {
                                scaleX = popScale + wobble
                                scaleY = popScale - wobble
                            }
                            .liquidGlassEffect(
                                shape = CircleShape,
                                cornerRadius = bubble.radiusDp.dp,
                                refraction = 0.32f,
                                curve = 0.90f,
                                dispersion = 0.08f,
                                tint = bubble.tint.copy(alpha = 0.08f)
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.26f),
                                        bubble.tint.copy(alpha = 0.12f),
                                        Color.Transparent
                                    ),
                                    center = Offset(radiusPx * 0.65f, radiusPx * 0.65f)
                                )
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isPopping = true
                                poppedCount++
                                if (poppedCount > bestCount) bestCount = poppedCount
                                bursts.add(
                                    BurstEffect(
                                        id = nextId++,
                                        centerXFraction = bubble.xFraction,
                                        centerYFraction = bubble.yFraction,
                                        baseRadiusPx = radiusPx
                                    )
                                )
                                bubbles.removeAll { it.id == bubble.id }
                                bubbles.add(createBubble())
                            }
                    )
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                bursts.forEach { burst ->
                    val cx = burst.centerXFraction * size.width
                    val cy = burst.centerYFraction * size.height
                    val p = burst.progress
                    val alpha = (1f - p).coerceIn(0f, 1f)

                    drawCircle(
                        color = CyanBright.copy(alpha = alpha * 0.55f),
                        radius = burst.baseRadiusPx * (0.8f + p * 1.1f),
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.5.dp.toPx() * alpha)
                    )

                    val dropletCount = 8
                    for (i in 0 until dropletCount) {
                        val angle = (i * (360f / dropletCount) + (burst.id * 17f)) * (Math.PI / 180.0)
                        val distance = burst.baseRadiusPx * (0.4f + p * 1.45f)
                        val dx = cx + (cos(angle) * distance).toFloat()
                        val dy = cy + (sin(angle) * distance).toFloat()
                        val dropRadius = (6.dp.toPx() * (1f - p * 0.7f)).coerceAtLeast(1f)

                        drawCircle(
                            color = Color.White.copy(alpha = alpha * 0.85f),
                            radius = dropRadius,
                            center = Offset(dx, dy)
                        )
                    }
                }
            }
        }

        GlassActionButton(
            text = "RESET ARENA",
            onClick = {
                poppedCount = 0
                bubbles.clear()
                repeat(6) { bubbles.add(createBubble()) }
            }
        )
    }
}

// ============================================================================
// SHARED COMPONENTS
// ============================================================================
@Composable
private fun GameHeaderTitle(firstWord: String, secondWord: String) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = firstWord,
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = secondWord,
            color = GoldAccent,
            fontSize = 30.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun StatLabel(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label  ",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 10.sp,
            letterSpacing = 1.5.sp
        )
        Text(
            text = value,
            color = GoldAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun GlassActionButton(
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .liquidGlassEffect(
                shape = shape,
                cornerRadius = 14.dp,
                refraction = 0.22f,
                curve = 0.65f
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .border(0.8.dp, Color.White.copy(alpha = 0.18f), shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.25f)),
                onClick = onClick
            )
            .padding(horizontal = 28.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 11.sp,
            letterSpacing = 2.5.sp,
            textAlign = TextAlign.Center
        )
    }
}