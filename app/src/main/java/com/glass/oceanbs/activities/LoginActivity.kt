@file:Suppress("SpellCheckingInspection", "DEPRECATION")

package com.glass.oceanbs.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StrictMode
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.widget.*
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.snackbar
import com.glass.oceanbs.R
import com.glass.oceanbs.database.TableUser
import com.glass.oceanbs.models.OWNER
import com.glass.oceanbs.models.User
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class LoginActivity : BaseActivity()  {

    private lateinit var progress : AlertDialog
    private lateinit var etCode: EditText
    private lateinit var etPhone: EditText
    private lateinit var parentLayout: LinearLayout
    private lateinit var btnLogIn: Button
    private lateinit var chckBoxLogin: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initComponents()
        FirebaseApp.initializeApp(this)
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun initComponents() {
        parentLayout = findViewById(R.id.parentLayoutLogin)
        etCode = findViewById(R.id.etCode)
        etPhone = findViewById(R.id.etPhone)
        btnLogIn = findViewById(R.id.btnLogIn)
        chckBoxLogin = findViewById(R.id.chckBoxLogin)

        val builder = AlertDialog.Builder(this, R.style.HalfDialogTheme)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.progress, null)

        builder.setView(dialogView)
        progress = builder.create()

        packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).apply {
            findViewById<TextView>(R.id.txtVersion).text = "versión $versionName"
        }

        setListeners()
    }

    private fun setListeners() {
        btnLogIn.setOnClickListener {
            if (Constants.internetConnected(this)) {
                if (validateFullFields())
                    sendCredentialsToServer()
            } else {
                Constants.showPopUpNoInternet(this)
            }
        }
    }

    private fun sendCredentialsToServer() {
        progress.show()
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val client = OkHttpClient()
        val builder = FormBody.Builder()
        .add("WebService","InicioSesion")
        .add("Codigo", etCode.text.toString().replace(" ",""))
        .add("TelMovil", etPhone.text.toString().replace(" ",""))
        .build()

        val request = Request.Builder().url(Constants.URL_USER).post(builder).build()
        client.newCall(request).enqueue(object : Callback{
            @Suppress("DEPRECATION")
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try{
                        val jsonRes = JSONObject(response.body!!.string())
                        Log.e("LOGIN", jsonRes.toString())

                        if (jsonRes.getInt("Error") > 0)
                            snackbar(applicationContext, parentLayout, jsonRes.getString("Mensaje"), Constants.Types.ERROR)
                        else{

                            // create object User and save it in SQLite DB
                            val user = User(
                                jsonRes.getInt("TipoUsuario"),
                                jsonRes.getString("IdPropietario"),
                                jsonRes.getString("IdColaborador"),
                                jsonRes.getString("Codigo"),
                                jsonRes.getString("Nombre"),
                                jsonRes.getString("ApellidoP"),
                                jsonRes.getString("ApellidoM"))

                            TableUser(this@LoginActivity).insertNewOrExistingUser(user, user.tipoUsuario)
                            Constants.setTipoUsuario(this@LoginActivity, user.tipoUsuario)

                            if (user.tipoUsuario == OWNER)
                                Constants.setUserId(this@LoginActivity, user.idPropietario)
                            else
                                Constants.setUserId(this@LoginActivity, user.idColaborador)

                            if (chckBoxLogin.isChecked)
                                Constants.setKeepLogin(this@LoginActivity, true)

                            // show a welcome message to the user
                            val userName = "${user.nombre} ${user.apellidoP} ${user.apellidoM}"
                            val t = Toast.makeText(this@LoginActivity, "Bienvenido \n$userName", Toast.LENGTH_LONG)
                            val v = t.view?.findViewById<TextView>(android.R.id.message)
                            if (v != null) v.gravity = Gravity.CENTER
                            t.show()

                            Constants.setUserName(this@LoginActivity, userName)

                            sendFirebaseToken()
                            this@LoginActivity.finish()

                            // start new activity main
                            startActivity(Intent(this@LoginActivity, NewMainActivity::class.java))
                        }

                    } catch (e: Error) {
                        snackbar(this@LoginActivity, parentLayout, e.message.toString(), Constants.Types.ERROR)
                    }

                    progress.dismiss()
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    snackbar(applicationContext, parentLayout, e.message.toString(), Constants.Types.ERROR)
                    progress.dismiss()
                }
            }
        })
    }

    private fun validateFullFields(): Boolean {
        return when {
            TextUtils.isEmpty(etCode.text.toString()) -> {
                etCode.error = "El código no puede estar vacío"
                false
            }
            TextUtils.isEmpty(etPhone.text.toString()) -> {
                etPhone.error = "El teléfono no puede estar vacío"
                false
            }
            else -> true
        }
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
            .getCurrentUserById(Constants.getUserId(applicationContext), Constants.getTipoUsuario(applicationContext))

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
                        Log.e("RES token", jsonRes.toString())

                        if (jsonRes.getInt("Error") > 0) {
                            Log.e("",jsonRes.getString("Mensaje"))
                            Constants.updateRefreshToken(applicationContext, true)
                        }
                        else{
                            Constants.updateRefreshToken(applicationContext, false)
                        }

                    } catch (_: Error) { }
                }
            }

            override fun onFailure(call: Call, e: IOException) { }
        })
    }
}
