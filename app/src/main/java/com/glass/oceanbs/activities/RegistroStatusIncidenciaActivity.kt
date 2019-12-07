@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.glass.oceanbs.R

class RegistroStatusIncidenciaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_status_incidencia)

        supportActionBar?.hide()

    }
}
