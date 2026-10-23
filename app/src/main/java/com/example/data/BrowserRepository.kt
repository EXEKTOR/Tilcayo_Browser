package com.example.data

import android.content.Context
import android.net.Uri
import com.example.model.Bookmark
import com.example.model.SearchEngine
import com.example.model.TabItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val PREFS_NAME = "tilcayo_browser_prefs"
private const val KEY_SEARCH_ENGINE = "search_engine_id"

class BrowserRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _searchEngine = MutableStateFlow(
        SearchEngine.fromId(prefs.getString(KEY_SEARCH_ENGINE, SearchEngine.GOOGLE.id) ?: SearchEngine.GOOGLE.id)
    )
    val searchEngine: StateFlow<SearchEngine> = _searchEngine.asStateFlow()

    fun setSearchEngine(engine: SearchEngine) {
        _searchEngine.value = engine
        prefs.edit().putString(KEY_SEARCH_ENGINE, engine.id).apply()
    }

    private val initialTab = TabItem(
        title = "Тилкайо Домой",
        url = "tilcayo://home"
    )

    private val _tabs = MutableStateFlow<List<TabItem>>(listOf(initialTab))
    val tabs: StateFlow<List<TabItem>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<Bookmark>>(
        listOf(
            Bookmark(title = "Google", url = "https://www.google.com"),
            Bookmark(title = "Яндекс", url = "https://ya.ru"),
            Bookmark(title = "Wikipedia", url = "https://ru.wikipedia.org"),
            Bookmark(title = "YouTube", url = "https://www.youtube.com"),
            Bookmark(title = "GitHub", url = "https://github.com"),
            Bookmark(title = "Хабр", url = "https://habr.com")
        )
    )
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

    fun getActiveTab(): TabItem {
        return _tabs.value.find { it.id == _activeTabId.value } ?: _tabs.value.firstOrNull() ?: initialTab
    }

    fun openNewTab(url: String = "tilcayo://home", isIncognito: Boolean = false): TabItem {
        val newTab = TabItem(
            url = url,
            title = if (url == "tilcayo://home") "Тилкайо Домой" else "Новая вкладка",
            isIncognito = isIncognito
        )
        _tabs.value = _tabs.value + newTab
        _activeTabId.value = newTab.id
        return newTab
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value
        if (currentList.size <= 1) {
            val resetTab = TabItem(url = "tilcayo://home", title = "Тилкайо Домой")
            _tabs.value = listOf(resetTab)
            _activeTabId.value = resetTab.id
            return
        }

        val closingIndex = currentList.indexOfFirst { it.id == tabId }
        val remaining = currentList.filterNot { it.id == tabId }
        _tabs.value = remaining

        if (_activeTabId.value == tabId) {
            val newIndex = (closingIndex - 1).coerceAtLeast(0)
            _activeTabId.value = remaining[newIndex].id
        }
    }

    fun selectTab(tabId: String) {
        if (_tabs.value.any { it.id == tabId }) {
            _activeTabId.value = tabId
        }
    }

    fun updateActiveTab(transform: (TabItem) -> TabItem) {
        val currentId = _activeTabId.value
        _tabs.value = _tabs.value.map {
            if (it.id == currentId) transform(it) else it
        }
    }

    fun updateTab(tabId: String, transform: (TabItem) -> TabItem) {
        _tabs.value = _tabs.value.map {
            if (it.id == tabId) transform(it) else it
        }
    }

    fun formatSearchOrUrl(input: String, engine: SearchEngine = _searchEngine.value): String {
        val trimmed = input.trim()
        if (trimmed.isBlank() || trimmed == "tilcayo://home") return "tilcayo://home"

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }

        val hasTld = trimmed.contains(".") && !trimmed.contains(" ") && trimmed.length > 3
        if (hasTld) {
            return "https://$trimmed"
        }

        val encoded = URLEncoder.encode(trimmed, StandardCharsets.UTF_8.toString())
        return "${engine.searchUrlPrefix}$encoded"
    }

    fun toggleBookmark(tab: TabItem) {
        val existing = _bookmarks.value.find { it.url == tab.url }
        if (existing != null) {
            _bookmarks.value = _bookmarks.value.filterNot { it.id == existing.id }
        } else {
            val newBookmark = Bookmark(
                title = tab.title.ifBlank { tab.url },
                url = tab.url
            )
            _bookmarks.value = _bookmarks.value + newBookmark
        }
    }

    fun isBookmarked(url: String): Boolean {
        return _bookmarks.value.any { it.url == url }
    }
}
