package com.glass.oceanbs.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.glass.oceanbs.R
import okhttp3.*
import java.io.IOException

class LoginActivity : AppCompatActivity()  {

    private lateinit var parentLayoutLogin: LinearLayout
    private lateinit var btnLogIn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()
        initComponents()
    }

    private fun initComponents(){
        parentLayoutLogin = findViewById(R.id.parentLayoutLogin)
        btnLogIn = findViewById(R.id.btnLogIn)

        setListeners()
    }

    private fun setListeners(){
        btnLogIn.setOnClickListener {
            //sendCredentialsToServer()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun sendCredentialsToServer(){
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val client = OkHttpClient()
        val url = "https://elsalto.gob.mx/RH/login"

        val builder = FormBody.Builder()
        .add("codigo","9615")
        .add("password","12345") //krzan
        .build()

        val request = Request.Builder()
            .url(url).post(builder).build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FAIL","${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.e("SUCCESS","${response.body()?.string()}")
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}
