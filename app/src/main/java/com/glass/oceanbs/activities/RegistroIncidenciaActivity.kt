@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.R

class RegistroIncidenciaActivity : AppCompatActivity() {

    private lateinit var txtBitacoraStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_incidencia)

        supportActionBar?.hide()

        initComponents()
    }

    private fun initComponents(){
        txtBitacoraStatus = findViewById(R.id.txtBitacoraStatus)

        txtBitacoraStatus.setOnClickListener {
            showPopBitacoraStatus()
        }
    }

    private fun showPopBitacoraStatus(){
        val dialog = Dialog(this, R.style.FullDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.pop_bitacora_status)

        val rvBitacoraStatusIncidencia = dialog.findViewById<RecyclerView>(R.id.rvBitacoraStatusIncidencia)
        rvBitacoraStatusIncidencia.layoutManager = LinearLayoutManager(this)

        //adapter

        dialog.show()
    }
}
