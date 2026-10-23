package com.example.model

data class UserProfile(
    val id: String = "guest_user",
    val displayName: String = "Гость",
    val email: String? = null,
    val avatarUrl: String? = null,
    val isGuest: Boolean = true
)
