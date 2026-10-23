package com.example.model

import android.graphics.Bitmap
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val imageBitmap: Bitmap? = null,
    val imageUrl: String? = null,
    val modelName: String = "Tilcayo 3.8 Flash",
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val groundingSources: List<String> = emptyList()
)
