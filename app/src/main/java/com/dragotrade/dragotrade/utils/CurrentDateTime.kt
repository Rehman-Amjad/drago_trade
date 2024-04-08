package com.dragotrade.dragotrade.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.*
import kotlin.math.abs

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
    fun getDaysDifference(currentDate: Date, tradeDate: Date): Long {
        val difference = abs(currentDate.time - tradeDate.time)
        return difference / (1000 * 60 * 60 * 24)
    }

    data class TimeDifference(
        val days: Long,
        val hours: Long,
        val minutes: Long,
        val seconds: Long
    )

    fun getTimeDifference(currentDate: Date, tradeDate: Date): TimeDifference {
        val difference = abs(currentDate.time - tradeDate.time)
        val days = difference / (1000 * 60 * 60 * 24)
        val hours = difference / (1000 * 60 * 60) % 24
        val minutes = difference / (1000 * 60) % 60
        val seconds = difference / 1000 % 60
        return TimeDifference(days, hours, minutes, seconds)
    }

    fun isTimeInFrame(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Boolean {
        val currentTime = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
        }
        val startTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, endMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return currentTime in startTime..endTime
    }
}