package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

private const val PREFS_NAME = "tilcayo_search_history"
private const val KEY_HISTORY = "history_queries"

class SearchHistoryRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _recentQueries = MutableStateFlow<List<String>>(emptyList())
    val recentQueries: StateFlow<List<String>> = _recentQueries.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val raw = prefs.getString(KEY_HISTORY, null)
        if (!raw.isNullOrBlank()) {
            try {
                val array = JSONArray(raw)
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    list.add(array.getString(i))
                }
                _recentQueries.value = list
            } catch (_: Exception) {
                _recentQueries.value = emptyList()
            }
        } else {
            _recentQueries.value = listOf(
                "новости технологий",
                "порода кошек тилкайо",
                "лучшие расширения для браузера"
            )
        }
    }

    private fun saveHistory(list: List<String>) {
        val array = JSONArray()
        list.take(20).forEach { array.put(it) }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    fun addQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || trimmed.startsWith("tilcayo://")) return

        val current = _recentQueries.value.toMutableList()
        current.remove(trimmed)
        current.add(0, trimmed)
        val updated = current.take(15)
        _recentQueries.value = updated
        saveHistory(updated)
    }

    fun removeQuery(query: String) {
        val updated = _recentQueries.value.filterNot { it == query }
        _recentQueries.value = updated
        saveHistory(updated)
    }

    fun clearAll() {
        _recentQueries.value = emptyList()
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
