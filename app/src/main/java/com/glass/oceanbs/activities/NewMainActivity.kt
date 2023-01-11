package com.glass.oceanbs.activities

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.CAPTION
import com.glass.oceanbs.Constants.COLOR
import com.glass.oceanbs.Constants.CURRENT_DATE
import com.glass.oceanbs.Constants.DATA
import com.glass.oceanbs.Constants.GET_CAROUSEL
import com.glass.oceanbs.Constants.GET_MAIN_ITEMS_HOME
import com.glass.oceanbs.Constants.LINK
import com.glass.oceanbs.Constants.LINK_TYPE
import com.glass.oceanbs.Constants.MEMBER_SINCE
import com.glass.oceanbs.Constants.MESSAGE_DISABLED
import com.glass.oceanbs.Constants.OPTIONS
import com.glass.oceanbs.Constants.OWNER_ID
import com.glass.oceanbs.Constants.OWNER_NAME
import com.glass.oceanbs.Constants.PHOTO
import com.glass.oceanbs.Constants.PHOTOS
import com.glass.oceanbs.Constants.TITLE
import com.glass.oceanbs.Constants.URL_IMAGES_CAROUSEL
import com.glass.oceanbs.Constants.URL_MAIN_ITEMS_HOME
import com.glass.oceanbs.Constants.WELCOME_CAPTION
import com.glass.oceanbs.Constants.getUserId
import com.glass.oceanbs.R
import com.glass.oceanbs.adapters.NewHomeItemAdapter
import com.glass.oceanbs.database.TableUser
import com.glass.oceanbs.extensions.Parameter
import com.glass.oceanbs.extensions.getDataFromServer
import com.glass.oceanbs.extensions.showExitDialog
import com.glass.oceanbs.models.ItemNewHome
import com.glass.oceanbs.models.OWNER
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.activity_new_main.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class NewMainActivity: BaseActivity() {

    companion object {
        var hasPendingNotifications = false
    }
    private val photosList: ArrayList<String> = ArrayList()
    private var imageListener = ImageListener { position, imageView ->
        Picasso.get().load(photosList[position]).fit().into(imageView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_main)
        getTopImagesFromServer()
        fabExit.setOnClickListener { showExitDialog() }
    }

    private fun setUpRecycler(items: List<ItemNewHome>) {
        with(NewHomeItemAdapter(items)) {
            rvMain.adapter = this
            onItemClicked = { intent, url ->
                url?.let {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                }
                intent?.let {
                    if (hasPendingNotifications) {
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancelAll()
                    }
                    it.putExtra(PHOTOS, photosList)
                    startActivity(it)
                    overridePendingTransition(0, 0)
                }
            }
        }
    }

    private fun getTopImagesFromServer() {
        getDataFromServer(
            webService = GET_CAROUSEL,
            url = URL_IMAGES_CAROUSEL,
            parent = layMain
        ) { jsonRes ->
            val arr = jsonRes.getJSONArray(DATA)
            for (i in 0 until arr.length()) {
                val j = arr.getJSONObject(i)
                photosList.add(j.getString(PHOTO))
            }
            runOnUiThread {
                carouselView.setImageListener(imageListener)
                carouselView.pageCount = photosList.size

                // actionScreen == 1.0 -> MainActivity::class.java
                // actionScreen == 2.0 -> AfterMarketActivity::class.java
                // actionScreen == 2.1 -> AfterMarketActivity::class.java + ConstructionFragment
                // actionScreen == 2.2 -> AfterMarketActivity::class.java + DocumentationFragment
                // actionScreen == 3.0 -> AfterMarketActivity::class.java + MainConversationFragment
                if (NotificationActivity.actionScreen == 1.0f) {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        putExtra(PHOTOS, photosList)
                    }); overridePendingTransition(0,0)
                    NotificationActivity.actionScreen = 0.0f
                } else if (NotificationActivity.actionScreen >= 2.0f) {
                    startActivity(Intent(this, AftermarketActivity::class.java).apply {
                        putExtra(PHOTOS, photosList)
                    }); overridePendingTransition(0,0)
                }
            }
        }
    }

    private fun getMainListFromServer() {
        getDataFromServer(
            webService = GET_MAIN_ITEMS_HOME,
            url = URL_MAIN_ITEMS_HOME,
            parent = layMain,
            parameters = listOf(
                Parameter(
                    key = OWNER_ID,
                    value = getUserId(this)
                )
            )
        ) { jsonRes ->
            with (jsonRes) {
                val mList = mutableListOf<ItemNewHome>()
                val strDate = getString(CURRENT_DATE)
                val strWelcome = getString(WELCOME_CAPTION)
                val strMember = getString(MEMBER_SINCE)
                val strOwner  = getString(OWNER_NAME)
                val options = getJSONArray(OPTIONS)

                for (i in 0 until options.length()) {
                    val j = options.getJSONObject(i)
                    val linkType = j.getInt(LINK_TYPE)
                    val msgDisabled = if (j.has(MESSAGE_DISABLED)) {
                        j.getString(MESSAGE_DISABLED)
                    } else { "" }
                    mList.add(
                        ItemNewHome(
                            title = j.getString(TITLE),
                            subtitle = j.getString(CAPTION),
                            hexColor = j.getString(COLOR),
                            openScreen = linkType == 1,
                            url = j.getString(LINK),
                            enabled = j.getInt("Habilitado") == 1,
                            messageDisabled = msgDisabled
                        )
                    )
                }
                setupViews(strWelcome, strDate, strMember, strOwner, mList)
            }
        }
    }

    private fun setupViews(
        strWelcome: String,
        strDate: String,
        strMember: String,
        strOwner: String,
        items: List<ItemNewHome>
    ) {
        runOnUiThread {
            txtWelcome.text = strWelcome
            txtDate.text = strDate
            if (Constants.getTipoUsuario(this) == OWNER) {
                txtMemberSince.text = strMember
                txtUserName.text = strOwner
            } else {
                val cUser = TableUser(applicationContext).getCurrentUserById(
                    getUserId(applicationContext),
                    Constants.getTipoUsuario(applicationContext)
                )
                val fullName = "${cUser.nombre} ${cUser.apellidoP} ${cUser.apellidoM}"
                txtUserName.text = fullName.replace(".", "").trim()
                txtMemberSince.text = getString(R.string.crecento_partner)
            }
            setUpRecycler(items)
        }
    }

    @Suppress("unused")
    private fun getTestlList() = listOf(
        ItemNewHome(
            title = "SEGUIMIENTO DE SU PROPIEDAD",
            subtitle = "Conozca los avances actuales...",
            hexColor = "#FFB264",
            openScreen = true,
            url = null,
            enabled = true
        )
    )

    override fun onResume() {
        super.onResume()
        hasPendingNotifications = false
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notifications = mNotificationManager.activeNotifications
        notifications.forEach {
            if (it.notification.channelId == "fcm_fallback_notification_channel") {
                hasPendingNotifications = true
                return@forEach
            }
        }
        getMainListFromServer()
        sendFirebaseToken()
    }

    private fun sendFirebaseToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null && !TextUtils.isEmpty(task.result)) {
                        val token: String = task.result!!
                        storeFirebaseTokenOnServer(token)
                    }
                }
            }
    }

    private fun storeFirebaseTokenOnServer(gcmToken: String) {
        val tipoUsuario = Constants.getTipoUsuario(applicationContext)
        val cUser = TableUser(applicationContext)
            .getCurrentUserById(
                getUserId(applicationContext),
                Constants.getTipoUsuario(applicationContext)
            )

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","GuardaToken")
            .add("TipoUsuario",tipoUsuario.toString())
            .add("IdPropietario",cUser.idPropietario)
            .add("IdColaborador",cUser.idColaborador)
            .add("Token",gcmToken)
            .build()

        val request = Request.Builder().url(Constants.URL_USER).post(builder).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body!!.string())
                        println("Token: $jsonRes")
                        if (jsonRes.getInt("Error") > 0) {
                            Log.e("Error Token", jsonRes.getString("Mensaje"))
                            Constants.updateRefreshToken(applicationContext, true)
                        } else {
                            Constants.updateRefreshToken(applicationContext, false)
                        }
                    } catch (_: Error) { }
                }
            }
            override fun onFailure(call: Call, e: IOException) { }
        })
    }
}
