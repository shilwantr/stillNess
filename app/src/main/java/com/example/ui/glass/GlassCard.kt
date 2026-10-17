package com.example.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.FrostedGlassBorder
import io.github.fletchmckee.liquid.liquid

/**
 * Clean, Borderless Refractive Liquid Glass Card.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    cornerRadius: Dp = 28.dp,
    borderBrush: Brush = FrostedGlassBorder,
    borderWidth: Dp = 0.dp,
    elevation: Dp = 0.dp,
    glowColor: Color = CyanGlow,
    onClick: (() -> Unit)? = null,
    testTag: String = "reusable_glass_card",
    content: @Composable BoxScope.() -> Unit
) {
    val actualShape = RoundedCornerShape(cornerRadius)
    val liquidState = LocalLiquidState.current

    val lensModifier = if (liquidState != null) {
        modifier.liquid(liquidState) {
            this.shape = actualShape
            frost = 0.dp
            refraction = 0.45f
            curve = 0.55f
            edge = 0.0f
            tint = Color.White.copy(alpha = 0.04f)
            saturation = 1.3f
            dispersion = 0.25f
        }
    } else {
        modifier.liquidGlassCardShader(
            cornerRadius = cornerRadius,
            refractionAmount = 5.dp,
            aberration = 0.20f
        )
    }

    if (onClick != null) {
        Surface(
            modifier = lensModifier
                .testTag(testTag)
                .clip(actualShape),
            shape = actualShape,
            color = Color.Transparent,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            onClick = onClick
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.04f))
            ) {
                content()
            }
        }
    } else {
        Surface(
            modifier = lensModifier
                .testTag(testTag)
                .clip(actualShape),
            shape = actualShape,
            color = Color.Transparent,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.04f))
            ) {
                content()
            }
        }
    }
}

/**
 * Clean, Borderless Liquid Glass Pill Search Bar.
 */
@Composable
fun GlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search practices, metrics, guides...",
    onClear: () -> Unit = { onQueryChange("") }
) {
    val shape = RoundedCornerShape(50)
    val liquidState = LocalLiquidState.current

    val glassModifier = if (liquidState != null) {
        modifier.liquid(liquidState) {
            this.shape = shape
            frost = 0.dp
            refraction = 0.35f
            curve = 0.45f
            edge = 0.0f
            tint = Color.White.copy(alpha = 0.06f)
            saturation = 1.2f
            dispersion = 0.15f
        }
    } else {
        modifier
    }

    Box(
        modifier = glassModifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White.copy(alpha = 0.80f),
                modifier = Modifier.size(17.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(CyanBright),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = Color.White.copy(alpha = 0.80f),
                    modifier = Modifier
                        .size(17.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false, color = Color.White),
                            onClick = onClear
                        )
                )
            }
        }
    }
}

/**
 * Clean, Borderless Refractive Liquid Glass Tile.
 */
@Composable
fun GlassTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.White
) {
    GlassCard(
        modifier = modifier.height(68.dp),
        cornerRadius = 22.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(11.dp))

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 11.sp
                )
            }
        }
    }
}