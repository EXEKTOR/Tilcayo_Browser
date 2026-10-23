package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

private const val PREFS_NAME = "tilcayo_auth_prefs"
private const val KEY_USER_ID = "auth_user_id"
private const val KEY_DISPLAY_NAME = "auth_display_name"
private const val KEY_EMAIL = "auth_email"
private const val KEY_AVATAR_URL = "auth_avatar_url"
private const val KEY_IS_GUEST = "auth_is_guest"
private const val KEY_ONBOARDING_DONE = "onboarding_done_0_1b"
private const val KEY_BETA_DISCLAIMER_DONE = "beta_disclaimer_done_0_1b"

class AuthRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _user = MutableStateFlow(loadUser())
    val user: StateFlow<UserProfile> = _user.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(
        prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    )
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isBetaDisclaimerDismissed = MutableStateFlow(
        prefs.getBoolean(KEY_BETA_DISCLAIMER_DONE, false)
    )
    val isBetaDisclaimerDismissed: StateFlow<Boolean> = _isBetaDisclaimerDismissed.asStateFlow()

    private fun loadUser(): UserProfile {
        val isGuest = prefs.getBoolean(KEY_IS_GUEST, true)
        if (isGuest) {
            return UserProfile(
                id = "guest_user",
                displayName = "Гость",
                email = null,
                avatarUrl = null,
                isGuest = true
            )
        }
        val id = prefs.getString(KEY_USER_ID, "google_user") ?: "google_user"
        val name = prefs.getString(KEY_DISPLAY_NAME, "Пользователь Google") ?: "Пользователь Google"
        val email = prefs.getString(KEY_EMAIL, "user@gmail.com")
        val avatar = prefs.getString(KEY_AVATAR_URL, null)
        return UserProfile(
            id = id,
            displayName = name,
            email = email,
            avatarUrl = avatar,
            isGuest = false
        )
    }

    fun signInWithGoogle(name: String, email: String, avatarUrl: String? = null) {
        val id = UUID.randomUUID().toString()
        prefs.edit()
            .putBoolean(KEY_IS_GUEST, false)
            .putString(KEY_USER_ID, id)
            .putString(KEY_DISPLAY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_AVATAR_URL, avatarUrl)
            .apply()

        _user.value = UserProfile(
            id = id,
            displayName = name,
            email = email,
            avatarUrl = avatarUrl,
            isGuest = false
        )
    }

    fun signInAsGuest() {
        prefs.edit()
            .putBoolean(KEY_IS_GUEST, true)
            .remove(KEY_USER_ID)
            .remove(KEY_DISPLAY_NAME)
            .remove(KEY_EMAIL)
            .remove(KEY_AVATAR_URL)
            .apply()

        _user.value = UserProfile(
            id = "guest_user",
            displayName = "Гость",
            email = null,
            avatarUrl = null,
            isGuest = true
        )
    }

    fun signOut() {
        signInAsGuest()
    }

    fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
        _isOnboardingCompleted.value = true
    }

    fun resetOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, false).apply()
        _isOnboardingCompleted.value = false
    }

    fun dismissBetaDisclaimer() {
        prefs.edit().putBoolean(KEY_BETA_DISCLAIMER_DONE, true).apply()
        _isBetaDisclaimerDismissed.value = true
    }
}
