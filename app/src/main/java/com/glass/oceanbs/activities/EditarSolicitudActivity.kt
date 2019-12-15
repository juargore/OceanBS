@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.glass.oceanbs.Constants
import com.glass.oceanbs.R
import okhttp3.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.textColor
import org.json.JSONObject
import java.io.IOException
import java.lang.Error

class EditarSolicitudActivity : AppCompatActivity() {

    private lateinit var layParentE: LinearLayout
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

    private var userId = ""
    private var solicitudId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_solicitud)

        supportActionBar?.hide()

        val args = intent.extras
        solicitudId = args!!.getString("solicitudId").toString()

        initComponents()
        getCurrentSolicitud()
    }

    @SuppressLint("SetTextI18n")
    private fun initComponents(){
        layParentE = findViewById(R.id.layParentE)
        imgBackEdit = findViewById(R.id.imgBackEdit)
        imgBackEdit.setOnClickListener { this.finish() }
        
        etCodigoE = findViewById(R.id.etCodigoE)
        spinDesarrolloE = findViewById(R.id.spinDesarrolloE)
        spinUnidadE = findViewById(R.id.spinUnidadE)
        etPropietarioE = findViewById(R.id.etPropietarioE)
        chckBoxReporta = findViewById(R.id.chckBoxReportaE)

        etReportaE = findViewById(R.id.etReportaE)
        spinRelacionE = findViewById(R.id.spinRelacionE)
        etTelMovilE = findViewById(R.id.etTelMovilE)
        etTelParticularE = findViewById(R.id.etTelParticularE)

        etEmailE = findViewById(R.id.etEmailE)
        etObservacionesE = findViewById(R.id.etObservacionesE)
        btnSaveSolicitud = findViewById(R.id.btnUpdateSolicitudE)
    }

    private fun getCurrentSolicitud(){
        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","ConsultaSolicitudAGIdMin")
            .add("Id", solicitudId)
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val res = response.body()?.string().toString()
                Log.e("--", res)
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
