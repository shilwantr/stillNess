package com.example.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.InteractiveWaveGraph
import com.example.ui.components.InteractiveBarGraph
import com.example.ui.glass.GlassCard
import com.example.ui.theme.BlueDroplet
import com.example.ui.theme.CyanBright
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PinkGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VioletBright
import com.example.ui.viewmodel.MetricViewType
import com.example.ui.viewmodel.TimeframeView
import com.example.ui.viewmodel.WellnessViewModel

@Composable
fun TrendsScreen(
    viewModel: WellnessViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMetric by viewModel.selectedMetricType.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val selectedIndex by viewModel.selectedGraphIndex.collectAsState()
    val recentMetrics by viewModel.recentMetrics.collectAsState()

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("trends_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            Text(
                text = "J O U R N A L",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 6.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(
                text = "Personal Health Trends & Daily Balance",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Metric Selector Tabs (Ultra-Transparent Frosted Glass Pill)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(0.8.dp, FrostedGlassBorder, RoundedCornerShape(24.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricViewType.values().forEach { mType ->
                    val isSel = mType == selectedMetric
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) Color.White.copy(alpha = 0.16f) else Color.Transparent)
                            .then(
                                if (isSel) Modifier.border(0.7.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
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

            Spacer(modifier = Modifier.height(14.dp))

            // Large Detailed Chart Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevation = 2.dp,
                testTag = "trends_wave_chart_card"
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
                        Column {
                            Text(
                                text = "${selectedMetric.label} Trend",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Weekly pattern across tracked sessions",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 11.sp
                            )
                        }

                        // Timeframe pills
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(0.7.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            TimeframeView.values().forEach { tf ->
                                val isSel = tf == selectedTimeframe
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) Color.White.copy(alpha = 0.18f) else Color.Transparent)
                                        .clickable { viewModel.setTimeframe(tf) }
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = tf.label,
                                        color = if (isSel) TextWhite else Color.White.copy(alpha = 0.55f),
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val points = viewModel.getChartPoints()
                    InteractiveBarGraph(
                        points = points,
                        selectedIndex = selectedIndex,
                        onSelectIndex = { viewModel.selectGraphIndex(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp),
                        unit = selectedMetric.unit,
                        maxVal = when (selectedMetric) {
                            MetricViewType.CALORIES -> 850f
                            MetricViewType.STEPS -> 12000f
                            MetricViewType.WATER -> 3000f
                            MetricViewType.EXERCISE -> 80f
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Key Highlights Row (Average, Streak, Goal Rate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f).height(98.dp),
                    cornerRadius = 18.dp,
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("DAILY AVG", color = Color.White.copy(alpha = 0.50f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = when (selectedMetric) {
                                MetricViewType.CALORIES -> "512 kcal"
                                MetricViewType.STEPS -> "8,450"
                                MetricViewType.WATER -> "2.1 L"
                                MetricViewType.EXERCISE -> "44 min"
                            },
                            color = CyanBright,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("+6.4% this week", color = Color.White.copy(alpha = 0.60f), fontSize = 10.sp)
                    }
                }

                GlassCard(
                    modifier = Modifier.weight(1f).height(98.dp),
                    cornerRadius = 18.dp,
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("STREAK", color = Color.White.copy(alpha = 0.50f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("7 Days", color = VioletBright, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Personal best!", color = Color.White.copy(alpha = 0.60f), fontSize = 10.sp)
                    }
                }

                GlassCard(
                    modifier = Modifier.weight(1f).height(98.dp),
                    cornerRadius = 18.dp,
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("GOAL RATE", color = Color.White.copy(alpha = 0.50f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("88%", color = PinkGlow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("On track", color = Color.White.copy(alpha = 0.60f), fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Historical Daily Breakdown List
            Text(
                text = "Recent Daily Log",
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    val logs = recentMetrics.take(7)
                    logs.forEachIndexed { index, metric ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(metric.date, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    "${metric.steps} steps • ${metric.exerciseMinutes} min active",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 11.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("${metric.activeCalories} kcal", color = OrangeFlame, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${metric.waterMl} ml", color = BlueDroplet, fontSize = 11.sp)
                            }
                        }
                        if (index < logs.size - 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(Color.White.copy(alpha = 0.1f))
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}
