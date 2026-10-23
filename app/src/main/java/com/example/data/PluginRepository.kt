package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.Plugin
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "PluginRepository"
private const val PREFS_NAME = "tilcayo_plugins_store"
private const val KEY_INSTALLED_PLUGINS = "installed_plugins_json"

class PluginRepository(private val context: Context) {

    private val firestore: FirebaseFirestore by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (_: Exception) {
            FirebaseFirestore.getInstance()
        }
    }

    private val _installedPlugins = MutableStateFlow<List<Plugin>>(emptyList())
    val installedPlugins: StateFlow<List<Plugin>> = _installedPlugins.asStateFlow()

    private val _storePlugins = MutableStateFlow<List<Plugin>>(emptyList())
    val storePlugins: StateFlow<List<Plugin>> = _storePlugins.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        loadInstalledPlugins()
        loadStoreCatalog()
    }

    private fun loadInstalledPlugins() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_INSTALLED_PLUGINS, null)
        if (json.isNullOrBlank()) {
            _installedPlugins.value = DefaultPlugins.initialPlugins
            saveToPrefs(DefaultPlugins.initialPlugins)
        } else {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<Plugin>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Plugin(
                            id = obj.optString("id"),
                            name = obj.optString("name"),
                            description = obj.optString("description"),
                            author = obj.optString("author", "Пользователь"),
                            authorId = obj.optString("authorId", ""),
                            version = obj.optString("version", "1.0.0"),
                            iconName = obj.optString("iconName", "extension"),
                            category = obj.optString("category", "Утилиты"),
                            jsCode = obj.optString("jsCode", ""),
                            cssCode = obj.optString("cssCode", ""),
                            matchUrlRegex = obj.optString("matchUrlRegex", ".*"),
                            isEnabled = obj.optBoolean("isEnabled", true),
                            isPreinstalled = obj.optBoolean("isPreinstalled", false),
                            downloads = obj.optLong("downloads", 0L)
                        )
                    )
                }
                _installedPlugins.value = list
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse saved plugins", e)
                _installedPlugins.value = DefaultPlugins.initialPlugins
            }
        }
    }

    private fun saveToPrefs(plugins: List<Plugin>) {
        val array = JSONArray()
        plugins.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("description", p.description)
                put("author", p.author)
                put("authorId", p.authorId)
                put("version", p.version)
                put("iconName", p.iconName)
                put("category", p.category)
                put("jsCode", p.jsCode)
                put("cssCode", p.cssCode)
                put("matchUrlRegex", p.matchUrlRegex)
                put("isEnabled", p.isEnabled)
                put("isPreinstalled", p.isPreinstalled)
                put("downloads", p.downloads)
            }
            array.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_INSTALLED_PLUGINS, array.toString())
            .apply()
    }

    fun togglePlugin(pluginId: String, isEnabled: Boolean) {
        val updated = _installedPlugins.value.map {
            if (it.id == pluginId) it.copy(isEnabled = isEnabled) else it
        }
        _installedPlugins.value = updated
        saveToPrefs(updated)
    }

    fun installPlugin(plugin: Plugin) {
        val current = _installedPlugins.value.toMutableList()
        val index = current.indexOfFirst { it.id == plugin.id }
        if (index >= 0) {
            current[index] = plugin.copy(isEnabled = true)
        } else {
            current.add(plugin.copy(isEnabled = true))
        }
        _installedPlugins.value = current
        saveToPrefs(current)
    }

    fun uninstallPlugin(pluginId: String) {
        val updated = _installedPlugins.value.filterNot { it.id == pluginId }
        _installedPlugins.value = updated
        saveToPrefs(updated)
    }

    fun saveOrUpdateCustomPlugin(plugin: Plugin) {
        val current = _installedPlugins.value.toMutableList()
        val index = current.indexOfFirst { it.id == plugin.id }
        if (index >= 0) {
            current[index] = plugin
        } else {
            current.add(0, plugin)
        }
        _installedPlugins.value = current
        saveToPrefs(current)
    }

    fun loadStoreCatalog() {
        scope.launch {
            try {
                val snapshot = firestore.collection("plugins").get().await()
                val firestorePlugins = snapshot.documents.mapNotNull { it.toObject(Plugin::class.java) }
                val merged = (DefaultPlugins.initialPlugins + firestorePlugins)
                    .distinctBy { it.id }
                _storePlugins.value = merged
            } catch (e: Exception) {
                Log.w(TAG, "Using fallback store plugins: ${e.message}")
                _storePlugins.value = DefaultPlugins.initialPlugins
            }
        }
    }

    suspend fun publishPluginToStore(plugin: Plugin): Result<Unit> {
        return try {
            val user = Firebase.auth.currentUser
            val authorId = user?.uid ?: "anonymous"
            val authorName = user?.displayName ?: "Тилкайо Кот"

            val published = plugin.copy(
                author = authorName,
                authorId = authorId,
                downloads = plugin.downloads + 1
            )
            firestore.collection("plugins").document(plugin.id).set(published).await()
            saveOrUpdateCustomPlugin(published)
            loadStoreCatalog()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to publish plugin", e)
            Result.failure(e)
        }
    }
}
