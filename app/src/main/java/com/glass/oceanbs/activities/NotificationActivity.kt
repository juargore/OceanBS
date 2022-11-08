package com.glass.oceanbs.activities

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.glass.oceanbs.Constants
import com.glass.oceanbs.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_notification.*

@Suppress("DEPRECATION")
class NotificationActivity : AppCompatActivity() {

    private var title = ""
    private var image = ""

    private var solicitudId = ""
    private var desarrollo = ""
    private var persona = ""
    private var codigoUnidad = ""
    private var fromServer = false

    private var t1 = ""
    private var t2 = ""
    private var t3 = ""
    private var t4 = ""
    private var t5 = ""
    private var t6 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        supportActionBar?.hide()

        val args = intent.extras

        try {
            image = args!!.getString("image").toString()
            title = args.getString("title").toString()
            solicitudId = args.getString("solicitudId").toString()
            desarrollo = args.getString("desarrollo").toString()
            persona = args.getString("persona").toString()
            codigoUnidad = args.getString("codigoUnidad").toString()
            fromServer = args.getBoolean("fromServer")

            t1 = args.getString("t1").toString()
            t2 = args.getString("t2").toString()
            t3 = args.getString("t3").toString()
            t4 = args.getString("t4").toString()
            t5 = args.getString("t5").toString()
            t6 = args.getString("t6").toString()

        } catch (e: Exception){
            e.printStackTrace()
        }

        setListeners()
        setInformation()

        createNotificationCopy(applicationContext)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setInformation(){
        txtTitleN.text = title
        Picasso.get().load(image).error(resources.getDrawable(R.drawable.logo_ocean_bs)).fit().into(imgN)

        txtTag1.text = t1
        txtTag2.text = t2
        txtTag3.text = t3
        txtTag4.text = t4
        txtTag5.text = t5
        txtTag6.text = t6
    }

    private fun setListeners(){
        btnEnteradoN.setOnClickListener {
            // close all activities and app
            finishAffinity()
        }

        btnRevSolN.setOnClickListener {

            //Open Main or Login Activity
            val remember = Constants.getKeepLogin(this)
            val intent: Intent = if(remember)
                Intent(this@NotificationActivity, MainActivity::class.java)
            else
                Intent(this@NotificationActivity, LoginActivity::class.java)

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            this.finish()
        }
    }

    private fun createNotificationCopy(context: Context) {
        val channelId = "oceanbs_channel_01"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
            .setDefaults(Notification.DEFAULT_LIGHTS and Notification.DEFAULT_SOUND)
            .setContentTitle(title)
            .setContentText("$t3 $t4")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle())
            .setSmallIcon(R.drawable.logo_ocean_bs)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { }
}
