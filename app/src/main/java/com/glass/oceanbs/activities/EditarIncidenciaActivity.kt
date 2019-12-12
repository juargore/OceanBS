@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.glass.oceanbs.R
import org.jetbrains.anko.alert
import org.jetbrains.anko.textColor

class EditarIncidenciaActivity : AppCompatActivity() {

    private lateinit var imgBackEdit: ImageView

    private lateinit var etCodigoE: EditText
    private lateinit var spinDesarrolloE: Spinner
    private lateinit var spinUnidadE: Spinner
    private lateinit var etPropietarioE: EditText
    private lateinit var chckBoxReporta: CheckBox

    private lateinit var etReportaE: EditText
    private lateinit var spinRelacionE: Spinner
    private lateinit var etTelMovilE: EditText
    private lateinit var etTelParticularE: EditText

    private lateinit var etEmailE: EditText
    private lateinit var etObservacionesE: EditText
    private lateinit var btnSaveSolicitud: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_incidencia)

        supportActionBar?.hide()
        
        initComponents()
    }

    @SuppressLint("SetTextI18n")
    private fun initComponents(){
        imgBackEdit = findViewById(R.id.imgBackEdit)
        imgBackEdit.setOnClickListener { this.finish() }
        
        etCodigoE = findViewById(R.id.etCodigoE)
        spinDesarrolloE = findViewById(R.id.spinDesarrolloE)
        spinUnidadE = findViewById(R.id.spinUnidadE)
        etPropietarioE = findViewById(R.id.etPropietarioE)
        chckBoxReporta = findViewById(R.id.chckBoxReporta)

        etReportaE = findViewById(R.id.etReportaE)
        spinRelacionE = findViewById(R.id.spinRelacionE)
        etTelMovilE = findViewById(R.id.etTelMovilE)
        etTelParticularE = findViewById(R.id.etTelParticularE)

        etEmailE = findViewById(R.id.etEmailE)
        etObservacionesE = findViewById(R.id.etObservacionesE)
        btnSaveSolicitud = findViewById(R.id.btnSaveSolicitud)

        btnSaveSolicitud.setOnClickListener { showConfirmDialog(this) }
    }

    private fun showConfirmDialog(context: Context){
        alert(resources.getString(R.string.msg_confirm_creation),
            "Confirmar Solicitud")
        {
            positiveButton(resources.getString(R.string.accept)) {
                // enviar datos de actualización al server
                
            }
            negativeButton(resources.getString(R.string.cancel)){}
        }.show().apply {
            getButton(AlertDialog.BUTTON_POSITIVE)?.let { it.textColor = resources.getColor(R.color.colorBlack) }
            getButton(AlertDialog.BUTTON_NEGATIVE)?.let { it.textColor = resources.getColor(R.color.colorAccent) }
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
