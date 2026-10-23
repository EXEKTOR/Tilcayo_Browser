package com.example.model

import com.example.R

enum class SearchEngine(
    val id: String,
    val displayName: String,
    val iconRes: Int,
    val hintText: String,
    val searchUrlPrefix: String,
    val visualSearchUrl: String
) {
    GOOGLE(
        id = "google",
        displayName = "Google",
        iconRes = R.drawable.ic_engine_google,
        hintText = "Поиск в Google или адрес",
        searchUrlPrefix = "https://www.google.com/search?q=",
        visualSearchUrl = "https://lens.google.com"
    ),
    YANDEX(
        id = "yandex",
        displayName = "Яндекс",
        iconRes = R.drawable.ic_engine_yandex,
        hintText = "Поиск в Яндексе или адрес",
        searchUrlPrefix = "https://ya.ru/search/?text=",
        visualSearchUrl = "https://yandex.ru/images/touch/search?rpt=imageview"
    );

    companion object {
        fun fromId(id: String): SearchEngine {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: GOOGLE
        }
    }
}
