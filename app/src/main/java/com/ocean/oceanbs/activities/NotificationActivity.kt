package com.ocean.oceanbs.activities

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ocean.oceanbs.Constants
import com.ocean.oceanbs.R
import com.ocean.oceanbs.extensions.Parameter
import com.ocean.oceanbs.extensions.getDataFromServer
import com.squareup.picasso.Picasso

@Suppress("DEPRECATION")
class NotificationActivity : AppCompatActivity() {

    companion object {
        // actionScreen == 1.0 -> MainActivity::class.java
        // actionScreen == 2.0 -> AfterMarketActivity::class.java
        // actionScreen == 2.1 -> AfterMarketActivity::class.java + ConstructionFragment
        // actionScreen == 2.2 -> AfterMarketActivity::class.java + DocumentationFragment
        // actionScreen == 3.0 -> AfterMarketActivity::class.java + MainConversationFragment
        var actionScreen = 0.0f
    }

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
    private var t7 = ""

    private lateinit var layParentNotification: RelativeLayout
    private lateinit var btnSecondary: Button
    private lateinit var btnExitN: Button
    private lateinit var btnEnteradoN: Button
    private lateinit var imgN: ImageView
    private lateinit var txtTitleN: TextView
    private lateinit var txtTag1: TextView
    private lateinit var txtTag2: TextView
    private lateinit var txtTag3: TextView
    private lateinit var txtTag4: TextView
    private lateinit var txtTag5: TextView
    private lateinit var txtTag6: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        supportActionBar?.hide()

        layParentNotification = findViewById(R.id.layParentNotification)
        btnSecondary = findViewById(R.id.btnSecondary)
        btnExitN = findViewById(R.id.btnExitN)
        btnEnteradoN = findViewById(R.id.btnEnteradoN)
        imgN = findViewById(R.id.imgN)
        txtTitleN = findViewById(R.id.txtTitleN)
        txtTag1 = findViewById(R.id.txtTag1)
        txtTag2 = findViewById(R.id.txtTag2)
        txtTag3 = findViewById(R.id.txtTag3)
        txtTag4 = findViewById(R.id.txtTag4)
        txtTag5 = findViewById(R.id.txtTag5)
        txtTag6 = findViewById(R.id.txtTag6)

        intent.extras?.let{
            image = it.getString("image").toString()
            title = it.getString("title").toString()
            solicitudId = it.getString("solicitudId").toString()
            desarrollo = it.getString("desarrollo").toString()
            persona = it.getString("persona").toString()
            codigoUnidad = it.getString("codigoUnidad").toString()
            fromServer = it.getBoolean("fromServer")

            t1 = it.getString("t1").toString()
            t2 = it.getString("t2").toString()
            t3 = it.getString("t3").toString()
            t4 = it.getString("t4").toString()
            t5 = it.getString("t5").toString()
            t6 = it.getString("t6").toString()
            t7 = it.getString("t7").toString()

            btnSecondary.text = when (t7) {
                "1" -> "REVISAR MIS SOLICITUDES"
                "2" -> "REVISAR MIS AVISOS"
                "3" -> "REVISAR MIS AVISOS"
                "4" -> "CONVERSACIÓN"
                else -> "N/A"
            }

            actionScreen = when (t7) {
                "1" -> 1.0f
                "2" -> 2.1f
                "3" -> 2.2f
                "4" -> 3.0f
                else -> 0.0f
            }
        }
        btnExitN.isEnabled = false
        btnExitN.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightGray))
        btnSecondary.isEnabled = false
        btnSecondary.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightGray))
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
            // enable exit and secondary buttons
            sendReponseToServer()
            btnEnteradoN.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightGray))
            btnEnteradoN.isEnabled = false

            btnExitN.isEnabled = true
            btnExitN.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
            btnSecondary.isEnabled = true
            btnSecondary.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        }

        btnExitN.setOnClickListener {
            // close all activities and app
            actionScreen = 0.0f
            finishAffinity()
        }

        btnSecondary.setOnClickListener {
            // open Main or Login Activity
            val remember = Constants.getKeepLogin(this)
            val intent: Intent = if(remember)
                Intent(this@NotificationActivity, NewMainActivity::class.java)
            else
                Intent(this@NotificationActivity, LoginActivity::class.java)

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    private fun sendReponseToServer() {
        getDataFromServer(
            showLoader = false,
            webService = Constants.POST_ANSWER_SATISFACTION,
            url = Constants.URL_ANSWER_SATISFACTION,
            parent = layParentNotification,
            parameters = listOf(
                Parameter(Constants.OWNER_ID, Constants.getUserId(this)),
                Parameter(Constants.RESPONSE, "2")
            )
        ) { }
    }

    @SuppressLint("ObsoleteSdkInt")
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

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { }
}
