package com.dragotrade.dragotrade.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// class to get current time and date
class CurrentDateTime constructor(context: Context) {
    var context : Context? = context
    fun getTimeWithAmPm(): String? {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }
    fun getCurrentDate(): String? {
        return SimpleDateFormat("dd/LLL/yyyy", Locale.getDefault()).format(Date())
    }
    fun getTimeMiles(): String? {
        return System.currentTimeMillis().toString()
    }
}