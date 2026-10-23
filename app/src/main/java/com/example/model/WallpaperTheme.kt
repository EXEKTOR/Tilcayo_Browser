package com.example.model

import androidx.compose.ui.graphics.Color

enum class ThemeMode(val title: String) {
    LIGHT("Светлая"),
    DARK("Тёмная"),
    SYSTEM("Системная")
}

data class GradientPreset(
    val id: String,
    val name: String,
    val colors: List<Color>,
    val accentColor: Color,
    val isLight: Boolean = false
)

data class AnimatedPreset(
    val id: String,
    val name: String,
    val description: String,
    val previewColors: List<Color>,
    val accentColor: Color,
    val isLight: Boolean = false
)

object WallpaperPresets {
    val animatedPresets = listOf(
        AnimatedPreset(
            id = "anim_aurora",
            name = "Северное сияние",
            description = "Плавные неоновые волны и изумрудные переливы (60 FPS)",
            previewColors = listOf(Color(0xFF0D2418), Color(0xFF00B4D8), Color(0xFF7209B7)),
            accentColor = Color(0xFF00F5D4),
            isLight = false
        ),
        AnimatedPreset(
            id = "anim_stardust",
            name = "Космическая пыль",
            description = "Парящие светящиеся золотые и лазурные микросферы",
            previewColors = listOf(Color(0xFF0F172A), Color(0xFF312E81), Color(0xFFE58A2B)),
            accentColor = Color(0xFFFBBF24),
            isLight = false
        ),
        AnimatedPreset(
            id = "anim_matrix",
            name = "Кибернетический поток",
            description = "Неоновый диджитал-дождь символов и квантовые линии",
            previewColors = listOf(Color(0xFF051910), Color(0xFF003820), Color(0xFF00FF66)),
            accentColor = Color(0xFF00FF88),
            isLight = false
        ),
        AnimatedPreset(
            id = "anim_liquid_gold",
            name = "Жидкое золото и перламутр",
            description = "Мягкие светлые перламутровые волны с золотистым блеском",
            previewColors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7), Color(0xFFFDE68A)),
            accentColor = Color(0xFFD97706),
            isLight = true
        ),
        AnimatedPreset(
            id = "anim_morning_dew",
            name = "Утренняя роса",
            description = "Зацикленные мягкие аквамариновые круги и хрустальные блики",
            previewColors = listOf(Color(0xFFF0FDF4), Color(0xFFE0F2FE), Color(0xFFBAE6FD)),
            accentColor = Color(0xFF0284C7),
            isLight = true
        ),
        AnimatedPreset(
            id = "anim_sun_dust",
            name = "Солнечные лучи и пыльца",
            description = "Парящие золотистые световые частицы на мягком градиенте",
            previewColors = listOf(Color(0xFFFFFDF5), Color(0xFFFEF3C7), Color(0xFFFDE68A)),
            accentColor = Color(0xFFE58A2B),
            isLight = true
        )
    )

    val presets = listOf(
        // Light Presets
        GradientPreset(
            id = "preset_pearl_white",
            name = "Белый жемчуг",
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF1F5F9),
                Color(0xFFE2E8F0)
            ),
            accentColor = Color(0xFFD97706),
            isLight = true
        ),
        GradientPreset(
            id = "preset_morning_sun",
            name = "Утренний рассвет",
            colors = listOf(
                Color(0xFFFFFBEB),
                Color(0xFFFEF3C7),
                Color(0xFFFDE68A)
            ),
            accentColor = Color(0xFFB45309),
            isLight = true
        ),
        GradientPreset(
            id = "preset_frost_ice",
            name = "Ледяной кристалл",
            colors = listOf(
                Color(0xFFF0FDF4),
                Color(0xFFE0F2FE),
                Color(0xFFEDE9FE)
            ),
            accentColor = Color(0xFF0284C7),
            isLight = true
        ),
        GradientPreset(
            id = "preset_mint_porcelain",
            name = "Мятный фарфор",
            colors = listOf(
                Color(0xFFF0FDF4),
                Color(0xFFDCFCE7),
                Color(0xFFBBF7D0)
            ),
            accentColor = Color(0xFF15803D),
            isLight = true
        ),
        GradientPreset(
            id = "preset_rose_opal",
            name = "Розовый опал",
            colors = listOf(
                Color(0xFFFFF1F2),
                Color(0xFFFFE4E6),
                Color(0xFFFECDD3)
            ),
            accentColor = Color(0xFFBE123C),
            isLight = true
        ),
        GradientPreset(
            id = "preset_lavender_haze",
            name = "Лавандовая дымка",
            colors = listOf(
                Color(0xFFFAF5FF),
                Color(0xFFF3E8FF),
                Color(0xFFE9D5FF)
            ),
            accentColor = Color(0xFF9333EA),
            isLight = true
        ),
        GradientPreset(
            id = "preset_sand_dune",
            name = "Золотистая дюна",
            colors = listOf(
                Color(0xFFFFFBEB),
                Color(0xFFF7EEDD),
                Color(0xFFEADBCE)
            ),
            accentColor = Color(0xFFD97706),
            isLight = true
        ),

        // Dark Presets
        GradientPreset(
            id = "preset_amber",
            name = "Золотой Тилкайо",
            colors = listOf(
                Color(0xFF2E1C0C),
                Color(0xFF141210),
                Color(0xFF1C140F)
            ),
            accentColor = Color(0xFFE58A2B),
            isLight = false
        ),
        GradientPreset(
            id = "preset_cosmic",
            name = "Глубокий космос",
            colors = listOf(
                Color(0xFF141830),
                Color(0xFF0E101A),
                Color(0xFF221638)
            ),
            accentColor = Color(0xFF6C8CFF),
            isLight = false
        ),
        GradientPreset(
            id = "preset_emerald",
            name = "Изумрудный кот",
            colors = listOf(
                Color(0xFF0D2418),
                Color(0xFF0E1412),
                Color(0xFF122E20)
            ),
            accentColor = Color(0xFF2ECC71),
            isLight = false
        ),
        GradientPreset(
            id = "preset_sunset",
            name = "Неоновый закат",
            colors = listOf(
                Color(0xFF331429),
                Color(0xFF13101C),
                Color(0xFF1D1B3A)
            ),
            accentColor = Color(0xFFFF6B8B),
            isLight = false
        ),
        GradientPreset(
            id = "preset_obsidian",
            name = "Тёмный обсидиан",
            colors = listOf(
                Color(0xFF0D0E12),
                Color(0xFF161820),
                Color(0xFF0F1015)
            ),
            accentColor = Color(0xFFD1D5DB),
            isLight = false
        ),
        GradientPreset(
            id = "preset_amethyst_night",
            name = "Аметистовая полночь",
            colors = listOf(
                Color(0xFF241433),
                Color(0xFF13091B),
                Color(0xFF1F102B)
            ),
            accentColor = Color(0xFFA855F7),
            isLight = false
        ),
        GradientPreset(
            id = "preset_deep_slate",
            name = "Глубокий кибер-сланец",
            colors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1E293B),
                Color(0xFF0B1120)
            ),
            accentColor = Color(0xFF38BDF8),
            isLight = false
        )
    )

    fun getPreset(id: String): GradientPreset {
        return presets.find { it.id == id } ?: presets.first()
    }

    fun getAnimatedPreset(id: String?): AnimatedPreset {
        return animatedPresets.find { it.id == id } ?: animatedPresets.first()
    }
}

data class UserWallpaperState(
    val presetId: String = "preset_pearl_white",
    val customImageUri: String? = null,
    val glassBlurAmount: Float = 0.65f,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val isAnimated: Boolean = false,
    val animatedPresetId: String? = null,
    val activeWallpaperPluginId: String? = null
)

