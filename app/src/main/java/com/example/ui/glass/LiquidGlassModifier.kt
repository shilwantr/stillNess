package com.example.ui.glass

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import io.github.fletchmckee.liquid.LiquidState

val LocalLiquidState = compositionLocalOf<LiquidState?> { null }

@org.intellij.lang.annotations.Language("AGSL")
private const val LiquidGlassShaderSrc = """
    uniform shader composable;
    uniform float2 size;
    uniform float cornerRadius;
    uniform float refractionAmount;
    uniform float aberration;

    vec4 main(vec2 fragCoord) {
        // Evaluate the pixel from the composable layer
        vec4 color = composable.eval(fragCoord);
        
        // Add subtle chromatic aberration effect
        vec2 offsetR = vec2(aberration, 0.0);
        vec2 offsetB = vec2(-aberration, 0.0);
        
        float r = composable.eval(fragCoord + offsetR).r;
        float g = color.g;
        float b = composable.eval(fragCoord + offsetB).b;
        
        // Mix the original color with the aberrated colors based on alpha
        return vec4(mix(color.r, r, color.a), g, mix(color.b, b, color.a), color.a);
    }
"""

/**
 * Applies a liquid glass shader effect to the modifier.
 * On Android 13+ (API 33+), it uses AGSL RuntimeShader.
 * On older versions, it acts as a no-op.
 */
fun Modifier.liquidGlassCardShader(
    cornerRadius: Dp,
    refractionAmount: Dp,
    aberration: Float
): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.graphicsLayer {
            val shader = RuntimeShader(LiquidGlassShaderSrc)
            shader.setFloatUniform("size", size.width, size.height)
            shader.setFloatUniform("cornerRadius", cornerRadius.toPx())
            shader.setFloatUniform("refractionAmount", refractionAmount.toPx())
            shader.setFloatUniform("aberration", aberration)

            renderEffect = RenderEffect.createRuntimeShaderEffect(
                shader, "composable"
            ).asComposeRenderEffect()
        }
    } else {
        this
    }
}

@org.intellij.lang.annotations.Language("AGSL")
private const val LiquidGlassBubbleShaderSrc = """
    uniform shader composable;
    uniform float2 size;
    uniform float phase;
    uniform float expansion;

    vec4 main(vec2 fragCoord) {
        // Subtle distortion based on phase
        vec2 uv = fragCoord / size;
        
        // Add a slight liquid wobble effect
        float wobble = sin(uv.y * 10.0 + phase) * 0.005 * expansion;
        vec2 distortedCoord = fragCoord + vec2(wobble * size.x, 0.0);
        
        return composable.eval(distortedCoord);
    }
"""

/**
 * Applies a liquid glass bubble shader effect to the modifier.
 * On Android 13+ (API 33+), it uses AGSL RuntimeShader.
 * On older versions, it acts as a no-op.
 */
fun Modifier.liquidGlassBubbleShader(
    phase: Float,
    expansion: Float
): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.graphicsLayer {
            val shader = RuntimeShader(LiquidGlassBubbleShaderSrc)
            shader.setFloatUniform("size", size.width, size.height)
            shader.setFloatUniform("phase", phase)
            shader.setFloatUniform("expansion", expansion)

            renderEffect = RenderEffect.createRuntimeShaderEffect(
                shader, "composable"
            ).asComposeRenderEffect()
        }
    } else {
        this
    }
}
