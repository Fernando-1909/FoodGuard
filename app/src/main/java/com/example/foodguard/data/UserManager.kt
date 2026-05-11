package com.example.foodguard.data

import android.content.Context
import android.content.SharedPreferences

class UserManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun setLoggedInUser(email: String) {
        prefs.edit().apply {
            putString("current_user_email", email)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun getUserEmail(): String? = prefs.getString("current_user_email", null)

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun logout() {
        prefs.edit().apply {
            putBoolean("is_logged_in", false)
            remove("current_user_email")
            apply()
        }
    }
}
