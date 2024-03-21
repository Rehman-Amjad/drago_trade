package com.dragotrade.dragotrade.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateTimeUtils {

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("d-MMM-yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getRelativeTime(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - timestamp
        return if (elapsedTime < 0) {
            // Timestamp is in the future
            "Future date"
        } else if (elapsedTime < 24 * 60 * 60 * 1000) {
            // Less than a day ago
            "Today"
        } else if (elapsedTime < 2 * 24 * 60 * 60 * 1000) {
            // Between 24 and 48 hours ago
            "Yesterday"
        } else {
            // More than two days ago, format as a date
            return formatDate(timestamp)
        }
    }

    fun getRelativeTimeWithDetails(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - timestamp
        return if (elapsedTime < 0) {
            // Timestamp is in the future
            "Future date"
        } else if (elapsedTime < 60 * 1000) {
            // Less than a minute ago
            "Just now"
        } else if (elapsedTime < 2 * 60 * 1000) {
            // Between 1 and 2 minutes ago
            "A minute ago"
        } else if (elapsedTime < 60 * 60 * 1000) {
            // Between 2 minutes and 1 hour ago
            (elapsedTime / (60 * 1000)).toString() + " minutes ago"
        } else if (elapsedTime < 2 * 60 * 60 * 1000) {
            // Between 1 and 2 hours ago
            "An hour ago"
        } else if (elapsedTime < 24 * 60 * 60 * 1000) {
            // Between 2 hours and 1 day ago
            (elapsedTime / (60 * 60 * 1000)).toString() + " hours ago"
        } else if (elapsedTime < 2 * 24 * 60 * 60 * 1000) {
            // Between 1 and 2 days ago
            "Yesterday"
        } else if (elapsedTime / (24 * 60 * 60 * 1000) < 7) {
            // More than two days ago, format as a date
            (elapsedTime / (24 * 60 * 60 * 1000)).toString() + " days ago"
        } else {
            // More than two days ago, format as a date
            return formatDate(timestamp)
        }
    }
}