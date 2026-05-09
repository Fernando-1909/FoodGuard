package com.example.foodguard.data

import android.content.Context
import android.content.SharedPreferences

class UserManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveUser(name: String, email: String, password: String) {
        prefs.edit().apply {
            putString("user_name", name)
            putString("user_email", email)
            putString("user_password", password)
            apply()
        }
    }

    fun getUserEmail(): String? = prefs.getString("user_email", null)
    fun getUserPassword(): String? = prefs.getString("user_password", null)
    fun getUserName(): String? = prefs.getString("user_name", null)

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean("is_logged_in", loggedIn).apply()
    }

    fun logout() {
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }
}
