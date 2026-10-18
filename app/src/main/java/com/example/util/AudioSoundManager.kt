package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * SoundPool-based Piano Chime system playing Jingle Bells pitch sequence for breathing phase changes.
 */
class BreathingPianoChime(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var pianoNoteId: Int = 0
    private var noteIndex = 0

    // Pitch shift multiplier sequence relative to base piano note (E4).
    // E, E, E | E, E, E | E, G, C, D, E | F, F, F, F, F, E, E, E...
    private val jingleBellsPitchSequence = floatArrayOf(
        1.0f,   // Jin-
        1.0f,   // gle
        1.0f,   // bells

        1.0f,   // Jin-
        1.0f,   // gle
        1.0f,   // bells

        1.0f,   // Jin-
        1.189f, // gle (G is 3 semitones up)
        0.793f, // all (C is 4 semitones down)
        0.891f, // the (D is 2 semitones down)
        1.0f,   // way (E)

        1.059f, // Oh 
        1.059f, // what
        1.059f, // fun
        1.059f, // it
        1.0f,   // is 
        1.0f,   // to
        1.0f,   // ride
        1.0f,   // in
        0.891f, // a
        0.891f, // one
        1.0f,   // horse
        0.891f, // open
        1.189f  // sleigh
    )

    init {
        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(attributes)
                .build()

            val rawResId = context.resources.getIdentifier("piano_soft_e", "raw", context.packageName)
            if (rawResId != 0) {
                pianoNoteId = soundPool?.load(context, rawResId, 1) ?: 0
            }
        } catch (_: Exception) {}
    }

    fun getNextPitchRate(): Float {
        val pitchRate = jingleBellsPitchSequence[noteIndex]
        noteIndex = (noteIndex + 1) % jingleBellsPitchSequence.size
        return pitchRate
    }

    fun playNextChime(): Boolean {
        if (pianoNoteId != 0) {
            val pitchRate = getNextPitchRate()
            soundPool?.play(pianoNoteId, 0.85f, 0.85f, 1, 0, pitchRate)
            return true
        }
        return false
    }

    fun resetMelody() {
        noteIndex = 0
    }

    fun release() {
        try {
            soundPool?.release()
        } catch (_: Exception) {}
        soundPool = null
    }
}

class AudioSoundManager(private val context: Context) {

    private val sampleRate = 44100
    private var ambientTrack: AudioTrack? = null
    private var ambientJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private var currentVolume = 0.7f
    val pianoChime = BreathingPianoChime(context)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun playBreathingPhaseChime(frequency: Float = 528f, durationMs: Int = 600) {
        // 1. Play from SoundPool if piano_soft_e sound file exists in res/raw/
        val playedFromSample = pianoChime.playNextChime()

        if (!playedFromSample) {
            // 2. Synthesize warm acoustic piano harmonic chime following the Jingle Bells pitch sequence
            val pitchRate = pianoChime.getNextPitchRate()
            val baseNoteHz = 329.63f * pitchRate // Base E4 = 329.63 Hz

            scope.launch {
                try {
                    val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                    val buffer = ShortArray(numSamples)
                    val baseFreq = baseNoteHz.toDouble()

                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        
                        // Exponential decay envelope mimicking acoustic piano hammer strike
                        val decay = exp(-4.5 * t)
                        val attack = (1.0 - exp(-800.0 * t)).coerceIn(0.0, 1.0)
                        val envelope = decay * attack

                        // Piano harmonic overtone series (Fundamental + 2nd, 3rd, 4th, 5th harmonics)
                        val f1 = sin(2 * PI * baseFreq * t) * 1.00
                        val f2 = sin(2 * PI * baseFreq * 2.0 * t) * 0.45
                        val f3 = sin(2 * PI * baseFreq * 3.0 * t) * 0.20
                        val f4 = sin(2 * PI * baseFreq * 4.0 * t) * 0.10
                        val f5 = sin(2 * PI * baseFreq * 5.0 * t) * 0.05

                        val wave = (f1 + f2 + f3 + f4 + f5) * envelope
                        buffer[i] = (wave * Short.MAX_VALUE * 0.35f).toInt().coerceIn(
                            Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()
                        ).toShort()
                    }

                    val track = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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
                    kotlinx.coroutines.delay(durationMs.toLong() + 50)
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        }

        // Haptic feedback
        triggerSubtleHaptic()
    }

    fun resetBreathingChimeMelody() {
        pianoChime.resetMelody()
    }

    fun triggerSubtleHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    fun startAmbientSound(baseFrequency: Float, secondaryFrequency: Float) {
        stopAmbientSound()

        ambientJob = scope.launch {
            try {
                val bufferSize = sampleRate * 2 // 2 seconds continuous buffer
                val buffer = ShortArray(bufferSize)

                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf.coerceAtLeast(bufferSize * 2))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                ambientTrack = track
                track.setVolume(currentVolume)
                track.play()

                var phase1 = 0.0
                var phase2 = 0.0
                val delta1 = 2 * PI * baseFrequency / sampleRate
                val delta2 = 2 * PI * secondaryFrequency / sampleRate

                while (isActive) {
                    for (i in 0 until bufferSize) {
                        // Gentle ambient chord oscillation
                        val s1 = sin(phase1) * 0.5
                        val s2 = sin(phase2) * 0.3
                        val modulation = 0.8 + 0.2 * sin(phase1 * 0.05)
                        val sample = ((s1 + s2) * modulation * Short.MAX_VALUE * 0.35f).toInt().coerceIn(
                            Short.MIN_VALUE.toInt(),
                            Short.MAX_VALUE.toInt()
                        )
                        buffer[i] = sample.toShort()

                        phase1 += delta1
                        if (phase1 > 2 * PI) phase1 -= 2 * PI
                        phase2 += delta2
                        if (phase2 > 2 * PI) phase2 -= 2 * PI
                    }
                    track.write(buffer, 0, bufferSize)
                }
            } catch (_: Exception) {}
        }
    }

    private var mediaPlayer: android.media.MediaPlayer? = null

    fun playAudioUrl(url: String, loop: Boolean = true, localCachePath: String? = null) {
        stopAmbientSound()
        try {
            mediaPlayer?.release()
            mediaPlayer = android.media.MediaPlayer().apply {
                val dataSource = if (localCachePath != null && java.io.File(localCachePath).exists()) {
                    localCachePath
                } else {
                    url
                }
                setDataSource(dataSource)
                isLooping = loop
                setVolume(currentVolume, currentVolume)
                setOnPreparedListener { start() }
                prepareAsync()
            }
        } catch (_: Exception) {}
    }

    fun stopAudioUrl() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        try {
            ambientTrack?.setVolume(currentVolume)
            mediaPlayer?.setVolume(currentVolume, currentVolume)
        } catch (_: Exception) {}
    }

    fun stopAmbientSound() {
        stopAudioUrl()
        ambientJob?.cancel()
        ambientJob = null
        try {
            ambientTrack?.pause()
            ambientTrack?.flush()
            ambientTrack?.stop()
            ambientTrack?.release()
        } catch (_: Exception) {}
        ambientTrack = null
    }

    fun release() {
        stopAmbientSound()
        stopAudioUrl()
        pianoChime.release()
    }
}
