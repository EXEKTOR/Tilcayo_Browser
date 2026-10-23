package com.example.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.UUID

@IgnoreExtraProperties
data class Bookmark(
    @DocumentId val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "",
    val url: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
