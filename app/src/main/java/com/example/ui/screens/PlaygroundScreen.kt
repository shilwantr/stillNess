package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.glass.GlassRobot
import com.example.ui.glass.isLight
import com.example.ui.glass.liquidGlassCardShader
import com.example.ui.viewmodel.WellnessViewModel
import io.github.fletchmckee.liquid.liquid
import com.example.ui.glass.LocalLiquidState

enum class PlaygroundTab(val title: String) {
    GAMES("Games"),
    MEW_MEW("Mew Mew"),
    EMO("EMO")
}

@Composable
fun PlaygroundScreen(
    viewModel: WellnessViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(PlaygroundTab.GAMES) }
    
    val isLight = com.example.ui.glass.LocalAmbientTheme.current.isLight
    val textColor = if (isLight) Color(0xFF1E2A20) else Color.White
    val textSubtle = if (isLight) Color(0xFF3B503E) else Color.White.copy(alpha = 0.65f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "P L A Y",
                    color = textColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 6.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Interactive Wellness Companions",
                    color = textSubtle,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        val pillShape = RoundedCornerShape(50.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(pillShape)
                .background(Color.White.copy(alpha = 0.08f))
                .border(0.8.dp, Color.White.copy(alpha = 0.15f), pillShape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaygroundTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.55f, stiffness = 80f),
                    label = "tabScale"
                )
                val targetColor = if (isSelected) {
                    if (isLight) Color(0xFF1E2A20) else Color.White
                } else {
                    if (isLight) Color(0xFF3B503E) else Color.White.copy(alpha = 0.6f)
                }
                val tabTextColor by animateColorAsState(targetValue = targetColor, label = "tabTextColor")

                val bgBrush = if (isSelected) {
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(30.dp))
                        .background(bgBrush)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                            onClick = { selectedTab = tab }
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.title,
                        color = tabTextColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.scale(scale)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = selectedTab,
                label = "PlaygroundCrossfade"
            ) { tab ->
                when (tab) {
                    PlaygroundTab.GAMES -> StillGamesScreen()
                    PlaygroundTab.MEW_MEW -> GlassRobot()
                    PlaygroundTab.EMO -> EmoRobotScreen(onClose = {})
                }
            }
        }
    }
}
