package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Opacity
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.JournalEntryEntity
import com.example.ui.components.InteractiveBarGraph
import com.example.ui.glass.GlassCard
import com.example.ui.glass.isLight
import com.example.ui.theme.BlueDroplet
import com.example.ui.theme.CyanBright
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PinkGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VioletBright
import com.example.ui.viewmodel.MetricViewType
import com.example.ui.viewmodel.WellnessViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalScreen(
    viewModel: WellnessViewModel,
    modifier: Modifier = Modifier
) {
    val todayMetric by viewModel.todayMetric.collectAsState()
    val journalEntries by viewModel.journalEntries.collectAsState()
    val waterEntries by viewModel.waterEntries.collectAsState()
    val selectedMetric by viewModel.selectedMetricType.collectAsState()
    val selectedIndex by viewModel.selectedGraphIndex.collectAsState()

    val currentSteps = todayMetric?.steps ?: 0
    val stepGoal = todayMetric?.stepGoal ?: 10000
    val activeBurn = todayMetric?.activeCalories ?: 0
    val waterMl = todayMetric?.waterMl ?: 0
    val waterGoalMl = todayMetric?.waterGoalMl ?: 2500

    var isComposerExpanded by remember { mutableStateOf(false) }
    var entryTitle by remember { mutableStateOf("") }
    var entryContent by remember { mutableStateOf("") }
    var selectedMoodTag by remember { mutableStateOf("Calm") }
    var entryToDelete by remember { mutableStateOf<JournalEntryEntity?>(null) }
    var showAllWaterLogs by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val todayFormatted = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    val moodOptions = listOf(
        Pair("Calm", CyanBright),
        Pair("Peaceful", Color(0xFF60A5FA)),
        Pair("Energized", OrangeFlame),
        Pair("Reflective", VioletBright),
        Pair("Grateful", Color(0xFF34D399)),
        Pair("Anxious", PinkGlow)
    )

    val isLight = com.example.ui.glass.LocalAmbientTheme.current.isLight
    val textColor = if (isLight) Color(0xFF1E2A20) else TextWhite
    val textSubtle = if (isLight) Color(0xFF3B503E) else Color.White.copy(alpha = 0.65f)
    val glassBg = if (isLight) Color(0xFF1B2A1D).copy(alpha = 0.08f) else Color.White.copy(alpha = 0.10f)
    val glassBorder = if (isLight) androidx.compose.ui.graphics.SolidColor(Color(0xFF2C3E2B).copy(alpha = 0.28f)) else FrostedGlassBorder

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("journal_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "J O U R N A L",
                        color = textColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 6.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Reflections, Steps & Hydration Log",
                        color = textSubtle,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(glassBg)
                        .border(0.8.dp, glassBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = CyanBright,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = todayFormatted,
                            color = textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ================================================================
            // 1. STEP TRACKER CARD
            // ================================================================
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevation = 6.dp,
                testTag = "card_step_tracker"
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(OrangeFlame.copy(alpha = 0.22f))
                                    .border(0.8.dp, OrangeFlame.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = null,
                                    tint = OrangeFlame,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Step Tracker",
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF34D399))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Hardware Sensor Active",
                                        color = Color(0xFF34D399),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Percentage badge
                        val stepProgress = (currentSteps.toFloat() / stepGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
                        val stepPercent = (stepProgress * 100).toInt()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(OrangeFlame.copy(alpha = 0.16f))
                                .border(0.7.dp, OrangeFlame.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$stepPercent%",
                                color = OrangeFlame,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Steps Main Metric
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "%,d".format(currentSteps),
                                color = TextWhite,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = " / %,d steps".format(stepGoal),
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                            )
                        }

                        // Stats: Distance & Calories
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "%.2f km".format(currentSteps * 0.00075f),
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Distance",
                                    color = Color.White.copy(alpha = 0.50f),
                                    fontSize = 10.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$activeBurn kcal",
                                    color = OrangeFlame,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Burned",
                                    color = Color.White.copy(alpha = 0.50f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Liquid Progress Bar
                    val animatedStepProgress by animateFloatAsState(
                        targetValue = (currentSteps.toFloat() / stepGoal.coerceAtLeast(1)).coerceIn(0f, 1f),
                        label = "StepProgressAnim"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedStepProgress)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(OrangeFlame, CyanBright)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Step Log / Test buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Log:",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 11.sp
                        )
                        listOf(250, 500, 1000).forEach { inc ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(0.7.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .clickable { viewModel.simulateStepIncrement(inc) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$inc",
                                    color = Color.White.copy(alpha = 0.88f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================================================================
            // 2. WATER DRINK TRACKER CARD
            // ================================================================
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevation = 6.dp,
                testTag = "card_water_tracker"
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BlueDroplet.copy(alpha = 0.22f))
                                    .border(0.8.dp, BlueDroplet.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Opacity,
                                    contentDescription = null,
                                    tint = BlueDroplet,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Water Drink Tracker",
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${waterEntries.size} drinks logged today",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Percentage badge
                        val waterProgress = (waterMl.toFloat() / waterGoalMl.coerceAtLeast(1)).coerceIn(0f, 1f)
                        val waterPercent = (waterProgress * 100).toInt()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BlueDroplet.copy(alpha = 0.18f))
                                .border(0.7.dp, BlueDroplet.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$waterPercent%",
                                color = BlueDroplet,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Water Main Metric
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "%,d".format(waterMl),
                                color = TextWhite,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = " / %,d ml".format(waterGoalMl),
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                            )
                        }

                        Text(
                            text = "%.1f L remaining".format(((waterGoalMl - waterMl).coerceAtLeast(0)) / 1000f),
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Liquid Progress Bar
                    val animatedWaterProgress by animateFloatAsState(
                        targetValue = (waterMl.toFloat() / waterGoalMl.coerceAtLeast(1)).coerceIn(0f, 1f),
                        label = "WaterProgressAnim"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedWaterProgress)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(BlueDroplet, CyanBright)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Log Buttons (+150ml, +250ml, +500ml) & Undo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.10f))
                                .border(0.7.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.addWater(150, "Cup") }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+150 ml", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BlueDroplet.copy(alpha = 0.22f))
                                .border(0.8.dp, BlueDroplet.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.addWater(250, "Glass") }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+250 ml", color = CyanBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.10f))
                                .border(0.7.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.addWater(500, "Bottle") }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+500 ml", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(0.7.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.undoWater() }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Undo Drink",
                                tint = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Water History Collapsible Toggle
                    if (waterEntries.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAllWaterLogs = !showAllWaterLogs }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showAllWaterLogs) "Hide today's water log" else "View today's drinks (${waterEntries.size})",
                                color = CyanBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (showAllWaterLogs) "▲" else "▼",
                                color = CyanBright,
                                fontSize = 10.sp
                            )
                        }

                        AnimatedVisibility(
                            visible = showAllWaterLogs,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
                                waterEntries.forEach { entry ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${timeFormat.format(Date(entry.timestamp))} • ${entry.vessel}",
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "+${entry.amountMl} ml",
                                            color = BlueDroplet,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================================================================
            // 3. REFLECTIVE JOURNAL COMPOSER & HISTORY
            // ================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reflections & Journal",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
                )

                // Write Reflection Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isComposerExpanded) CyanBright.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f))
                        .border(
                            0.8.dp,
                            if (isComposerExpanded) CyanBright else Color.White.copy(alpha = 0.25f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { isComposerExpanded = !isComposerExpanded }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isComposerExpanded) Icons.Default.Close else Icons.Default.EditNote,
                            contentDescription = null,
                            tint = if (isComposerExpanded) CyanBright else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isComposerExpanded) "Close" else "Write Entry",
                            color = if (isComposerExpanded) CyanBright else TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Journal Composer
            AnimatedVisibility(
                visible = isComposerExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    cornerRadius = 24.dp,
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "New Reflection",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Auto-captures: %,d steps • %,d ml water".format(currentSteps, waterMl),
                            color = CyanBright.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mood Tag Selector
                        Text(
                            text = "Current State:",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            moodOptions.take(3).forEach { (moodName, moodColor) ->
                                val isSelected = selectedMoodTag == moodName
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) moodColor.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.07f))
                                        .border(
                                            0.8.dp,
                                            if (isSelected) moodColor else Color.White.copy(alpha = 0.15f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedMoodTag = moodName }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = moodName,
                                        color = if (isSelected) moodColor else Color.White.copy(alpha = 0.75f),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            moodOptions.drop(3).forEach { (moodName, moodColor) ->
                                val isSelected = selectedMoodTag == moodName
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) moodColor.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.07f))
                                        .border(
                                            0.8.dp,
                                            if (isSelected) moodColor else Color.White.copy(alpha = 0.15f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedMoodTag = moodName }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = moodName,
                                        color = if (isSelected) moodColor else Color.White.copy(alpha = 0.75f),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Entry Title Input
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(0.8.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            if (entryTitle.isEmpty()) {
                                Text(
                                    text = "Title (e.g. Morning thoughts, Quiet evening...)",
                                    color = Color.White.copy(alpha = 0.40f),
                                    fontSize = 13.sp
                                )
                            }
                            BasicTextField(
                                value = entryTitle,
                                onValueChange = { entryTitle = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                                cursorBrush = SolidColor(CyanBright),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Entry Content Input
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(0.8.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            if (entryContent.isEmpty()) {
                                Text(
                                    text = "Pour your thoughts, feelings, breakthroughs, or notes for the day here...",
                                    color = Color.White.copy(alpha = 0.40f),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                            BasicTextField(
                                value = entryContent,
                                onValueChange = { entryContent = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 13.sp, lineHeight = 18.sp),
                                cursorBrush = SolidColor(CyanBright),
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Submit Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CyanBright.copy(alpha = 0.3f))
                                    .border(1.dp, CyanBright, RoundedCornerShape(14.dp))
                                    .clickable {
                                        if (entryContent.isNotBlank() || entryTitle.isNotBlank()) {
                                            viewModel.saveJournalEntry(
                                                title = entryTitle,
                                                content = entryContent,
                                                mood = selectedMoodTag
                                            )
                                            entryTitle = ""
                                            entryContent = ""
                                            isComposerExpanded = false
                                        }
                                    }
                                    .padding(horizontal = 18.dp, vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = CyanBright,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Save Reflection",
                                        color = TextWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Journal Entries Feed
            if (journalEntries.isEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your journal is quiet",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Write Entry' above to capture your thoughts, emotions, and daily milestones.",
                            color = Color.White.copy(alpha = 0.50f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                val timeFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    journalEntries.forEach { entry ->
                        val moodColor = moodOptions.find { it.first.equals(entry.mood, ignoreCase = true) }?.second ?: CyanBright
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp,
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(moodColor.copy(alpha = 0.20f))
                                                .border(0.7.dp, moodColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = entry.mood,
                                                color = moodColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = timeFormat.format(Date(entry.timestamp)),
                                            color = Color.White.copy(alpha = 0.50f),
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Delete icon
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete entry",
                                        tint = Color.White.copy(alpha = 0.40f),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { entryToDelete = entry }
                                    )
                                }

                                if (entry.title.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = entry.title,
                                        color = TextWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (entry.content.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = entry.content,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Snapshot Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "👟 %,d steps".format(entry.stepCountAtEntry),
                                            color = Color.White.copy(alpha = 0.65f),
                                            fontSize = 10.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "💧 %,d ml water".format(entry.waterMlAtEntry),
                                            color = Color.White.copy(alpha = 0.65f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ================================================================
            // 4. WEEKLY HEALTH TRENDS (REAL DATA)
            // ================================================================
            Text(
                text = "Weekly Activity Pattern",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metric Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(0.8.dp, FrostedGlassBorder, RoundedCornerShape(20.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricViewType.values().forEach { mType ->
                    val isSel = mType == selectedMetric
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSel) Color.White.copy(alpha = 0.16f) else Color.Transparent)
                            .then(
                                if (isSel) Modifier.border(0.7.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                else Modifier
                            )
                            .clickable { viewModel.setMetricType(mType) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mType.label.substringBefore(" "),
                            color = if (isSel) TextWhite else Color.White.copy(alpha = 0.60f),
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val points = viewModel.getChartPoints()
                    InteractiveBarGraph(
                        points = points,
                        selectedIndex = selectedIndex,
                        onSelectIndex = { viewModel.selectGraphIndex(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        unit = selectedMetric.unit,
                        maxVal = when (selectedMetric) {
                            MetricViewType.CALORIES -> 850f
                            MetricViewType.STEPS -> 12000f
                            MetricViewType.WATER -> 3000f
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }

        // Delete Confirmation Dialog
        entryToDelete?.let { entry ->
            AlertDialog(
                onDismissRequest = { entryToDelete = null },
                containerColor = Color(0xFF102048),
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        text = "Delete Reflection?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete this journal entry? This action cannot be undone.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteJournalEntry(entry.id)
                            entryToDelete = null
                        }
                    ) {
                        Text("Delete", color = PinkGlow, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { entryToDelete = null }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }
    }
}
