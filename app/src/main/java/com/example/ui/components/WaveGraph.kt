package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.VioletGlow

data class ChartPoint(
    val label: String,
    val value: Float,
    val isToday: Boolean = false
)

@Composable
fun InteractiveWaveGraph(
    points: List<ChartPoint>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth().height(160.dp),
    unit: String = "kcal",
    maxVal: Float = 850f
) {
    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(600))
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val paddingStart = 16.dp.toPx()
                        val paddingEnd = 38.dp.toPx()
                        val availableWidth = (size.width - paddingStart - paddingEnd).coerceAtLeast(1f)
                        if (points.isNotEmpty()) {
                            val step = availableWidth / (points.size - 1).coerceAtLeast(1)
                            val relativeX = offset.x - paddingStart
                            val tappedIndex = (relativeX / step + 0.5f).toInt().coerceIn(0, points.size - 1)
                            onSelectIndex(tappedIndex)
                        }
                    }
                }
        ) {
            if (points.isEmpty()) return@Canvas

            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            val paddingStart = 16.dp.toPx()
            val paddingEnd = 38.dp.toPx()
            val paddingTop = 12.dp.toPx()
            val paddingBottom = 26.dp.toPx()

            val chartWidth = (w - paddingStart - paddingEnd).coerceAtLeast(1f)
            val chartHeight = (h - paddingTop - paddingBottom).coerceAtLeast(1f)

            // 1. Minimal Horizontal Reference Grid Lines (clean and subtle)
            val gridLevels = listOf(0.0f, 0.5f, 1.0f)
            val safeMax = if (maxVal <= 0f) 1f else maxVal
            val gridValues = listOf(0, (safeMax / 2).toInt(), safeMax.toInt())
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)

            for (i in gridLevels.indices) {
                val level = gridLevels[i]
                val y = paddingTop + chartHeight * (1f - level)

                drawLine(
                    color = Color.White.copy(alpha = 0.06f),
                    start = Offset(paddingStart, y),
                    end = Offset(w - paddingEnd, y),
                    strokeWidth = 0.8.dp.toPx(),
                    pathEffect = dashEffect
                )

                // Grid label on right side
                val labelText = gridValues[i].toString()
                val measured = textMeasurer.measure(
                    text = labelText,
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.40f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = labelText,
                    topLeft = Offset(w - paddingEnd + 6.dp.toPx(), y - measured.size.height / 2f),
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.40f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            // 2. Compute Points coordinates
            val stepX = chartWidth / (points.size - 1).coerceAtLeast(1)
            val coords = points.mapIndexed { idx, pt ->
                val x = paddingStart + idx * stepX
                val normalizedVal = (pt.value / safeMax).coerceIn(0f, 1f) * animatedProgress.value
                val y = paddingTop + chartHeight * (1f - normalizedVal)
                Offset(x, y)
            }

            if (coords.isEmpty()) return@Canvas

            // 3. Construct Smooth Cubic Bezier Curves
            val curvePath = Path()
            val fillPath = Path()

            curvePath.moveTo(coords[0].x, coords[0].y)
            fillPath.moveTo(coords[0].x, paddingTop + chartHeight)
            fillPath.lineTo(coords[0].x, coords[0].y)

            for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val ctrlX1 = p0.x + (p1.x - p0.x) / 2f
                val ctrlY1 = p0.y
                val ctrlX2 = p0.x + (p1.x - p0.x) / 2f
                val ctrlY2 = p1.y

                curvePath.cubicTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, p1.x, p1.y)
                fillPath.cubicTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, p1.x, p1.y)
            }

            fillPath.lineTo(coords.last().x, paddingTop + chartHeight)
            fillPath.close()

            // 4. Draw Ultra-Translucent Shimmering Gradient Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CyanBright.copy(alpha = 0.12f),
                        VioletGlow.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    startY = paddingTop,
                    endY = paddingTop + chartHeight
                )
            )

            // 5. Draw Glowing Crisp Wave Line
            drawPath(
                path = curvePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        CyanBright.copy(alpha = 0.90f),
                        CyanGlow.copy(alpha = 0.85f),
                        VioletGlow.copy(alpha = 0.85f)
                    ),
                    startX = paddingStart,
                    endX = paddingStart + chartWidth
                ),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // 6. Draw Points and Day Labels
            coords.forEachIndexed { idx, coord ->
                val pt = points[idx]
                val isSelected = idx == selectedIndex

                if (isSelected) {
                    drawCircle(
                        color = CyanBright.copy(alpha = 0.20f),
                        radius = 8.dp.toPx(),
                        center = coord
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.5.dp.toPx(),
                        center = coord
                    )
                    drawCircle(
                        color = CyanBright,
                        radius = 3.5.dp.toPx(),
                        center = coord,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                } else {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.50f),
                        radius = 2.dp.toPx(),
                        center = coord
                    )
                }

                // X-axis Day Label
                val labelColor = if (isSelected) CyanBright else Color.White.copy(alpha = 0.50f)
                val labelWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                val labelLayout = textMeasurer.measure(
                    text = pt.label,
                    style = TextStyle(
                        color = labelColor,
                        fontSize = 10.sp,
                        fontWeight = labelWeight
                    )
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = pt.label,
                    topLeft = Offset(
                        coord.x - labelLayout.size.width / 2f,
                        h - paddingBottom + 6.dp.toPx()
                    ),
                    style = TextStyle(
                        color = labelColor,
                        fontSize = 10.sp,
                        fontWeight = labelWeight
                    )
                )
            }
        }
    }
}

@Composable
fun InteractiveBarGraph(
    points: List<ChartPoint>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth().height(160.dp),
    unit: String = "kcal",
    maxVal: Float = 850f
) {
    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(600))
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val paddingStart = 16.dp.toPx()
                        val paddingEnd = 38.dp.toPx()
                        val availableWidth = (size.width - paddingStart - paddingEnd).coerceAtLeast(1f)
                        if (points.isNotEmpty()) {
                            val step = availableWidth / (points.size - 1).coerceAtLeast(1)
                            val relativeX = offset.x - paddingStart
                            val tappedIndex = (relativeX / step + 0.5f).toInt().coerceIn(0, points.size - 1)
                            onSelectIndex(tappedIndex)
                        }
                    }
                }
        ) {
            if (points.isEmpty()) return@Canvas

            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            val paddingStart = 16.dp.toPx()
            val paddingEnd = 38.dp.toPx()
            val paddingTop = 12.dp.toPx()
            val paddingBottom = 26.dp.toPx()

            val chartWidth = (w - paddingStart - paddingEnd).coerceAtLeast(1f)
            val chartHeight = (h - paddingTop - paddingBottom).coerceAtLeast(1f)

            // 1. Horizontal Reference Grid Lines
            val gridLevels = listOf(0.0f, 0.5f, 1.0f)
            val safeMax = if (maxVal <= 0f) 1f else maxVal
            val gridValues = listOf(0, (safeMax / 2).toInt(), safeMax.toInt())
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)

            for (i in gridLevels.indices) {
                val level = gridLevels[i]
                val y = paddingTop + chartHeight * (1f - level)

                drawLine(
                    color = Color.White.copy(alpha = 0.06f),
                    start = Offset(paddingStart, y),
                    end = Offset(w - paddingEnd, y),
                    strokeWidth = 0.8.dp.toPx(),
                    pathEffect = dashEffect
                )

                val labelText = gridValues[i].toString()
                val measured = textMeasurer.measure(
                    text = labelText,
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.40f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = labelText,
                    topLeft = Offset(w - paddingEnd + 6.dp.toPx(), y - measured.size.height / 2f),
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.40f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            // 2. Compute Points coordinates & Draw Bars
            val stepX = chartWidth / (points.size - 1).coerceAtLeast(1)
            val barWidth = (stepX * 0.45f).coerceAtMost(24.dp.toPx()) // Bar width
            
            val coords = points.mapIndexed { idx, pt ->
                val x = paddingStart + idx * stepX
                val normalizedVal = (pt.value / safeMax).coerceIn(0f, 1f) * animatedProgress.value
                val y = paddingTop + chartHeight * (1f - normalizedVal)
                Offset(x, y)
            }

            if (coords.isEmpty()) return@Canvas

            coords.forEachIndexed { idx, coord ->
                val pt = points[idx]
                val isSelected = idx == selectedIndex

                // Draw Bar
                val barTop = coord.y
                val barBottom = paddingTop + chartHeight
                val barHeight = (barBottom - barTop).coerceAtLeast(0.01f)
                val barLeft = coord.x - barWidth / 2f
                
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = if (isSelected) listOf(CyanBright, CyanGlow) else listOf(CyanBright.copy(alpha = 0.4f), VioletGlow.copy(alpha = 0.2f)),
                        startY = barTop,
                        endY = barBottom
                    ),
                    topLeft = Offset(barLeft, barTop),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Draw Value text if selected
                if (isSelected) {
                    val valueLabel = "${pt.value.toInt()}"
                    val valMeasured = textMeasurer.measure(
                        text = valueLabel,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = valueLabel,
                        topLeft = Offset(coord.x - valMeasured.size.width / 2f, barTop - valMeasured.size.height - 4.dp.toPx()),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // X-axis Day Label
                val labelColor = if (isSelected) CyanBright else Color.White.copy(alpha = 0.50f)
                val labelWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                val labelLayout = textMeasurer.measure(
                    text = pt.label,
                    style = TextStyle(
                        color = labelColor,
                        fontSize = 10.sp,
                        fontWeight = labelWeight
                    )
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = pt.label,
                    topLeft = Offset(
                        coord.x - labelLayout.size.width / 2f,
                        h - paddingBottom + 6.dp.toPx()
                    ),
                    style = TextStyle(
                        color = labelColor,
                        fontSize = 10.sp,
                        fontWeight = labelWeight
                    )
                )
            }
        }
    }
}
