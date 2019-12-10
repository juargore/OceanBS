@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.snackbar
import com.glass.oceanbs.R
import com.glass.oceanbs.database.TableUser
import com.glass.oceanbs.models.User
import okhttp3.*
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.io.IOException
import java.lang.Error

class LoginActivity : AppCompatActivity()  {

    private lateinit var progress : AlertDialog
    private lateinit var etCode: EditText
    private lateinit var etPhone: EditText

    private lateinit var parentLayout: LinearLayout
    private lateinit var btnLogIn: Button
    private lateinit var chckBoxLogin: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()
        initComponents()
    }

    @SuppressLint("InflateParams")
    private fun initComponents(){
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

        setListeners()
    }

    private fun setListeners(){
        btnLogIn.setOnClickListener {

            if(Constants.internetConnected(this)){
                if(validateFullFields())
                    sendCredentialsToServer()
            } else
                Constants.showPopUpNoInternet(this)
        }
    }

    private fun sendCredentialsToServer(){
        progress.show()

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val client = OkHttpClient()
        val builder = FormBody.Builder()
        .add("WebService","InicioSesion")
        .add("Codigo", etCode.text.toString().replace(" ",""))
        .add("TelMovil", etPhone.text.toString().replace(" ",""))
        .build()

        val request = Request.Builder()
            .url(Constants.URL_PARENT).post(builder).build()

        client.newCall(request).enqueue(object : Callback{
            override fun onResponse(call: Call, response: Response) {
                try{
                    val jsonRes = JSONObject(response.body()!!.string())
                    Log.e("--","$jsonRes")

                    if(jsonRes.getInt("Error") > 0)
                        runOnUiThread{ snackbar(applicationContext, parentLayout, jsonRes.getString("Mensaje"))}
                    else{

                        // create object User and save it in SQLite DB
                        val user = User(
                            jsonRes.getString("Id"),
                            jsonRes.getBoolean("Colaborador"),
                            jsonRes.getString("Codigo"),
                            jsonRes.getString("Nombre"),
                            jsonRes.getString("ApellidoP"),
                            jsonRes.getString("ApellidoM")
                        )

                        TableUser(applicationContext).insertNewOrExistingUser(user)
                        Constants.setUserId(applicationContext, user.id)

                        if(chckBoxLogin.isChecked)
                            Constants.setKeepLogin(applicationContext, true)

                        // show a welcome message to the user
                        runOnUiThread { toast("Bienvenido") }
                        this@LoginActivity.finish()

                        // start new activity main
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                    }

                } catch (e: Error){
                    snackbar(applicationContext, parentLayout, e.message.toString())
                }

                progress.dismiss()
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("--","${e.message}")
                snackbar(applicationContext, parentLayout, e.message.toString())
                progress.dismiss()
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

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}
