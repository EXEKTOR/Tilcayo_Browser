# План реализации: Улучшение ИИ-режима, поиск в сети, голосовая озвучка и фикс краша WebView

## 1. Анализ требований и обнаруженных ошибок
1. **Отображение модели (Баг "выбрал 3.5, а пишет 3.8")**:
   - В `ChatMessage` значение `modelName` по умолчанию было захардкожено как `"Tilcayo 3.8 Flash"`, а в `MainActivity.kt` при создании плейсхолдера ответа не передавалось имя текущей выбранной модели (`selectedModel.displayName`). Исправляем передачу `selectedModel.displayName`.
2. **Типографика и дизайн режима ИИ**:
   - Обычный текст сообщений не поддерживал форматирование Markdown (заголовки, жирный текст `**...**`, списки, цитаты, встроенный код `` `code` `` и блоки кода ``` ``` с подсветкой и кнопкой копирования). Добавим компонент форматирования `MarkdownText` со стилизованными плашками для кода и цитат в стиле Liquid Glass.
3. **Кнопка поиска в интернете для ИИ**:
   - Добавим переключатель «🌐 Поиск в Google» над строкой ввода в `SmartTilcayoDialog`.
   - Если включен: в системный промпт / запрос добавляется инструкция актуализации через Google Search и поиск по текущим данным, отображается бейдж «С поиском в Google». Если выключен: ответ формулируется строго из базы знаний без внешнего поиска.
4. **Голосовая озвучка через Google TTS**:
   - Реализуем менеджер `TilcayoTtsManager` на базе системного `android.speech.tts.TextToSpeech`.
   - Добавим кнопку динамика 🔊 у каждого сообщения ИИ: нажатие воспроизводит озвучку на русском/английском языке, повторное нажатие останавливает чтение.
5. **Фикс ошибки падения рендерера Chromium (`Renderer process crash detected code -1`)**:
   - Ошибка возникает в WebView, когда рендерер Chromium аварийно завершается из-за переполнения памяти или несовместимости с EGL эмулятора.
   - В `BrowserWebView.kt` переопределим `onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean` и вернем `true`, что предотвращает краш приложения, корректно перезагружает страницу и показывает пользователю мягкое уведомление о восстановлении.

---

## 2. Предлагаемые изменения по файлам

### `app/src/main/java/com/example/ui/components/SmartTilcayoDialog.kt`
- Внедрение парсера Markdown и красивого рендеринга текста (`MarkdownText`):
  - Жирный шрифт, курсив, заголовки `##`.
  - Блоки кода со шрифтом Monospace, темной плашкой и кнопкой «Копировать».
  - Цитаты и списки.
- Добавление переключателя «Поиск в Google» над полем ввода с анимацией и бейджем.
- Добавление кнопки динамика 🔊 / ⏹️ у сообщений ассистента для озвучки через Google TTS.
- Передача `isWebSearchEnabled` в коллбэк отправки сообщения.

### `app/src/main/java/com/example/util/TilcayoTtsManager.kt`
- Новый класс-синглтон для работы с Google TextToSpeech:
  - Инициализация языка (русский `Locale("ru", "RU")`, авто-переключение на системный).
  - Методы `speak(text: String)`, `stop()`, `isSpeaking(): Boolean`.
  - Корректное освобождение ресурсов при закрытии диалога.

### `app/src/main/java/com/example/MainActivity.kt`
- Исправление бага модели: при создании сообщения ассистента явно передавать `modelName = selectedModel.displayName`.
- Поддержка параметра `webSearchEnabled` в `sendAiMessage(promptText, imageBitmap, webSearchEnabled)`.
- Инициализация и проброс `TilcayoTtsManager` в `SmartTilcayoDialog`.

### `app/src/main/java/com/example/ai/TilcayoAiManager.kt`
- Если включен `webSearchEnabled`, автоматически активировать Google Search Grounding или дополнять системный контекст для выполнения поиска актуальной информации в Google.

### `app/src/main/java/com/example/ui/components/BrowserWebView.kt`
- Добавление защиты от краша:
  ```kotlin
  override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
      // Предотвращает вылет всего приложения при падении Chromium
      onLoadingChanged(false)
      hasError = true
      errorMessage = "Процесс вкладки был перезапущен"
      return true
  }
  ```

---

## 3. План верификации
1. `compile_applet`: проверка сборки Kotlin кода.
2. `gradle :app:testDebugUnitTest`: прогон модульных тестов Robolectric.
3. Проверка интерфейса:
   - Переключение модели на `Tilcayo 3.5 Flash-Lite`: проверка, что в сообщении пишется именно 3.5.
   - Переключение кнопки «Поиск в Google».
   - Нажатие на кнопку динамика для озвучки ответа.
   - Проверка красивого отображения Markdown и блоков кода.
