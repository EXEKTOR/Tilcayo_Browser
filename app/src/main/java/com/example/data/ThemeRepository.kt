package com.example.data

import android.content.Context
import com.example.model.ThemeMode
import com.example.model.UserWallpaperState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "tilcayo_theme_prefs"
private const val KEY_PRESET_ID = "preset_id"
private const val KEY_CUSTOM_URI = "custom_image_uri"
private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_IS_ANIMATED = "is_animated"
private const val KEY_ANIM_PRESET_ID = "anim_preset_id"
private const val KEY_WALLPAPER_PLUGIN_ID = "wallpaper_plugin_id"

class ThemeRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val initialThemeMode: ThemeMode = try {
        val saved = prefs.getString(KEY_THEME_MODE, ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
        ThemeMode.valueOf(saved)
    } catch (_: Exception) {
        ThemeMode.LIGHT
    }

    private val _wallpaperState = MutableStateFlow(
        UserWallpaperState(
            presetId = prefs.getString(KEY_PRESET_ID, "preset_pearl_white") ?: "preset_pearl_white",
            customImageUri = prefs.getString(KEY_CUSTOM_URI, null),
            themeMode = initialThemeMode,
            isAnimated = prefs.getBoolean(KEY_IS_ANIMATED, false),
            animatedPresetId = prefs.getString(KEY_ANIM_PRESET_ID, null),
            activeWallpaperPluginId = prefs.getString(KEY_WALLPAPER_PLUGIN_ID, null)
        )
    )
    val wallpaperState: StateFlow<UserWallpaperState> = _wallpaperState.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        val currentPreset = _wallpaperState.value.presetId
        // Automatically align preset with chosen theme mode if current preset conflicts
        val updatedPreset = when (mode) {
            ThemeMode.LIGHT -> if (currentPreset == "preset_amber" || currentPreset == "preset_obsidian") "preset_pearl_white" else currentPreset
            ThemeMode.DARK -> if (currentPreset == "preset_pearl_white") "preset_amber" else currentPreset
            ThemeMode.SYSTEM -> currentPreset
        }

        _wallpaperState.value = _wallpaperState.value.copy(
            themeMode = mode,
            presetId = updatedPreset
        )
        prefs.edit()
            .putString(KEY_THEME_MODE, mode.name)
            .putString(KEY_PRESET_ID, updatedPreset)
            .apply()
    }

    fun toggleThemeMode() {
        val current = _wallpaperState.value.themeMode
        val nextMode = when (current) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
        setThemeMode(nextMode)
    }

    fun selectPreset(presetId: String) {
        _wallpaperState.value = _wallpaperState.value.copy(
            presetId = presetId,
            customImageUri = null,
            isAnimated = false,
            animatedPresetId = null,
            activeWallpaperPluginId = null
        )
        prefs.edit()
            .putString(KEY_PRESET_ID, presetId)
            .remove(KEY_CUSTOM_URI)
            .putBoolean(KEY_IS_ANIMATED, false)
            .remove(KEY_ANIM_PRESET_ID)
            .remove(KEY_WALLPAPER_PLUGIN_ID)
            .apply()
    }

    fun selectAnimatedPreset(animId: String) {
        _wallpaperState.value = _wallpaperState.value.copy(
            isAnimated = true,
            animatedPresetId = animId,
            activeWallpaperPluginId = null,
            customImageUri = null
        )
        prefs.edit()
            .putBoolean(KEY_IS_ANIMATED, true)
            .putString(KEY_ANIM_PRESET_ID, animId)
            .remove(KEY_WALLPAPER_PLUGIN_ID)
            .remove(KEY_CUSTOM_URI)
            .apply()
    }

    fun setWallpaperPlugin(pluginId: String) {
        _wallpaperState.value = _wallpaperState.value.copy(
            isAnimated = true,
            activeWallpaperPluginId = pluginId,
            animatedPresetId = null,
            customImageUri = null
        )
        prefs.edit()
            .putBoolean(KEY_IS_ANIMATED, true)
            .putString(KEY_WALLPAPER_PLUGIN_ID, pluginId)
            .remove(KEY_ANIM_PRESET_ID)
            .remove(KEY_CUSTOM_URI)
            .apply()
    }

    fun setCustomWallpaper(uriString: String) {
        _wallpaperState.value = _wallpaperState.value.copy(
            customImageUri = uriString,
            isAnimated = false,
            animatedPresetId = null,
            activeWallpaperPluginId = null
        )
        prefs.edit()
            .putString(KEY_CUSTOM_URI, uriString)
            .putBoolean(KEY_IS_ANIMATED, false)
            .remove(KEY_ANIM_PRESET_ID)
            .remove(KEY_WALLPAPER_PLUGIN_ID)
            .apply()
    }

    fun resetToDefault() {
        setThemeMode(ThemeMode.LIGHT)
        selectPreset("preset_pearl_white")
    }
}


