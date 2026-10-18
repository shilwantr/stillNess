package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.glass.LocalLiquidState
import com.example.ui.glass.liquidGlassCardShader
import com.example.ui.theme.CyanBright
import io.github.fletchmckee.liquid.liquid
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class StillTab(val title: String, val icon: ImageVector, val tag: String) {
    TODAY("Today", Icons.Default.Home, "nav_tab_today"),
    BREATHE("Practice", Icons.Default.SelfImprovement, "nav_tab_breathe"),
    TRENDS("Journal", Icons.Default.EditNote, "nav_tab_trends"),
    THERAPISTS("Therapy", Icons.Default.SupportAgent, "nav_tab_therapists"),
    PROFILE("Profile", Icons.Default.Person, "nav_tab_profile")
}

@Composable
fun StillBottomNavBar(
    currentTab: StillTab,
    onTabSelected: (StillTab) -> Unit,
    modifier: Modifier = Modifier
) {
    // Store measured bounds of each tab inside the Row
    val tabBounds = remember { mutableStateMapOf<StillTab, Rect>() }

    // Animatables for the left and right edges of the fluid bubble
    val bubbleLeft = remember { Animatable(0f) }
    val bubbleRight = remember { Animatable(0f) }
    val isBubbleInitialized = remember { mutableStateOf(false) }

    val targetRect = tabBounds[currentTab]

    LaunchedEffect(targetRect) {
        if (targetRect == null) return@LaunchedEffect

        if (!isBubbleInitialized.value) {
            // Snap immediately on first layout pass
            bubbleLeft.snapTo(targetRect.left)
            bubbleRight.snapTo(targetRect.right)
            isBubbleInitialized.value = true
        } else {
            val movingRight = targetRect.left >= bubbleLeft.value

            // Slow, viscous reach toward the newly selected tab
            val leadingSpec = spring<Float>(
                dampingRatio = 0.5f,
                stiffness = 50f
            )
            // Slow, elastic tail pull with soft liquid wobble on arrival
            val trailingSpec = spring<Float>(
                dampingRatio = 0.5f,
                stiffness = 50f
            )

            if (movingRight) {
                launch {
                    bubbleRight.animateTo(
                        targetValue = targetRect.right,
                        animationSpec = leadingSpec
                    )
                }
                launch {
                    delay(70) // Surface-tension cling before tail detaches
                    bubbleLeft.animateTo(
                        targetValue = targetRect.left,
                        animationSpec = trailingSpec
                    )
                }
            } else {
                launch {
                    bubbleLeft.animateTo(
                        targetValue = targetRect.left,
                        animationSpec = leadingSpec
                    )
                }
                launch {
                    delay(70) // Surface-tension cling before tail detaches
                    bubbleRight.animateTo(
                        targetValue = targetRect.right,
                        animationSpec = trailingSpec
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .testTag("still_bottom_nav_bar"),
        contentAlignment = Alignment.Center
    ) {
        val barShape = RoundedCornerShape(32.dp)
        val bubbleShape = RoundedCornerShape(60.dp)
        val liquidState = LocalLiquidState.current

        // Outer glass bar shader
        val barModifier = if (liquidState != null) {
            Modifier.liquid(liquidState) {
                this.shape = barShape
                frost = 0.dp
                refraction = 0.1f
                curve = 0.50f
                edge = 0.0f
                tint = Color.White.copy(alpha = 0.04f)
                saturation = 1.3f
                dispersion = 0.0f
            }
        } else {
            Modifier.liquidGlassCardShader(
                cornerRadius = 32.dp,
                refractionAmount = 6.dp,
                aberration = 0.22f
            )
        }

        // Shifting fluid droplet shader (stronger convex lens & refraction, no white rim)
        val bubbleLiquidModifier = if (liquidState != null) {
            Modifier.liquid(liquidState) {
                this.shape = bubbleShape
                frost = 0.1.dp
                refraction = 0.5f
                curve = 0.78f
                edge = 0.0f
                tint = CyanBright.copy(alpha = 0.05f)
                saturation = 1.45f
                dispersion = 0.09f
            }
        } else {
            Modifier.liquidGlassCardShader(
                cornerRadius = 22.dp,
                refractionAmount = 11.dp,
                aberration = 0.35f
            )
        }

        // Glass Bar Container (holds both the liquid bubble and the foreground tab Row)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 0.dp,
                    shape = barShape,
                    ambientColor = Color(0xFF001858).copy(alpha = 0.40f),
                    spotColor = CyanBright.copy(alpha = 0.25f)
                )
                .then(barModifier)
                .clip(barShape)
                .background(Color.White.copy(alpha = 0.04f))
                .padding(horizontal = 8.dp, vertical = 7.dp),
            contentAlignment = Alignment.TopStart
        ) {
            // 1. Moving Liquid Glass Droplet Bubble
            if (targetRect != null && isBubbleInitialized.value) {
                Box(
                    modifier = Modifier
                        .layout { measurable, _ ->
                            val activeRect = targetRect
                            val left = bubbleLeft.value
                            val right = bubbleRight.value
                            val currentWidth = (right - left).coerceAtLeast(1f)
                            val baseWidth = activeRect.width.coerceAtLeast(1f)

                            // Compress height slightly when stretched across tabs; plump on arrival bounce
                            val stretchRatio =
                                ((currentWidth - baseWidth) / baseWidth).coerceIn(-0.15f, 1.8f)
                            val verticalSquash = stretchRatio * 3.5.dp.toPx()

                            val top = activeRect.top + verticalSquash
                            val height =
                                (activeRect.height - verticalSquash * 2f).coerceAtLeast(2f)

                            val widthPx = currentWidth.roundToInt().coerceAtLeast(1)
                            val heightPx = height.roundToInt().coerceAtLeast(1)

                            val placeable =
                                measurable.measure(Constraints.fixed(widthPx, heightPx))
                            layout(widthPx, heightPx) {
                                placeable.place(left.roundToInt(), top.roundToInt())
                            }
                        }
                        .then(bubbleLiquidModifier)
                        .clip(bubbleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                )
            }

            // 2. Foreground Tab Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StillTab.entries.forEach { tab ->
                    val isSelected = tab == currentTab

                    // Slower swell as the droplet engulfs the icon
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = 0.58f,
                            stiffness = 75f
                        ),
                        label = "tabScale"
                    )

                    val dotScale by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = spring(
                            dampingRatio = 0.55f,
                            stiffness = 90f
                        ),
                        label = "dotScale"
                    )

                    val tint by animateColorAsState(
                        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f),
                        animationSpec = spring(stiffness = 90f),
                        label = "tabTint"
                    )

                    Box(
                        modifier = Modifier
                            .testTag(tab.tag)
                            .onGloballyPositioned { coordinates ->
                                tabBounds[tab] = coordinates.boundsInParent()
                            }
                            .clip(bubbleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.White.copy(alpha = 0.25f)),
                                onClick = { onTabSelected(tab) }
                            )
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.scale(scale),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = tint,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.title,
                                color = tint,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(3.5.dp)
                                    .scale(dotScale)
                                    .clip(CircleShape)
                                    .background(CyanBright)
                            )
                        }
                    }
                }
            }
        }
    }
}