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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_solicitud)

        supportActionBar?.hide()
        
        initComponents()
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
        chckBoxReporta = findViewById(R.id.chckBoxReporta)

        etReportaE = findViewById(R.id.etReportaE)
        spinRelacionE = findViewById(R.id.spinRelacionE)
        etTelMovilE = findViewById(R.id.etTelMovilE)
        etTelParticularE = findViewById(R.id.etTelParticularE)

        etEmailE = findViewById(R.id.etEmailE)
        etObservacionesE = findViewById(R.id.etObservacionesE)
        btnSaveSolicitud = findViewById(R.id.btnSaveSolicitud)

        btnSaveSolicitud.setOnClickListener { showConfirmDialog() }
    }

    private fun sendDataToServer(){
        //progress.show()

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        userId = Constants.getUserId(this)

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","GuardarSolicitudAG")
            .add("Id", "") // not empty -> modify
            .add("Codigo", "") //desarrollo
            .add("IdProducto", "") // unidad
            .add("ReportaPropietario", "") // 0 || 1
            .add("NombrePR", "") //nombre del propietario
            .add("TipoRelacionPropietario", "") // 0,1,2,3,4,5,6
            .add("TelCelularPR", "")
            .add("TelParticularPR", "")
            .add("CorreoElectronicoPR", "")
            .add("Observaciones", "")
            .add("IdColaborador1", userId)
            .add("Status", "1") // active / inactive
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonRes = JSONObject(response.body()!!.string())
                    Log.e("--","$jsonRes")

                    //runOnUiThread { progress.dismiss() }
                } catch (e: Error){
                    Constants.snackbar(applicationContext, layParentE, e.message.toString())
                    //runOnUiThread { progress.dismiss() }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("--","${e.message}")
                Constants.snackbar(applicationContext, layParentE, e.message.toString())
                //progress.dismiss()
            }
        })
    }

    private fun showConfirmDialog(){
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
