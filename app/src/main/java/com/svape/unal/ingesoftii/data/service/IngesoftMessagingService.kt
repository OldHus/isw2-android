package com.svape.unal.ingesoftii.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.svape.unal.ingesoftii.MainActivity
import com.svape.unal.ingesoftii.R

private const val CHANNEL_ID = "ingesoft_general"
private const val TAG = "IngesoftFCM"

class IngesoftMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔑 Nuevo token FCM generado: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(
            TAG,
            "📩 onMessageReceived disparado. From=${message.from} data=${message.data} notification=${message.notification?.title}"
        )

        val title = message.notification?.title ?: return
        val body = message.notification?.body ?: ""

        val manager = getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Avisos del curso",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        val notificationId = System.currentTimeMillis().toInt()
        val type = message.data["type"]

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("type", type)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .build()

        manager.notify(notificationId, notification)
        Log.d(TAG, "✅ Notificación mostrada con manager.notify()")
    }
}