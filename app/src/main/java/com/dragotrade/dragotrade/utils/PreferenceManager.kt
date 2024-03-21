package com.dragotrade.dragotrade.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager private constructor(context: Context) {

    private val preference: SharedPreferences =
        context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager {
            return instance ?: synchronized(this) {
                instance ?: PreferenceManager(context).also { instance = it }
            }
        }
    }

    fun putString(key: String, value: String) {
        preference.edit().putString(key, value).apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        preference.edit().putBoolean(key, value).apply()
    }

    fun putInt(key: String, value: Int) {
        preference.edit().putInt(key, value).apply()
    }

    fun putLong(key: String, value: Long) {
        preference.edit().putLong(key, value).apply()
    }
    fun putFloat(key: String, value: Float) {
        preference.edit().putFloat(key, value).apply()
    }

    fun getString(key: String): String {
        return preference.getString(key, "").toString()
    }

    fun getBoolean(key: String): Boolean {
        return preference.getBoolean(key, false)
    }

    fun getInt(key: String): Int {
        return preference.getInt(key, 0)
    }
    fun getLong(key: String): Long {
        return preference.getLong(key, 0)
    }

    fun getFloat(key: String): Float {
        return preference.getFloat(key, 0.0f)
    }

    fun clear() {
        preference.edit().clear().apply()
    }

}