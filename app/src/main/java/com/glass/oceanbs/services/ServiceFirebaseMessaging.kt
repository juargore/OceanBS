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
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
        //println("AQUI: ${remoteMessage.data}")
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

        val arguments = Bundle()
        arguments.putString("solicitudId", remoteMessage.data["solicitudId"])
        arguments.putString("desarrollo",remoteMessage.data["desarrollo"])
        arguments.putString("persona",remoteMessage.data["persona"])
        arguments.putString("codigoUnidad",remoteMessage.data["codigoUnidad"])
        arguments.putString("t1", remoteMessage.data["t1"])
        arguments.putString("t2", remoteMessage.data["t2"])
        arguments.putString("t3", remoteMessage.data["t3"])
        arguments.putString("t4", remoteMessage.data["t4"])
        arguments.putString("t5", remoteMessage.data["t5"])
        arguments.putString("t6", remoteMessage.data["t6"])
        arguments.putString("t7", remoteMessage.data["t7"])

        arguments.putString("title", remoteMessage.data["title"])
        arguments.putString("body",remoteMessage.notification?.body)
        arguments.putString("image",remoteMessage.data["image"])
        arguments.putBoolean("fromServer", false)

        val notificationIntent: Intent
        if (Constants.getKeepLogin(applicationContext)) {
            notificationIntent = Intent(applicationContext, NotificationActivity::class.java)
            notificationIntent.putExtras(arguments)
            notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        } else {
            notificationIntent = Intent(applicationContext, LoginActivity::class.java)
            notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        notificationBuilder.setContentIntent(pendingIntent)

        // send broadcast to inform UI that must refresh conversation
        if (remoteMessage.data["t7"] == "4") {
            val action = Intent("UPDATE")
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(action)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}
