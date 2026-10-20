package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import com.example.ui.glass.GlassRobot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassWaterIndicator
import com.example.ui.components.InteractiveWaveGraph
import com.example.ui.components.StillTab
import com.example.ui.glass.GlassCard
import com.example.ui.glass.GlassTile
import com.example.ui.glass.MoodSelectorCard
import com.example.ui.theme.BlueDroplet
import com.example.ui.theme.CyanBright
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.GlassBorderActiveCyan
import com.example.ui.theme.LiquidAzure
import com.example.ui.theme.LiquidCobalt
import com.example.ui.theme.OrangeFlame
import com.example.ui.glass.isLight
import com.example.ui.theme.PinkGlow
import com.example.ui.theme.VioletBright
import com.example.ui.viewmodel.WellnessViewModel

@Composable
fun DashboardScreen(
    viewModel: WellnessViewModel,
    modifier: Modifier = Modifier
) {
    val todayMetric by viewModel.todayMetric.collectAsState()
    val selectedIndex by viewModel.selectedGraphIndex.collectAsState()
    val currentQuote by viewModel.currentQuote.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val completedSessions by viewModel.completedSessions.collectAsState()

    val isQuoteCardVisible by viewModel.isQuoteCardVisible.collectAsState()
    val isMoodCardVisible by viewModel.isMoodCardVisible.collectAsState()
    val isTelemetryCardVisible by viewModel.isTelemetryCardVisible.collectAsState()
    val isTilesCardVisible by viewModel.isTilesCardVisible.collectAsState()
    val isBreathworkCardVisible by viewModel.isBreathworkCardVisible.collectAsState()
    val isHydrationCardVisible by viewModel.isHydrationCardVisible.collectAsState()
    val isWalkingCardVisible by viewModel.isWalkingCardVisible.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showWaterDialog by remember { mutableStateOf(false) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var showRobotDialog by remember { mutableStateOf(false) }
    var showGamesDialog by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }

    val activeBurn = todayMetric?.activeCalories ?: 0
    val currentSteps = todayMetric?.steps ?: 0
    val stepGoal = todayMetric?.stepGoal ?: 10000
    val waterMl = todayMetric?.waterMl ?: 0
    val waterGoalMl = todayMetric?.waterGoalMl ?: 2500
    val exerciseMins = todayMetric?.exerciseMinutes ?: 0
    val upcomingCount = appointments.count { it.status == "UPCOMING" }

    val scrollState = rememberScrollState()
    val ambientTheme by viewModel.ambientTheme.collectAsState()
    val isLight = ambientTheme.isLight
    val textColor = if (isLight) Color(0xFF1E2A20) else Color.White
    val textSubtle = if (isLight) Color(0xFF3B503E) else Color.White.copy(alpha = 0.72f)
    val textCaption = if (isLight) Color(0xFF5B7260) else Color.White.copy(alpha = 0.52f)
    val glassBg = if (isLight) Color(0xFF1B2A1D).copy(alpha = 0.08f) else Color.White.copy(alpha = 0.14f)
    val glassBorder = if (isLight) androidx.compose.ui.graphics.SolidColor(Color(0xFF2C3E2B).copy(alpha = 0.28f)) else FrostedGlassBorder

    androidx.activity.compose.BackHandler(enabled = showRobotDialog || showGamesDialog) {
        if (showGamesDialog) showGamesDialog = false
        else if (showRobotDialog) showRobotDialog = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // ================================================================
            // 1. Minimal Liquid Glass Header
            // ================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STILL",
                        color = textColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        modifier = Modifier.testTag("app_brand_title")
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Mindful Presence & Daily Balance",
                        color = textSubtle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(glassBg)
                            .border(1.dp, glassBorder, RoundedCornerShape(20.dp))
                            .clickable { viewModel.cycleAmbientTheme() }
                            .padding(horizontal = 11.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val dotColor = when (ambientTheme) {
                                com.example.ui.glass.AmbientTheme.MIDNIGHT_SAGE -> Color(0xFF8FA89B)
                                com.example.ui.glass.AmbientTheme.COSMIC_DUSK -> Color(0xFF9D6BFF)
                                com.example.ui.glass.AmbientTheme.OCEAN_ABYSS -> Color(0xFF38BDF8)
                                com.example.ui.glass.AmbientTheme.FERRIS_WHEEL -> Color(0xFFE4E4E7)
                                com.example.ui.glass.AmbientTheme.TIME_DYNAMIC -> {
                                    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                                    when (hour) {
                                        in 5..11 -> Color(0xFF90C978)
                                        in 12..17 -> Color(0xFFD49A63)
                                        else -> Color(0xFF7874C4)
                                    }
                                }
                                com.example.ui.glass.AmbientTheme.LOOPING_TRIOS -> Color(0xFF283447)
                                com.example.ui.glass.AmbientTheme.RESONANCE_TUNNEL -> Color(0xFF8A5CF5)
                                com.example.ui.glass.AmbientTheme.CLEAN_WHITE -> Color(0xFF2C3E2B)
                                com.example.ui.glass.AmbientTheme.LIGHT_TOPOGRAPHY -> Color(0xFF4A6B53)
                                com.example.ui.glass.AmbientTheme.LIGHT_ZEN_RIPPLES -> Color(0xFF3A5A78)
                                com.example.ui.glass.AmbientTheme.LIGHT_HALFTONE_WAVE -> Color(0xFF6B5A78)
                            }
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = ambientTheme.label,
                                color = textColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ================================================================
            // 2. TOP CARD AFTER HEADING: Rotating Daily Reflection Quote Card
            //    Changes automatically every time the user opens the app
            // ================================================================
            EditableCardWrapper(
                isVisible = isQuoteCardVisible,
                isEditMode = isEditMode,
                onToggle = { viewModel.toggleQuoteCard() }
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 28.dp,
                    borderBrush = GlassBorderActiveCyan,
                    elevation = 12.dp,
                    testTag = "card_quote_of_the_day"
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .border(0.8.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = "Daily Reflection",
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DAILY REFLECTION",
                                color = CyanBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Next quote button
                            GlassIconButton(
                                onClick = { viewModel.nextQuote() },
                                size = 32.dp,
                                testTag = "btn_next_quote"
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Next Quote",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Favorite quote toggle
                            GlassIconButton(
                                onClick = { viewModel.toggleFavoriteQuote() },
                                size = 32.dp,
                                isActive = currentQuote.isFavorite,
                                testTag = "btn_favorite_quote"
                            ) {
                                Icon(
                                    imageVector = if (currentQuote.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite Quote",
                                    tint = if (currentQuote.isFavorite) PinkGlow else Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedContent(
                        targetState = currentQuote,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "QuoteTransition"
                    ) { quote ->
                        Column {
                            Text(
                                text = "“${quote.text}”",
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "— ${quote.author}",
                                    color = textSubtle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isLight) Color(0xFF2C3E2B) else CyanBright.copy(alpha = 0.75f),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Rotates on open",
                                        color = textCaption,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            } // End EditableCardWrapper for Quote

            Spacer(modifier = Modifier.height(12.dp))

            // ================================================================
            // 4. Minimal Mood Check-in Selector Card
            // ================================================================
            EditableCardWrapper(
                isVisible = isMoodCardVisible,
                isEditMode = isEditMode,
                onToggle = { viewModel.toggleMoodCard() }
            ) {
                MoodSelectorCard(
                    selectedMood = selectedMood,
                    onMoodSelected = { viewModel.selectMood(it) },
                    onMewMewClick = { showRobotDialog = true },
                    onGamesClick = { showGamesDialog = true },
                    onEmoClick = { viewModel.setEmoScreenOpen(true) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================================================================
            // 7. Minimal Recent Activity & Wellness Sanctuary Feed (Searchable)
            // ================================================================
            Text(
                text = "Daily Practices & Activity",
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            val q = searchQuery.trim().lowercase()

            // Practice 1: Glass Bubble Breathing
            if (q.isEmpty() || "box breathing practice meditate calm bubble".contains(q)) {
            EditableCardWrapper(
                isVisible = isBreathworkCardVisible,
                isEditMode = isEditMode,
                onToggle = { viewModel.toggleBreathworkCard() }
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    cornerRadius = 22.dp,
                    elevation = 6.dp,
                    onClick = { viewModel.setTab(StillTab.BREATHE) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LiquidAzure.copy(alpha = 0.25f))
                                    .border(0.9.dp, CyanBright.copy(alpha = 0.55f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SelfImprovement,
                                    contentDescription = null,
                                    tint = CyanBright,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Glass Bubble Breathwork",
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (completedSessions.isNotEmpty()) {
                                        "${completedSessions.size} session(s) completed • Tap to practice"
                                    } else {
                                        "5 min guided rhythm • Restores calm"
                                    },
                                    color = textSubtle,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "Breathe",
                            color = if (isLight) Color(0xFF2C3E2B) else CyanBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            } // End EditableCardWrapper for Breathwork

            // Practice 2: Hydration Intake with Animated Glass Water Indicator
            if (q.isEmpty() || "hydration water intake drink glass".contains(q)) {
            EditableCardWrapper(
                isVisible = isHydrationCardVisible,
                isEditMode = isEditMode,
                onToggle = { viewModel.toggleHydrationCard() }
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    cornerRadius = 22.dp,
                    elevation = 6.dp,
                    onClick = { showWaterDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BlueDroplet.copy(alpha = 0.20f))
                                    .border(0.9.dp, BlueDroplet.copy(alpha = 0.55f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                GlassWaterIndicator(
                                    currentMl = waterMl,
                                    goalMl = waterGoalMl,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Hydration Intake",
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${(waterMl / 1000f).formatOne()} / ${(waterGoalMl / 1000f).formatOne()} Litres (${(waterMl * 100 / waterGoalMl.coerceAtLeast(1))}%)",
                                    color = textSubtle,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "+ Log",
                            color = if (isLight) Color(0xFF2C3E2B) else CyanBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            } // End EditableCardWrapper for Hydration

            // Practice 3: Daily Walking Steps
            if (q.isEmpty() || "walking steps activity calories run".contains(q)) {
            EditableCardWrapper(
                isVisible = isWalkingCardVisible,
                isEditMode = isEditMode,
                onToggle = { viewModel.toggleWalkingCard() }
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    cornerRadius = 22.dp,
                    elevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(OrangeFlame.copy(alpha = 0.22f))
                                    .border(0.9.dp, OrangeFlame.copy(alpha = 0.55f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = null,
                                    tint = OrangeFlame,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Walking Activity",
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "%,d / %,d steps • %d kcal".format(currentSteps, stepGoal, activeBurn),
                                    color = textSubtle,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "Auto Track",
                            color = OrangeFlame,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            } // End EditableCardWrapper for Walking

            Spacer(modifier = Modifier.height(92.dp))
        }

        // Robot Overlay
        androidx.compose.animation.AnimatedVisibility(
            visible = showRobotDialog,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF040B1A).copy(alpha = 0.65f))
                    .pointerInput(Unit) { detectTapGestures { } } // Block touches from passing through
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    cornerRadius = 32.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        GlassRobot()
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .clip(CircleShape)
                                .clickable { showRobotDialog = false }
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Text("Close", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Games Overlay (full screen card same as mew mew robot)
        androidx.compose.animation.AnimatedVisibility(
            visible = showGamesDialog,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF040B1A).copy(alpha = 0.70f))
                    .pointerInput(Unit) { detectTapGestures { } } // Block touches from passing through
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    cornerRadius = 32.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        StillGamesScreen(modifier = Modifier.fillMaxSize())

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .clip(CircleShape)
                                .clickable { showGamesDialog = false }
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("Close", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Water Dialog
        if (showWaterDialog) {
            AlertDialog(
                onDismissRequest = { showWaterDialog = false },
                containerColor = Color(0xFF102048),
                shape = RoundedCornerShape(28.dp),
                title = {
                    Text(
                        text = "Log Water Intake",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Current: ${(waterMl / 1000f).formatOne()} / ${(waterGoalMl / 1000f).formatOne()} Litres",
                            color = CyanBright,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassButton(
                                text = "+150 ml",
                                onClick = {
                                    viewModel.addWater(150, "Small Cup")
                                    showWaterDialog = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                            GlassButton(
                                text = "+250 ml",
                                onClick = {
                                    viewModel.addWater(250, "Standard Glass")
                                    showWaterDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                isActive = true
                            )
                            GlassButton(
                                text = "+500 ml",
                                onClick = {
                                    viewModel.addWater(500, "Bottle")
                                    showWaterDialog = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.undoWater()
                                    showWaterDialog = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Undo Entry",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Undo last water entry (-250 ml)",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showWaterDialog = false }) {
                        Text("Close", color = CyanBright)
                    }
                }
            )
        }

        // Daily Wellness Goals Dialog
        if (showTaskDialog) {
            AlertDialog(
                onDismissRequest = { showTaskDialog = false },
                containerColor = Color(0xFF102048),
                shape = RoundedCornerShape(28.dp),
                title = {
                    Text(
                        text = "Daily Wellness Sanctuary Goals",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val bDone = if (completedSessions.isNotEmpty()) "✓" else "○"
                        val wDone = if (waterMl >= waterGoalMl) "✓" else "○"
                        val sDone = if (currentSteps >= stepGoal) "✓" else "○"
                        Text(
                            text = "$bDone Complete 5-minute Glass Bubble Breathing\n" +
                                "$wDone Reach ${(waterGoalMl / 1000f).formatOne()}L Hydration (${(waterMl / 1000f).formatOne()}L logged)\n" +
                                "$sDone Walk %,d steps (%,d logged)\n".format(stepGoal, currentSteps) +
                                "○ Evening 10-minute Quiet The Mind meditation",
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTaskDialog = false }) {
                        Text("Done", color = CyanBright)
                    }
                }
            )
        }
    }
}

private fun Float.formatOne(): String = String.format("%.1f", this)
