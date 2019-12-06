@file:Suppress("SpellCheckingInspection", "DEPRECATION")

package com.glass.oceanbs.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.R
import com.glass.oceanbs.adapters.IncidenciaAdapter
import org.jetbrains.anko.alert
import org.jetbrains.anko.textColor

class IncidenciasActivity : AppCompatActivity() {

    private lateinit var rvIncidencias: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incidencias)

        supportActionBar?.hide()

        rvIncidencias = findViewById(R.id.rvIncidencias)

        rvIncidencias.layoutManager = LinearLayoutManager(this)
        val adapter = IncidenciaAdapter(this, object : IncidenciaAdapter.InterfaceOnClick{
            override fun onItemClick(pos: Int) {
                val intent = Intent(applicationContext, RegistroIncidenciaActivity::class.java)
                startActivity(intent)
            }
        }, object : IncidenciaAdapter.InterfaceOnLongClick{
            override fun onItemLongClick(pos: Int) {
                showDeleteDialog()
            }
        })

        rvIncidencias.adapter = adapter
    }

    private fun showDeleteDialog(){
        alert(resources.getString(R.string.msg_confirm_deletion),
            "Eliminar Incidencia")
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
