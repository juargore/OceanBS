@file:Suppress("SpellCheckingInspection", "DEPRECATION")

package com.glass.oceanbs.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.glass.oceanbs.Constants
import com.glass.oceanbs.R
import com.glass.oceanbs.database.TableUser
import com.glass.oceanbs.models.GenericObj
import com.glass.oceanbs.models.OWNER
import com.glass.oceanbs.models.Propietario
import com.glass.oceanbs.models.Solicitud
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class EditSolicitudActivity : BaseActivity() {

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

    private var listDesarrollos: ArrayList<GenericObj> = ArrayList()
    private var listUnidades: ArrayList<GenericObj> = ArrayList()

    private var userId = ""
    private var solicitudId = ""
    private var desarrollo = ""
    private var persona = ""
    private var codigoUnidad = ""

    private var fDesarrollosFirst = true
    private var fUnidadesFirst = true
    private lateinit var cSolicitud: Solicitud
    private lateinit var cPropietario: Propietario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_solicitud)
        intent.extras?.let {
            solicitudId = it.getString("solicitudId").toString()
            desarrollo = it.getString("desarrollo").toString()
            persona = it.getString("persona").toString()
            codigoUnidad = it.getString("codigoUnidad").toString()
        }
        initComponents()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun initComponents() {
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
            if (validateFullFields())
                sendDataToServer()
        }

        val builder = AlertDialog.Builder(this, R.style.HalfDialogTheme)
        val inflat = this.layoutInflater
        val dialogView = inflat.inflate(R.layout.progress, null)

        titleProgress = dialogView.findViewById(R.id.loading_title)

        builder.setView(dialogView)
        progress = builder.create()

        if (Constants.internetConnected(this)) {
            getCurrentSolicitud()
        } else {
            Constants.showPopUpNoInternet(this)
        }
    }

    private fun getCurrentSolicitud() {
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
                        val js = JSONObject(response.body!!.string())
                        if (js.getInt("Error") == 0) {
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
                            getListDesarrollos()
                        } else{
                            progress.dismiss()
                            Constants.snackbar(applicationContext, layParentE, js.getString("Mensaje"), Constants.Types.ERROR)
                        }
                    }catch (e: Error) {
                        progress.dismiss()
                    }
                }
            }
        })
    }


    // get a list of all desarrollos in Server
    private fun getListDesarrollos() {
        val client = OkHttpClient()
        val builder: FormBody = if (Constants.getTipoUsuario(this) == OWNER) {
            // propietario
            FormBody.Builder()
                .add("WebService","ConsultaDesarrollosIdPropietario")
                .add("IdPropietario",Constants.getUserId(this))
                .build()
        } else {
            // colaborador
            FormBody.Builder()
                .add("WebService","ConsultaDesarrollosTodos")
                .build()
        }

        val request = Request.Builder().url(Constants.URL_SUCURSALES).post(builder).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body!!.string())
                        if (jsonRes.getInt("Error") == 0) {
                            val arrayDesarrollos = jsonRes.getJSONArray("Datos")
                            for (i in 0 until arrayDesarrollos.length()) {
                                val jsonObj : JSONObject = arrayDesarrollos.getJSONObject(i)
                                listDesarrollos.add( GenericObj (
                                    jsonObj.getString("Id"),
                                    jsonObj.getString("Codigo"),
                                    jsonObj.getString("Nombre"),
                                    "${jsonObj.getString("Calle")} ${jsonObj.getString("NumExt")}",
                                    jsonObj.getString("Fotografia")))
                            }
                            setUpSpinnerDesarrollos()
                        }
                    }catch (_: Error) { }
                }
            }
        })
    }

    private fun setUpSpinnerDesarrollos() {
        val desarrollosList: ArrayList<String> = ArrayList()

        for (i in listDesarrollos)
            desarrollosList.add(i.Nombre)

        desarrollosList.add(0, "Seleccionar")

        val adapterDesarrollo = ArrayAdapter(this, R.layout.spinner_text, desarrollosList)
        spinDesarrolloE.adapter = adapterDesarrollo

        if (Constants.getTipoUsuario(this) == OWNER && listDesarrollos.size == 1) {
            // propietario
            spinDesarrolloE.setSelection(1)
        }

        spinDesarrolloE.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (pos != 0) {
                    if (fDesarrollosFirst) {
                        val strId: String = listDesarrollos[pos-1].Id
                        getListUnidad(strId)
                        fDesarrollosFirst = false
                    } else {
                        val strId: String = listDesarrollos[pos-1].Id
                        getListUnidad(strId)
                        resetAllEdittext()
                    }
                } else {
                    listUnidades.clear()
                    setUpSpinnerUnidad()
                }
            }
        }

        for (i in 0 until listDesarrollos.size) {
            if (cSolicitud.CodigoDesarrollo == listDesarrollos[i].Codigo)
                spinDesarrolloE.setSelection(i+1)
        }
    }

    // get list of unidad according the desarrollo id
    private fun getListUnidad(idDesarrollo: String) {
        val client = OkHttpClient()
        val builder: FormBody = if (Constants.getTipoUsuario(this) == OWNER) {
            // propietario
            FormBody.Builder()
                .add("WebService","ConsultaUnidadesIdDesarrolloIdPropietario")
                .add("IdDesarrollo", idDesarrollo)
                .add("IdPropietario",Constants.getUserId(this))
                .build()
        } else {
            // colaborador
            FormBody.Builder()
                .add("WebService","ConsultaUnidadesIdDesarrollo")
                .add("IdDesarrollo", idDesarrollo)
                .build()
        }

        val request = Request.Builder().url(Constants.URL_PRODUCTO).post(builder).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonRes = JSONObject(response.body!!.string())

                    if (jsonRes.getInt("Error") == 0) {
                        val arrayDesarrollos = jsonRes.getJSONArray("Datos")
                        listUnidades.clear()

                        for (i in 0 until arrayDesarrollos.length()) {
                            val jsonObj : JSONObject = arrayDesarrollos.getJSONObject(i)

                            listUnidades.add(
                                GenericObj(
                                    jsonObj.getString("Id"),
                                    jsonObj.getString("Codigo"),
                                    jsonObj.getString("Nombre"),
                                    jsonObj.getString("FechaEntrega")
                                )
                            )
                        }

                        runOnUiThread {
                            etPropietarioE.setText("")
                            setUpSpinnerUnidad() }
                    }
                } catch (_: Error) { }
            }
        })
    }

    private fun resetAllEdittext() {
        etReportaE.setText("")
        etTelMovilE.setText("")
        etTelParticularE.setText("")
        etEmailE.setText("")
        chckBoxReporta.isChecked = false
        spinRelacionE.setSelection(0)
    }

    // fill spinner unidad
    private fun setUpSpinnerUnidad() {
        val unidadesList: ArrayList<String> = ArrayList()

        for (i in listUnidades)
            unidadesList.add(i.Codigo)

        unidadesList.add(0, "Seleccionar")
        val adapterUnidad = ArrayAdapter(applicationContext, R.layout.spinner_text, unidadesList)

        spinUnidadE.adapter = adapterUnidad

        if (Constants.getTipoUsuario(this) == OWNER && listUnidades.size == 1) {
            // propietario
            spinUnidadE.setSelection(1)
        }

        spinUnidadE.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (pos != 0) {
                    if (fUnidadesFirst) {
                        getPropietarioName(listUnidades[pos-1].Id)
                        fUnidadesFirst = false
                    } else{
                        getPropietarioName(listUnidades[pos-1].Id)
                        resetAllEdittext()
                    }
                } else{
                    etPropietarioE.setText("")
                    resetAllEdittext()
                }
            }
        }

        for (i in 0 until listUnidades.size) {
            if (cSolicitud.CodigoUnidad == listUnidades[i].Codigo)
                spinUnidadE.setSelection(i+1)
        }

        fillDataFirstTime()
    }


    private fun fillDataFirstTime() {
        etCodigoE.setText(cSolicitud.Codigo)

        if (cSolicitud.ReportaPropietario.toInt() == 1) {
            chckBoxReporta.isChecked = true
        }

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

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    private fun fillDataAccordingCheck() {
        if (chckBoxReporta.isChecked) {
            etReportaE.setText("${cPropietario.nombre} ${cPropietario.apellidoP} ${cPropietario.apellidoM}")
            etTelMovilE.setText(cPropietario.telMovil)
            etTelParticularE.setText(cPropietario.telParticular)
            etEmailE.setText(cPropietario.correoElecP)
            spinRelacionE.setSelection(0)

            if (Constants.getTipoUsuario(this) == OWNER) {
                // propietario
                chckBoxReporta.isEnabled = false
                etReportaE.isEnabled = false
                etReportaE.background = resources.getDrawable(R.drawable.rectangle_round_corner_gray_fill)
                etReportaE.setTextColor(resources.getColor(R.color.colorBlack))
                spinRelacionE.isEnabled = false
                spinRelacionE.background = resources.getDrawable(R.drawable.rectangle_round_corner_gray_fill)
                etTelMovilE.isEnabled = false
                etTelMovilE.background = resources.getDrawable(R.drawable.rectangle_round_corner_gray_fill)
                etTelMovilE.setTextColor(resources.getColor(R.color.colorBlack))
                etEmailE.isEnabled = false
                etEmailE.background = resources.getDrawable(R.drawable.rectangle_round_corner_gray_fill)
                etEmailE.setTextColor(resources.getColor(R.color.colorBlack))
            }
        } else{
            etReportaE.setText("")
            etTelMovilE.setText("")
            etTelParticularE.setText("")
            etEmailE.setText("")
        }
    }

    private fun getPropietarioName(idUnidad: String) {
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
                runOnUiThread {
                    try {
                        val j = JSONObject(response.body!!.string())

                        if (j.getInt("Error") == 0) {
                            cPropietario = Propietario(
                                j.getString("Id"),
                                j.getString("Nombre"),
                                j.getString("ApellidoP"),
                                j.getString("ApellidoM"),
                                j.getString("TelMovil"),
                                j.getString("TelCasa"),
                                j.getString("CorreoElecP")
                            )

                            etPropietarioE.setText("${cPropietario.nombre} ${cPropietario.apellidoP} ${cPropietario.apellidoM}")
                            if (Constants.getTipoUsuario(applicationContext) == OWNER) {
                                chckBoxReporta.isChecked = true
                                fillDataAccordingCheck()
                            }
                        }
                    }catch (_: Error) { }
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
    private fun sendDataToServer() {
        progress.show()
        progress.setCancelable(false)
        titleProgress.text = "Enviando información"

        val reporta : Int = if (chckBoxReporta.isChecked) {1}else{0}
        userId = Constants.getUserId(this)

        val user = TableUser(this).getCurrentUserById(Constants.getUserId(this), Constants.getTipoUsuario(this))
        val idColaborador = user.idColaborador

        val client = OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS).build()

        val builder = FormBody.Builder()
            .add("WebService","GuardaSolicitudAG")
            .add("Id", cSolicitud.Id) // empty if new
            .add("Codigo", etCodigoE.text.toString()) //codigo
            .add("IdProducto", listUnidades[spinUnidadE.selectedItemPosition-1].Id) // unidad
            .add("ReportaPropietario", "$reporta") // 0 || 1
            .add("NombrePR", etReportaE.text.toString()) //nombre del propietario
            .add("TipoRelacionPropietario", "${spinRelacionE.selectedItemPosition}")
            .add("TelCelularPR", etTelMovilE.text.toString())
            .add("TelParticularPR", etTelParticularE.text.toString())
            .add("CorreoElectronicoPR", etEmailE.text.toString())
            .add("Observaciones", etObservacionesE.text.toString())
            .add("IdColaborador1", idColaborador)
            .add("Status", "1") // active / inactive
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body!!.string())

                        if (jsonRes.getInt("Error") == 0) {
                            Constants.snackbar(applicationContext, layParentE, jsonRes.getString("Mensaje"), Constants.Types.SUCCESS)
                            Constants.updateRefreshSolicitudes(applicationContext, true)
                        } else{
                            Constants.snackbar(applicationContext, layParentE, jsonRes.getString("Mensaje"), Constants.Types.ERROR)
                        }; progress.dismiss()

                    } catch (e: Error) {
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
}
