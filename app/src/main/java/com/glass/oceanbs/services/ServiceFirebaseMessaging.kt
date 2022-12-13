package com.glass.oceanbs.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.glass.oceanbs.Constants
import com.glass.oceanbs.R
import com.glass.oceanbs.activities.LoginActivity
import com.glass.oceanbs.activities.NotificationActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class ServiceFirebaseMessaging : FirebaseMessagingService() {

    @SuppressLint("UnspecifiedImmutableFlag", "ObsoleteSdkInt")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val channelId = "oceanbs_channel_01"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val name = "oceanbs_channel"
            val description = "This is OceanBs Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(channelId, name, importance)
            mChannel.description = description
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.setShowBadge(false)
            notificationManager.createNotificationChannel(mChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle())
            .setSmallIcon(R.drawable.logo_ocean_bs)
            .setAutoCancel(true)

        val notificationIntent: Intent

        if (Constants.getKeepLogin(applicationContext)) {
            notificationIntent = Intent(applicationContext, NotificationActivity::class.java)
            notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            notificationIntent.putExtra("solicitudId", remoteMessage.data["solicitudId"])
            notificationIntent.putExtra("desarrollo",remoteMessage.data["desarrollo"])
            notificationIntent.putExtra("persona",remoteMessage.data["persona"])
            notificationIntent.putExtra("codigoUnidad",remoteMessage.data["codigoUnidad"])
            notificationIntent.putExtra("t1", remoteMessage.data["t1"])
            notificationIntent.putExtra("t2", remoteMessage.data["t2"])
            notificationIntent.putExtra("t3", remoteMessage.data["t3"])
            notificationIntent.putExtra("t4", remoteMessage.data["t4"])
            notificationIntent.putExtra("t5", remoteMessage.data["t5"])
            notificationIntent.putExtra("t6", remoteMessage.data["t6"])

            notificationIntent.putExtra("title", remoteMessage.data["title"])
            notificationIntent.putExtra("body",remoteMessage.notification?.body)
            notificationIntent.putExtra("image",remoteMessage.data["image"])
            notificationIntent.putExtra("fromServer", false)
        } else {
            notificationIntent = Intent(applicationContext, LoginActivity::class.java)
            notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        notificationBuilder.setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}
