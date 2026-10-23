package com.example.util

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

private const val TAG = "TilcayoTtsManager"

class TilcayoTtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _speakingMessageId = MutableStateFlow<String?>(null)
    val speakingMessageId: StateFlow<String?> = _speakingMessageId.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val tts = textToSpeech ?: return
            val result = tts.setLanguage(Locale("ru", "RU"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default system locale
                tts.setLanguage(Locale.getDefault())
            }
            tts.setPitch(1.05f)
            tts.setSpeechRate(1.0f)

            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _speakingMessageId.value = utteranceId
                }

                override fun onDone(utteranceId: String?) {
                    if (_speakingMessageId.value == utteranceId) {
                        _speakingMessageId.value = null
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (_speakingMessageId.value == utteranceId) {
                        _speakingMessageId.value = null
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.w(TAG, "TTS playback error $errorCode for $utteranceId")
                    if (_speakingMessageId.value == utteranceId) {
                        _speakingMessageId.value = null
                    }
                }
            })

            isInitialized = true
            Log.d(TAG, "Google/System TTS successfully initialized")
        } else {
            Log.e(TAG, "Failed to initialize TTS engine with status: $status")
            isInitialized = false
        }
    }

    fun speak(messageId: String, rawText: String) {
        val tts = textToSpeech ?: return
        if (!isInitialized) {
            Log.w(TAG, "TTS not yet initialized")
            return
        }

        // If currently speaking this same message, stop playback
        if (_speakingMessageId.value == messageId) {
            stop()
            return
        }

        val speechText = cleanMarkdownForSpeech(rawText)
        if (speechText.isBlank()) return

        stop()
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, messageId)
        _speakingMessageId.value = messageId
        tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, params, messageId)
    }

    fun stop() {
        textToSpeech?.stop()
        _speakingMessageId.value = null
    }

    fun shutdown() {
        try {
            stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }

    companion object {
        fun cleanMarkdownForSpeech(markdown: String): String {
            return markdown
                // Remove code blocks
                .replace(Regex("```[a-zA-Z]*\\n([\\s\\S]*?)```"), " блок кода ")
                // Remove inline code
                .replace(Regex("`([^`]+)`"), "$1")
                // Remove bold and italic markers
                .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
                .replace(Regex("\\*([^*]+)\\*"), "$1")
                .replace(Regex("__(.+?)__"), "$1")
                .replace(Regex("_(.+?)_"), "$1")
                // Remove headers
                .replace(Regex("(?m)^#{1,6}\\s*"), "")
                // Remove blockquote markers
                .replace(Regex("(?m)^>\\s*"), "")
                // Remove bullet list asterisks/dashes
                .replace(Regex("(?m)^[\\*\\-\\+]\\s+"), "")
                // Remove Markdown URLs [text](url)
                .replace(Regex("\\[([^\\]]+)\\]\\([^\\)]+\\)"), "$1")
                // Remove bare URLs
                .replace(Regex("https?://\\S+"), "ссылка")
                .trim()
        }
    }
}
