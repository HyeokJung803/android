package com.studyapp.android.util

import android.content.Context
import android.content.SharedPreferences

object PreferenceManager {
    private const val PREF_NAME = "studyapp_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_NICKNAME = "nickname"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(context: Context, userId: Long, nickname: String) {
        getPreferences(context).edit().apply {
            putLong(KEY_USER_ID, userId)
            putString(KEY_NICKNAME, nickname)
            apply()
        }
    }

    fun getUserId(context: Context): Long {
        return getPreferences(context).getLong(KEY_USER_ID, 0L)
    }

    fun getNickname(context: Context): String {
        return getPreferences(context).getString(KEY_NICKNAME, "") ?: ""
    }

    fun isLoggedIn(context: Context): Boolean {
        return getUserId(context) > 0L
    }

    fun clearUser(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}