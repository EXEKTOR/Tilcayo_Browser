package com.example.model

enum class TilcayoModel(
    val displayName: String,
    val geminiModelId: String,
    val description: String,
    val badge: String
) {
    TILCAYO_35_FLASH_LITE(
        displayName = "Tilcayo 3.5 Flash-Lite",
        geminiModelId = "gemini-3.5-flash-lite",
        description = "Сверхбыстрый и лёгкий кот. Мгновенные ответы и базовые задачи.",
        badge = "Быстрый"
    ),
    TILCAYO_38_FLASH(
        displayName = "Tilcayo 3.8 Flash",
        geminiModelId = "gemini-3.8-flash",
        description = "Универсальный мудрый кот с поиском в Google Search и острым чутьем.",
        badge = "Рекомендуемый"
    ),
    TILCAYO_4_ARGON(
        displayName = "Tilcayo 4 Argon",
        geminiModelId = "gemini-4-argon",
        description = "Глубокий аналитический ум. Сложные рассуждения, код и анализ изображений.",
        badge = "Мощный"
    );

    companion object {
        val DEFAULT = TILCAYO_38_FLASH
    }
}
