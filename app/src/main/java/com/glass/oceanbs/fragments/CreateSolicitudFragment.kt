@file:Suppress("SpellCheckingInspection", "DEPRECATION")

package com.glass.oceanbs.fragments


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.StrictMode
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.fragment.app.Fragment
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.snackbar
import com.glass.oceanbs.R
import com.glass.oceanbs.activities.ListIncidenciasActivity
import com.glass.oceanbs.activities.MainActivity
import com.glass.oceanbs.models.GenericObj
import com.glass.oceanbs.models.Propietario
import com.squareup.picasso.Picasso
import okhttp3.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.textColor
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


class CreateSolicitudFragment : Fragment() {

    private lateinit var progress : AlertDialog
    private lateinit var titleProgress: TextView
    private lateinit var layParentN: LinearLayout

    private lateinit var etCodigoN: EditText
    private lateinit var spinDesarrolloN: Spinner
    private lateinit var spinUnidadN: Spinner
    private lateinit var etPropietarioN: EditText
    private lateinit var chckBoxReportaN: CheckBox

    private lateinit var etReportaN: EditText
    private lateinit var spinRelacionN: Spinner
    private lateinit var etTelMovilN: EditText
    private lateinit var etTelParticularN: EditText

    private lateinit var etEmailN: EditText
    private lateinit var etObservacionesN: EditText
    private lateinit var btnSaveSolicitud: Button
    private lateinit var policy: StrictMode.ThreadPolicy

    private var userId = ""
    private var suggestedId = ""

    private var listDesarrollos: ArrayList<GenericObj> = ArrayList()
    private var listUnidades: ArrayList<GenericObj> = ArrayList()
    private lateinit var cPropietario: Propietario

    companion object{
        fun newInstance(): CreateSolicitudFragment {
            return CreateSolicitudFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_create_solicitud, container, false)

        policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        initComponents(rootView)
        if(Constants.internetConnected(activity!!)){
            getSuggestedCode()
        } else
            Constants.showPopUpNoInternet(activity!!)
        //getSuggestedCode()

        return rootView
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun initComponents(view: View){
        userId = Constants.getUserId(context!!)

        layParentN = view.findViewById(R.id.layParentN)
        etCodigoN = view.findViewById(R.id.etCodigoN)
        spinDesarrolloN = view.findViewById<Spinner>(R.id.spinDesarrolloN)
        spinUnidadN = view.findViewById(R.id.spinUnidadN)
        etPropietarioN = view.findViewById(R.id.etPropietarioN)
        chckBoxReportaN = view.findViewById(R.id.chckBoxReporta)

        etReportaN = view.findViewById(R.id.etReportaN)
        spinRelacionN = view.findViewById(R.id.spinRelacionN)
        etTelMovilN = view.findViewById(R.id.etTelMovilN)
        etTelParticularN = view.findViewById(R.id.etTelParticularN)

        etEmailN = view.findViewById(R.id.etEmailN)
        etObservacionesN = view.findViewById(R.id.etObservacionesN)
        btnSaveSolicitud = view.findViewById(R.id.btnSaveSolicitud)

        val builder = AlertDialog.Builder(context!!, R.style.HalfDialogTheme)
        val inflat = this.layoutInflater
        val dialogView = inflat.inflate(R.layout.progress, null)

        titleProgress = dialogView.findViewById(R.id.loading_title)

        builder.setView(dialogView)
        progress = builder.create()

        btnSaveSolicitud.setOnClickListener {
            if(validateFullFields())
                sendDataToServer()
        }

        chckBoxReportaN.setOnClickListener {
            if(::cPropietario.isInitialized && etPropietarioN.text.toString() != "")
                fillDataAccordingCheck()
            else{
                chckBoxReportaN.isChecked = false
                snackbar(context!!, layParentN, "Elija una Unidad para obtener la información del Propietario", Constants.Types.INFO)
            }
        }
    }

    // get current code according the WS
    private fun getSuggestedCode(){

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","GetCodigoSugeridoSolicitudAG")
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonRes = JSONObject(response.body()!!.string())
                    if(jsonRes.getInt("Error") == 0){
                        suggestedId = jsonRes.getString("CodigoSugeridoSolicitudAG")
                        runOnUiThread { etCodigoN.setText(suggestedId) }
                    }
                }catch (e: Error){ }
            }
        })

        getListDesarrollos()
    }

    // get a list of all desarrollos in Server
    private fun getListDesarrollos(){
        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","ConsultaDesarrollosTodos")
            .build()

        val request = Request.Builder().url(Constants.URL_SUCURSALES).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonRes = JSONObject(response.body()!!.string())

                    if(jsonRes.getInt("Error") == 0){
                        val arrayDesarrollos = jsonRes.getJSONArray("Datos")
                        listDesarrollos.clear()

                        for(i in 0 until arrayDesarrollos.length()){
                            val jsonObj : JSONObject = arrayDesarrollos.getJSONObject(i)

                            listDesarrollos.add( GenericObj (
                                jsonObj.getString("Id"),
                                jsonObj.getString("Codigo"),
                                jsonObj.getString("Nombre"),
                                "${jsonObj.getString("Calle")} ${jsonObj.getString("NumExt")}",
                                jsonObj.getString("Fotografia")))
                        }

                        runOnUiThread { setUpFirstSpinners() }
                    }
                }catch (e: Error){ }
            }
        })
    }

    // get list of unidad according the desarrollo id
    private fun getListUnidad(idDesarrollo: String){
        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","ConsultaUnidadesIdDesarrollo")
            .add("IdDesarrollo", idDesarrollo)
            .build()

        val request = Request.Builder().url(Constants.URL_PRODUCTO).post(builder).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonRes = JSONObject(response.body()!!.string())

                    if(jsonRes.getInt("Error") == 0){
                        val arrayDesarrollos = jsonRes.getJSONArray("Datos")
                        listUnidades.clear()

                        for(i in 0 until arrayDesarrollos.length()){
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
                            etPropietarioN.setText("")
                            resetAllEdittext()
                            setUpSpinnerUnidad() }
                    }
                }catch (e: Error){ }
            }
        })
    }

    // get nombre de propietario according unidad
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

                        runOnUiThread {
                            etPropietarioN.setText(
                                "${cPropietario.nombre} ${cPropietario.apellidoP} ${cPropietario.apellidoM}")
                        }
                    }
                }catch (e: Error){ }
            }
        })
    }

    // fill spinner desarrollos and relacion propietario
    private fun setUpFirstSpinners(){

        val relationList = arrayOf("Seleccionar", "Esposo(a)", "Hijo(a)", "Otro familiar", "Administrador", "Arrendatario", "Otro")
        val adapterRelation = ArrayAdapter(context!!, R.layout.spinner_text, relationList)
        spinRelacionN.adapter = adapterRelation

        val desarrollosList: ArrayList<String> = ArrayList()

        for (i in listDesarrollos)
            desarrollosList.add(i.Nombre)

        desarrollosList.add(0, "Seleccionar")
        val adapterDesarrollo = ArrayAdapter(context!!, R.layout.spinner_text, desarrollosList)

        spinDesarrolloN.adapter = adapterDesarrollo
        spinDesarrolloN.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if(pos != 0){
                    val strId: String = listDesarrollos[pos-1].Id
                    getListUnidad(strId)
                } else{
                    listUnidades.clear()
                    setUpSpinnerUnidad()
                }
            }
        }
    }

    // fill spinner unidad
    private fun setUpSpinnerUnidad(){
        val unidadesList: ArrayList<String> = ArrayList()

        for (i in listUnidades)
            unidadesList.add(i.Codigo)

        unidadesList.add(0, "Seleccionar")
        val adapterUnidad = ArrayAdapter(context!!, R.layout.spinner_text, unidadesList)

        spinUnidadN.adapter = adapterUnidad
        spinUnidadN.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if(pos != 0){
                    if(chckBoxReportaN.isChecked)
                        resetAllEdittext()

                    getPropietarioName(listUnidades[pos-1].Id)
                } else{
                    etPropietarioN.setText("")
                    resetAllEdittext()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fillDataAccordingCheck(){
        if(chckBoxReportaN.isChecked){
            //etReportaN.isEnabled = false
            etReportaN.setText("${cPropietario.nombre} ${cPropietario.apellidoP} ${cPropietario.apellidoM}")

            //etTelMovilN.isEnabled = false
            etTelMovilN.setText(cPropietario.telMovil)

            //etTelParticularN.isEnabled = false
            etTelParticularN.setText(cPropietario.telParticular)

            //etEmailN.isEnabled = false
            etEmailN.setText(cPropietario.correoElecP)

            //spinRelacionN.isEnabled = false
            spinRelacionN.setSelection(0)
        } else{
            resetAllEdittext()
        }
    }

    // reset values in each edittext below
    private fun resetAllEdittext(){
        etReportaN.setText("")
        //etReportaN.isEnabled = true
        etTelMovilN.setText("")
        //etTelMovilN.isEnabled = true
        etTelParticularN.setText("")
        //etTelParticularN.isEnabled = true
        etEmailN.setText("")
        //etEmailN.isEnabled = true
        spinRelacionN.setSelection(0)
        //spinRelacionN.isEnabled = true
        chckBoxReportaN.isChecked = false
    }

    private fun validateFullFields(): Boolean {
        val msg = "Este campo no puede estar vacío"
        return when {
            TextUtils.isEmpty(etPropietarioN.text.toString()) -> {
                etPropietarioN.error = msg; false }
            TextUtils.isEmpty(etReportaN.text.toString()) -> {
                etReportaN.error = msg; false }
            TextUtils.isEmpty(etTelMovilN.text.toString()) -> {
                etTelMovilN.error = msg; false }
            TextUtils.isEmpty(etEmailN.text.toString()) -> {
                etEmailN.error = msg; false }
            else -> true
        }
    }

    // send values to server to create a new solicitud
    @SuppressLint("SetTextI18n")
    private fun sendDataToServer(){
        progress.show()
        progress.setCancelable(false)
        titleProgress.text = "Enviando información"

        val reporta : Int = if(chckBoxReportaN.isChecked){1}else{0}
        val client = OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS).build()

        val builder = FormBody.Builder()
            .add("WebService","GuardaSolicitudAG")
            .add("Id", "") // empty if new
            .add("Codigo", etCodigoN.text.toString()) //codigo
            .add("IdProducto", listUnidades[spinUnidadN.selectedItemPosition-1].Id) // unidad
            .add("ReportaPropietario", "$reporta") // 0 | 1
            .add("NombrePR", etReportaN.text.toString()) //nombre del propietario
            .add("TipoRelacionPropietario", "${spinRelacionN.selectedItemPosition}")
            .add("TelCelularPR", etTelMovilN.text.toString())
            .add("TelParticularPR", etTelParticularN.text.toString())
            .add("CorreoElectronicoPR", etEmailN.text.toString())
            .add("Observaciones", etObservacionesN.text.toString())
            .add("IdColaborador1", userId)
            .add("Status", "1") // active | inactive
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body()!!.string())
                        Log.e("ANS",  jsonRes.toString())

                        if(jsonRes.getInt("Error") == 0){
                            snackbar(context!!, layParentN, jsonRes.getString("Mensaje"), Constants.Types.SUCCESS)
                            Constants.updateRefreshSolicitudes(context!!, true)
                            showResumeDialog(context!!, jsonRes.getString("Id"))
                        } else
                            snackbar(context!!, layParentN, jsonRes.getString("Mensaje"), Constants.Types.ERROR)

                        progress.dismiss()

                    } catch (e: Error){
                        snackbar(context!!, layParentN, e.message.toString(), Constants.Types.ERROR)
                        progress.dismiss()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    snackbar(context!!, layParentN, e.message.toString(), Constants.Types.ERROR)
                    progress.dismiss()
                }
            }
        })
    }

    // dialog showing current info about solicitud
    @SuppressLint("SetTextI18n")
    private fun showResumeDialog(context: Context, solicitudId: String){
        val dialog = Dialog(context, R.style.FullDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.pop_data_solicitud)

        val photo = dialog.findViewById<ImageView>(R.id.rPhoto)
        val desarrollo = dialog.findViewById<TextView>(R.id.rDesarrollo)
        val direccion = dialog.findViewById<TextView>(R.id.rDirección)
        val unidad = dialog.findViewById<TextView>(R.id.rUnidad)
        val fecha = dialog.findViewById<TextView>(R.id.rFechaEntrega)
        val propietario = dialog.findViewById<TextView>(R.id.rPropietario)
        val celular = dialog.findViewById<TextView>(R.id.rCelular)
        val email = dialog.findViewById<TextView>(R.id.rEmail)

        Picasso.get().load("${Constants.URL_IMAGES}${listDesarrollos[spinDesarrolloN.selectedItemPosition-1].extra2}").error(resources.getDrawable(R.drawable.ic_no_image)).fit().into(photo)
        desarrollo.text = "Desarrollo ${spinDesarrolloN.selectedItem}"
        direccion.text = listDesarrollos[spinDesarrolloN.selectedItemPosition-1].extra1
        //unidad.text = "Unidad ${spinUnidadN.selectedItem}"
        unidad.text = "Unidad ${listUnidades[spinUnidadN.selectedItemPosition-1].Codigo}"
        fecha.text = "Entregada: ${listUnidades[spinUnidadN.selectedItemPosition-1].extra1}"
        propietario.text = "Propietario\n${etPropietarioN.text}"
        celular.text = "${etTelMovilN.text}"
        email.text = etEmailN.text.toString()

        val btnAdd = dialog.findViewById<Button>(R.id.btnAddIncidencias)
        val btnCancel = dialog.findViewById<TextView>(R.id.btnCancelIncidencias)

        btnCancel.setOnClickListener {

            //clear edittext
            spinDesarrolloN.setSelection(0)
            resetAllEdittext()
            dialog.dismiss()

            // go to first tab
            MainActivity.goToFirstTab()

        }
        btnAdd.setOnClickListener {
            dialog.dismiss()

            val intent = Intent(activity, ListIncidenciasActivity::class.java)
            intent.putExtra("solicitudId", solicitudId)
            intent.putExtra("desarrollo", spinDesarrolloN.selectedItem.toString())
            intent.putExtra("persona", etReportaN.text.toString())
            intent.putExtra("codigoUnidad", listUnidades[spinUnidadN.selectedItemPosition-1].Codigo)
            startActivity(intent)
        }

        dialog.show()
        dialog.setCancelable(false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(view != null){
            if(isVisibleToUser){

                etObservacionesN.setText("")
                etPropietarioN.error = null
                etReportaN.error = null
                etTelMovilN.error = null
                etEmailN.error = null

                val ft = fragmentManager?.beginTransaction()
                ft?.detach(this)?.attach(this)?.commit()
            }
        }
    }
}
