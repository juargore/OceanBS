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
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.glass.oceanbs.Constants
import com.glass.oceanbs.R
import com.glass.oceanbs.activities.IncidenciasActivity
import okhttp3.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.textColor
import org.json.JSONObject
import java.io.IOException
import java.lang.Error


class NewSolicitudFragment : Fragment() {

    private lateinit var layParentN: LinearLayout
    private lateinit var etCodigoS: EditText
    private lateinit var spinDesarrolloS: Spinner
    private lateinit var spinUnidadS: Spinner
    private lateinit var etPropietarioS: EditText
    private lateinit var chckBoxReporta: CheckBox

    private lateinit var etReportaS: EditText
    private lateinit var spinRelacionS: Spinner
    private lateinit var etTelMovilS: EditText
    private lateinit var etTelParticularS: EditText

    private lateinit var etEmailS: EditText
    private lateinit var etObservacionesS: EditText
    private lateinit var btnSaveSolicitud: Button

    private var userId = ""
    private lateinit var policy: StrictMode.ThreadPolicy

    companion object{
        fun newInstance(): NewSolicitudFragment {
            return NewSolicitudFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_new_solicitud, container, false)

        policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        initComponents(rootView)
        setUpSpinners()

        return rootView
    }

    // get current code according the WS
    private fun getCurrentCode(){
        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","")
            .add("", "")
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()
        val cCode = client.newCall(request).execute( ).body().toString()

        getListDesarrollos()
    }

    // get a list of all desarrollos in DB
    private fun getListDesarrollos(){
        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","")
            .add("", "")
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()
        val listStrDesarrollos = client.newCall(request).execute( ).body().toString()
    }

    // get list of unidad according the desarrollo id
    private fun getListUnidad(){
        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","")
            .add("", "")
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()
        val listStrUnidad = client.newCall(request).execute( ).body().toString()
    }

    // get nombre de propietario according unidad
    private fun getPropietarioName() : String{
        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","")
            .add("", "")
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()
        val propietario = client.newCall(request).execute( ).body().toString()

        return propietario
    }

    @SuppressLint("SetTextI18n")
    private fun initComponents(view: View){
        userId = Constants.getUserId(context!!)

        layParentN = view.findViewById(R.id.layParentN)
        etCodigoS = view.findViewById(R.id.etCodigoN)
        spinDesarrolloS = view.findViewById(R.id.spinDesarrolloN)
        spinUnidadS = view.findViewById(R.id.spinUnidadN)
        etPropietarioS = view.findViewById(R.id.etPropietarioN)
        chckBoxReporta = view.findViewById(R.id.chckBoxReporta)

        etReportaS = view.findViewById(R.id.etReportaN)
        spinRelacionS = view.findViewById(R.id.spinRelacionN)
        etTelMovilS = view.findViewById(R.id.etTelMovilN)
        etTelParticularS = view.findViewById(R.id.etTelParticularN)

        etEmailS = view.findViewById(R.id.etEmailN)
        etObservacionesS = view.findViewById(R.id.etObservacionesN)
        btnSaveSolicitud = view.findViewById(R.id.btnSaveSolicitud)

        btnSaveSolicitud.setOnClickListener { showConfirmDialog(context!!) }
    }

    private fun setUpSpinners(){
        val relationList = arrayOf(" ", "Esposo(a)", "Hijo(a)", "Otro familiar", "Administrador", "Arrendatario", "Otro")
        val adapter = ArrayAdapter(context!!, R.layout.spinner_text, relationList)

        spinRelacionS.adapter = adapter
        //spinDesarrolloS.adapter = adapter
        //spinUnidadS.adapter = adapter
    }

    private fun showConfirmDialog(context: Context){
        alert(resources.getString(R.string.msg_confirm_creation),
            "Confirmar Solicitud")
        {
            positiveButton(resources.getString(R.string.accept)) {
                //sendDataToServer()
            }
            negativeButton(resources.getString(R.string.cancel)){}
        }.show().apply {
            getButton(AlertDialog.BUTTON_POSITIVE)?.let { it.textColor = resources.getColor(R.color.colorBlack) }
            getButton(AlertDialog.BUTTON_NEGATIVE)?.let { it.textColor = resources.getColor(R.color.colorAccent) }
        }
    }

    private fun sendDataToServer(){
        //progress.show()

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","GuardarSolicitudAG")
            .add("Id", "") // empty if new
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

                    runOnUiThread { showResumeDialog(context!!) }

                    //runOnUiThread { progress.dismiss() }
                } catch (e: Error){
                    Constants.snackbar(context!!, layParentN, e.message.toString())
                    //runOnUiThread { progress.dismiss() }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("--","${e.message}")
                Constants.snackbar(context!!, layParentN, e.message.toString())
                //progress.dismiss()
            }
        })
    }

    private fun showResumeDialog(context: Context){
        val dialog = Dialog(context, R.style.FullDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.pop_data_solicitud)

        val btnAdd = dialog.findViewById<Button>(R.id.btnAddIncidencias)
        btnAdd.setOnClickListener {
            dialog.dismiss()

            val intent = Intent(activity, IncidenciasActivity::class.java)
            startActivity(intent)
        }

        dialog.show()
    }

}
