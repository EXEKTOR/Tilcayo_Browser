package com.example.model

import java.util.UUID

data class TabItem(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "about:blank",
    val title: String = "Новая вкладка",
    val faviconUrl: String? = null,
    val isIncognito: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isDesktopMode: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
