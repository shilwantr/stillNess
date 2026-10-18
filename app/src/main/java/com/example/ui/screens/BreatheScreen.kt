package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BreathingMode
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassIconButton
import com.example.ui.glass.AudioWaveformVisualizer
import com.example.ui.glass.EnhancedLiquidGlassOrb
import com.example.ui.glass.GlassCard
import com.example.ui.glass.GlassStyle
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.GlassBorderActiveCyan
import com.example.ui.theme.GlassBorderGradient
import com.example.ui.theme.LiquidAzure
import com.example.ui.theme.LiquidCobalt
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VioletBright
import com.example.ui.viewmodel.WellnessViewModel

enum class PracticeMainMode(val label: String) {
    BREATHE("Breathe"),
    HEAL("Heal")
}

enum class BreatheTab(val label: String) {
    RELAXING("Relaxing"),
    MEDITATION("Meditation"),
    AMBIENT("Ambient")
}

@Composable
fun BreatheScreen(
    viewModel: WellnessViewModel,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { PracticeMainMode.values().size })
    val coroutineScope = rememberCoroutineScope()
    val practiceMainMode = PracticeMainMode.values()[pagerState.currentPage]
    var subTab by remember { mutableStateOf(BreatheTab.RELAXING) }
    var currentVisualStyle by remember { mutableStateOf(GlassStyle.BALLOON) }

    val isBreathingActive by viewModel.isBreathingActive.collectAsState()
    val isBreathingPaused by viewModel.isBreathingPaused.collectAsState()
    val breathingMode by viewModel.breathingMode.collectAsState()
    val breathingPhase by viewModel.breathingPhase.collectAsState()
    val phaseSecRemaining by viewModel.phaseRemainingSeconds.collectAsState()
    val totalSecRemaining by viewModel.totalBreathingSecondsRemaining.collectAsState()
    val orbFraction by viewModel.orbExpansionFraction.collectAsState()
    val durationMinutes by viewModel.breathingDurationMinutes.collectAsState()

    val activeMeditationTrack by viewModel.activeMeditationTrack.collectAsState()
    val isMeditationPlaying by viewModel.isMeditationPlaying.collectAsState()
    val meditationSecs by viewModel.meditationRemainingSeconds.collectAsState()
    val meditationTracks by viewModel.meditationTracks.collectAsState()
    val relaxingMusic by viewModel.relaxingMusic.collectAsState()

    val activeAmbient by viewModel.activeAmbientSound.collectAsState()
    val ambientSounds by viewModel.ambientSounds.collectAsState()
    val ambientVol by viewModel.ambientVolume.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("breathe_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            // Header Row with Side-by-Side Mode Switcher (Breathe | Heal)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (practiceMainMode == PracticeMainMode.BREATHE) "BREATHE" else "HEAL",
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Side-by-Side Practice Mode Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .border(0.8.dp, FrostedGlassBorder, RoundedCornerShape(22.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PracticeMainMode.values().forEach { mode ->
                        val isSel = mode == practiceMainMode
                        val bgBrush = if (isSel) {
                            Brush.horizontalGradient(
                                listOf(
                                    LiquidCobalt.copy(alpha = 0.55f),
                                    CyanGlow.copy(alpha = 0.35f)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(bgBrush)
                                .border(
                                    0.8.dp,
                                    if (isSel) GlassBorderActiveCyan else SolidColor(Color.Transparent),
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(mode.ordinal)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                color = if (isSel) TextWhite else Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val currentPageMode = PracticeMainMode.values()[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    if (currentPageMode == PracticeMainMode.BREATHE) {
                // 1. Signature 3D Liquid Glass Bubble Hero Card
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp),
                    cornerRadius = 32.dp,
                    borderBrush = GlassBorderActiveCyan,
                    elevation = 14.dp,
                    testTag = "card_breathing_orb_hero"
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Phase and Cue Header
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isBreathingActive) breathingPhase.label else breathingMode.title,
                                color = CyanBright,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBreathingActive) breathingPhase.cueText else breathingMode.description,
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }

                        // Centered Smooth 3D Refractive Liquid Glass Bubble
                        Box(
                            modifier = Modifier.size(215.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EnhancedLiquidGlassOrb(
                                modifier = Modifier.fillMaxSize(),
                                expansionFraction = orbFraction,
                                isBreathingActive = isBreathingActive,
                                style = currentVisualStyle
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (isBreathingActive) {
                                    Text(
                                        text = "$phaseSecRemaining",
                                        color = TextWhite,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = breathingPhase.label.uppercase(),
                                        color = Color.White.copy(alpha = 0.78f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.5.sp
                                    )
                                } else {
                                    Text(
                                        text = "${breathingMode.inhaleSeconds}-${breathingMode.holdAfterInhaleSeconds}-${breathingMode.exhaleSeconds}",
                                        color = Color.White.copy(alpha = 0.90f),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "GLASS BUBBLE",
                                        color = CyanBright.copy(alpha = 0.80f),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.4.sp
                                    )
                                }
                            }
                        }

                        // Controls Row
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val totalMin = totalSecRemaining / 60
                            val totalSec = totalSecRemaining % 60
                            Text(
                                text = String.format("%02d:%02d remaining", totalMin, totalSec),
                                color = TextSubtle,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isBreathingActive) {
                                    GlassIconButton(
                                        onClick = {
                                            if (isBreathingPaused) viewModel.startBreathingSession()
                                            else viewModel.pauseBreathingSession()
                                        },
                                        size = 54.dp,
                                        testTag = "btn_pause_breathing"
                                    ) {
                                        Icon(
                                            imageVector = if (isBreathingPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                            contentDescription = "Pause",
                                            tint = TextWhite,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    GlassIconButton(
                                        onClick = { viewModel.stopBreathingSession() },
                                        size = 54.dp,
                                        isActive = true,
                                        testTag = "btn_stop_breathing"
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = "Stop",
                                            tint = TextWhite,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                } else {
                                    GlassButton(
                                        text = "Begin ${breathingMode.title}",
                                        onClick = { viewModel.startBreathingSession() },
                                        isActive = true,
                                        modifier = Modifier.width(220.dp),
                                        testTag = "btn_start_breathing"
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Animation Style Selector
                Text(
                    text = "Visual Animation Style",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassStyle.values().forEach { style ->
                        GlassButton(
                            text = style.label,
                            onClick = { currentVisualStyle = style },
                            isActive = style == currentVisualStyle,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Selection List
                Text(
                    text = "Breathing Pattern Modes",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                BreathingMode.values().forEach { mode ->
                    val isSel = mode == breathingMode
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        cornerRadius = 20.dp,
                        borderBrush = if (isSel) GlassBorderActiveCyan else GlassBorderGradient,
                        onClick = { viewModel.setBreathingMode(mode) },
                        testTag = "btn_mode_${mode.name}"
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    mode.title,
                                    color = if (isSel) CyanBright else TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(mode.description, color = TextMuted, fontSize = 12.sp)
                            }
                            val patternStr = "${mode.inhaleSeconds}-${mode.holdAfterInhaleSeconds}-${mode.exhaleSeconds}-${mode.holdAfterExhaleSeconds}"
                            Text(
                                patternStr,
                                color = if (isSel) CyanBright else TextSubtle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Target Duration",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2, 5, 10, 15).forEach { mins ->
                        GlassButton(
                            text = "$mins min",
                            onClick = { viewModel.setBreathingDurationMinutes(mins) },
                            isActive = mins == durationMinutes,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {

            // Segmented Liquid Glass Sub Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(1.dp, FrostedGlassBorder, RoundedCornerShape(26.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BreatheTab.values().forEach { tab ->
                    val isSel = tab == subTab
                    val subBgBrush = if (isSel) {
                        Brush.horizontalGradient(
                            listOf(
                                LiquidCobalt.copy(alpha = 0.55f),
                                CyanGlow.copy(alpha = 0.35f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(subBgBrush)
                            .border(
                                0.9.dp,
                                if (isSel) GlassBorderActiveCyan else SolidColor(Color.Transparent),
                                RoundedCornerShape(22.dp)
                            )
                            .clickable { subTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            color = if (isSel) TextWhite else Color.White.copy(alpha = 0.68f),
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            when (subTab) {
                BreatheTab.RELAXING -> {
                    RelaxingMusicSection(
                        viewModel = viewModel,
                        relaxingMusic = relaxingMusic,
                        activeMeditationTrack = activeMeditationTrack,
                        isMeditationPlaying = isMeditationPlaying,
                        meditationSecs = meditationSecs
                    )
                }

            BreatheTab.MEDITATION -> {
                    AnimatedVisibility(
                        visible = activeMeditationTrack != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        activeMeditationTrack?.let { activeTrack ->
                            val totalSecs = (activeTrack.durationMinutes * 60).coerceAtLeast(1)
                            val elapsedSecs = (totalSecs - meditationSecs).coerceAtLeast(0)
                            val progress = (elapsedSecs.toFloat() / totalSecs).coerceIn(0f, 1f)

                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                cornerRadius = 32.dp,
                                borderBrush = GlassBorderActiveCyan,
                                elevation = 14.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(
                                                    Brush.radialGradient(
                                                        listOf(
                                                            CyanBright,
                                                            LiquidCobalt,
                                                            Color(0xFF020B1E)
                                                        )
                                                    )
                                                )
                                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.GraphicEq,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = activeTrack.title,
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "STILL • ${activeTrack.category}",
                                                color = Color.White.copy(alpha = 0.78f),
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    AudioWaveformVisualizer(isPlaying = isMeditationPlaying)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = String.format("%d:%02d", elapsedSecs / 60, elapsedSecs % 60),
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = String.format("-%d:%02d", meditationSecs / 60, meditationSecs % 60),
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.28f))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progress.coerceAtLeast(0.04f))
                                                .height(4.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        GlassIconButton(
                                            onClick = { viewModel.startMeditation(activeTrack) },
                                            size = 42.dp
                                        ) {
                                            Icon(Icons.Default.Replay, contentDescription = "Restart", tint = Color.White)
                                        }
                                        GlassIconButton(
                                            onClick = {
                                                if (isMeditationPlaying) viewModel.pauseMeditation()
                                                else viewModel.startMeditation(activeTrack)
                                            },
                                            size = 50.dp,
                                            isActive = true
                                        ) {
                                            Icon(
                                                imageVector = if (isMeditationPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Play/Pause",
                                                tint = Color.White
                                            )
                                        }
                                        GlassIconButton(
                                            onClick = { viewModel.stopMeditation() },
                                            size = 42.dp
                                        ) {
                                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "Curated Guided Sessions",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (meditationTracks.isEmpty()) {
                        Text(
                            text = "Syncing meditation tracks from database...",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    meditationTracks.forEach { track ->
                        val isPlaying = activeMeditationTrack?.id == track.id && isMeditationPlaying
                        val trackColor = remember(track.accentColor) {
                            try {
                                Color(android.graphics.Color.parseColor(track.accentColor))
                            } catch (_: Exception) {
                                CyanBright
                            }
                        }
                        val trackIcon = remember(track.iconName) {
                            when (track.iconName) {
                                "bedtime" -> Icons.Default.Bedtime
                                "favorite" -> Icons.Default.Favorite
                                "psychology" -> Icons.Default.AutoAwesome
                                "wb_sunny" -> Icons.Default.WbSunny
                                else -> Icons.Default.SelfImprovement
                            }
                        }

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 7.dp),
                            cornerRadius = 24.dp,
                            borderBrush = if (isPlaying) {
                                Brush.horizontalGradient(
                                    listOf(trackColor, trackColor.copy(alpha = 0.5f))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(trackColor.copy(alpha = 0.40f), Color.White.copy(alpha = 0.08f))
                                )
                            }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Thematic Visual Cover Art Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(
                                                        trackColor.copy(alpha = if (isPlaying) 0.50f else 0.28f),
                                                        trackColor.copy(alpha = 0.08f)
                                                    )
                                                )
                                            )
                                            .border(
                                                1.dp,
                                                if (isPlaying) trackColor else trackColor.copy(alpha = 0.45f),
                                                RoundedCornerShape(18.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = trackIcon,
                                            contentDescription = track.title,
                                            tint = if (isPlaying) trackColor else Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    // 2. Track Title & Category Badges
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(trackColor.copy(alpha = 0.20f))
                                                    .border(0.6.dp, trackColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = track.category.uppercase(),
                                                    color = trackColor,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 1.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Text(
                                                text = "${track.baseFrequency.toInt()} Hz",
                                                color = trackColor.copy(alpha = 0.85f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = track.title,
                                            color = TextWhite,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = "${track.durationMinutes} min • ${if (track.isNarrated) "Voice Guidance" else "Ambient Resonance"}",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // 3. Play/Pause Action Button
                                    GlassIconButton(
                                        onClick = {
                                            if (isPlaying) viewModel.pauseMeditation()
                                            else viewModel.startMeditation(track)
                                        },
                                        size = 46.dp,
                                        isActive = isPlaying
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play Track",
                                            tint = if (isPlaying) trackColor else TextWhite,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = track.description,
                                    color = TextSubtle,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )

                                if (isPlaying) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.GraphicEq,
                                                contentDescription = "Playing",
                                                tint = trackColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Playing ${track.baseFrequency.toInt()} Hz Tone",
                                                color = trackColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        val mins = meditationSecs / 60
                                        val secs = meditationSecs % 60
                                        Text(
                                            text = String.format("%02d:%02d remaining", mins, secs),
                                            color = trackColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else if (track.downloadProgress > 0f && track.downloadProgress < 1f) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Downloading...",
                                            color = TextSubtle,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(4.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.1f))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(track.downloadProgress)
                                                    .height(4.dp)
                                                    .clip(CircleShape)
                                                    .background(trackColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                BreatheTab.AMBIENT -> {
                    Text(
                        text = "Calming Soundscapes",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Organic harmonic frequencies for sleep, focus and calm",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 22.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            if (activeAmbient != null) {
                                AudioWaveformVisualizer(isPlaying = true)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Volume",
                                    tint = CyanBright,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Slider(
                                    value = ambientVol,
                                    onValueChange = { viewModel.setAmbientVolume(it) },
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = CyanBright,
                                        inactiveTrackColor = Color(0x33FFFFFF)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (ambientSounds.isEmpty()) {
                        Text(
                            text = "Syncing ambient sounds from database...",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    ambientSounds.forEach { snd ->
                        val isPlaying = activeAmbient?.id == snd.id
                        val sndIcon = remember(snd.iconName) {
                            when (snd.iconName) {
                                "water_drop" -> Icons.Default.Opacity
                                "waves" -> Icons.Default.Waves
                                "notifications" -> Icons.Default.Notifications
                                "water" -> Icons.Default.Opacity
                                "bedtime" -> Icons.Default.Bedtime
                                else -> Icons.Default.GraphicEq
                            }
                        }
                        val sndTint = remember(snd.id) {
                            when (snd.id) {
                                "snd_rain" -> Color(0xFF38BDF8)
                                "snd_ocean" -> Color(0xFF60A5FA)
                                "snd_forest" -> Color(0xFF34D399)
                                "snd_stream" -> Color(0xFF2DD4BF)
                                "snd_night" -> Color(0xFFA78BFA)
                                else -> CyanBright
                            }
                        }

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            cornerRadius = 22.dp,
                            borderBrush = if (isPlaying) Brush.horizontalGradient(listOf(sndTint, sndTint.copy(alpha = 0.5f))) else GlassBorderGradient,
                            onClick = { viewModel.toggleAmbientSound(snd) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isPlaying) sndTint.copy(alpha = 0.40f)
                                                else sndTint.copy(alpha = 0.12f)
                                            )
                                            .border(
                                                0.9.dp,
                                                if (isPlaying) sndTint else sndTint.copy(alpha = 0.35f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = sndIcon,
                                            contentDescription = snd.name,
                                            tint = if (isPlaying) Color.White else sndTint,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = snd.name,
                                            color = if (isPlaying) sndTint else TextWhite,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = snd.description,
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Text(
                                    text = if (isPlaying) "Playing" else "Play",
                                    color = if (isPlaying) CyanBright else TextSubtle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
            } // Closes else (PracticeMainMode.HEAL)

                    Spacer(modifier = Modifier.height(92.dp))
                }
            }
        }
    }
}

@Composable
fun RelaxingMusicSection(
    viewModel: WellnessViewModel,
    relaxingMusic: List<com.example.domain.model.MeditationTrack>,
    activeMeditationTrack: com.example.domain.model.MeditationTrack?,
    isMeditationPlaying: Boolean,
    meditationSecs: Int
) {
    Text(
        text = "Relaxing Music",
        color = TextWhite,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
    Text(
        text = "Real acoustic MP3 tracks streamed in HD from Cloud",
        color = TextMuted,
        fontSize = 12.sp
    )
    Spacer(modifier = Modifier.height(14.dp))

    AnimatedVisibility(
        visible = activeMeditationTrack != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        activeMeditationTrack?.let { activeTrack ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                cornerRadius = 26.dp,
                borderBrush = GlassBorderActiveCyan
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Playing",
                                    tint = CyanBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "NOW PLAYING MP3",
                                    color = CyanBright,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeTrack.title,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassIconButton(
                                onClick = {
                                    if (isMeditationPlaying) viewModel.pauseMeditation()
                                    else viewModel.startMeditation(activeTrack)
                                },
                                size = 44.dp,
                                isActive = isMeditationPlaying
                            ) {
                                Icon(
                                    imageVector = if (isMeditationPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White
                                )
                            }
                            GlassIconButton(
                                onClick = { viewModel.stopMeditation() },
                                size = 44.dp
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    if (relaxingMusic.isEmpty()) {
        Text(
            text = "Loading relaxing music from database...",
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }

    relaxingMusic.forEach { track ->
        val isPlaying = activeMeditationTrack?.id == track.id && isMeditationPlaying
        val trackColor = remember(track.accentColor) {
            try {
                Color(android.graphics.Color.parseColor(track.accentColor))
            } catch (_: Exception) {
                CyanBright
            }
        }
        val trackIcon = remember(track.iconName) {
            when (track.iconName) {
                "bedtime" -> Icons.Default.Bedtime
                "favorite" -> Icons.Default.Favorite
                "waves" -> Icons.Default.Waves
                else -> Icons.Default.AutoAwesome
            }
        }

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 7.dp),
            cornerRadius = 24.dp,
            borderBrush = if (isPlaying) {
                Brush.horizontalGradient(
                    listOf(trackColor, trackColor.copy(alpha = 0.5f))
                )
            } else {
                Brush.horizontalGradient(
                    listOf(trackColor.copy(alpha = 0.40f), Color.White.copy(alpha = 0.08f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Cover Art Tile
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        trackColor.copy(alpha = if (isPlaying) 0.50f else 0.28f),
                                        trackColor.copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                if (isPlaying) trackColor else trackColor.copy(alpha = 0.45f),
                                RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = trackIcon,
                            contentDescription = track.title,
                            tint = if (isPlaying) trackColor else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // 2. Track Title & Category
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(trackColor.copy(alpha = 0.20f))
                                    .border(0.6.dp, trackColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = track.category.uppercase(),
                                    color = trackColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "HD MP3",
                                color = trackColor.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = track.title,
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${track.durationMinutes} min • Real Cloud Audio",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    // 3. Play Button
                    GlassIconButton(
                        onClick = {
                            if (isPlaying) viewModel.pauseMeditation()
                            else viewModel.startMeditation(track)
                        },
                        size = 46.dp,
                        isActive = isPlaying
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = track.description,
                    color = TextSubtle,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                if (isPlaying) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Playing",
                                tint = trackColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Streaming Live MP3",
                                color = trackColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        val mins = meditationSecs / 60
                        val secs = meditationSecs % 60
                        Text(
                            text = String.format("%02d:%02d remaining", mins, secs),
                            color = trackColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (track.downloadProgress > 0f && track.downloadProgress < 1f) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Downloading...",
                            color = TextSubtle,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(track.downloadProgress)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(trackColor)
                            )
                        }
                    }
                }
            }
        }
    }
}