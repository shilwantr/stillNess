import re

file_path = 'app/src/main/java/com/example/ui/glass/CosmicHorizonBackground.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add to enum
enum_pattern = r'BIOLUMINESCENT_WAKE\("Deep Abyssal Wake", "A hypnotic top-down view of an illuminated boat cutting through pitch-black water"\)'
enum_replacement = 'BIOLUMINESCENT_WAKE("Deep Abyssal Wake", "A hypnotic top-down view of an illuminated boat cutting through pitch-black water"),\n    FERRIS_WHEEL("Night Ferris Wheel", "Cinemagraph of an illuminated Ferris wheel against a pure black sky")'
content = re.sub(enum_pattern, enum_replacement, content)

# Add to when statement
when_pattern = r'AmbientTheme\.BIOLUMINESCENT_WAKE -> drawBioluminescentWake\(w, h, phase1, phase2, pulse\)'
when_replacement = 'AmbientTheme.BIOLUMINESCENT_WAKE -> drawBioluminescentWake(w, h, phase1, phase2, pulse)\n            AmbientTheme.FERRIS_WHEEL -> drawFerrisWheel(w, h, phase1, phase2, pulse)'
content = re.sub(when_pattern, when_replacement, content)

# Add function
func = """
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
"""

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content + "\n" + func)
