package com.example.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import org.json.JSONArray
import org.json.JSONObject

enum class PluginType(val title: String, val description: String) {
    SCRIPT("Веб-скрипт", "Выполняет JavaScript и стилизует веб-страницы"),
    STYLESHEET("CSS стили", "Принудительно меняет стили открываемых сайтов"),
    WALLPAPER("Живые обои", "HTML5 Canvas/WebGL анимации для фона домашнего экрана")
}

@IgnoreExtraProperties
data class Plugin(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val author: String = "Тилкайо Team",
    val authorId: String = "",
    val version: String = "1.0.0",
    val iconName: String = "extension",
    val category: String = "Утилиты",
    val pluginType: String = PluginType.SCRIPT.name,
    val jsCode: String = "",
    val cssCode: String = "",
    val htmlCode: String = "",
    val matchUrlRegex: String = ".*",
    val isEnabled: Boolean = true,
    val isPreinstalled: Boolean = false,
    val downloads: Long = 0L,
    val manifestVersion: Int = 2,
    val permissions: List<String> = emptyList()
) {
    fun appliesTo(url: String): Boolean {
        if (!isEnabled) return false
        if (pluginType == PluginType.WALLPAPER.name) return false
        if (matchUrlRegex.isBlank() || matchUrlRegex == ".*") return true
        return try {
            Regex(matchUrlRegex, RegexOption.IGNORE_CASE).containsMatchIn(url)
        } catch (_: Exception) {
            true
        }
    }

    fun isWallpaperPlugin(): Boolean {
        return pluginType == PluginType.WALLPAPER.name || category == "Живые обои" || htmlCode.isNotBlank()
    }

    fun toJsonString(): String {
        val json = JSONObject()
        json.put("id", id)
        json.put("name", name)
        json.put("description", description)
        json.put("author", author)
        json.put("version", version)
        json.put("iconName", iconName)
        json.put("category", category)
        json.put("pluginType", pluginType)
        json.put("jsCode", jsCode)
        json.put("cssCode", cssCode)
        json.put("htmlCode", htmlCode)
        json.put("matchUrlRegex", matchUrlRegex)
        json.put("manifestVersion", manifestVersion)
        val permArray = JSONArray()
        permissions.forEach { permArray.put(it) }
        json.put("permissions", permArray)
        return json.toString(2)
    }

    companion object {
        fun fromJsonString(jsonStr: String): Plugin? {
            return try {
                val json = JSONObject(jsonStr)
                val perms = mutableListOf<String>()
                json.optJSONArray("permissions")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        perms.add(arr.getString(i))
                    }
                }
                Plugin(
                    id = json.optString("id", "imported_" + System.currentTimeMillis()),
                    name = json.optString("name", "Импортированный плагин"),
                    description = json.optString("description", ""),
                    author = json.optString("author", "Пользователь"),
                    version = json.optString("version", "1.0.0"),
                    iconName = json.optString("iconName", "code"),
                    category = json.optString("category", "Утилиты"),
                    pluginType = json.optString("pluginType", PluginType.SCRIPT.name),
                    jsCode = json.optString("jsCode", ""),
                    cssCode = json.optString("cssCode", ""),
                    htmlCode = json.optString("htmlCode", ""),
                    matchUrlRegex = json.optString("matchUrlRegex", ".*"),
                    isEnabled = true,
                    isPreinstalled = false,
                    manifestVersion = json.optInt("manifestVersion", 2),
                    permissions = perms
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}

