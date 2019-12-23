@file:Suppress("SpellCheckingInspection", "DEPRECATION")

package com.glass.oceanbs.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.glass.oceanbs.Constants
import com.glass.oceanbs.R
import com.glass.oceanbs.models.Propietario
import com.glass.oceanbs.models.Solicitud
import okhttp3.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.textColor
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class EditSolicitudActivity : AppCompatActivity() {

    private lateinit var progress : AlertDialog
    private lateinit var titleProgress: TextView
    private lateinit var layParentE: LinearLayout
    private lateinit var imgBackEdit: ImageView
    private lateinit var txtTitleDesarrolloE: TextView
    private lateinit var txtSubTitleDesarrolloE: TextView

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
    private var desarrollo = ""
    private var persona = ""
    private var codigoUnidad = ""
    private lateinit var cSolicitud: Solicitud
    private lateinit var cPropietario: Propietario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_solicitud)

        supportActionBar?.hide()

        val args = intent.extras
        solicitudId = args!!.getString("solicitudId").toString()
        desarrollo = args.getString("desarrollo").toString()
        persona = args.getString("persona").toString()
        codigoUnidad = args.getString("codigoUnidad").toString()

        initComponents()
        getCurrentSolicitud()
    }

    @SuppressLint("InflateParams")
    private fun initComponents(){
        layParentE = findViewById(R.id.layParentE)
        txtTitleDesarrolloE = findViewById(R.id.txtTitleDesarrolloE)
        txtSubTitleDesarrolloE = findViewById(R.id.txtSubTitleDesarrolloE)
        imgBackEdit = findViewById(R.id.imgBackEdit)

        txtTitleDesarrolloE.text = "$desarrollo $codigoUnidad"
        txtSubTitleDesarrolloE.text = persona
        imgBackEdit.setOnClickListener { this.finish() }
        
        etCodigoE = findViewById(R.id.etCodigoE)
        spinDesarrolloE = findViewById(R.id.spinDesarrolloE)
        spinUnidadE = findViewById(R.id.spinUnidadE)
        etPropietarioE = findViewById(R.id.etPropietarioE)

        chckBoxReporta = findViewById(R.id.chckBoxReportaE)
        chckBoxReporta.setOnClickListener { fillDataAccordingCheck() }

        etReportaE = findViewById(R.id.etReportaE)
        spinRelacionE = findViewById(R.id.spinRelacionE)
        etTelMovilE = findViewById(R.id.etTelMovilE)
        etTelParticularE = findViewById(R.id.etTelParticularE)

        etEmailE = findViewById(R.id.etEmailE)
        etObservacionesE = findViewById(R.id.etObservacionesE)
        btnSaveSolicitud = findViewById(R.id.btnUpdateSolicitudE)
        btnSaveSolicitud.setOnClickListener {
            if(validateFullFields())
                sendDataToServer()
        }

        val builder = AlertDialog.Builder(this, R.style.HalfDialogTheme)
        val inflat = this.layoutInflater
        val dialogView = inflat.inflate(R.layout.progress, null)

        titleProgress = dialogView.findViewById(R.id.loading_title)

        builder.setView(dialogView)
        progress = builder.create()
    }

    private fun fillFields(){
        getPropietarioName(cSolicitud.IdProducto)

        etCodigoE.setText(cSolicitud.Codigo)

        val adapterDesarrollo = ArrayAdapter(this, R.layout.spinner_text, arrayOf(desarrollo))
        spinDesarrolloE.adapter = adapterDesarrollo
        spinDesarrolloE.isEnabled = false

        val adapterUnidad = ArrayAdapter(this, R.layout.spinner_text, arrayOf(cSolicitud.CodigoUnidad))
        spinUnidadE.adapter = adapterUnidad
        spinUnidadE.isEnabled = false

        etPropietarioE.setText(cSolicitud.NombrePropietario)

        if(cSolicitud.ReportaPropietario.toInt() == 1){
            chckBoxReporta.isChecked = true
        }

        fillDataFirstTime()
    }


    private fun fillDataFirstTime(){
        etReportaE.setText(cSolicitud.NombrePR)
        etTelMovilE.setText(cSolicitud.TelCelularPR)
        etTelParticularE.setText(cSolicitud.TelParticularPR)
        etEmailE.setText(cSolicitud.CorreoElectronicoPR)
        etObservacionesE.setText(cSolicitud.Observaciones)

        val relationList = arrayOf("Seleccionar", "Esposo(a)", "Hijo(a)", "Otro familiar", "Administrador", "Arrendatario", "Otro")
        val adapterRelation = ArrayAdapter(this, R.layout.spinner_text, relationList)
        spinRelacionE.adapter = adapterRelation
        spinRelacionE.setSelection(cSolicitud.TipoRelacionPropietario.toInt())
    }

    @SuppressLint("SetTextI18n")
    private fun fillDataAccordingCheck(){
        if(chckBoxReporta.isChecked){
            etReportaE.setText("${cPropietario.nombre} ${cPropietario.apellidoP} ${cPropietario.apellidoM}")
            etTelMovilE.setText(cPropietario.telMovil)
            etTelParticularE.setText(cPropietario.telParticular)
            etEmailE.setText(cPropietario.correoElecP)
            spinRelacionE.setSelection(0)
        } else{
            etReportaE.setText("")
            etTelMovilE.setText("")
            etTelParticularE.setText("")
            etEmailE.setText("")
        }
    }

    private fun getPropietarioName(idUnidad: String){
        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","ConsultaPropietarioIdUnidad")
            .add("IdUnidad", idUnidad)
            .build()

        val request = Request.Builder().url(Constants.URL_PRODUCTO).post(builder).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                try {
                    val j = JSONObject(response.body()!!.string())

                    if(j.getInt("Error") == 0){
                        cPropietario = Propietario(
                            j.getString("Id"),
                            j.getString("Nombre"),
                            j.getString("ApellidoP"),
                            j.getString("ApellidoM"),
                            j.getString("TelMovil"),
                            j.getString("TelCasa"),
                            j.getString("CorreoElecP")
                        )
                    }
                }catch (e: Error){ }
            }
        })
    }

    private fun getCurrentSolicitud(){
        progress.show()

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","ConsultaSolicitudAGIdApp")
            .add("Id", solicitudId)
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { progress.dismiss()
                    Constants.snackbar(applicationContext, layParentE, e.message.toString(), Constants.Types.ERROR) }
            }
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val js = JSONObject(response.body()!!.string())
                        if(js.getInt("Error") == 0){
                            val j = js.getJSONArray("Datos").getJSONObject(0)

                            cSolicitud = Solicitud(
                                j.getString("Id"),
                                j.getString("Codigo"),
                                j.getString("IdDesarrollo"),
                                j.getString("CodigoDesarrollo"),
                                j.getString("IdProducto"),
                                j.getString("CodigoUnidad"),
                                j.getString("IdPropietario"),
                                j.getString("NombrePropietario"),
                                j.getString("ReportaPropietario"),
                                j.getString("TipoRelacionPropietario"),
                                j.getString("NombrePR"),
                                j.getString("TelCelularPR"),
                                j.getString("TelParticularPR"),
                                j.getString("CorreoElectronicoPR"),
                                j.getString("Observaciones"),
                                j.getString("IdColaborador1"),
                                j.getString("Status")
                            )

                            progress.dismiss()
                            fillFields()
                        } else{
                            progress.dismiss()
                            Constants.snackbar(applicationContext, layParentE, js.getString("Mensaje"), Constants.Types.ERROR)
                        }
                    }catch (e: Error){
                        progress.dismiss()
                    }
                }
            }
        })
    }

    private fun validateFullFields(): Boolean{
        val msg = "Este campo no puede estar vacío"
        return when {
            TextUtils.isEmpty(etReportaE.text.toString()) -> {
                etReportaE.error = msg; false }
            TextUtils.isEmpty(etTelMovilE.text.toString()) -> {
                etTelMovilE.error = msg; false }
            TextUtils.isEmpty(etEmailE.text.toString()) -> {
                etEmailE.error = msg; false }
            else -> true
        }
    }

    // send values to server to create a new solicitud
    @SuppressLint("SetTextI18n")
    private fun sendDataToServer(){
        progress.show()
        progress.setCancelable(false)
        titleProgress.text = "Enviando Información"

        val reporta : Int = if(chckBoxReporta.isChecked){1}else{0}
        userId = Constants.getUserId(this)

        val client = OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS).build()

        val builder = FormBody.Builder()
            .add("WebService","GuardaSolicitudAG")
            .add("Id", cSolicitud.Id) // empty if new
            .add("Codigo", etCodigoE.text.toString()) //codigo
            .add("IdProducto", cSolicitud.IdProducto) // unidad
            .add("ReportaPropietario", "$reporta") // 0 || 1
            .add("NombrePR", etReportaE.text.toString()) //nombre del propietario
            .add("TipoRelacionPropietario", "${spinRelacionE.selectedItemPosition}")
            .add("TelCelularPR", etTelMovilE.text.toString())
            .add("TelParticularPR", etTelParticularE.text.toString())
            .add("CorreoElectronicoPR", etEmailE.text.toString())
            .add("Observaciones", etObservacionesE.text.toString())
            .add("IdColaborador1", userId)
            .add("Status", "1") // active / inactive
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body()!!.string())

                        if(jsonRes.getInt("Error") == 0){
                            Constants.snackbar(applicationContext, layParentE, jsonRes.getString("Mensaje"), Constants.Types.SUCCESS)
                            Constants.updateRefreshSolicitudes(applicationContext, true)
                        } else{
                            Constants.snackbar(applicationContext, layParentE, jsonRes.getString("Mensaje"), Constants.Types.ERROR)
                        }; progress.dismiss()

                    } catch (e: Error){
                        Constants.snackbar(applicationContext, layParentE, e.message.toString(), Constants.Types.ERROR)
                        progress.dismiss()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Constants.snackbar(applicationContext, layParentE, e.message.toString(), Constants.Types.ERROR)
                    progress.dismiss()
                }
            }
        })
    }

    /*private fun showInfoDialog(){
        alert("La solicitud se actualizó con éxito!",
            "Solicitud Actualizada")
        {
            positiveButton(resources.getString(R.string.accept)) {
                this@EditSolicitudActivity.finish()
            }
        }.show().apply {
            getButton(AlertDialog.BUTTON_POSITIVE)?.let { it.textColor = resources.getColor(R.color.colorBlack) }
        }.setCancelable(false)
    }*/

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}
