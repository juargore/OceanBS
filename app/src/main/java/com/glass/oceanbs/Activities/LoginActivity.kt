package com.glass.oceanbs.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.glass.oceanbs.R

class LoginActivity : AppCompatActivity() {

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
            startActivity(Intent(this, MainActivity::class.java))
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
