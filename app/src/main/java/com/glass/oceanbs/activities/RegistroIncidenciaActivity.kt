@file:Suppress("SpellCheckingInspection", "DEPRECATION")

package com.glass.oceanbs.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.R
import com.glass.oceanbs.adapters.BitacoraStatusAdapter
import org.jetbrains.anko.alert
import org.jetbrains.anko.textColor

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

        val adapter = BitacoraStatusAdapter(this, object : BitacoraStatusAdapter.InterfaceOnClick{
            override fun onItemClick(pos: Int) {
                val intent = Intent(applicationContext, RegistroStatusIncidenciaActivity::class.java)
                startActivity(intent)
            }
        }, object : BitacoraStatusAdapter.InterfaceOnLongClick{
            override fun onItemLongClick(pos: Int) {
                showDeleteDialog()
            }
        })

        rvBitacoraStatusIncidencia.adapter = adapter

        dialog.show()
    }

    private fun showDeleteDialog(){
        alert(resources.getString(R.string.msg_confirm_deletion),
            "Eliminar Status de Incidencia")
        {
            positiveButton(resources.getString(R.string.accept)) {

            }
            negativeButton(resources.getString(R.string.cancel)){}
        }.show().apply {
            getButton(AlertDialog.BUTTON_POSITIVE)?.let { it.textColor = resources.getColor(R.color.colorBlack) }
            getButton(AlertDialog.BUTTON_NEGATIVE)?.let { it.textColor = resources.getColor(R.color.colorAccent) }
        }
    }
}
