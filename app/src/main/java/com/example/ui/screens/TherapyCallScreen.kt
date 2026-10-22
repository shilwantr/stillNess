package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.components.GlassIconButton
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.GlassBorderActiveCyan
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.PinkGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VioletBright
import com.example.ui.theme.VioletGlow
import com.example.ui.viewmodel.WellnessViewModel
import com.example.util.AgoraConfig
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun TherapyCallScreen(
    viewModel: WellnessViewModel,
    onCallEnded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val callState by viewModel.callState.collectAsState()
    val context = LocalContext.current

    // Request Camera & Audio permissions dynamically
    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasPermissions = perms[Manifest.permission.RECORD_AUDIO] == true && perms[Manifest.permission.CAMERA] == true
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    val durationMin = callState.durationSeconds / 60
    val durationSec = callState.durationSeconds % 60
    val durationStr = String.format("%02d:%02d", durationMin, durationSec)

    val infiniteTransition = rememberInfiniteTransition(label = "VoiceVisualizer")
    val visualizerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "VisualizerPhase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark)
            .testTag("therapy_call_screen")
    ) {
        // -------------------------------------------------------------
        // 1. Remote Participant Full-Screen Background
        // -------------------------------------------------------------
        val showRemoteVideo = callState.isVideoCall &&
                callState.remoteUid != null &&
                !callState.isRemoteVideoMuted

        if (showRemoteVideo) {
            AndroidView(
                factory = { ctx ->
                    SurfaceView(ctx).apply {
                        viewModel.agoraRtcManager.setupRemoteVideo(this, callState.remoteUid!!)
                    }
                },
                update = { view ->
                    callState.remoteUid?.let { uid ->
                        viewModel.agoraRtcManager.setupRemoteVideo(view, uid)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Audio-only or Remote Camera Disabled: Luxury Frosted Avatar Background
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Pulsing Audio Avatar Halo
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    CyanGlow.copy(alpha = 0.5f),
                                    VioletGlow.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(1.5.dp, GlassBorderActiveCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Participant Avatar",
                        tint = TextWhite,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = callState.therapistName.ifEmpty { "Therapy Consultation" },
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = callState.therapistTitle.ifEmpty { "Licensed Clinical Practitioner" },
                    color = TextMuted,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Call Duration Counter
                Text(
                    text = durationStr,
                    color = CyanBright,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Real-Time Audio Frequency Wave Visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barCount = 28
                        val barWidth = 3.dp.toPx()
                        val totalWidth = barCount * barWidth * 2f
                        val startX = (size.width - totalWidth) / 2f
                        val centerY = size.height / 2f

                        for (i in 0 until barCount) {
                            val x = startX + i * barWidth * 2f
                            val wave = if (callState.isMuted) 0.1f else {
                                val freq = sin(i * 0.35f + visualizerPhase)
                                (0.2f + 0.8f * kotlin.math.abs(freq)).coerceIn(0.1f, 1f)
                            }
                            val barHeight = size.height * 0.7f * wave

                            drawLine(
                                brush = Brush.verticalGradient(
                                    colors = listOf(CyanBright, VioletBright, PinkGlow),
                                    startY = centerY - barHeight / 2f,
                                    endY = centerY + barHeight / 2f
                                ),
                                start = Offset(x, centerY - barHeight / 2f),
                                end = Offset(x, centerY + barHeight / 2f),
                                strokeWidth = barWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 2. Picture-in-Picture (PIP) Local Video Preview (Top Right)
        // -------------------------------------------------------------
        if (callState.isVideoCall) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 70.dp, end = 20.dp)
                    .size(width = 110.dp, height = 160.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.5.dp, GlassBorderActiveCyan, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!callState.isLocalVideoDisabled) {
                    AndroidView(
                        factory = { ctx ->
                            SurfaceView(ctx).apply {
                                setZOrderMediaOverlay(true)
                                viewModel.agoraRtcManager.setupLocalVideo(this)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideocamOff,
                            contentDescription = "Camera Muted",
                            tint = TextSubtle,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Camera Off", color = TextSubtle, fontSize = 10.sp)
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 3. Header & Session Status Bar (Top Center)
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Security & Encrypted Session Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(0.8.dp, FrostedGlassBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (callState.remoteUid != null) Color(0xFF10B981) else Color(0xFFF59E0B))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Encrypted",
                    tint = CyanBright,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = callState.connectionQuality,
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Agora App ID Config Warning Banner (if unconfigured)
            if (!AgoraConfig.isConfigured) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.20f))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.50f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Demo Mode: Set your Agora App ID in AgoraConfig.kt to stream between devices.",
                        color = Color(0xFFFDE68A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (showRemoteVideo) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${callState.therapistName} • $durationStr",
                    color = TextWhite.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // -------------------------------------------------------------
        // 4. In-Call Interactive Toolbar (Bottom Center)
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 36.dp, start = 20.dp, end = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.dp, FrostedGlassBorder, RoundedCornerShape(32.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Microphone Mute / Unmute
                GlassIconButton(
                    onClick = { viewModel.toggleCallMute() },
                    size = 50.dp,
                    isActive = callState.isMuted,
                    testTag = "btn_call_mute"
                ) {
                    Icon(
                        imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (callState.isMuted) PinkGlow else TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 2. Video Camera Toggle
                if (callState.isVideoCall) {
                    GlassIconButton(
                        onClick = { viewModel.toggleCallVideo() },
                        size = 50.dp,
                        isActive = callState.isLocalVideoDisabled,
                        testTag = "btn_call_video"
                    ) {
                        Icon(
                            imageVector = if (callState.isLocalVideoDisabled) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Toggle Video",
                            tint = if (callState.isLocalVideoDisabled) PinkGlow else TextWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 3. Camera Flip (Front / Rear)
                    GlassIconButton(
                        onClick = { viewModel.switchCamera() },
                        size = 50.dp,
                        testTag = "btn_call_flip_camera"
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Flip Camera",
                            tint = TextWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 4. Speakerphone Toggle
                GlassIconButton(
                    onClick = { viewModel.toggleCallSpeaker() },
                    size = 50.dp,
                    isActive = callState.isSpeakerOn,
                    testTag = "btn_call_speaker"
                ) {
                    Icon(
                        imageVector = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = "Speaker",
                        tint = if (callState.isSpeakerOn) CyanBright else TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 5. End Call Action (Radiant Red Circle)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFEF4444),
                                    Color(0xFFB91C1C)
                                )
                            )
                        )
                        .clickable {
                            viewModel.endCall()
                            onCallEnded()
                        }
                        .testTag("btn_end_call"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
