@file:Suppress("PrivatePropertyName")

package com.glass.oceanbs.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.glass.oceanbs.R

class SplashScreenActivity : AppCompatActivity() {

    private val DISPLAY_LENGTH = 3000

    override fun onCreate(savedInstanceState: Bundle?) {

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        supportActionBar?.hide()

        Handler().postDelayed({

            val intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
            startActivity(intent)

            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            this.finish()

        }, DISPLAY_LENGTH.toLong())
    }
}
