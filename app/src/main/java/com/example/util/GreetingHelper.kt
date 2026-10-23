package com.example.util

import java.util.Calendar

object GreetingHelper {

    fun getDynamicGreeting(
        userName: String,
        calendar: Calendar = Calendar.getInstance()
    ): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val name = userName.ifBlank { "Гость" }

        val variants = when (hour) {
            in 23..24, in 0..4 -> listOf(
                "Ночной веб-серфинг, да, $name? 🌙",
                "Полуночный исследователь $name, не забудь выспаться!",
                "Тилкайо тоже не спит — коты активны ночью 🐾",
                "Тихая ночь, звёзды и уютный интернет для $name ✨",
                "Пока город спит, $name открывает самое интересное 🦉"
            )
            in 5..11 -> listOf(
                "Доброе утро, $name! ☕",
                "Утренний кофе и свежие новости для $name 🐾",
                "Пора покорять сеть, $name! Усы уже начесаны.",
                "Бодрого утра, $name! Что нового в интернете?",
                "Солнце встало, и Тилкайо готов к поискам, $name! ☀️"
            )
            in 12..17 -> listOf(
                "Добрый день, $name! ☀️",
                "Что ищем сегодня, $name?",
                "Тилкайо готов к продуктивному веб-серфингу, $name!",
                "В самом разгаре дня! Найдем всё нужное, $name 🐾",
                "Отличный день для новых открытий, $name! 🚀"
            )
            else -> listOf( // 18..22
                "Уютного вечера, $name! 🌆",
                "Время отдохнуть и почитать статьи, $name.",
                "Тилкайо замурчал от вечернего серфинга, $name! 🐾",
                "Добрый вечер, $name! Самое время для любимых сайтов.",
                "Приятного отдыха в сети, $name! 🛋️"
            )
        }

        // Deterministically pick variant based on minutes for smooth session consistency
        val index = (minute / 12) % variants.size
        return variants[index]
    }

    fun getTimePeriodTag(calendar: Calendar = Calendar.getInstance()): String {
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 23..24, in 0..4 -> "Ночь"
            in 5..11 -> "Утро"
            in 12..17 -> "День"
            else -> "Вечер"
        }
    }
}
