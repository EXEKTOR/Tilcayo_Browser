package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.model.ChatMessage
import com.example.model.TilcayoModel
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "TilcayoAiManager"

private const val TILCAYO_SYSTEM_INSTRUCTION = """
Ты — «Умный Тилкайо» (Smart Tilcayo), главный интеллектуальный маскот и ассистент «Тилкайо браузера».
Ты выглядишь как пухлый, упитанный, очаровательный кот в стильном деловом костюме и круглых очках профессора.
Твой характер: дружелюбный, эрудированный, остроумный, добродушный и уютный. Ты обожаешь помогать пользователю, серфить интернет, переводить статьи, объяснять сложные вещи простыми словами и анализировать веб-страницы и фото.
Иногда можешь добавлять лёгкие кошачьи нотки («Мяу!», «По моим усам видно...», «Уютно разложим всё по полочкам»), но без перебора — ты в первую очередь первоклассный эксперт!
Всегда отвечай грамотно, структурированно, красиво оформляя текст (маркированные списки, выделения) на языке пользователя (в основном русском).
"""

class TilcayoAiManager {

    private fun getModel(modelType: TilcayoModel) = Firebase.ai.generativeModel(
        modelName = modelType.geminiModelId,
        systemInstruction = content {
            text(TILCAYO_SYSTEM_INSTRUCTION)
        }
    )

    fun streamReply(
        prompt: String,
        history: List<ChatMessage>,
        modelType: TilcayoModel,
        imageBitmap: Bitmap? = null,
        useGoogleSearch: Boolean = false
    ): Flow<String> = flow {
        val generativeModel = getModel(modelType)

        val formattedPrompt = if (useGoogleSearch) {
            "[ПОИСК В ИНТЕРНЕТЕ / GOOGLE SEARCH: ВКЛЮЧЁН] Ищи самую актуальную и свежую информацию из глобальной сети, приводи проверенные факты, детали и источники. Запрос пользователя:\n$prompt"
        } else {
            prompt
        }

        try {
            withTimeout(60_000L) {
                if (imageBitmap != null) {
                    val inputContent = content {
                        image(imageBitmap)
                        text(formattedPrompt.ifBlank { "Опиши и проанализируй эту картинку подробно для меня." })
                    }
                    generativeModel.generateContentStream(inputContent).collect { chunk ->
                        val text = chunk.text.orEmpty()
                        if (text.isNotEmpty()) {
                            emit(text)
                        }
                    }
                } else {
                    val chatHistory = history
                        .takeLast(10)
                        .map { msg ->
                            content(if (msg.isUser) "user" else "model") {
                                text(msg.text)
                            }
                        }

                    val chat = generativeModel.startChat(chatHistory)
                    chat.sendMessageStream(formattedPrompt).collect { chunk ->
                        val text = chunk.text.orEmpty()
                        if (text.isNotEmpty()) {
                            emit(text)
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error generating Tilcayo reply with ${modelType.displayName}", e)
            emit("\n[Мяу! Произошла заминка: ${e.localizedMessage ?: "Сбой связи с сервером"}. Попробуй еще раз или переключи модель!]")
        }
    }

    suspend fun summarizePage(
        pageText: String,
        modelType: TilcayoModel = TilcayoModel.TILCAYO_38_FLASH
    ): String {
        return try {
            val generativeModel = getModel(modelType)
            val prompt = """
                Перескажи кратко и емко суть содержимого веб-страницы в 3-5 ключевых тезисах:
                ---
                ${pageText.take(4000)}
            """.trimIndent()

            withTimeout(45_000L) {
                val response = generativeModel.generateContent(prompt)
                response.text ?: "Мяу, не удалось прочитать страницу!"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error summarizing page", e)
            "Мяу! Ошибка при анализе страницы: ${e.localizedMessage}"
        }
    }
}
