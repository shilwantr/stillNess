package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * 15 Distinct Emotional States matching EMO robot's behavioral palette.
 */
enum class EmoMood(
    val color: Color
) {
    NORMAL(Color(0xFF00E5FF)),      // Pure EMO Cyber Cyan
    HAPPY(Color(0xFF00F5D4)),       // Joyful Aqua
    LAUGH(Color(0xFF18FFFF)),       // Electric Cyan
    SAD(Color(0xFF2979FF)),         // Melancholy Oceanic Blue
    SLEEP(Color(0xFF00B0FF)),       // Deep Dream Azure
    WAKE_UP(Color(0xFF64FFDA)),     // Soft Dawn Turquoise
    ANGRY(Color(0xFF00E5FF)),       // Pure EMO Cyber Cyan (Anger expressed through geometry)
    SURPRISED(Color(0xFF80DEEA)),   // Dilated Electric Ice
    CONFUSED(Color(0xFFA7FFEB)),    // Mint Inquisitive
    LOVE(Color(0xFFFF2A85)),        // Radiant Passion Pink
    CURIOUS(Color(0xFF00E5FF)),     // Cyber Tracking Cyan
    CRYING(Color(0xFF1E88E5)),      // Watery Cobalt
    DIZZY(Color(0xFF76FF03)),       // Kinetic Acid Lime
    EXCITED(Color(0xFF00E5FF)),     // Sparkle Cyan
    WINK(Color(0xFF00E5FF))         // Playful Cyan
}

/**
 * 100% Pure OLED EMO Robot Face.
 * Zero UI clutter. Pure pitch black screen (#000000) except for the eyes and tight neon glow.
 * Eyes placed at ~30% from the top with wide separation and authentic OLED micro-matrix texture.
 */
@Composable
fun EmoRobotScreen(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val scope = rememberCoroutineScope()

    // 1. Force Fullscreen Immersive Mode (hide status bar, navigation bar, punch-hole cutouts)
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

    // 2. Cute robot pet SFX synthesizer (pure chirps, trills, purrs, and emotional audio - no speech narration)
    var isThinking by remember { mutableStateOf(false) }
    val robotAudio = remember { EmoPureAudioEngine() }

    DisposableEffect(Unit) {
        onDispose {
            robotAudio.shutdown()
        }
    }

    // 3. Emotional State
    var mood by remember {
        val moodStr = activity?.intent?.getStringExtra("EMO_MOOD")
        val parsedMood = if (moodStr != null) {
            try { EmoMood.valueOf(moodStr.uppercase()) } catch (_: Exception) { EmoMood.NORMAL }
        } else {
            EmoMood.NORMAL
        }
        mutableStateOf(parsedMood)
    }

    // Dynamic broadcast listener for runtime mood changes: am broadcast -a com.example.SET_EMO_MOOD -p com.aistudio.stillwellness.vxqkr --es mood ANGRY
    DisposableEffect(Unit) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: android.content.Intent?) {
                val moodStr = intent?.getStringExtra("mood")
                if (moodStr != null) {
                    try {
                        val newMood = EmoMood.valueOf(moodStr.uppercase())
                        mood = newMood
                        robotAudio.playMoodAudio(newMood)
                    } catch (_: Exception) {}
                }
            }
        }
        val filter = android.content.IntentFilter("com.example.SET_EMO_MOOD")
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(receiver, filter)
            }
        } catch (_: Exception) {}
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    // Screen Backlight State (0.0f = pitch black screen when asleep, 1.0f = screen awake)
    val screenBacklight = remember { Animatable(1f) }

    // Natural Blinking & Eyelid Aperture (1.0 = fully open, 0.0 = fully closed)
    val leftBlinkAnim = remember { Animatable(1f) }
    val rightBlinkAnim = remember { Animatable(1f) }

    // Decoupled Coherent Binocular Gaze Dynamics (independent ballistic trajectory per eye)
    val leftLookXAnim = remember { Animatable(0f) }
    val leftLookYAnim = remember { Animatable(0f) }
    val rightLookXAnim = remember { Animatable(0f) }
    val rightLookYAnim = remember { Animatable(0f) }

    // Touch Following Physics
    val dragXAnim = remember { Animatable(0f) }
    val dragYAnim = remember { Animatable(0f) }

    // Hardware Sensors (Accelerometer & Gyroscope Physics)
    val tiltXAnim = remember { Animatable(0f) }
    val tiltYAnim = remember { Animatable(0f) }
    val gyroZAnim = remember { Animatable(0f) }

    // 3b. Real-time Hardware Accelerometer & Gyroscope Listener
    val sensorManager = remember(context) {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }
    val accelerometer = remember(sensorManager) {
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }
    val gyroscope = remember(sensorManager) {
        sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    DisposableEffect(sensorManager, accelerometer, gyroscope) {
        var lastShakeTime = 0L
        var isUpsideDown = false
        var filteredAx = 0f
        var filteredAy = 0f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        val ax = event.values[0]
                        val ay = event.values[1]
                        val az = event.values[2]

                        // Low-pass filter for smooth organic inertia
                        filteredAx = filteredAx * 0.85f + ax * 0.15f
                        filteredAy = filteredAy * 0.85f + ay * 0.15f

                        // Physical gravity pull:
                        // Tilting phone right (ax < 0) pulls eyes right (+X)
                        // Tilting phone left (ax > 0) pulls eyes left (-X)
                        val targetTiltX = (-filteredAx * 3.6f).coerceIn(-35f, 35f)
                        val targetTiltY = ((filteredAy - 7.5f) * 2.8f).coerceIn(-25f, 25f)

                        scope.launch {
                            tiltXAnim.animateTo(targetTiltX, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))
                        }
                        scope.launch {
                            tiltYAnim.animateTo(targetTiltY, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))
                        }

                        // Shake Detection (Vigorous acceleration jerk)
                        val magnitude = kotlin.math.sqrt(ax * ax + ay * ay + az * az)
                        val now = System.currentTimeMillis()
                        if (magnitude > 22.0f && now - lastShakeTime > 3500L && mood != EmoMood.SLEEP) {
                            lastShakeTime = now
                            mood = EmoMood.DIZZY
                            robotAudio.playMoodAudio(EmoMood.DIZZY)
                            scope.launch {
                                delay(3800)
                                if (mood == EmoMood.DIZZY) mood = EmoMood.NORMAL
                            }
                        }

                        // Upside Down detection (ay < -4.0 m/s^2)
                        if (ay < -4.0f && !isUpsideDown && mood != EmoMood.SLEEP) {
                            isUpsideDown = true
                            mood = EmoMood.SURPRISED
                            robotAudio.playMoodAudio(EmoMood.SURPRISED)
                        } else if (ay > 2.0f && isUpsideDown) {
                            isUpsideDown = false
                            if (mood == EmoMood.SURPRISED) {
                                mood = EmoMood.NORMAL
                                robotAudio.playChirp(700f, 1050f, 180)
                            }
                        }
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        val gz = event.values[2] // Angular velocity around Z (yaw/roll spin)
                        val targetRoll = (-gz * 4.2f).coerceIn(-12f, 12f)
                        scope.launch {
                            gyroZAnim.animateTo(targetRoll, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium))
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null) {
            accelerometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
            gyroscope?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // Laugh / Jitter vibration
    val jitterYAnim = remember { Animatable(0f) }

    // Continuous Living Micro-Drift (Eyes are ALWAYS subtly moving like real human/pet eyes)
    val infiniteTransition = rememberInfiniteTransition(label = "EmoLivingDynamics")

    // Decoupled Asynchronous Ocular Micro-Drifts (continuous smooth sine ease-in-out turnaround)
    val leftMicroDriftX by infiniteTransition.animateFloat(
        initialValue = -2.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LeftMicroDriftX"
    )
    val leftMicroDriftY by infiniteTransition.animateFloat(
        initialValue = -1.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LeftMicroDriftY"
    )
    val rightMicroDriftX by infiniteTransition.animateFloat(
        initialValue = -2.0f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RightMicroDriftX"
    )
    val rightMicroDriftY by infiniteTransition.animateFloat(
        initialValue = -1.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RightMicroDriftY"
    )

    // Breathing scale pulsation (subtle expansion & contraction)
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingScale"
    )

    // Holographic ChatGPT spiral rotation on left eye
    val spiralRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpiralRotation"
    )

    // Dizzy rotation angle (inertial pendulum swing)
    val dizzyAngle by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DizzyAngle"
    )

    // Heartbeat pulse for LOVE
    val heartPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HeartPulse"
    )

    // Expressive Emotional Personality Palette (Activated on tap)
    val moodReactions = remember {
        listOf(
            EmoMood.ANGRY,
            EmoMood.HAPPY,
            EmoMood.CURIOUS,
            EmoMood.LOVE,
            EmoMood.SURPRISED,
            EmoMood.LAUGH,
            EmoMood.WINK,
            EmoMood.EXCITED,
            EmoMood.CONFUSED,
            EmoMood.SAD,
            EmoMood.DIZZY
        )
    }
    var emotionIndex by remember { mutableIntStateOf(0) }

    // Trigger an expressive emotional reaction with pure robotic SFX (no speech narration)
    fun triggerNextEmotion() {
        if (isThinking) return
        val targetMood = moodReactions[emotionIndex % moodReactions.size]
        emotionIndex++

        scope.launch {
            // 1. Thinking / inquisitive chirping phase (shows ChatGPT spiral on left eye)
            isThinking = true
            mood = EmoMood.CURIOUS
            robotAudio.playChirp(720f, 1080f, 130)
            delay(1000)
            isThinking = false

            // 2. Transition directly into the emotion with distinct robotic SFX
            mood = targetMood
            robotAudio.playMoodAudio(targetMood)

            if (targetMood != EmoMood.SLEEP) {
                delay(3200)
                if (mood == targetMood) {
                    mood = EmoMood.NORMAL
                }
            }
        }
    }

    // 4. Laugh vibration loop (smooth organic harmonic bounce instead of linear zigzag)
    LaunchedEffect(mood) {
        if (mood == EmoMood.LAUGH) {
            while (isActive && mood == EmoMood.LAUGH) {
                jitterYAnim.animateTo(-4.2f, tween(52, easing = FastOutSlowInEasing))
                jitterYAnim.animateTo(4.2f, tween(52, easing = FastOutSlowInEasing))
            }
        } else {
            if (jitterYAnim.value != 0f) {
                jitterYAnim.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
            }
        }
    }

    // 5. Backlight & Sleep / Wake-up transition (powers down screen on sleep, wakes up smoothly)
    LaunchedEffect(mood) {
        when (mood) {
            EmoMood.SLEEP -> {
                coroutineScope {
                    launch { leftBlinkAnim.animateTo(0.0f, tween(320, easing = FastOutSlowInEasing)) }
                    launch { rightBlinkAnim.animateTo(0.0f, tween(320, easing = FastOutSlowInEasing)) }
                }
                delay(200)
                screenBacklight.animateTo(0.0f, tween(1100, easing = FastOutSlowInEasing))
            }
            EmoMood.WAKE_UP -> {
                screenBacklight.animateTo(1.0f, tween(800, easing = FastOutSlowInEasing))
                delay(250)
                coroutineScope {
                    launch { leftBlinkAnim.animateTo(0.35f, tween(260, easing = FastOutSlowInEasing)) }
                    launch { rightBlinkAnim.animateTo(0.35f, tween(260, easing = FastOutSlowInEasing)) }
                }
                delay(140)
                coroutineScope {
                    launch { leftBlinkAnim.animateTo(0.06f, tween(120, easing = FastOutSlowInEasing)) }
                    launch { rightBlinkAnim.animateTo(0.06f, tween(120, easing = FastOutSlowInEasing)) }
                }
                delay(100)
                coroutineScope {
                    launch { leftBlinkAnim.animateTo(1.0f, tween(320, easing = FastOutSlowInEasing)) }
                    launch { rightBlinkAnim.animateTo(1.0f, tween(320, easing = FastOutSlowInEasing)) }
                }
                delay(1200)
                mood = EmoMood.NORMAL
            }
            EmoMood.WINK -> {
                coroutineScope {
                    launch { leftBlinkAnim.animateTo(1.0f, tween(140, easing = FastOutSlowInEasing)) }
                    launch { rightBlinkAnim.animateTo(0.04f, tween(140, easing = FastOutSlowInEasing)) }
                }
            }
            else -> {
                if (screenBacklight.value < 1f) {
                    screenBacklight.animateTo(1.0f, tween(400, easing = FastOutSlowInEasing))
                }
                if (leftBlinkAnim.value < 0.95f || rightBlinkAnim.value < 0.95f) {
                    coroutineScope {
                        launch { leftBlinkAnim.animateTo(1.0f, tween(180, easing = FastOutSlowInEasing)) }
                        launch { rightBlinkAnim.animateTo(1.0f, tween(180, easing = FastOutSlowInEasing)) }
                    }
                }
            }
        }
    }

    // 6. Natural Organic Blinking Loop (FluxGarage / RobotEye / Biological model)
    LaunchedEffect(mood) {
        if (mood != EmoMood.SLEEP && mood != EmoMood.WAKE_UP && mood != EmoMood.WINK) {
            while (isActive) {
                // Natural variable pause between blinks: 2.2s to 5.6s
                delay(Random.nextLong(2200, 5600))
                if (mood != EmoMood.SLEEP && mood != EmoMood.WAKE_UP && mood != EmoMood.LAUGH && mood != EmoMood.WINK) {
                    val roll = Random.nextFloat()
                    if (roll < 0.20f) {
                        // 20% chance: Partial Blink (closes to 40%-55% then reopens quickly)
                        executeOrganicBlink(
                            leftBlink = leftBlinkAnim,
                            rightBlink = rightBlinkAnim,
                            isPartial = true
                        )
                    } else {
                        // 80% chance: Full natural organic blink
                        executeOrganicBlink(
                            leftBlink = leftBlinkAnim,
                            rightBlink = rightBlinkAnim,
                            isPartial = false
                        )

                        // 22% chance of Double Blink following full blink
                        if (Random.nextFloat() < 0.22f) {
                            delay(Random.nextLong(90, 140))
                            executeOrganicBlink(
                                leftBlink = leftBlinkAnim,
                                rightBlink = rightBlinkAnim,
                                isPartial = false,
                                closeMs = Random.nextInt(48, 65),
                                openMs = Random.nextInt(75, 100)
                            )
                        }
                    }
                }
            }
        }
    }

    // 7. Organic Coherent Saccades & Fixation Dynamics
    // - Ballistic gaze shifts with natural settling
    // - Brief fixation before looking elsewhere (1.8s - 4.2s)
    // - Leading eye phase asymmetry & optical parallax disparity
    // - Strict boundary clamping (-30dp to +30dp horizontal, -16dp to +14dp vertical)
    LaunchedEffect(mood) {
        if (mood != EmoMood.SLEEP) {
            while (isActive) {
                // Natural fixation interval: the robot's gaze settles on a focal point
                delay(Random.nextLong(1800, 4200))

                if (dragXAnim.value == 0f && dragYAnim.value == 0f && mood != EmoMood.SLEEP) {
                    val (baseTargetX, baseTargetY) = when (Random.nextInt(8)) {
                        0 -> Pair(0f, 0f)         // Look forward/center
                        1 -> Pair(0f, 0f)         // Look forward/center
                        2 -> Pair(-22f, -4f)      // Glance upper left
                        3 -> Pair(22f, -4f)       // Glance upper right
                        4 -> Pair(-26f, 6f)       // Glance lower left
                        5 -> Pair(26f, 6f)        // Glance lower right
                        6 -> Pair(-18f, 0f)       // Glance left
                        else -> Pair(18f, 0f)     // Glance right
                    }

                    // Parallax & Leading Eye Vergence Asymmetry:
                    // When glancing left: left eye leads and has slightly larger excursion (1.05x vs 0.93x)
                    // When glancing right: right eye leads (1.05x vs 0.93x)
                    val (leftTargetX, rightTargetX) = when {
                        baseTargetX < -4f -> Pair(baseTargetX * 1.05f, baseTargetX * 0.93f)
                        baseTargetX > 4f -> Pair(baseTargetX * 0.93f, baseTargetX * 1.05f)
                        else -> Pair(0f, 0f)
                    }

                    val leftTargetY = baseTargetY
                    val rightTargetY = baseTargetY

                    // Natural saccade spring: crisp ballistic acceleration + soft settling snap
                    val saccadeSpec = spring<Float>(
                        dampingRatio = 0.82f,
                        stiffness = 240f
                    )

                    val isLeftLeading = baseTargetX < 0f
                    val asyncLagMs = if (baseTargetX != 0f) Random.nextLong(10, 20) else 0L

                    coroutineScope {
                        launch {
                            if (!isLeftLeading && asyncLagMs > 0) delay(asyncLagMs)
                            leftLookXAnim.animateTo(leftTargetX, saccadeSpec)
                        }
                        launch {
                            if (!isLeftLeading && asyncLagMs > 0) delay(asyncLagMs)
                            leftLookYAnim.animateTo(leftTargetY, saccadeSpec)
                        }
                        launch {
                            if (isLeftLeading && asyncLagMs > 0) delay(asyncLagMs)
                            rightLookXAnim.animateTo(rightTargetX, saccadeSpec)
                        }
                        launch {
                            if (isLeftLeading && asyncLagMs > 0) delay(asyncLagMs)
                            rightLookYAnim.animateTo(rightTargetY, saccadeSpec)
                        }
                    }
                }
            }
        } else {
            // Smoothly return gaze to center when sleeping
            coroutineScope {
                launch { leftLookXAnim.animateTo(0f, tween(300, easing = FastOutSlowInEasing)) }
                launch { leftLookYAnim.animateTo(0f, tween(300, easing = FastOutSlowInEasing)) }
                launch { rightLookXAnim.animateTo(0f, tween(300, easing = FastOutSlowInEasing)) }
                launch { rightLookYAnim.animateTo(0f, tween(300, easing = FastOutSlowInEasing)) }
            }
        }
    }

    // Initial startup greeting chirp (pure cute robotic SFX)
    LaunchedEffect(Unit) {
        delay(600)
        robotAudio.playChirp(520f, 880f, 120)
        delay(40)
        robotAudio.playChirp(880f, 1320f, 160)
    }

    // Dynamic single color throughout the eye
    val currentEyeColor by animateColorAsState(
        targetValue = mood.color,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "EyeColor"
    )

    // =========================================================================
    // 100% PURE BLACK OLED CANVAS - ZERO CLUTTER
    // =========================================================================
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Every pixel is pure #000000 black
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        if (mood != EmoMood.SLEEP) {
                            mood = EmoMood.CURIOUS
                            robotAudio.playChirp(720f, 960f, 140)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            dragXAnim.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                        }
                        scope.launch {
                            dragYAnim.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                        }
                        if (mood == EmoMood.CURIOUS) {
                            mood = EmoMood.NORMAL
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            val newX = (dragXAnim.value + dragAmount.x * 0.40f).coerceIn(-50f, 50f)
                            val newY = (dragYAnim.value + dragAmount.y * 0.40f).coerceIn(-30f, 30f)
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
                        robotAudio.playMoodAudio(EmoMood.LOVE)
                        scope.launch {
                            delay(3200)
                            if (mood == EmoMood.LOVE) mood = EmoMood.NORMAL
                        }
                    },
                    onLongPress = {
                        if (mood == EmoMood.SLEEP) {
                            mood = EmoMood.WAKE_UP
                            robotAudio.playMoodAudio(EmoMood.WAKE_UP)
                        } else {
                            mood = EmoMood.SLEEP
                            robotAudio.playMoodAudio(EmoMood.SLEEP)
                        }
                    },
                    onTap = {
                        if (mood == EmoMood.SLEEP) {
                            mood = EmoMood.WAKE_UP
                            robotAudio.playMoodAudio(EmoMood.WAKE_UP)
                        } else {
                            // Tap invokes emotional reaction sequence with cute robot SFX
                            triggerNextEmotion()
                        }
                    }
                )
            }
    ) {
        // Physical forces & shared posture
        val sharedTiltX = tiltXAnim.value
        val sharedTiltY = jitterYAnim.value + tiltYAnim.value
        val sharedDragX = dragXAnim.value
        val sharedDragY = dragYAnim.value
        val emotionTilt by animateFloatAsState(
            targetValue = when (mood) {
                EmoMood.CONFUSED -> 14f
                else -> 0f
            },
            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow),
            label = "EmotionTilt"
        )
        val totalTilt = (dragXAnim.value * 0.08f) + gyroZAnim.value + emotionTilt + if (mood == EmoMood.DIZZY) dizzyAngle else 0f

        // Independent Gaze + Micro-Drift + Touch + Tilt offsets per eye (safely clamped within screen boundaries)
        val leftEyeOffsetX = (leftLookXAnim.value + leftMicroDriftX + sharedTiltX + sharedDragX).coerceIn(-48f, 48f)
        val leftEyeOffsetY = (leftLookYAnim.value + leftMicroDriftY + sharedTiltY + sharedDragY).coerceIn(-30f, 30f)

        val rightEyeOffsetX = (rightLookXAnim.value + rightMicroDriftX + sharedTiltX + sharedDragX).coerceIn(-48f, 48f)
        val rightEyeOffsetY = (rightLookYAnim.value + rightMicroDriftY + sharedTiltY + sharedDragY).coerceIn(-30f, 30f)

        // Correct Eye Placement: ~30% from the top (as seen in actual EMO robot)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.30f))

            // Container with shared tilt & breathing dynamics
            Box(
                modifier = Modifier
                    .rotate(totalTilt)
                    .scale(breathingScale * if (mood == EmoMood.LOVE) heartPulse else 1f),
                contentAlignment = Alignment.Center
            ) {
                // Substantial Separation: 54dp between eyes (matching EMO proportions)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(54.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Eye (independent gaze & offset)
                    PureImoEye(
                        isLeft = true,
                        mood = mood,
                        eyeColor = currentEyeColor,
                        blinkAperture = leftBlinkAnim.value,
                        gazeX = leftLookXAnim.value + sharedDragX,
                        gazeY = leftLookYAnim.value + sharedDragY,
                        offsetX = leftEyeOffsetX,
                        offsetY = leftEyeOffsetY,
                        backlightAlpha = screenBacklight.value,
                        isThinking = isThinking,
                        spiralRotation = spiralRotation
                    )

                    // Right Eye (independent gaze & offset)
                    PureImoEye(
                        isLeft = false,
                        mood = mood,
                        eyeColor = currentEyeColor,
                        blinkAperture = rightBlinkAnim.value,
                        gazeX = rightLookXAnim.value + sharedDragX,
                        gazeY = rightLookYAnim.value + sharedDragY,
                        offsetX = rightEyeOffsetX,
                        offsetY = rightEyeOffsetY,
                        backlightAlpha = screenBacklight.value,
                        isThinking = false,
                        spiralRotation = 0f
                    )
                }

                // Comic-style red anger mark overlay (renders above eyes only when EmoMood.ANGRY is active)
                AngerMarksOverlay(
                    mood = mood,
                    modifier = Modifier.matchParentSize()
                )
            }

            Spacer(modifier = Modifier.weight(0.70f))
        }
    }
}

/**
 * Natural biological/robotic organic blink executor.
 * Supports:
 * - Smooth closing (55-75ms) and reopening (85-115ms)
 * - Natural closed pause (16-26ms)
 * - Decoupled left/right micro-asymmetry (0-10ms)
 * - Partial blinks (eyelid closes to 40%-55% before reopening)
 */
private suspend fun executeOrganicBlink(
    leftBlink: Animatable<Float, AnimationVector1D>,
    rightBlink: Animatable<Float, AnimationVector1D>,
    isPartial: Boolean = false,
    closedTarget: Float = if (isPartial) Random.nextFloat() * 0.15f + 0.40f else 0.02f,
    closeMs: Int = Random.nextInt(55, 75),
    openMs: Int = Random.nextInt(85, 115)
) {
    val asyncLag = Random.nextLong(0, 10)
    val leftFirst = Random.nextBoolean()

    coroutineScope {
        launch {
            if (!leftFirst) delay(asyncLag)
            leftBlink.animateTo(closedTarget, tween(closeMs, easing = FastOutSlowInEasing))
            delay(Random.nextLong(16, 26))
            leftBlink.animateTo(1.0f, tween(openMs, easing = FastOutSlowInEasing))
        }
        launch {
            if (leftFirst) delay(asyncLag)
            rightBlink.animateTo(closedTarget, tween(closeMs, easing = FastOutSlowInEasing))
            delay(Random.nextLong(16, 26))
            rightBlink.animateTo(1.0f, tween(openMs, easing = FastOutSlowInEasing))
        }
    }
}

/**
 * Procedurally builds the organic squoval eye contour, smoothly morphing into the fierce
 * asymmetric angry brow contour as [angryProgress] approaches 1.0f.
 *
 * Left and right eyes are mathematically mirrored across the vertical facial axis:
 * - Upper inner brow slopes downward toward the center of the face (\ /)
 * - Tapered, angular upper inner contour with rounded mechanical transitions
 * - Bottom contour retains authentic organic 42dp squoval curvature
 * - When angryProgress == 0f, produces exact standard rounded rectangle
 */
private fun Density.buildEyeContourPath(
    w: Float,
    h: Float,
    baseR: Float,
    isLeft: Boolean,
    angryProgress: Float
): Path {
    val path = Path()
    val p = angryProgress.coerceIn(0f, 1f)

    // Standard Skia cubic bezier approximation constant for 90-degree circular arcs
    val k = 0.55228475f

    if (p <= 0.001f) {
        // Pure squoval path (Fast path when not angry)
        path.addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                rect = androidx.compose.ui.geometry.Rect(0f, 0f, w, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(baseR, baseR)
            )
        )
        return path
    }

    // Dynamic angry parameters
    val innerDropY = h * 0.44f * p
    val outerDropY = h * 0.07f * p
    val rIn = baseR + (14.dp.toPx() - baseR) * p
    val rOut = baseR + (26.dp.toPx() - baseR) * p

    if (isLeft) {
        // =====================================================================
        // LEFT EYE (Viewer's left):
        // Inner edge is at X = w (facing face center / right).
        // Outer edge is at X = 0 (left).
        // Brow slopes downward from outer-left to inner-right (\).
        // =====================================================================

        // 1. Bottom-Left Corner (Outer Bottom) -> curves from (0, h - baseR) to (baseR, h)
        path.moveTo(0f, h - baseR)
        path.cubicTo(
            0f, h - baseR * (1f - k),
            baseR * (1f - k), h,
            baseR, h
        )

        // 2. Bottom Edge -> horizontal line to (w - baseR, h)
        path.lineTo(w - baseR, h)

        // 3. Bottom-Right Corner (Inner Bottom) -> curves to (w, h - baseR)
        path.cubicTo(
            w - baseR * (1f - k), h,
            w, h - baseR * (1f - k),
            w, h - baseR
        )

        // 4. Inner Vertical Edge (Right side) -> rises up to (w, innerDropY + rIn)
        val innerCornerBottomY = innerDropY + rIn
        path.lineTo(w, innerCornerBottomY)

        // 5. Inner-Top Corner (Right side) -> curves from (w, innerDropY + rIn) to (w - rIn, innerDropY)
        path.cubicTo(
            w, innerDropY + rIn * (1f - k),
            w - rIn * (1f - k), innerDropY,
            w - rIn, innerDropY
        )

        // 6. Top Brow -> slopes upward-left toward outer-top (rOut, outerDropY)
        val dx = (w - rIn) - rOut
        val dy = innerDropY - outerDropY
        path.cubicTo(
            (w - rIn) - dx * 0.35f, innerDropY - dy * 0.28f,
            (w - rIn) - dx * 0.70f, innerDropY - dy * 0.72f,
            rOut, outerDropY
        )

        // 7. Outer-Top Corner (Left side) -> curves from (rOut, outerDropY) to (0, outerDropY + rOut)
        path.cubicTo(
            rOut * (1f - k), outerDropY,
            0f, outerDropY + rOut * (1f - k),
            0f, outerDropY + rOut
        )

        // 8. Outer Vertical Edge (Left side) -> descends to (0, h - baseR)
        path.lineTo(0f, h - baseR)
        path.close()

    } else {
        // =====================================================================
        // RIGHT EYE (Viewer's right):
        // Inner edge is at X = 0 (facing face center / left).
        // Outer edge is at X = w (right).
        // Brow slopes downward from outer-right to inner-left (/).
        // =====================================================================

        // 1. Bottom-Right Corner (Outer Bottom) -> curves from (w, h - baseR) to (w - baseR, h)
        path.moveTo(w, h - baseR)
        path.cubicTo(
            w, h - baseR * (1f - k),
            w - baseR * (1f - k), h,
            w - baseR, h
        )

        // 2. Bottom Edge -> horizontal line to (baseR, h)
        path.lineTo(baseR, h)

        // 3. Bottom-Left Corner (Inner Bottom) -> curves to (0, h - baseR)
        path.cubicTo(
            baseR * (1f - k), h,
            0f, h - baseR * (1f - k),
            0f, h - baseR
        )

        // 4. Inner Vertical Edge (Left side) -> rises up to (0, innerDropY + rIn)
        val innerCornerBottomY = innerDropY + rIn
        path.lineTo(0f, innerCornerBottomY)

        // 5. Inner-Top Corner (Left side) -> curves from (0, innerDropY + rIn) to (rIn, innerDropY)
        path.cubicTo(
            0f, innerDropY + rIn * (1f - k),
            rIn * (1f - k), innerDropY,
            rIn, innerDropY
        )

        // 6. Top Brow -> slopes upward-right toward outer-top (w - rOut, outerDropY)
        val dx = (w - rOut) - rIn
        val dy = innerDropY - outerDropY
        path.cubicTo(
            rIn + dx * 0.35f, innerDropY - dy * 0.28f,
            rIn + dx * 0.70f, innerDropY - dy * 0.72f,
            w - rOut, outerDropY
        )

        // 7. Outer-Top Corner (Right side) -> curves from (w - rOut, outerDropY) to (w, outerDropY + rOut)
        path.cubicTo(
            w - rOut * (1f - k), outerDropY,
            w, outerDropY + rOut * (1f - k),
            w, outerDropY + rOut
        )

        // 8. Outer Vertical Edge (Right side) -> descends to (w, h - baseR)
        path.lineTo(w, h - baseR)
        path.close()
    }

    return path
}

/**
 * Comic-style red anger mark (ikari / pulsing vein symbol) overlay.
 * Renders compact, angular red cross-vein symbols above the robot eyes.
 * Visible ONLY when [mood] is [EmoMood.ANGRY].
 */
@Composable
fun AngerMarksOverlay(
    mood: EmoMood,
    modifier: Modifier = Modifier,
    eyeWidth: Dp = 159.dp,
    eyeHeight: Dp = 153.dp,
    eyeSpacing: Dp = 54.dp
) {
    if (mood != EmoMood.ANGRY) return

    val angerColor = Color(0xFFFF2233) // Intense vibrant comic coral-red
    val glowColor = Color(0xFFFF1744)

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        val rightEyeCenterX = cx + (eyeWidth.toPx() + eyeSpacing.toPx()) / 2f
        val leftEyeCenterX = cx - (eyeWidth.toPx() + eyeSpacing.toPx()) / 2f

        // 1. Primary Anger Mark: Upper-right region of right eye (floating cleanly above outer brow)
        val primaryX = rightEyeCenterX + eyeWidth.toPx() * 0.42f
        val primaryY = cy - eyeHeight.toPx() * 0.56f
        val primarySize = 16.dp.toPx()
        val primaryStroke = 3.6.dp.toPx()

        drawAngerMark(
            centerX = primaryX,
            centerY = primaryY,
            size = primarySize,
            strokeWidth = primaryStroke,
            rotationDeg = 15f,
            color = angerColor,
            glowColor = glowColor
        )

        // 2. Secondary Anger Mark: Smaller, upper-left region of left eye
        val secondaryX = leftEyeCenterX - eyeWidth.toPx() * 0.42f
        val secondaryY = cy - eyeHeight.toPx() * 0.52f
        val secondarySize = 10.5.dp.toPx()
        val secondaryStroke = 2.6.dp.toPx()

        drawAngerMark(
            centerX = secondaryX,
            centerY = secondaryY,
            size = secondarySize,
            strokeWidth = secondaryStroke,
            rotationDeg = -18f,
            color = angerColor,
            glowColor = glowColor
        )
    }
}

/**
 * Draws a single classic anime anger mark (4 curved L-brackets forming an outward-branching vein cross)
 */
private fun DrawScope.drawAngerMark(
    centerX: Float,
    centerY: Float,
    size: Float,
    strokeWidth: Float,
    rotationDeg: Float,
    color: Color,
    glowColor: Color
) {
    val gap = size * 0.22f
    val armLen = size * 0.95f

    val markPath = Path().apply {
        // Top-Left L-bracket: (-armLen, -gap) -> (-gap, -gap) -> (-gap, -armLen)
        moveTo(-armLen, -gap)
        quadraticTo(-gap * 1.35f, -gap * 1.35f, -gap, -armLen)

        // Top-Right L-bracket: (gap, -armLen) -> (gap, -gap) -> (armLen, -gap)
        moveTo(gap, -armLen)
        quadraticTo(gap * 1.35f, -gap * 1.35f, armLen, -gap)

        // Bottom-Right L-bracket: (armLen, gap) -> (gap, gap) -> (gap, armLen)
        moveTo(armLen, gap)
        quadraticTo(gap * 1.35f, gap * 1.35f, gap, armLen)

        // Bottom-Left L-bracket: (-gap, armLen) -> (-gap, gap) -> (-armLen, gap)
        moveTo(-gap, armLen)
        quadraticTo(-gap * 1.35f, gap * 1.35f, -armLen, gap)
    }

    withTransform({
        translate(left = centerX, top = centerY)
        rotate(degrees = rotationDeg, pivot = Offset.Zero)
    }) {
        // Subtle emissive glow rim
        drawPath(
            path = markPath,
            color = glowColor.copy(alpha = 0.28f),
            style = Stroke(
                width = strokeWidth + 4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        // Sharp primary vein stroke
        drawPath(
            path = markPath,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

/**
 * Authentic EMO Robot Eye.
 * - Single uniform vibrant color throughout
 * - Accurate EMO proportions: 159dp x 153dp squoval (42dp corner radius)
 * - Authentic OLED micro-matrix scanline interference pattern
 * - Tight, crisp neon perimeter glow
 * - ChatGPT thinking spiral on left eye
 */
@Composable
private fun PureImoEye(
    isLeft: Boolean,
    mood: EmoMood,
    eyeColor: Color,
    blinkAperture: Float,
    gazeX: Float,
    gazeY: Float,
    offsetX: Float,
    offsetY: Float,
    backlightAlpha: Float,
    isThinking: Boolean,
    spiralRotation: Float
) {
    // EMO eye dimensions: Scaled by 50%
    val eyeWidth: Dp = 159.dp
    val eyeHeight: Dp = 153.dp
    val cornerRadius: Dp = 42.dp

    // Layer 1: Primary Expression Transition (smooth organic squish & stretch spring)
    val targetScaleX = when (mood) {
        EmoMood.SURPRISED -> 0.92f
        EmoMood.LAUGH -> 1.06f
        EmoMood.ANGRY -> 1.05f
        EmoMood.CONFUSED -> if (isLeft) 1.06f else 0.92f
        else -> 1.0f
    }
    val targetScaleY = when (mood) {
        EmoMood.SURPRISED -> 1.20f
        EmoMood.LAUGH -> 0.88f
        EmoMood.ANGRY -> 0.94f
        EmoMood.CONFUSED -> if (isLeft) 1.16f else 0.80f
        else -> 1.0f
    }

    val animScaleX by animateFloatAsState(
        targetValue = targetScaleX,
        animationSpec = spring(dampingRatio = 0.80f, stiffness = Spring.StiffnessMediumLow),
        label = "EyeScaleX"
    )
    val animScaleY by animateFloatAsState(
        targetValue = targetScaleY,
        animationSpec = spring(dampingRatio = 0.80f, stiffness = Spring.StiffnessMediumLow),
        label = "EyeScaleY"
    )

    // Animated progress for ANGRY expression morph (0f = normal squoval, 1f = fierce angry brow)
    val angryProgress by animateFloatAsState(
        targetValue = if (mood == EmoMood.ANGRY) 1.0f else 0.0f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "AngryProgress"
    )

    // Layer 2: Secondary Directional Deformation (continuous optical vergence gradient)
    val gazeFactorX = if (isLeft) -gazeX else gazeX
    val targetGazeDeformX = 1.0f + (gazeFactorX * 0.0009f).coerceIn(-0.022f, 0.025f)
    val targetGazeDeformY = 1.0f - (gazeY * 0.0012f).coerceIn(-0.022f, 0.025f)

    val finalScaleX = animScaleX * targetGazeDeformX
    val finalScaleY = animScaleY * targetGazeDeformY

    // Layer 2: Eyelid vertical gaze coordination (smoothly animated to prevent stutter)
    val targetGazeAperture = when {
        mood == EmoMood.SLEEP || mood == EmoMood.WINK -> 0f
        else -> (-gazeY / 140f).coerceIn(-0.06f, 0.03f)
    }
    val animGazeAperture by animateFloatAsState(
        targetValue = targetGazeAperture,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow),
        label = "GazeAperture"
    )

    // Effective aperture for this individual eye (0.0 = fully shut, 1.0 = fully open)
    val baseAperture = when {
        mood == EmoMood.WINK && !isLeft -> 0.04f
        else -> blinkAperture.coerceIn(0.0f, 1.0f)
    }
    val finalAperture = (baseAperture + animGazeAperture).coerceIn(0.0f, 1.0f)

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(width = eyeWidth, height = eyeHeight)
            .scale(scaleX = finalScaleX, scaleY = finalScaleY),
        contentAlignment = Alignment.Center
    ) {
        // Derive multidimensional luminance palette from base eye color
        val coreLuminance = Color(
            red = (eyeColor.red * 0.70f + 0.30f).coerceIn(0f, 1f),
            green = (eyeColor.green * 0.70f + 0.30f).coerceIn(0f, 1f),
            blue = (eyeColor.blue * 0.70f + 0.30f).coerceIn(0f, 1f),
            alpha = eyeColor.alpha
        )
        val edgeAbsorption = Color(
            red = (eyeColor.red * 0.76f).coerceIn(0f, 1f),
            green = (eyeColor.green * 0.80f).coerceIn(0f, 1f),
            blue = (eyeColor.blue * 0.86f).coerceIn(0f, 1f),
            alpha = eyeColor.alpha
        )

        // 1. Shape-conforming Squoval Glow Bloom (Dynamically contracts as eyelids close)
        if (backlightAlpha > 0.02f && finalAperture > 0.04f) {
            Canvas(modifier = Modifier.size(width = eyeWidth + 36.dp, height = eyeHeight + 36.dp)) {
                val glowCorner = cornerRadius.toPx()
                val baseW = eyeWidth.toPx()
                val baseH = eyeHeight.toPx()
                val glowAlpha = backlightAlpha * (0.20f + 0.80f * finalAperture)

                val leftOffset = (size.width - baseW) / 2f
                val topOffset = (size.height - baseH) / 2f

                // Biological eyelid aperture travel: upper lid 70%, lower lid 30%
                val seamRatio = 0.70f
                val topLidY = baseH * seamRatio * (1f - finalAperture)
                val bottomLidY = baseH - (baseH * (1f - seamRatio)) * (1f - finalAperture)
                val apertureH = (bottomLidY - topLidY).coerceAtLeast(2.dp.toPx())
                val apertureTop = topOffset + topLidY

                if (angryProgress > 0.01f) {
                    val glowPath = buildEyeContourPath(
                        w = baseW,
                        h = baseH,
                        baseR = glowCorner,
                        isLeft = isLeft,
                        angryProgress = angryProgress
                    )
                    val glowShells = listOf(
                        Pair(28.dp.toPx(), 0.04f),
                        Pair(18.dp.toPx(), 0.09f),
                        Pair(9.dp.toPx(), 0.18f),
                        Pair(3.dp.toPx(), 0.32f)
                    )
                    translate(left = leftOffset, top = topOffset) {
                        val drawGlowShells: () -> Unit = {
                            glowShells.forEach { (strokeW, alphaRatio) ->
                                drawPath(
                                    path = glowPath,
                                    color = eyeColor.copy(alpha = alphaRatio * glowAlpha),
                                    style = Stroke(
                                        width = strokeW,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }
                        if (finalAperture < 0.95f) {
                            val glowAperturePath = Path().apply {
                                moveTo(-18.dp.toPx(), topLidY - 14.dp.toPx())
                                lineTo(baseW + 18.dp.toPx(), topLidY - 14.dp.toPx())
                                lineTo(baseW + 18.dp.toPx(), bottomLidY + 14.dp.toPx())
                                lineTo(-18.dp.toPx(), bottomLidY + 14.dp.toPx())
                                close()
                            }
                            clipPath(glowAperturePath) {
                                drawGlowShells()
                            }
                        } else {
                            drawGlowShells()
                        }
                    }
                } else {
                    // 4 progressive squoval bloom shells matching rounded-rectangle contour
                    val shells = listOf(
                        Triple(16.dp.toPx(), 0.04f, 7.dp.toPx()),   // Outer soft atmospheric dispersion
                        Triple(10.dp.toPx(), 0.09f, 4.5.dp.toPx()), // Mid ambient halo
                        Triple(5.dp.toPx(), 0.18f, 2.dp.toPx()),    // Inner tight glow
                        Triple(2.dp.toPx(), 0.32f, 0.8.dp.toPx())   // Razor perimeter rim
                    )

                    shells.forEach { (expand, alphaRatio, rExpand) ->
                        val shellH = apertureH + expand * 2f * finalAperture
                        val shellTop = apertureTop - expand * finalAperture
                        val shellCornerY = (glowCorner * finalAperture).coerceAtLeast(3.dp.toPx())

                        drawRoundRect(
                            color = eyeColor.copy(alpha = alphaRatio * glowAlpha),
                            topLeft = Offset(leftOffset - expand, shellTop),
                            size = Size(baseW + expand * 2f, shellH),
                            cornerRadius = CornerRadius(glowCorner + rExpand, shellCornerY + rExpand)
                        )
                    }
                }
            }
        }

        // 2. Main Eye Surface with Multidimensional Gradient, Dynamic Aperture & Micro-Matrix Texture
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (backlightAlpha <= 0.01f) return@Canvas

            val seamRatio = 0.70f

            // If eyelids are virtually shut, draw clean eyelid seam crease line
            if (finalAperture <= 0.04f && mood != EmoMood.SLEEP) {
                val seamY = h * seamRatio
                drawLine(
                    color = eyeColor.copy(alpha = 0.55f * backlightAlpha),
                    start = Offset(w * 0.16f, seamY),
                    end = Offset(w * 0.84f, seamY),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                return@Canvas
            }

            // Calculate organic eyelid aperture bounds
            val topLidY = h * seamRatio * (1f - finalAperture)
            val bottomLidY = h - (h * (1f - seamRatio)) * (1f - finalAperture)

            val aperturePath = Path().apply {
                val topDip = (bottomLidY - topLidY) * 0.06f
                moveTo(0f, topLidY)
                quadraticTo(w * 0.5f, topLidY + topDip, w, topLidY)
                lineTo(w, bottomLidY)
                quadraticTo(w * 0.5f, bottomLidY - topDip * 0.5f, 0f, bottomLidY)
                close()
            }

            // Dynamic luminous core focal displacement:
            // When gazing in a direction, the internal emissive focal center shifts towards that direction
            // creating natural optical 3D depth and intention without artificial pupils.
            val coreShiftX = (gazeX / 90f).coerceIn(-0.10f, 0.10f)
            val coreShiftY = (gazeY / 70f).coerceIn(-0.08f, 0.08f)
            val coreCenterX = w * (0.48f + coreShiftX)
            val coreCenterY = h * (0.44f + coreShiftY)

            // Multidimensional surface brush: luminous emissive center with subtle perimeter absorption
            val eyeSurfaceBrush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.00f to coreLuminance,
                    0.46f to eyeColor,
                    0.86f to edgeAbsorption,
                    1.00f to edgeAbsorption.copy(alpha = 0.94f)
                ),
                center = Offset(coreCenterX, coreCenterY),
                radius = maxOf(w, h) * 0.70f
            )

            val drawEyeContent: DrawScope.() -> Unit = {
                when (mood) {
                    // =========================================================
                    // HAPPY / LAUGH: Arched Inverted Crescent Smiling Eye (^ ^)
                    // =========================================================
                    EmoMood.HAPPY, EmoMood.LAUGH -> {
                        val strokeW = 28.dp.toPx()
                        val path = Path().apply {
                            val startY = h * 0.76f
                            val peakY = h * 0.16f
                            moveTo(w * 0.10f, startY)
                            quadraticTo(w * 0.50f, peakY, w * 0.90f, startY)
                        }
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(listOf(coreLuminance, eyeColor)),
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }

                    // =========================================================
                    // LOVE: Beating Heart Eye with Luminous Depth
                    // =========================================================
                    EmoMood.LOVE -> {
                        val heartPath = Path().apply {
                            val cx = w / 2f
                            val cy = h / 2f
                            moveTo(cx, cy + h * 0.38f)
                            cubicTo(cx - w * 0.55f, cy + h * 0.02f, cx - w * 0.48f, cy - h * 0.42f, cx, cy - h * 0.18f)
                            cubicTo(cx + w * 0.48f, cy - h * 0.42f, cx + w * 0.55f, cy + h * 0.02f, cx, cy + h * 0.38f)
                            close()
                        }
                        drawPath(path = heartPath, brush = eyeSurfaceBrush, style = Fill)
                    }

                    // =========================================================
                    // SLEEP: Gentle Horizontal Closed Arc (- -)
                    // =========================================================
                    EmoMood.SLEEP -> {
                        val strokeW = 16.dp.toPx()
                        val path = Path().apply {
                            moveTo(w * 0.12f, h * 0.50f)
                            quadraticTo(w * 0.50f, h * 0.58f, w * 0.88f, h * 0.50f)
                        }
                        drawPath(
                            path = path,
                            color = eyeColor.copy(alpha = 0.90f * backlightAlpha),
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }

                    // =========================================================
                    // SAD / CRYING: Drooping Downward Tilted Puppy Eye
                    // =========================================================
                    EmoMood.SAD, EmoMood.CRYING -> {
                        val sadPath = Path().apply {
                            val tilt = if (isLeft) -0.16f else 0.16f
                            val topY = h * (0.24f + tilt)
                            moveTo(w * 0.12f, topY)
                            cubicTo(w * 0.30f, h * 0.08f, w * 0.70f, h * 0.08f, w * 0.88f, h * (0.24f - tilt))
                            cubicTo(w * 0.96f, h * 0.55f, w * 0.88f, h * 0.90f, w * 0.50f, h * 0.94f)
                            cubicTo(w * 0.12f, h * 0.90f, w * 0.04f, h * 0.55f, w * 0.12f, topY)
                            close()
                        }
                        drawPath(path = sadPath, brush = eyeSurfaceBrush, style = Fill)
                    }

                    // =========================================================
                    // DEFAULT / NORMAL / ANGRY / CURIOUS / SURPRISED: Morphing Squoval
                    // =========================================================
                    else -> {
                        val eyePath = buildEyeContourPath(
                            w = w,
                            h = h,
                            baseR = cornerRadius.toPx(),
                            isLeft = isLeft,
                            angryProgress = angryProgress
                        )

                        // 1. Multidimensional surface fill with subtle radial luminance
                        drawPath(path = eyePath, brush = eyeSurfaceBrush, style = Fill)

                        // 2. Refined, subtle OLED micro-matrix raster texture (non-dominating)
                        clipPath(eyePath) {
                            val rasterSpacing = 2.4.dp.toPx()
                            val rasterThickness = 0.75.dp.toPx()
                            var yPos = 0f
                            while (yPos < h) {
                                drawLine(
                                    color = Color.Black.copy(alpha = 0.055f),
                                    start = Offset(0f, yPos),
                                    end = Offset(w, yPos),
                                    strokeWidth = rasterThickness
                                )
                                yPos += rasterSpacing
                            }

                            // Ultra-fine subpixel columns
                            val colSpacing = 3.2.dp.toPx()
                            val colThickness = 0.6.dp.toPx()
                            var xPos = 0f
                            while (xPos < w) {
                                drawLine(
                                    color = Color.Black.copy(alpha = 0.035f),
                                    start = Offset(xPos, 0f),
                                    end = Offset(xPos, h),
                                    strokeWidth = colThickness
                                )
                                xPos += colSpacing
                            }
                        }

                        // 3. Delicate inner perimeter rim shading (subtle glass encapsulation)
                        drawPath(
                            path = eyePath,
                            color = edgeAbsorption.copy(alpha = 0.35f),
                            style = Stroke(width = 1.4.dp.toPx())
                        )

                        // 4. Holographic ChatGPT Spiral on Left Eye when Thinking
                        if (isLeft && isThinking) {
                            rotate(degrees = spiralRotation, pivot = Offset(coreCenterX, coreCenterY)) {
                                drawChatGptSpiral(
                                    center = Offset(coreCenterX, coreCenterY),
                                    radius = w * 0.26f,
                                    color = Color.White.copy(alpha = 0.92f)
                                )
                            }
                        }
                    }
                }
            }

            if (finalAperture < 0.99f && mood != EmoMood.SLEEP) {
                clipPath(aperturePath) {
                    drawEyeContent()
                }
                // Eyelid slit accent line when closing tightly
                if (finalAperture < 0.16f) {
                    drawLine(
                        color = coreLuminance.copy(alpha = (1f - finalAperture / 0.16f) * 0.70f * backlightAlpha),
                        start = Offset(w * 0.15f, topLidY),
                        end = Offset(w * 0.85f, topLidY),
                        strokeWidth = 2.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            } else {
                drawEyeContent()
            }
        }
    }
}

/**
 * Draws the authentic ChatGPT 6-petal spiral geometry on the robot's eye during thinking.
 */
private fun DrawScope.drawChatGptSpiral(
    center: Offset,
    radius: Float,
    color: Color
) {
    val strokeWidth = 2.6.dp.toPx()
    for (i in 0 until 6) {
        val angle = i * 60f
        rotate(degrees = angle, pivot = center) {
            val loopWidth = radius * 0.50f
            val loopHeight = radius * 0.90f
            val left = center.x - loopWidth / 2f
            val top = center.y - loopHeight * 0.68f

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
 * Authentic Cute Robot Pet SFX Synthesizer (Zero narration, 100% expressive robotic sound effects).
 * Synthesizes dynamic electronic chirps, melodic arpeggios, warbles, and emotional cues with
 * warm harmonic overtone synthesis and smooth Hann window amplitude envelopes.
 */
class EmoPureAudioEngine {

    fun playMoodAudio(mood: EmoMood) {
        CoroutineScope(Dispatchers.IO).launch {
            when (mood) {
                // Cheerful 3-note ascending arpeggio (E5 -> A5 -> E6)
                EmoMood.HAPPY -> {
                    playChirp(659f, 880f, 90, 0.22f)
                    delay(30)
                    playChirp(880f, 1318f, 130, 0.24f)
                }
                // Giggling staccato chirps synced with eye jitter
                EmoMood.LAUGH -> {
                    repeat(4) {
                        playChirp(920f, 1280f, 50, 0.22f)
                        delay(35)
                    }
                }
                // Falling melancholy slide
                EmoMood.SAD -> {
                    playChirp(680f, 320f, 360, 0.20f)
                }
                // Gentle sleepy power-down tone
                EmoMood.SLEEP -> {
                    playChirp(440f, 220f, 380, 0.18f)
                    delay(50)
                    playWarble(180f, 6f, 15f, 220, 0.12f)
                }
                // Energetic 2-stage wake-up greeting
                EmoMood.WAKE_UP -> {
                    playChirp(340f, 680f, 140, 0.20f)
                    delay(60)
                    playChirp(680f, 1360f, 200, 0.25f)
                }
                // Low robotic grumble chirp
                EmoMood.ANGRY -> {
                    playChirp(340f, 200f, 160, 0.25f)
                    delay(30)
                    playChirp(260f, 180f, 220, 0.28f)
                }
                // High-velocity upward swoop
                EmoMood.SURPRISED -> {
                    playChirp(420f, 1450f, 190, 0.26f)
                }
                // Inquisitive questioning inflection ("Hmm?")
                EmoMood.CONFUSED -> {
                    playChirp(480f, 520f, 80, 0.18f)
                    delay(40)
                    playChirp(580f, 920f, 160, 0.24f)
                }
                // Sweet harmonic double chime
                EmoMood.LOVE -> {
                    playChirp(587f, 880f, 140, 0.22f)
                    delay(40)
                    playChirp(880f, 1175f, 210, 0.25f)
                }
                // Inquiring double pip
                EmoMood.CURIOUS -> {
                    playChirp(720f, 960f, 100, 0.22f)
                    delay(35)
                    playChirp(880f, 1180f, 120, 0.24f)
                }
                // Descending weeping slide
                EmoMood.CRYING -> {
                    playChirp(620f, 280f, 340, 0.18f)
                    delay(60)
                    playChirp(420f, 240f, 280, 0.16f)
                }
                // Kinetic oscillating warble
                EmoMood.DIZZY -> {
                    playWarble(620f, 14f, 140f, 480, 0.22f)
                }
                // Sparkling 4-note victory flourish
                EmoMood.EXCITED -> {
                    playChirp(784f, 988f, 70, 0.22f)
                    delay(25)
                    playChirp(988f, 1175f, 70, 0.23f)
                    delay(25)
                    playChirp(1175f, 1568f, 140, 0.26f)
                }
                // Crisp playful wink
                EmoMood.WINK -> {
                    playChirp(840f, 1300f, 120, 0.23f)
                }
                // Soft acknowledgment blip
                EmoMood.NORMAL -> {
                    playChirp(750f, 880f, 90, 0.18f)
                }
            }
        }
    }

    fun playChirp(startFreq: Float, endFreq: Float, durationMs: Int, volume: Float = 0.22f) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val freq = startFreq + (endFreq - startFreq) * progress
                val envelope = sin(progress * PI).toFloat()
                // Fundamental sine wave + subtle 2nd harmonic (octave) for warm, cute robot pet timbre
                val sample = sin(2.0 * PI * i * freq / sampleRate).toFloat() +
                             0.12f * sin(4.0 * PI * i * freq / sampleRate).toFloat()
                buffer[i] = (sample * envelope * Short.MAX_VALUE * volume).toInt().coerceIn(-32768, 32767).toShort()
            }

            playBuffer(buffer, sampleRate)
        } catch (_: Exception) {
        }
    }

    fun playWarble(baseFreq: Float, modFreq: Float, modDepth: Float, durationMs: Int, volume: Float = 0.22f) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val t = i.toDouble() / sampleRate
                val currentFreq = baseFreq + modDepth * sin(2.0 * PI * modFreq * t).toFloat()
                val envelope = sin(progress * PI).toFloat()
                val sample = sin(2.0 * PI * i * currentFreq / sampleRate).toFloat() +
                             0.10f * sin(4.0 * PI * i * currentFreq / sampleRate).toFloat()
                buffer[i] = (sample * envelope * Short.MAX_VALUE * volume).toInt().coerceIn(-32768, 32767).toShort()
            }

            playBuffer(buffer, sampleRate)
        } catch (_: Exception) {
        }
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        var track: AudioTrack? = null
        try {
            track = AudioTrack.Builder()
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

            track.write(buffer, 0, buffer.size)
            track.play()
            val durationMs = (buffer.size * 1000L) / sampleRate
            Thread.sleep(durationMs + 12L)
        } catch (_: Exception) {
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (_: Exception) {
            }
        }
    }

    fun shutdown() {
        // Pure SFX engine has no standing background service or memory leak
    }
}
