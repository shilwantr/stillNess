package com.example.ui.glass

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import io.github.fletchmckee.liquid.liquid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class RobotMood {
    IDLE,
    HAPPY,
    DANCING,
    SAD,
    SPINNING,
    FOOT_TRICK,
    TICKLE,
    BELLY_POKE,
    WALKING_SIDEWAYS,
    SUPER_JUMP,
    WAVE_HELLO,
    LOVE_STRUCK,
    SLEEPY,
    LASER_SCAN
}

/**
 * Cute Robotic Kid Voice + Melodic Chirp Synthesizer.
 */
class RobotVoiceEngine(
    context: Context,
    private val scope: CoroutineScope,
    private val onSpeakingChanged: (Boolean) -> Unit
) {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    var isMuted: Boolean = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setPitch(1.85f)
                tts?.setSpeechRate(1.05f)
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
                isTtsReady = true
            }
        }
    }

    fun speak(text: String, mood: RobotMood) {
        if (isMuted) return

        scope.launch(Dispatchers.Default) {
            playMoodChirp(mood)
        }

        if (!isTtsReady) return

        val cleanText = text.replace(Regex("[^\\p{L}\\p{N}\\p{P}\\p{Z}]"), "").trim()
        if (cleanText.isEmpty()) return

        when (mood) {
            RobotMood.HAPPY, RobotMood.TICKLE, RobotMood.WAVE_HELLO -> {
                tts?.setPitch(1.95f)
                tts?.setSpeechRate(1.12f)
            }
            RobotMood.DANCING, RobotMood.FOOT_TRICK, RobotMood.WALKING_SIDEWAYS -> {
                tts?.setPitch(1.88f)
                tts?.setSpeechRate(1.08f)
            }
            RobotMood.SUPER_JUMP, RobotMood.SPINNING -> {
                tts?.setPitch(1.98f)
                tts?.setSpeechRate(1.15f)
            }
            RobotMood.LOVE_STRUCK, RobotMood.BELLY_POKE -> {
                tts?.setPitch(1.92f)
                tts?.setSpeechRate(1.04f)
            }
            RobotMood.SAD -> {
                tts?.setPitch(1.42f)
                tts?.setSpeechRate(0.82f)
            }
            RobotMood.SLEEPY -> {
                tts?.setPitch(1.35f)
                tts?.setSpeechRate(0.75f)
            }
            RobotMood.LASER_SCAN -> {
                tts?.setPitch(1.72f)
                tts?.setSpeechRate(1.18f)
            }
            RobotMood.IDLE -> {
                tts?.setPitch(1.80f)
                tts?.setSpeechRate(1.0f)
            }
        }

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.9f)
        }
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "robot_utt_${System.currentTimeMillis()}")
    }

    private fun playMoodChirp(mood: RobotMood) {
        try {
            val sampleRate = 24000
            val durationMs = when (mood) {
                RobotMood.WALKING_SIDEWAYS -> 320
                RobotMood.SUPER_JUMP -> 300
                RobotMood.LOVE_STRUCK -> 280
                RobotMood.SLEEPY -> 310
                RobotMood.LASER_SCAN -> 270
                RobotMood.WAVE_HELLO -> 230
                RobotMood.FOOT_TRICK -> 260
                RobotMood.TICKLE -> 240
                RobotMood.BELLY_POKE -> 220
                RobotMood.SAD -> 260
                RobotMood.SPINNING -> 250
                else -> 170
            }
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val freq = when (mood) {
                    RobotMood.FOOT_TRICK -> if (progress < 0.5f) 580f + 640f * progress else 940f + 220f * progress
                    RobotMood.TICKLE -> 720f + 350f * sin(progress * 8f * PI).toFloat()
                    RobotMood.BELLY_POKE -> if (progress < 0.4f) 640f else 880f
                    RobotMood.WALKING_SIDEWAYS -> {
                        // Playful 4-step walking arpeggio
                        when {
                            progress < 0.25f -> 587f
                            progress < 0.50f -> 659f
                            progress < 0.75f -> 784f
                            else -> 880f
                        }
                    }
                    RobotMood.SUPER_JUMP -> {
                        // Rising launch whistle + high sparkle
                        if (progress < 0.7f) 480f + 920f * (progress / 0.7f) else 1180f
                    }
                    RobotMood.WAVE_HELLO -> if (progress < 0.45f) 740f else 987f
                    RobotMood.LOVE_STRUCK -> {
                        // Major triad shimmer
                        when {
                            progress < 0.33f -> 659f
                            progress < 0.66f -> 830f
                            else -> 987f
                        }
                    }
                    RobotMood.SLEEPY -> 540f - (220f * progress)
                    RobotMood.LASER_SCAN -> 860f + 420f * cos(progress * 10f * PI).toFloat()
                    RobotMood.HAPPY -> if (progress < 0.5f) 660f + 300f * progress else 900f + 400f * progress
                    RobotMood.DANCING -> if (progress < 0.5f) 720f else 960f
                    RobotMood.SAD -> 600f - (260f * progress)
                    RobotMood.SPINNING -> 800f + 250f * sin(progress * 6f * PI).toFloat()
                    RobotMood.IDLE -> 700f + 110f * progress
                }

                val envelope = sin(progress * PI).toFloat()
                val sample = sin(2.0 * PI * i * freq / sampleRate).toFloat()
                buffer[i] = (sample * envelope * Short.MAX_VALUE * 0.20f).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 30L)
            audioTrack.release()
        } catch (_: Exception) {
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}

/**
 * Enlarged Interactive Bubble-Based Liquid Glass Companion Robot.
 *
 * Touch & Gesture Interactions:
 * - Tap Feet -> High snappy foot bounce up to chest, catch, and place back!
 * - Tap Hoverboard Base -> Walk sideways left & right with waddling stride!
 * - Tap Top Headphone Band / Double-Tap Stomach -> Super Jump + 360° Mid-Air Flip!
 * - Tap Hands -> Tickle interaction | Double-Tap Hands -> Wave Hello & Wink!
 * - Tap Stomach -> Belly poke | Long-Press Stomach -> Dance Groove!
 * - Tap Head -> HAPPY | Double-Tap / Long-Press Head -> LOVE_STRUCK heart eyes!
 * - Tap Ears -> SAD pout | Long-Press Ears -> SLEEPY power-nap with Zzz!
 * - Double-Tap Speech Bubble -> Holographic Visor Laser Scan!
 * - Horizontal Drag -> 3D perspective turn (fast swipe triggers 360° Spin!)
 */
@Composable
fun GlassRobot(
    modifier: Modifier = Modifier,
    enableVoice: Boolean = true,
    showQuickActions: Boolean = true,
    initialMessage: String = "Tap my hoverboard to walk sideways, or tap my feet!"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mood by remember { mutableStateOf(RobotMood.IDLE) }
    var speechText by remember { mutableStateOf(initialMessage) }
    var isSpeaking by remember { mutableStateOf(false) }
    var interactionTick by remember { mutableIntStateOf(0) }

    // Foot bounce-catch-place state (-1 = Left foot, +1 = Right foot)
    var activeFootSide by remember { mutableIntStateOf(-1) }
    val footTrickAnim = remember { Animatable(0f) }

    // Hand tickle & wave animation states
    val tickleAnim = remember { Animatable(0f) }
    val waveAnim = remember { Animatable(0f) }
    var activeWaveSide by remember { mutableIntStateOf(1) }

    // Belly poke animation state
    val bellyPokeAnim = remember { Animatable(0f) }

    // Sideways walking animation state (0f..1f full patrol cycle)
    val walkAnim = remember { Animatable(0f) }

    // Super jump + mid-air flip animation state
    val superJumpAnim = remember { Animatable(0f) }

    // Laser scan animation state
    val laserScanAnim = remember { Animatable(0f) }

    val voiceEngine = remember(context) {
        RobotVoiceEngine(
            context = context,
            scope = scope,
            onSpeakingChanged = { speaking -> isSpeaking = speaking }
        )
    }

    LaunchedEffect(enableVoice) {
        voiceEngine.isMuted = !enableVoice
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceEngine.shutdown()
        }
    }

    // 3D Y-axis rotation state
    val rotationYAnim = remember { Animatable(0f) }
    var dragRotationY by remember { mutableFloatStateOf(0f) }

    // Blinking loop
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((2800..4600).random().toLong())
            isBlinking = true
            delay(140)
            isBlinking = false
        }
    }

    // Auto-return to IDLE for timed state expressions
    LaunchedEffect(mood, interactionTick) {
        val autoReturnMoods = setOf(
            RobotMood.HAPPY,
            RobotMood.DANCING,
            RobotMood.SAD,
            RobotMood.SPINNING,
            RobotMood.LOVE_STRUCK,
            RobotMood.SLEEPY
        )
        if (mood in autoReturnMoods) {
            val duration = when (mood) {
                RobotMood.DANCING -> 5400L
                RobotMood.SLEEPY -> 5200L
                RobotMood.LOVE_STRUCK -> 4200L
                else -> 3800L
            }
            delay(duration)
            mood = RobotMood.IDLE
            speechText = listOf(
                "Ready for our next adventure? ✨",
                "Tap my hoverboard to see me walk sideways!",
                "Double-tap my tummy for a moon jump! 🚀",
                "Floating in refractive liquid glass 💧"
            ).random()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "robot_life")

    // Snappier, lively breathing cycle (3100ms)
    val breathPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breath_phase"
    )

    // Lively dance groove (700ms)
    val dancePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dance_phase"
    )

    val talkPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 230, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "talk_phase"
    )

    val sadSlouch by animateFloatAsState(
        targetValue = if (mood == RobotMood.SAD || mood == RobotMood.SLEEPY) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "sad_slouch"
    )

    val happyBounce by animateFloatAsState(
        targetValue = if (mood == RobotMood.HAPPY || mood == RobotMood.LOVE_STRUCK) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "happy_bounce"
    )

    // ==================== 1. FOOT BALL TRICK KINEMATICS (SCALED UP) ====================
    val p = footTrickAnim.value
    val isDoingFootTrick = mood == RobotMood.FOOT_TRICK && p > 0.001f

    var trickFootOffsetX = 0f
    var trickFootOffsetY = 0f
    var trickBallMorph = 0f
    var trickFootSpin = 0f
    var trickHandCatch = 0f
    var trickBendDown = 0f

    if (isDoingFootTrick) {
        when {
            p < 0.35f -> {
                val t = (p / 0.35f).coerceIn(0f, 1f)
                trickFootOffsetY = if (t < 0.6f) {
                    val upT = t / 0.6f
                    -172f * sin(upT * (PI / 2)).toFloat()
                } else {
                    val downT = (t - 0.6f) / 0.4f
                    -172f + (86f * sin(downT * (PI / 2)).toFloat())
                }
                trickFootOffsetX = (-activeFootSide * 40f) * sin(t * (PI / 2)).toFloat()
                trickBallMorph = sin(t * (PI / 2)).toFloat()
                trickFootSpin = t * 540f * activeFootSide
                trickHandCatch = ((t - 0.25f) / 0.75f).coerceIn(0f, 1f)
                trickBendDown = -0.22f * sin(t * PI).toFloat()
            }

            p < 0.58f -> {
                val t = ((p - 0.35f) / 0.23f).coerceIn(0f, 1f)
                trickFootOffsetY = -86f + (11f * sin(t * PI * 2f).toFloat())
                trickFootOffsetX = -activeFootSide * 40f
                trickBallMorph = 1f
                trickFootSpin = 540f * activeFootSide
                trickHandCatch = 1f
                trickBendDown = 0.06f * sin(t * PI).toFloat()
            }

            p < 0.86f -> {
                val t = ((p - 0.58f) / 0.28f).coerceIn(0f, 1f)
                val smoothT = ((1f - cos(t * PI)) / 2f).toFloat()
                trickFootOffsetY = -86f * (1f - smoothT)
                trickFootOffsetX = (-activeFootSide * 40f) * (1f - smoothT)
                trickBallMorph = 1f - smoothT
                trickFootSpin = 540f * activeFootSide * (1f - smoothT)
                trickHandCatch = 1f - (0.3f * smoothT)
                trickBendDown = sin(t * (PI / 2)).toFloat() * 1.15f
            }

            else -> {
                val t = ((p - 0.86f) / 0.14f).coerceIn(0f, 1f)
                val smoothUp = ((1f + cos(t * PI)) / 2f).toFloat()
                trickFootOffsetY = 0f
                trickFootOffsetX = 0f
                trickBallMorph = 0f
                trickFootSpin = 0f
                trickHandCatch = 0.7f * smoothUp
                trickBendDown = smoothUp
            }
        }
    }

    // ==================== 2. SIDEWAYS WALKING KINEMATICS ====================
    val walkP = walkAnim.value
    val isWalking = mood == RobotMood.WALKING_SIDEWAYS && walkP > 0.001f

    // Smooth patrol: Center -> Left (-56dp) -> Right (+56dp) -> Center
    val walkOffsetX = if (isWalking) {
        -56f * sin(walkP * 2f * PI).toFloat()
    } else {
        0f
    }
    // Direction of travel (-1 = moving left, +1 = moving right)
    val walkDirection = if (isWalking) {
        -cos(walkP * 2f * PI).toFloat()
    } else {
        0f
    }
    // 8 brisk steps across the patrol cycle
    val stepCycle = if (isWalking) sin(walkP * 8f * PI).toFloat() else 0f
    val leftLegWalkY = if (isWalking) (-14f * stepCycle).coerceAtLeast(0f) else 0f
    val rightLegWalkY = if (isWalking) (14f * stepCycle).coerceAtLeast(0f) else 0f
    val leftFootWalkX = if (isWalking) 8f * stepCycle else 0f
    val rightFootWalkX = if (isWalking) -8f * stepCycle else 0f
    val walkBodyBobY = if (isWalking) -5f * abs(cos(walkP * 8f * PI).toFloat()) else 0f
    val walkBodyTiltZ = if (isWalking) (7f * walkDirection) + (3.5f * stepCycle) else 0f

    // ==================== 3. SUPER JUMP + 360 FLIP KINEMATICS ====================
    val jumpP = superJumpAnim.value
    val isJumping = mood == RobotMood.SUPER_JUMP && jumpP > 0.001f

    var jumpOffsetY = 0f
    var jumpFlipRotationX = 0f
    var jumpSquashX = 1f
    var jumpSquashY = 1f

    if (isJumping) {
        when {
            // Phase 1: Deep anticipation crouch
            jumpP < 0.20f -> {
                val t = jumpP / 0.20f
                val crouch = sin(t * (PI / 2)).toFloat()
                jumpOffsetY = 18f * crouch
                jumpSquashX = 1f + 0.14f * crouch
                jumpSquashY = 1f - 0.14f * crouch
            }
            // Phase 2: High launch + 360° mid-air flip
            jumpP < 0.80f -> {
                val t = (jumpP - 0.20f) / 0.60f
                jumpOffsetY = 18f * (1f - t) - 115f * sin(t * PI).toFloat()
                jumpFlipRotationX = 360f * ((1f - cos(t * PI)) / 2f).toFloat()
                val stretch = sin(t * PI).toFloat() * 0.08f
                jumpSquashX = 1f - stretch
                jumpSquashY = 1f + stretch
            }
            // Phase 3: Cushioned landing bounce
            else -> {
                val t = (jumpP - 0.80f) / 0.20f
                val impact = sin(t * PI).toFloat()
                jumpOffsetY = 10f * impact
                jumpSquashX = 1f + 0.10f * impact
                jumpSquashY = 1f - 0.10f * impact
            }
        }
    }

    // ==================== 4. TICKLE, WAVE & BELLY POKE KINEMATICS ====================
    val tickleProgress = tickleAnim.value
    val isTickling = mood == RobotMood.TICKLE && tickleProgress > 0.001f
    val tickleWiggle = if (isTickling) sin(tickleProgress * PI * 8).toFloat() * 18f else 0f

    val waveProgress = waveAnim.value
    val isWaving = mood == RobotMood.WAVE_HELLO && waveProgress > 0.001f
    val waveSwing = if (isWaving) sin(waveProgress * PI * 8).toFloat() * 28f else 0f

    val bellyProgress = bellyPokeAnim.value
    val isBellyPoked = mood == RobotMood.BELLY_POKE && bellyProgress > 0.001f
    val bellyScaleBump = if (isBellyPoked) 1f + (0.24f * sin(bellyProgress * PI).toFloat()) else 1f

    val isDancing = mood == RobotMood.DANCING
    val danceSin = if (isDancing) sin(dancePhase.toDouble()).toFloat() else 0f
    val danceCos = if (isDancing) cos(dancePhase.toDouble()).toFloat() else 0f
    val breathSin = sin(breathPhase.toDouble()).toFloat()
    val talkSin = if (isSpeaking) abs(sin(talkPhase.toDouble()).toFloat()) else 0f

    val headOffsetY = (-3.2f * breathSin) +
            (10f * sadSlouch) -
            (7.5f * happyBounce) +
            (4f * abs(danceSin)) +
            (26f * trickBendDown) +
            walkBodyBobY -
            (if (isBellyPoked) 12f * sin(bellyProgress * PI).toFloat() else 0f)

    val headRotationZ = (9f * danceSin) +
            tickleWiggle +
            walkBodyTiltZ +
            (-8f * sadSlouch) +
            (6f * happyBounce * breathSin) +
            (if (mood == RobotMood.LOVE_STRUCK) 9f * breathSin else 0f) +
            (activeFootSide * 9f * trickBendDown.coerceAtLeast(0f))

    val stomachOffsetY = (-1.6f * breathSin) +
            (6f * sadSlouch) +
            (3.5f * abs(danceCos)) +
            walkBodyBobY +
            (18f * trickBendDown.coerceAtLeast(0f))

    val loveBeat = if (mood == RobotMood.LOVE_STRUCK) 0.08f * abs(sin(breathPhase * 3.0).toFloat()) else 0f
    val stomachScale = (1f + (0.03f * breathSin) + (0.05f * abs(danceSin)) + loveBeat) * bellyScaleBump

    // Hand angles for wave, walking, tickle, foot catch, jump, and dance
    val leftHandAngle = when {
        isDoingFootTrick -> (-42f * trickHandCatch) + (18f * trickBendDown.coerceAtLeast(0f))
        isWaving && activeWaveSide == -1 -> -135f + waveSwing
        isTickling -> -68f + (26f * sin(tickleProgress * PI * 6).toFloat())
        isJumping -> -125f * sin(jumpP * PI).toFloat()
        isWalking -> -24f + (28f * stepCycle)
        mood == RobotMood.DANCING -> -36f + (40f * danceSin)
        mood == RobotMood.HAPPY || mood == RobotMood.LOVE_STRUCK -> -40f + (15f * breathSin)
        mood == RobotMood.SAD || mood == RobotMood.SLEEPY -> 14f
        else -> -9f + (5f * breathSin)
    }

    val rightHandAngle = when {
        isDoingFootTrick -> (42f * trickHandCatch) - (18f * trickBendDown.coerceAtLeast(0f))
        isWaving && activeWaveSide == 1 -> 135f + waveSwing
        isTickling -> 68f - (26f * sin(tickleProgress * PI * 6).toFloat())
        isJumping -> 125f * sin(jumpP * PI).toFloat()
        isWalking -> 24f + (28f * stepCycle)
        mood == RobotMood.DANCING -> 36f + (40f * danceCos)
        mood == RobotMood.HAPPY || mood == RobotMood.LOVE_STRUCK -> 40f - (15f * breathSin)
        mood == RobotMood.SAD || mood == RobotMood.SLEEPY -> -14f
        else -> 9f - (5f * breathSin)
    }

    val handCatchOffsetX = 18f * trickHandCatch
    val handBendOffsetY = 28f * trickBendDown.coerceAtLeast(0f)

    val leftLegLiftY = (if (isDancing) (-8f * danceSin).coerceAtLeast(0f) else 0f) + leftLegWalkY
    val rightLegLiftY = (if (isDancing) (8f * danceSin).coerceAtLeast(0f) else 0f) + rightLegWalkY

    // ==================== INTERACTION TRIGGERS ====================
    fun triggerMood(newMood: RobotMood, messages: List<String>) {
        val chosen = messages.random()
        mood = newMood
        speechText = chosen
        interactionTick++
        voiceEngine.speak(chosen, newMood)
    }

    fun triggerFootTrick(side: Int) {
        if (mood == RobotMood.FOOT_TRICK && footTrickAnim.isRunning) return
        interactionTick++
        activeFootSide = side
        val chosen = listOf(
            "Wheeee! High bubble bounce! Catching it now! 🫧",
            "Up, up, and away! Reaching down to put my foot back! ✨",
            "Highest bounce in the liquid glass lab! 💙"
        ).random()
        mood = RobotMood.FOOT_TRICK
        speechText = chosen
        voiceEngine.speak(chosen, RobotMood.FOOT_TRICK)

        scope.launch {
            footTrickAnim.snapTo(0f)
            footTrickAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 3200, easing = LinearEasing)
            )
            footTrickAnim.snapTo(0f)
            mood = RobotMood.IDLE
        }
    }

    fun triggerWalkSideways() {
        if (mood == RobotMood.WALKING_SIDEWAYS && walkAnim.isRunning) return
        interactionTick++
        val chosen = listOf(
            "Step, step, waddle! Walking sideways on my glass pad! 🐾",
            "Left patrol, right patrol! Look at my little feet go! ✨",
            "Sideways moonwalk in liquid glass! 🫧"
        ).random()
        mood = RobotMood.WALKING_SIDEWAYS
        speechText = chosen
        voiceEngine.speak(chosen, RobotMood.WALKING_SIDEWAYS)

        scope.launch {
            walkAnim.snapTo(0f)
            walkAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 3600, easing = LinearEasing)
            )
            walkAnim.snapTo(0f)
            mood = RobotMood.IDLE
        }
    }

    fun triggerSuperJump() {
        if (mood == RobotMood.SUPER_JUMP && superJumpAnim.isRunning) return
        interactionTick++
        val chosen = listOf(
            "3, 2, 1... Anti-gravity somersault! 🚀",
            "Whoa! Full 360 flip in mid-air! ✨",
            "Bouncy glass moon jump! Stuck the landing! 💙"
        ).random()
        mood = RobotMood.SUPER_JUMP
        speechText = chosen
        voiceEngine.speak(chosen, RobotMood.SUPER_JUMP)

        scope.launch {
            superJumpAnim.snapTo(0f)
            superJumpAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1650, easing = FastOutSlowInEasing)
            )
            superJumpAnim.snapTo(0f)
            mood = RobotMood.IDLE
        }
    }

    fun triggerTickle() {
        if (mood == RobotMood.TICKLE && tickleAnim.isRunning) return
        interactionTick++
        val chosen = listOf(
            "Hahaha! That tickles my glass fingers! 🤭",
            "Teehee! Stop tickling, my core is sparkling! ✨",
            "Bubbly giggles incoming! 💙"
        ).random()
        mood = RobotMood.TICKLE
        speechText = chosen
        voiceEngine.speak(chosen, RobotMood.TICKLE)

        scope.launch {
            tickleAnim.snapTo(0f)
            tickleAnim.animateTo(1f, tween(durationMillis = 1800, easing = LinearEasing))
            tickleAnim.snapTo(0f)
            mood = RobotMood.IDLE
        }
    }

    fun triggerWaveHello(side: Int = 1) {
        if (mood == RobotMood.WAVE_HELLO && waveAnim.isRunning) return
        interactionTick++
        activeWaveSide = side
        val chosen = listOf(
            "Hi there, best friend! Sending you a big glass wave! 👋",
            "Hello human! High-five from the future! ✨",
            "Waving at you with my bubble hand! 💙"
        ).random()
        mood = RobotMood.WAVE_HELLO
        speechText = chosen
        voiceEngine.speak(chosen, RobotMood.WAVE_HELLO)

        scope.launch {
            waveAnim.snapTo(0f)
            waveAnim.animateTo(1f, tween(durationMillis = 1900, easing = LinearEasing))
            waveAnim.snapTo(0f)
            mood = RobotMood.IDLE
        }
    }

    fun triggerBellyPoke() {
        if (mood == RobotMood.BELLY_POKE && bellyPokeAnim.isRunning) return
        interactionTick++
        val chosen = listOf(
            "Boop! My energy core is super charged! ⚡",
            "Hehe, my tummy is full of liquid starlight! ✨",
            "That felt like a bouncy bubble hug! 💙"
        ).random()
        mood = RobotMood.BELLY_POKE
        speechText = chosen
        voiceEngine.speak(chosen, RobotMood.BELLY_POKE)

        scope.launch {
            bellyPokeAnim.snapTo(0f)
            bellyPokeAnim.animateTo(1f, tween(durationMillis = 1200, easing = FastOutSlowInEasing))
            bellyPokeAnim.snapTo(0f)
            mood = RobotMood.IDLE
        }
    }

    fun triggerLaserScan() {
        if (mood == RobotMood.LASER_SCAN && laserScanAnim.isRunning) return
        interactionTick++
        val chosen = listOf(
            "Scanning human vibe... 100 percent awesome detected! 🔍",
            "Running holographic friendliness diagnostics! ✨",
            "Beep boop! Stress levels dropping, calm levels rising! 💎"
        ).random()
        mood = RobotMood.LASER_SCAN
        speechText = chosen
        voiceEngine.speak(chosen, RobotMood.LASER_SCAN)

        scope.launch {
            laserScanAnim.snapTo(0f)
            laserScanAnim.animateTo(1f, tween(durationMillis = 2200, easing = LinearEasing))
            laserScanAnim.snapTo(0f)
            mood = RobotMood.IDLE
        }
    }

    fun triggerSpin() {
        interactionTick++
        val chosen = listOf(
            "Wheee! Smooth 360 spin! 🌀",
            "Spinning in liquid glass! ✨",
            "Look, no wires! Just pure bubbles!"
        ).random()
        mood = RobotMood.SPINNING
        speechText = chosen
        voiceEngine.speak(chosen, RobotMood.SPINNING)

        scope.launch {
            rotationYAnim.snapTo(0f)
            rotationYAnim.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
            )
            rotationYAnim.snapTo(0f)
        }
    }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val shouldSpin = abs(dragRotationY) > 55f
                        scope.launch {
                            rotationYAnim.snapTo(dragRotationY)
                            dragRotationY = 0f
                            if (shouldSpin) {
                                triggerSpin()
                            } else {
                                rotationYAnim.animateTo(
                                    0f,
                                    spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)
                                )
                            }
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    dragRotationY = (dragRotationY + dragAmount * 0.5f).coerceIn(-70f, 70f)
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. SPEECH BUBBLE (Single-tap -> Speak | Double-tap -> Laser Scan)
        GlassBubble(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .height(42.dp)
                .gestureClickable(
                    onTap = { voiceEngine.speak(speechText, mood) },
                    onDoubleTap = { triggerLaserScan() }
                ),
            cornerRadius = 21.dp,
            tintAlpha = 0.06f
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = speechText,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(180)) },
                    label = "speech_bubble_text"
                ) { text ->
                    Text(
                        text = text,
                        color = Color.White.copy(alpha = 0.94f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 2. ENLARGED ROBOT BODY CONTAINER (240dp x 295dp)
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(295.dp)
                .offset(
                    x = walkOffsetX.dp,
                    y = jumpOffsetY.dp
                )
                .graphicsLayer {
                    rotationY = rotationYAnim.value + dragRotationY
                    rotationX = jumpFlipRotationX
                    scaleX = jumpSquashX
                    scaleY = jumpSquashY
                    cameraDistance = 16f * density
                },
            contentAlignment = Alignment.TopCenter
        ) {
            // ================= HEAD & HEADPHONES =================
            Box(
                modifier = Modifier
                    .offset(y = headOffsetY.dp)
                    .graphicsLayer {
                        rotationZ = headRotationZ
                        rotationX = -12f * trickBendDown.coerceAtLeast(0f)
                    },
                contentAlignment = Alignment.Center
            ) {
                // Headphone Top Band Arc (Tap -> Super Jump!)
                Canvas(
                    modifier = Modifier
                        .size(width = 156.dp, height = 120.dp)
                        .offset(y = (-10).dp)
                        .noRippleClickable { triggerSuperJump() }
                ) {
                    drawArc(
                        brush = Brush.horizontalGradient(
                            listOf(
                                CyanBright.copy(alpha = 0.38f),
                                Color.White.copy(alpha = 0.20f),
                                CyanBright.copy(alpha = 0.38f)
                            )
                        ),
                        startAngle = 195f,
                        sweepAngle = 150f,
                        useCenter = false,
                        style = Stroke(width = 6.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Left Headphone Ear Bubble (Tap -> SAD | Long-Press -> SLEEPY)
                GlassBubble(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = (-12).dp)
                        .size(width = 22.dp, height = 52.dp)
                        .gestureClickable(
                            onTap = {
                                triggerMood(
                                    RobotMood.SAD,
                                    listOf(
                                        "Aww, my headphone ear feels shy... 🥺",
                                        "Pat my head to cheer me up please! 💧"
                                    )
                                )
                            },
                            onLongPress = {
                                triggerMood(
                                    RobotMood.SLEEPY,
                                    listOf(
                                        "Yaaawn... taking a cozy glass power nap... 💤",
                                        "Dreaming of electric bubbles... Zzz 🌙"
                                    )
                                )
                            }
                        ),
                    cornerRadius = 12.dp,
                    tintAlpha = 0.08f
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(width = 5.dp, height = 26.dp)
                            .clip(CircleShape)
                            .background(CyanBright.copy(alpha = 0.65f))
                    )
                }

                // Right Headphone Ear Bubble (Tap -> SAD | Long-Press -> SLEEPY)
                GlassBubble(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 12.dp)
                        .size(width = 22.dp, height = 52.dp)
                        .gestureClickable(
                            onTap = {
                                triggerMood(
                                    RobotMood.SAD,
                                    listOf(
                                        "Feeling a tiny bit blue today... 🥺",
                                        "Tap my head for a big smile! 💙"
                                    )
                                )
                            },
                            onLongPress = {
                                triggerMood(
                                    RobotMood.SLEEPY,
                                    listOf(
                                        "So sleepy... recharge mode activated... 💤",
                                        "Floating into dreamland... Zzz ✨"
                                    )
                                )
                            }
                        ),
                    cornerRadius = 12.dp,
                    tintAlpha = 0.08f
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(width = 5.dp, height = 26.dp)
                            .clip(CircleShape)
                            .background(CyanBright.copy(alpha = 0.65f))
                    )
                }

                // Main Head Bubble (Tap -> HAPPY | Double-Tap / Long-Press -> LOVE_STRUCK)
                GlassBubble(
                    modifier = Modifier
                        .size(width = 134.dp, height = 108.dp)
                        .gestureClickable(
                            onTap = {
                                triggerMood(
                                    RobotMood.HAPPY,
                                    listOf(
                                        "Hehe, head pats make me glow! ✨",
                                        "Yay! Happiness sensors at 100 percent! 💙",
                                        "You are my favorite human!"
                                    )
                                )
                            },
                            onDoubleTap = {
                                triggerMood(
                                    RobotMood.LOVE_STRUCK,
                                    listOf(
                                        "Aww! You make my glass heart melt! 💖",
                                        "Sending you maximum robo-love! 🥰",
                                        "Heart sensors overflowing with sparkles! ✨"
                                    )
                                )
                            },
                            onLongPress = {
                                triggerMood(
                                    RobotMood.LOVE_STRUCK,
                                    listOf(
                                        "So much love in the glass lab! 💙",
                                        "Best companion ever! ✨"
                                    )
                                )
                            }
                        ),
                    cornerRadius = 36.dp,
                    tintAlpha = 0.07f
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(13.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color(0xFF040B1A).copy(alpha = 0.58f)),
                        contentAlignment = Alignment.Center
                    ) {
                        RobotEyesCanvas(
                            mood = mood,
                            isBlinking = isBlinking,
                            isSpeaking = isSpeaking,
                            talkSin = talkSin,
                            dancePhase = dancePhase,
                            breathSin = breathSin,
                            trickFootOffsetY = trickFootOffsetY,
                            walkDirection = walkDirection,
                            laserProgress = laserScanAnim.value
                        )
                    }
                }
            }

            // ================= TORSO / STOMACH & HANDS =================
            Box(
                modifier = Modifier
                    .offset(y = (118f + stomachOffsetY).dp)
                    .width(196.dp),
                contentAlignment = Alignment.Center
            ) {
                // Left Hand Bubble (Tap -> TICKLE | Double-Tap -> WAVE_HELLO)
                GlassBubble(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(
                            x = (8f + handCatchOffsetX).dp,
                            y = ((if (isDancing) -6f * danceSin else 0f) + handBendOffsetY).dp
                        )
                        .graphicsLayer {
                            rotationZ = leftHandAngle
                            transformOrigin = TransformOrigin(0.5f, 0.15f)
                        }
                        .size(width = 30.dp, height = 58.dp)
                        .gestureClickable(
                            onTap = { triggerTickle() },
                            onDoubleTap = { triggerWaveHello(side = -1) }
                        ),
                    cornerRadius = 15.dp,
                    tintAlpha = 0.07f
                ) {}

                // Stomach / Torso Bubble (Tap -> BELLY POKE | Double-Tap -> SUPER JUMP | Long-Press -> DANCE)
                GlassBubble(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = stomachScale
                            scaleY = stomachScale
                            rotationZ = -headRotationZ * 0.35f
                            rotationX = -10f * trickBendDown.coerceAtLeast(0f)
                        }
                        .size(width = 94.dp, height = 76.dp)
                        .gestureClickable(
                            onTap = { triggerBellyPoke() },
                            onDoubleTap = { triggerSuperJump() },
                            onLongPress = {
                                triggerMood(
                                    RobotMood.DANCING,
                                    listOf(
                                        "Grooving in liquid glass! Let's dance! 🎶",
                                        "Bouncing to the cyber beat! 🕺✨",
                                        "Bubble party mode activated! 💙"
                                    )
                                )
                            }
                        ),
                    cornerRadius = 30.dp,
                    tintAlpha = 0.06f
                ) {
                    val coreColor = when (mood) {
                        RobotMood.SAD, RobotMood.SLEEPY -> Color(0xFF4A6FA5)
                        RobotMood.LOVE_STRUCK -> Color(0xFFFF6B9D)
                        RobotMood.HAPPY, RobotMood.DANCING, RobotMood.FOOT_TRICK,
                        RobotMood.TICKLE, RobotMood.BELLY_POKE, RobotMood.SUPER_JUMP,
                        RobotMood.WALKING_SIDEWAYS -> CyanBright
                        else -> CyanGlow
                    }
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val pulse = if (isSpeaking) 0.26f * talkSin else 0.14f * breathSin
                        val radius = (14.dp.toPx()) * (1f + pulse)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    coreColor.copy(alpha = 0.68f),
                                    coreColor.copy(alpha = 0.0f)
                                ),
                                center = center,
                                radius = radius * 1.9f
                            ),
                            radius = radius * 1.9f,
                            center = center
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.48f),
                            radius = radius * 0.36f,
                            center = center
                        )
                    }
                }

                // Right Hand Bubble (Tap -> TICKLE | Double-Tap -> WAVE_HELLO)
                GlassBubble(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(
                            x = (-8f - handCatchOffsetX).dp,
                            y = ((if (isDancing) 6f * danceSin else 0f) + handBendOffsetY).dp
                        )
                        .graphicsLayer {
                            rotationZ = rightHandAngle
                            transformOrigin = TransformOrigin(0.5f, 0.15f)
                        }
                        .size(width = 30.dp, height = 58.dp)
                        .gestureClickable(
                            onTap = { triggerTickle() },
                            onDoubleTap = { triggerWaveHello(side = 1) }
                        ),
                    cornerRadius = 15.dp,
                    tintAlpha = 0.07f
                ) {}
            }

            // ================= LEGS & FEET BUBBLES =================
            Row(
                modifier = Modifier.offset(y = (198f + sadSlouch * 5f).dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.Top
            ) {
                val isLeftFootActive = isDoingFootTrick && activeFootSide == -1
                val isRightFootActive = isDoingFootTrick && activeFootSide == 1

                // Left Leg + Left Foot (Tap -> High Snappy Foot Bounce)
                Column(
                    modifier = Modifier
                        .offset(y = (-leftLegLiftY).dp)
                        .graphicsLayer {
                            rotationZ = when {
                                isDancing -> 7f * danceSin
                                isWalking -> 10f * stepCycle
                                else -> 0f
                            }
                        }
                        .noRippleClickable { triggerFootTrick(side = -1) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlassBubble(
                        modifier = Modifier
                            .offset(y = (if (isLeftFootActive) 7f * trickBendDown.coerceAtLeast(0f) else 0f).dp)
                            .size(width = 30.dp, height = 35.dp),
                        cornerRadius = 13.dp,
                        tintAlpha = 0.06f
                    ) {}

                    Spacer(modifier = Modifier.height(4.dp))

                    val leftFootW = 50f - (15f * if (isLeftFootActive) trickBallMorph else 0f)
                    val leftFootH = 25f + (10f * if (isLeftFootActive) trickBallMorph else 0f)
                    val leftFootCorner = 14f + (4f * if (isLeftFootActive) trickBallMorph else 0f)

                    GlassBubble(
                        modifier = Modifier
                            .offset(
                                x = ((if (isLeftFootActive) trickFootOffsetX else 0f) + leftFootWalkX).dp,
                                y = (if (isLeftFootActive) trickFootOffsetY else 0f).dp
                            )
                            .graphicsLayer {
                                rotationZ = if (isLeftFootActive) trickFootSpin else 0f
                            }
                            .size(width = leftFootW.dp, height = leftFootH.dp),
                        cornerRadius = leftFootCorner.dp,
                        tintAlpha = if (isLeftFootActive) 0.12f else 0.08f
                    ) {}
                }

                // Right Leg + Right Foot (Tap -> High Snappy Foot Bounce)
                Column(
                    modifier = Modifier
                        .offset(y = (-rightLegLiftY).dp)
                        .graphicsLayer {
                            rotationZ = when {
                                isDancing -> -7f * danceSin
                                isWalking -> -10f * stepCycle
                                else -> 0f
                            }
                        }
                        .noRippleClickable { triggerFootTrick(side = 1) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlassBubble(
                        modifier = Modifier
                            .offset(y = (if (isRightFootActive) 7f * trickBendDown.coerceAtLeast(0f) else 0f).dp)
                            .size(width = 30.dp, height = 35.dp),
                        cornerRadius = 13.dp,
                        tintAlpha = 0.06f
                    ) {}

                    Spacer(modifier = Modifier.height(4.dp))

                    val rightFootW = 50f - (15f * if (isRightFootActive) trickBallMorph else 0f)
                    val rightFootH = 25f + (10f * if (isRightFootActive) trickBallMorph else 0f)
                    val rightFootCorner = 14f + (4f * if (isRightFootActive) trickBallMorph else 0f)

                    GlassBubble(
                        modifier = Modifier
                            .offset(
                                x = ((if (isRightFootActive) trickFootOffsetX else 0f) + rightFootWalkX).dp,
                                y = (if (isRightFootActive) trickFootOffsetY else 0f).dp
                            )
                            .graphicsLayer {
                                rotationZ = if (isRightFootActive) trickFootSpin else 0f
                            }
                            .size(width = rightFootW.dp, height = rightFootH.dp),
                        cornerRadius = rightFootCorner.dp,
                        tintAlpha = if (isRightFootActive) 0.12f else 0.08f
                    ) {}
                }
            }

            // ================= HOVERBOARD / BASE GLASS PAD (Tap -> Walk Sideways!) =================
            GlassBubble(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(width = 186.dp, height = 20.dp)
                    .gestureClickable(
                        onTap = { triggerWalkSideways() },
                        onDoubleTap = { triggerSuperJump() }
                    ),
                cornerRadius = 50.dp,
                tintAlpha = 0.05f
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    CyanGlow.copy(alpha = if (isWalking) 0.40f else 0.24f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        // 3. OPTIONAL QUICK-INTERACTION GLASS BAR
        if (showQuickActions) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickActionGlassPill("Walk ↔") { triggerWalkSideways() }
                QuickActionGlassPill("Jump 🚀") { triggerSuperJump() }
                QuickActionGlassPill("Wave 👋") { triggerWaveHello(1) }
                QuickActionGlassPill("Dance 🎶") {
                    triggerMood(
                        RobotMood.DANCING,
                        listOf(
                            "Grooving in liquid glass! 🎶",
                            "Look at those bubble moves! ✨"
                        )
                    )
                }
                QuickActionGlassPill("Spin 🌀") { triggerSpin() }
            }
        }
    }
}

@Composable
private fun QuickActionGlassPill(
    label: String,
    onClick: () -> Unit
) {
    GlassBubble(
        modifier = Modifier
            .height(30.dp)
            .noRippleClickable(onClick = onClick),
        cornerRadius = 15.dp,
        tintAlpha = 0.06f
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp),
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Draws the glowing Cyan eyes, expressions, laser scan, and animated talking mouth inside the head visor.
 */
@Composable
private fun RobotEyesCanvas(
    mood: RobotMood,
    isBlinking: Boolean,
    isSpeaking: Boolean,
    talkSin: Float,
    dancePhase: Float,
    breathSin: Float,
    trickFootOffsetY: Float,
    walkDirection: Float,
    laserProgress: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val eyeLookX = if (mood == RobotMood.WALKING_SIDEWAYS) {
            walkDirection * 8.dp.toPx()
        } else {
            0f
        }
        val eyeLookY = if (mood == RobotMood.FOOT_TRICK) {
            (trickFootOffsetY / 172f) * 6.dp.toPx()
        } else {
            0f
        }

        val leftCenter = Offset(w * 0.33f + eyeLookX, h * 0.45f + eyeLookY)
        val rightCenter = Offset(w * 0.67f + eyeLookX, h * 0.45f + eyeLookY)
        val eyeWidth = w * 0.24f
        val eyeHeight = if (isBlinking && mood == RobotMood.IDLE) h * 0.05f else h * 0.32f

        // Top camera sensor dot
        drawCircle(
            color = Color.White.copy(alpha = 0.22f),
            radius = 3.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.13f)
        )

        // Ambient eye glow
        val glowColor = if (mood == RobotMood.LOVE_STRUCK) Color(0xFFFF6B9D) else CyanGlow
        listOf(leftCenter, rightCenter).forEach { center ->
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.45f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = eyeWidth * 1.2f
                ),
                radius = eyeWidth * 1.2f,
                center = center
            )
        }

        when (mood) {
            RobotMood.IDLE, RobotMood.WALKING_SIDEWAYS -> {
                listOf(leftCenter, rightCenter).forEach { c ->
                    drawRoundRect(
                        color = CyanBright,
                        topLeft = Offset(c.x - eyeWidth / 2f, c.y - eyeHeight / 2f),
                        size = Size(eyeWidth, eyeHeight),
                        cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                    )
                }
            }

            RobotMood.HAPPY, RobotMood.TICKLE, RobotMood.SUPER_JUMP -> {
                listOf(leftCenter, rightCenter).forEach { c ->
                    val path = Path().apply {
                        moveTo(c.x - eyeWidth * 0.48f, c.y + eyeHeight * 0.15f)
                        quadraticTo(
                            c.x, c.y - eyeHeight * 0.58f,
                            c.x + eyeWidth * 0.48f, c.y + eyeHeight * 0.15f
                        )
                    }
                    drawPath(
                        path = path,
                        color = CyanBright,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            RobotMood.WAVE_HELLO -> {
                // Left eye normal, right eye happy wink
                drawRoundRect(
                    color = CyanBright,
                    topLeft = Offset(leftCenter.x - eyeWidth / 2f, leftCenter.y - eyeHeight / 2f),
                    size = Size(eyeWidth, eyeHeight),
                    cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                )
                val winkPath = Path().apply {
                    moveTo(rightCenter.x - eyeWidth * 0.46f, rightCenter.y + eyeHeight * 0.1f)
                    quadraticTo(
                        rightCenter.x, rightCenter.y - eyeHeight * 0.52f,
                        rightCenter.x + eyeWidth * 0.46f, rightCenter.y + eyeHeight * 0.1f
                    )
                }
                drawPath(
                    path = winkPath,
                    color = CyanBright,
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            RobotMood.LOVE_STRUCK -> {
                val heartScale = 1f + 0.10f * abs(breathSin)
                listOf(leftCenter, rightCenter).forEach { c ->
                    val r = (eyeWidth * 0.52f) * heartScale
                    val heartPath = Path().apply {
                        moveTo(c.x, c.y + r * 0.65f)
                        cubicTo(
                            c.x - r * 1.2f, c.y + r * 0.05f,
                            c.x - r * 0.65f, c.y - r * 0.85f,
                            c.x, c.y - r * 0.25f
                        )
                        cubicTo(
                            c.x + r * 0.65f, c.y - r * 0.85f,
                            c.x + r * 1.2f, c.y + r * 0.05f,
                            c.x, c.y + r * 0.65f
                        )
                        close()
                    }
                    drawPath(
                        path = heartPath,
                        color = Color(0xFFFF7AA8)
                    )
                }
            }

            RobotMood.SLEEPY -> {
                // Closed downward sleeping arcs + floating "Z"
                listOf(leftCenter, rightCenter).forEach { c ->
                    val sleepPath = Path().apply {
                        moveTo(c.x - eyeWidth * 0.45f, c.y - eyeHeight * 0.05f)
                        quadraticTo(
                            c.x, c.y + eyeHeight * 0.42f,
                            c.x + eyeWidth * 0.45f, c.y - eyeHeight * 0.05f
                        )
                    }
                    drawPath(
                        path = sleepPath,
                        color = CyanBright.copy(alpha = 0.78f),
                        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                // Floating Z glyph in top-right of visor
                val zOffsetY = -4.dp.toPx() * breathSin
                val zx = w * 0.78f
                val zy = h * 0.24f + zOffsetY
                val zs = 6.dp.toPx()
                val zPath = Path().apply {
                    moveTo(zx, zy)
                    lineTo(zx + zs, zy)
                    lineTo(zx, zy + zs)
                    lineTo(zx + zs, zy + zs)
                }
                drawPath(
                    path = zPath,
                    color = CyanBright.copy(alpha = 0.75f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            RobotMood.FOOT_TRICK, RobotMood.BELLY_POKE, RobotMood.LASER_SCAN -> {
                listOf(leftCenter, rightCenter).forEach { c ->
                    drawCircle(
                        color = CyanBright,
                        radius = eyeWidth * 0.48f,
                        center = c
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = eyeWidth * 0.15f,
                        center = Offset(c.x - eyeWidth * 0.14f, c.y - eyeWidth * 0.14f)
                    )
                }
            }

            RobotMood.DANCING -> {
                val bounce = sin(dancePhase.toDouble()).toFloat() * 3.dp.toPx()
                drawRoundRect(
                    color = CyanBright,
                    topLeft = Offset(leftCenter.x - eyeWidth / 2f, leftCenter.y - eyeHeight / 2f + bounce),
                    size = Size(eyeWidth, eyeHeight * 0.85f),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
                drawRoundRect(
                    color = CyanBright,
                    topLeft = Offset(rightCenter.x - eyeWidth / 2f, rightCenter.y - eyeHeight / 2f - bounce),
                    size = Size(eyeWidth, eyeHeight * 0.85f),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }

            RobotMood.SAD -> {
                listOf(leftCenter to -1f, rightCenter to 1f).forEach { (c, dir) ->
                    val path = Path().apply {
                        moveTo(c.x - eyeWidth * 0.45f, c.y + dir * 3.5.dp.toPx())
                        lineTo(c.x + eyeWidth * 0.45f, c.y - dir * 3.5.dp.toPx())
                    }
                    drawPath(
                        path = path,
                        color = CyanBright.copy(alpha = 0.85f),
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                val tearOffsetY = ((breathSin + 1f) * 4.5.dp.toPx())
                drawCircle(
                    color = CyanBright.copy(alpha = 0.75f),
                    radius = 3.5.dp.toPx(),
                    center = Offset(leftCenter.x - 5.dp.toPx(), leftCenter.y + 12.dp.toPx() + tearOffsetY)
                )
            }

            RobotMood.SPINNING -> {
                listOf(leftCenter, rightCenter).forEach { c ->
                    drawCircle(
                        color = CyanBright,
                        radius = eyeWidth * 0.46f,
                        center = c,
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = c
                    )
                }
            }
        }

        // Holographic sweeping scan bar during LASER_SCAN
        if (mood == RobotMood.LASER_SCAN && laserProgress > 0.001f) {
            val scanY = h * (0.12f + 0.76f * abs(sin(laserProgress * PI * 2).toFloat()))
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, CyanBright, Color.White, CyanBright, Color.Transparent)
                ),
                start = Offset(w * 0.08f, scanY),
                end = Offset(w * 0.92f, scanY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Animated cute glowing mouth
        val mouthCenterY = h * 0.80f
        if (isSpeaking) {
            val mouthW = w * (0.18f + 0.10f * talkSin)
            val mouthH = h * (0.06f + 0.12f * talkSin)
            drawRoundRect(
                color = CyanBright,
                topLeft = Offset(w * 0.5f - mouthW / 2f, mouthCenterY - mouthH / 2f),
                size = Size(mouthW, mouthH),
                cornerRadius = CornerRadius(mouthH / 2f, mouthH / 2f)
            )
        } else if (
            mood == RobotMood.HAPPY ||
            mood == RobotMood.FOOT_TRICK ||
            mood == RobotMood.TICKLE ||
            mood == RobotMood.BELLY_POKE ||
            mood == RobotMood.WALKING_SIDEWAYS ||
            mood == RobotMood.WAVE_HELLO ||
            mood == RobotMood.LOVE_STRUCK ||
            mood == RobotMood.SUPER_JUMP
        ) {
            val smilePath = Path().apply {
                moveTo(w * 0.42f, mouthCenterY - 2.dp.toPx())
                quadraticTo(w * 0.5f, mouthCenterY + 6.dp.toPx(), w * 0.58f, mouthCenterY - 2.dp.toPx())
            }
            drawPath(
                path = smilePath,
                color = if (mood == RobotMood.LOVE_STRUCK) Color(0xFFFF7AA8) else CyanBright.copy(alpha = 0.85f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Pure, borderless refractive liquid glass bubble building block for the robot's body parts.
 */
@Composable
private fun GlassBubble(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    tintAlpha: Float = 0.05f,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val liquidState = LocalLiquidState.current

    val glassModifier = if (liquidState != null) {
        modifier.liquid(liquidState) {
            this.shape = shape
            frost = 0.dp
            refraction = 0.52f
            curve = 0.65f
            edge = 0.0f // Strictly 0f for zero white or black border
            tint = Color.White.copy(alpha = tintAlpha)
            saturation = 1.35f
            dispersion = 0.28f
        }
    } else {
        modifier.liquidGlassCardShader(
            cornerRadius = cornerRadius,
            refractionAmount = 5.dp,
            aberration = 0.20f
        )
    }

    Box(
        modifier = glassModifier
            .clip(shape)
            .background(Color.White.copy(alpha = tintAlpha)),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    return this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@Composable
private fun Modifier.gestureClickable(
    onTap: () -> Unit,
    onDoubleTap: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
): Modifier {
    return this.pointerInput(onTap, onDoubleTap, onLongPress) {
        detectTapGestures(
            onTap = { onTap() },
            onDoubleTap = onDoubleTap?.let { callback -> { callback() } },
            onLongPress = onLongPress?.let { callback -> { callback() } }
        )
    }
}