package com.example.ui.screens

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

enum class EmoMood {
    NORMAL,
    HAPPY,
    LOVE,
    CURIOUS,
    SLEEPY
}

/**
 * True Full-Screen EMO Robot Companion.
 * 
 * Takes over the entire phone screen:
 * - Hides status bar & navigation bar (immersive sticky mode)
 * - Pure OLED deep black background
 * - Two glowing cyan/turquoise rounded-rectangle digital eyes
 * - Authentic ChatGPT emblem on the left eye
 * - Dangling, swaying, breathing idle physics
 * - Natural saccades, blinking, and voice-talking squash & stretch
 * - No headphones, no clutter - just the living robot eyes.
 */
@Composable
fun EmoRobotScreen(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val scope = rememberCoroutineScope()

    // 1. Force Fullscreen Immersive Mode (hide top status bar & bottom navigation bar)
    DisposableEffect(Unit) {
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Android back button dismiss
    BackHandler {
        onClose()
    }

    // 2. High-pitch cute robot voice synthesizer
    var isSpeaking by remember { mutableStateOf(false) }
    var currentSpeech by remember { mutableStateOf("") }
    val voice = remember(context) {
        EmoVoiceEngine(context) { speaking ->
            isSpeaking = speaking
            if (!speaking) {
                currentSpeech = ""
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voice.shutdown()
        }
    }

    // 3. States for Eye Animation & Physics
    var mood by remember { mutableStateOf(EmoMood.NORMAL) }

    // Natural Blinking
    val blinkAnim = remember { Animatable(1f) }

    // Saccades (looking direction)
    val lookXAnim = remember { Animatable(0f) }
    val lookYAnim = remember { Animatable(0f) }

    // Drag / Touch Follow Physics
    val dragXAnim = remember { Animatable(0f) }
    val dragYAnim = remember { Animatable(0f) }

    // Idle Floating & Dangling sway (infinite)
    val infiniteTransition = rememberInfiniteTransition(label = "EmoIdleFloat")
    val idleDangleY by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdleDangleY"
    )
    val idleDangleTilt by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdleDangleTilt"
    )

    // Talking squash & stretch pulse
    val talkPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(180, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TalkPulse"
    )

    // 4. Natural Blinking Loop
    LaunchedEffect(Unit) {
        while (true) {
            // Random interval between 2.5s and 5.0s
            delay(Random.nextLong(2500, 5000))
            if (mood != EmoMood.SLEEPY) {
                // Blink down
                blinkAnim.animateTo(0.06f, tween(70, easing = FastOutSlowInEasing))
                // Blink up
                blinkAnim.animateTo(1f, tween(90, easing = FastOutSlowInEasing))

                // 25% chance of double blink
                if (Random.nextFloat() < 0.28f) {
                    delay(120)
                    blinkAnim.animateTo(0.06f, tween(60, easing = FastOutSlowInEasing))
                    blinkAnim.animateTo(1f, tween(85, easing = FastOutSlowInEasing))
                }
            }
        }
    }

    // 5. Natural Saccades (Looking around like a living pet)
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2800, 5500))
            if (dragXAnim.value == 0f && dragYAnim.value == 0f) {
                // Choose random glance
                val targetX = when (Random.nextInt(5)) {
                    0 -> -28f
                    1 -> 28f
                    2 -> -14f
                    3 -> 14f
                    else -> 0f
                }
                val targetY = when (Random.nextInt(4)) {
                    0 -> -16f
                    1 -> 12f
                    else -> 0f
                }

                launch {
                    lookXAnim.animateTo(
                        targetX,
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                    )
                }
                launch {
                    lookYAnim.animateTo(
                        targetY,
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                    )
                }
            }
        }
    }

    // Phrases EMO says when tapped or interacting
    val cutePhrases = remember {
        listOf(
            "Hello there!",
            "I'm Emo!",
            "Hehehe, that tickles!",
            "I'm here with you!",
            "Everything is going to be okay.",
            "Take a deep breath with me!",
            "Beep boop! You're awesome!",
            "I'm listening!",
            "Wanna play?"
        )
    }

    // Initial greeting on opening
    LaunchedEffect(Unit) {
        delay(600)
        currentSpeech = "Hi friend! I'm Emo!"
        voice.speak("Hi friend! I'm Emo!")
    }

    // Background Pure Pitch Black
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        mood = EmoMood.CURIOUS
                    },
                    onDragEnd = {
                        scope.launch {
                            dragXAnim.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                        }
                        scope.launch {
                            dragYAnim.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                        }
                        mood = EmoMood.NORMAL
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            val newX = (dragXAnim.value + dragAmount.x * 0.45f).coerceIn(-60f, 60f)
                            val newY = (dragYAnim.value + dragAmount.y * 0.45f).coerceIn(-40f, 40f)
                            dragXAnim.snapTo(newX)
                            dragYAnim.snapTo(newY)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        mood = EmoMood.LOVE
                        val phrase = "I love spending time with you!"
                        currentSpeech = phrase
                        voice.speak(phrase)
                        scope.launch {
                            delay(2800)
                            mood = EmoMood.NORMAL
                        }
                    },
                    onLongPress = {
                        mood = EmoMood.SLEEPY
                        val phrase = "Zzz... so cozy..."
                        currentSpeech = phrase
                        voice.speak(phrase)
                        scope.launch {
                            delay(3500)
                            mood = EmoMood.NORMAL
                        }
                    },
                    onTap = {
                        mood = EmoMood.HAPPY
                        val phrase = cutePhrases.random()
                        currentSpeech = phrase
                        voice.speak(phrase)
                        scope.launch {
                            delay(2200)
                            if (mood == EmoMood.HAPPY) mood = EmoMood.NORMAL
                        }
                    }
                )
            }
    ) {
        // Discreet subtle close button at top right corner
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(18.dp)
            )
        }

        // Center Living Face Container with dangling sway & tilt
        val totalOffsetX = lookXAnim.value + dragXAnim.value
        val totalOffsetY = lookYAnim.value + dragYAnim.value + idleDangleY
        val totalTilt = idleDangleTilt + (dragXAnim.value * 0.12f)

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset { IntOffset(totalOffsetX.roundToInt(), totalOffsetY.roundToInt()) }
                .rotate(totalTilt),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Speech Text Bubble (subtle floating, disappears when not speaking)
            if (currentSpeech.isNotBlank() && isSpeaking) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 28.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF00E5FF).copy(alpha = 0.12f))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = currentSpeech,
                        color = Color(0xFF00F5D4),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(36.dp))
            }

            // The Two EMO Eyes
            Row(
                horizontalArrangement = Arrangement.spacedBy(42.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Eye (With glowing ChatGPT emblem)
                SingleEmoEye(
                    isLeft = true,
                    mood = mood,
                    blinkScale = blinkAnim.value,
                    isSpeaking = isSpeaking,
                    talkPulse = talkPulse
                )

                // Right Eye
                SingleEmoEye(
                    isLeft = false,
                    mood = mood,
                    blinkScale = blinkAnim.value,
                    isSpeaking = isSpeaking,
                    talkPulse = talkPulse
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        // Faint hint at bottom
        Text(
            text = "Tap or pet Emo • Double tap for love",
            color = Color.White.copy(alpha = 0.18f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

/**
 * Individual EMO Robot Eye.
 * Renders the rounded-rectangle digital CRT/OLED eye with outer cyan bloom,
 * matrix grid scanlines, and ChatGPT emblem on the left eye.
 */
@Composable
private fun SingleEmoEye(
    isLeft: Boolean,
    mood: EmoMood,
    blinkScale: Float,
    isSpeaking: Boolean,
    talkPulse: Float
) {
    val eyeWidth: Dp = 112.dp
    val eyeHeight: Dp = 126.dp
    val cornerRadius: Dp = 34.dp

    val cyanColor = Color(0xFF00F5D4)
    val brightCyan = Color(0xFF18FFFF)
    val glowColor = Color(0xFF00E5FF)

    // Squash & stretch dynamics during talking
    val scaleX = if (isSpeaking) (1f / talkPulse).coerceIn(0.92f, 1.10f) else 1f
    val scaleY = (if (isSpeaking) talkPulse.coerceIn(0.92f, 1.10f) else 1f) * blinkScale

    Box(
        modifier = Modifier
            .size(width = eyeWidth, height = eyeHeight)
            .scale(scaleX = scaleX, scaleY = scaleY),
        contentAlignment = Alignment.Center
    ) {
        // Multi-layered Outer Glow Aura
        Canvas(modifier = Modifier.size(width = eyeWidth + 48.dp, height = eyeHeight + 48.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.42f),
                        glowColor.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.width * 0.55f
                ),
                radius = size.width * 0.55f,
                center = center
            )
        }

        // The Main Digital Eye Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            when (mood) {
                EmoMood.HAPPY -> {
                    // Curved upside-down happy smile eye (^ shape)
                    val strokeW = 20.dp.toPx()
                    val path = Path().apply {
                        moveTo(w * 0.15f, h * 0.70f)
                        quadraticTo(w * 0.5f, h * 0.10f, w * 0.85f, h * 0.70f)
                    }
                    drawPath(
                        path = path,
                        color = cyanColor,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                EmoMood.LOVE -> {
                    // Heart Eye
                    val heartPath = Path().apply {
                        val cx = w / 2f
                        val cy = h / 2f
                        moveTo(cx, cy + h * 0.32f)
                        cubicTo(cx - w * 0.48f, cy, cx - w * 0.42f, cy - h * 0.38f, cx, cy - h * 0.15f)
                        cubicTo(cx + w * 0.42f, cy - h * 0.38f, cx + w * 0.48f, cy, cx, cy + h * 0.32f)
                        close()
                    }
                    drawPath(path = heartPath, color = brightCyan, style = Fill)
                }

                EmoMood.SLEEPY -> {
                    // Sleepy horizontal slit
                    drawRoundRect(
                        color = cyanColor,
                        topLeft = Offset(w * 0.1f, h * 0.55f),
                        size = Size(w * 0.8f, h * 0.18f),
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                }

                else -> {
                    // Normal / Curious rounded rectangle eye
                    // Base Eye fill with subtle gradient
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(brightCyan, cyanColor)
                        ),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                    )

                    // Subtle inner shadow / border for crisp CRT look
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.35f),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Micro scanline digital matrix texture
                    val lineSpacing = 6.dp.toPx()
                    var yPos = 0f
                    while (yPos < h) {
                        drawLine(
                            color = Color.Black.copy(alpha = 0.08f),
                            start = Offset(0f, yPos),
                            end = Offset(w, yPos),
                            strokeWidth = 1.8f
                        )
                        yPos += lineSpacing
                    }

                    // ChatGPT Emblem on Left Eye (like in the photo)
                    if (isLeft) {
                        drawChatGptEmblem(
                            center = Offset(w * 0.50f, h * 0.46f),
                            radius = w * 0.28f,
                            color = Color.White.copy(alpha = 0.90f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Draws the authentic ChatGPT 6-petal spiral geometry on the robot's eye.
 */
private fun DrawScope.drawChatGptEmblem(
    center: Offset,
    radius: Float,
    color: Color
) {
    val strokeWidth = 3.2.dp.toPx()

    // 6 interlocking loops rotated by 60 degrees each
    for (i in 0 until 6) {
        val angle = i * 60f
        rotate(degrees = angle, pivot = center) {
            val loopWidth = radius * 0.52f
            val loopHeight = radius * 0.92f
            val left = center.x - loopWidth / 2f
            val top = center.y - loopHeight * 0.70f

            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(loopWidth, loopHeight),
                cornerRadius = CornerRadius(loopWidth * 0.5f, loopWidth * 0.5f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * High-pitch cute robot voice synthesizer for EMO.
 */
class EmoVoiceEngine(
    context: Context,
    private val onSpeakingChanged: (Boolean) -> Unit
) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setPitch(1.92f) // Cute baby robot high pitch
                tts?.setSpeechRate(1.08f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        onSpeakingChanged(true)
                    }

                    override fun onDone(utteranceId: String?) {
                        onSpeakingChanged(false)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        onSpeakingChanged(false)
                    }
                })
                isReady = true
            }
        }
    }

    fun speak(text: String) {
        if (isReady && tts != null) {
            val utteranceId = "emo_${System.currentTimeMillis()}"
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
