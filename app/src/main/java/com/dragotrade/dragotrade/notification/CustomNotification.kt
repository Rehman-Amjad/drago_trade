package com.dragotrade.dragotrade.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomNotification constructor(private var context: Context) {

    private lateinit var firestore : FirebaseFirestore

    private val CHANNEL_ID = "My Channel"
    private val NOTIFICATION_ID = 100

    fun ShowNotification(rightNotificationIcon: Int, text: String?) {
        val drawable =
            ResourcesCompat.getDrawable(context.getResources(), rightNotificationIcon, null)
        val bitmapDrawable = (drawable as BitmapDrawable?)!!
        val bitmapIcon = bitmapDrawable.bitmap
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification: Notification

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("My Notification")
            .setContentText("This is a notification message.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel Description"
            }
            val notificationManager = ContextCompat.getSystemService(
                context,NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun saveNotification(userUID: String,title: String,message: String){
        val currentTimestamp = System.currentTimeMillis()
        firestore = FirebaseFirestore.getInstance()
        val map = hashMapOf<String,Any>(
            Constants.KEY_NOTIFICATION_TITLE to title,
            Constants.KEY_NOTIFICATION_MESSAGE to message,
            Constants.KEY_TIMESTAMP to currentTimestamp.toString(),
            Constants.KEY_DATE to getCurrentDate().toString(),
            Constants.KEY_TIME to getTimeWithAmPm().toString(),
        )
        firestore.collection(Constants.COLLECTION_USER).document(userUID)
            .collection("notification")
            .document()
            .set(map)
    }

    private fun getTimeWithAmPm(): String? {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }
    private fun getCurrentDate(): String? {
        return SimpleDateFormat("dd/LLL/yyyy", Locale.getDefault()).format(Date())
    }

}