@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.glass.oceanbs.R

class EditarIncidenciaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_incidencia)

        supportActionBar?.hide()
    }
}
