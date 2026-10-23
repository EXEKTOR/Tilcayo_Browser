package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DiagnosticsUtil {

    const val APP_VERSION = "0.1B"

    fun generateBugReportInfo(
        context: Context,
        currentEngine: String = "Google",
        themeMode: String = "Светлая",
        activePluginsCount: Int = 0
    ): String {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)

        val totalMemMb = memInfo.totalMem / (1024 * 1024)
        val availMemMb = memInfo.availMem / (1024 * 1024)

        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val densityDpi = displayMetrics.densityDpi

        val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        return """
            ### 📋 Отчет о конфигурации для разработчика (Тилкайо 0.1B)
            - **Дата:** $dateStr
            - **Приложение:** Тилкайо Браузер v$APP_VERSION (BETA)
            - **Устройство:** ${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL} (${Build.DEVICE})
            - **Плата/Процессор:** ${Build.HARDWARE} (${Build.BOARD})
            - **Версия Android:** ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            - **Сборка ОС:** ${Build.DISPLAY}
            - **Экран:** ${screenWidth}x${screenHeight} px (плотность: ${densityDpi} dpi)
            - **ОЗУ:** свободно $availMemMb МБ из $totalMemMb МБ (Low RAM: ${memInfo.lowMemory})
            - **Поисковая система:** $currentEngine
            - **Тема оформления:** $themeMode
            - **Активных плагинов:** $activePluginsCount
            ---
            *Пожалуйста, опишите обнаруженную ошибку и приложите данный отчет при отправке разработчику.*
        """.trimIndent()
    }
}
