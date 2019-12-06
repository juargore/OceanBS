package com.glass.oceanbs.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.glass.oceanbs.R

class RegistroIncidenciaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_incidencia)

        supportActionBar?.hide()
    }
}
