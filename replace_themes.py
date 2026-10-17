import re

with open('app/src/main/java/com/example/ui/glass/CosmicHorizonBackground.kt', 'r', encoding='utf-8') as f:
    content = f.read()

def get_theme_func(name, bg_colors, bloom_colors, wave_colors, crest_colors):
    return f"""private fun DrawScope.{name}(w: Float, h: Float, p1: Float, p2: Float, pulse: Float) {{
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
{bg_colors}
            ),
            start = Offset.Zero,
            end = Offset(w, h)
        )
    )

    val bloomCenter = Offset(
        x = w * (0.22f + 0.06f * sin(p1)),
        y = h * (0.18f + 0.04f * cos(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
{bloom_colors},
                Color.Transparent
            ),
            center = bloomCenter,
            radius = w * 0.85f
        ),
        center = bloomCenter,
        radius = w * 0.85f
    )

    val themeWave = Path().apply {{
        moveTo(0f, h * 0.68f)
        cubicTo(
            w * 0.28f, h * (0.52f + 0.05f * sin(p1)),
            w * 0.62f, h * (0.18f + 0.05f * cos(p2)),
            w, h * 0.06f
        )
        lineTo(w, h * 0.64f)
        cubicTo(
            w * 0.68f, h * (0.54f - 0.04f * sin(p2)),
            w * 0.34f, h * (0.82f + 0.04f * cos(p1)),
            0f, h * 0.94f
        )
        close()
    }}

    drawPath(
        path = themeWave,
        brush = Brush.linearGradient(
            colors = listOf(
{wave_colors}
            ),
            start = Offset(0f, h * 0.8f),
            end = Offset(w, h * 0.1f)
        )
    )

    val crestCenter = Offset(
        x = w * (0.72f - 0.07f * cos(p1)),
        y = h * (0.38f + 0.05f * sin(p2))
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
{crest_colors},
                Color.Transparent
            ),
            center = crestCenter,
            radius = w * 0.65f
        ),
        center = crestCenter,
        radius = w * 0.65f
    )
}}
"""

nordic_bg = "                Color(0xFF0B101B),\n                Color(0xFF131B2A),\n                Color(0xFF1B263B),\n                Color(0xFF0F1624)"
nordic_bloom = "                Color(0xFF7C98B3).copy(alpha = 0.35f * pulse),\n                Color(0xFFC5B4E3).copy(alpha = 0.22f)"
nordic_wave = "                Color(0xFF7C98B3).copy(alpha = 0.45f),\n                Color(0xFF8B9CB6).copy(alpha = 0.40f),\n                Color(0xFFC5B4E3).copy(alpha = 0.32f),\n                Color(0xFFB3A1D6).copy(alpha = 0.25f)"
nordic_crest = "                Color(0xFFC5B4E3).copy(alpha = 0.28f * pulse),\n                Color(0xFF7C98B3).copy(alpha = 0.15f)"

linen_bg = "                Color(0xFFF4F1EA),\n                Color(0xFFEEEAE2),\n                Color(0xFFE8E2D7),\n                Color(0xFFDFD9CE)"
linen_bloom = "                Color(0xFF6B705C).copy(alpha = 0.18f * pulse),\n                Color(0xFF8F937D).copy(alpha = 0.10f)"
linen_wave = "                Color(0xFF6B705C).copy(alpha = 0.25f),\n                Color(0xFF8F937D).copy(alpha = 0.20f),\n                Color(0xFFC87D65).copy(alpha = 0.15f),\n                Color(0xFFE8E2D7).copy(alpha = 0.10f)"
linen_crest = "                Color(0xFFC87D65).copy(alpha = 0.14f * pulse),\n                Color(0xFF6B705C).copy(alpha = 0.08f)"

dusk_bg = "                Color(0xFF0B091A),\n                Color(0xFF1E1035),\n                Color(0xFF2E134D),\n                Color(0xFF220D3D)"
dusk_bloom = "                Color(0xFFC084FC).copy(alpha = 0.36f * pulse),\n                Color(0xFFA855F7).copy(alpha = 0.20f)"
dusk_wave = "                Color(0xFFC084FC).copy(alpha = 0.45f),\n                Color(0xFF6B21A8).copy(alpha = 0.40f),\n                Color(0xFFFB923C).copy(alpha = 0.32f),\n                Color(0xFFF43F5E).copy(alpha = 0.25f)"
dusk_crest = "                Color(0xFFFB923C).copy(alpha = 0.28f * pulse),\n                Color(0xFFF43F5E).copy(alpha = 0.15f)"

ocean_bg = "                Color(0xFF021B2B),\n                Color(0xFF052C46),\n                Color(0xFF093E62),\n                Color(0xFF05263D)"
ocean_bloom = "                Color(0xFF22D3EE).copy(alpha = 0.35f * pulse),\n                Color(0xFF06B6D4).copy(alpha = 0.20f)"
ocean_wave = "                Color(0xFF22D3EE).copy(alpha = 0.45f),\n                Color(0xFF06B6D4).copy(alpha = 0.40f),\n                Color(0xFF38BDF8).copy(alpha = 0.32f),\n                Color(0xFF0284C7).copy(alpha = 0.25f)"
ocean_crest = "                Color(0xFF38BDF8).copy(alpha = 0.28f * pulse),\n                Color(0xFF0284C7).copy(alpha = 0.15f)"

nordic_func = get_theme_func("drawNordicTwilight", nordic_bg, nordic_bloom, nordic_wave, nordic_crest)
linen_func = get_theme_func("drawWarmLinen", linen_bg, linen_bloom, linen_wave, linen_crest)
dusk_func = get_theme_func("drawCosmicDusk", dusk_bg, dusk_bloom, dusk_wave, dusk_crest)
ocean_func = get_theme_func("drawOceanAbyss", ocean_bg, ocean_bloom, ocean_wave, ocean_crest)

pattern = r'private fun DrawScope\.drawNordicTwilight\(.*?\).*?private fun DrawScope\.drawFloatingLightMotes'
replacement = f"{nordic_func}\n{linen_func}\n{dusk_func}\n{ocean_func}\nprivate fun DrawScope.drawFloatingLightMotes"

new_content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/glass/CosmicHorizonBackground.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)

