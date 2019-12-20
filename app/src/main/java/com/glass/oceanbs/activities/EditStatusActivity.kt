@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.activities

import android.Manifest
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.*
import androidx.cardview.widget.CardView
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.snackbar
import com.glass.oceanbs.R
import com.glass.oceanbs.models.Status
import com.squareup.picasso.Picasso
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class EditStatusActivity : AppCompatActivity() {

    private lateinit var progress : AlertDialog
    private lateinit var layParentER: LinearLayout
    private lateinit var txtTitleER: TextView
    private lateinit var txtSubTitleER: TextView
    private lateinit var imgBackStatusER: ImageView

    private lateinit var txtShowPhoto1ER: TextView
    private lateinit var cardPhoto1ER: CardView
    private lateinit var imgPhoto1ER: ImageView

    private lateinit var txtShowPhoto2ER: TextView
    private lateinit var cardPhoto2ER: CardView
    private lateinit var imgPhoto2ER: ImageView

    private lateinit var txtShowPhoto3ER: TextView
    private lateinit var cardPhoto3ER: CardView
    private lateinit var imgPhoto3ER: ImageView

    private lateinit var etAltaE: EditText
    private lateinit var etModifER: EditText

    private lateinit var spinnerStatusER: Spinner
    private lateinit var spinnerRegistraER: Spinner
    private lateinit var spinnerAtiendeER: Spinner
    private lateinit var etObservacionesER: EditText
    private lateinit var btnUpdateStatusER: Button
    private lateinit var cStatus: Status

    private val GALLERY = 1
    private val CAMERA = 2
    private var SELECTED = 0

    private var mCameraFileName1 = ""
    private var mCameraFileName2 = ""
    private var mCameraFileName3 = ""

    private var idSolicitud = ""
    private var idStatus = ""
    private var persona = ""
    private var desarrollo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_status)

        supportActionBar?.hide()

        Constants.checkPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        val extras = intent.extras
        idStatus = extras!!.getString("idStatus").toString()
        persona = extras.getString("persona").toString()
        desarrollo = extras.getString("desarrollo").toString()

        initComponents()
        getStatusIncidencia()
    }

    private fun initComponents(){
        layParentER = findViewById(R.id.layParentER)
        txtTitleER = findViewById(R.id.txtTitleER)
        txtSubTitleER = findViewById(R.id.txtSubTitleER)
        imgBackStatusER = findViewById(R.id.imgBackStatusER)

        txtShowPhoto1ER = findViewById(R.id.txtShowPhoto1ER)
        cardPhoto1ER = findViewById(R.id.cardPhoto1ER)
        imgPhoto1ER = findViewById(R.id.imgPhoto1ER)

        txtShowPhoto2ER = findViewById(R.id.txtShowPhoto2ER)
        cardPhoto2ER = findViewById(R.id.cardPhoto2ER)
        imgPhoto2ER = findViewById(R.id.imgPhoto2ER)

        txtShowPhoto3ER = findViewById(R.id.txtShowPhoto3ER)
        cardPhoto3ER = findViewById(R.id.cardPhoto3ER)
        imgPhoto3ER = findViewById(R.id.imgPhoto3ER)

        etAltaE = findViewById(R.id.etAltaER)
        etModifER = findViewById(R.id.etModifER)

        spinnerStatusER = findViewById(R.id.spinnerStatusER)
        spinnerRegistraER = findViewById(R.id.spinnerRegistraER)
        spinnerAtiendeER = findViewById(R.id.spinnerAtiendeER)
        etObservacionesER = findViewById(R.id.etObservacionesER)
        btnUpdateStatusER = findViewById(R.id.btnUpdateStatusER)


        // set up progress dialg
        val builder = AlertDialog.Builder(this, R.style.HalfDialogTheme)
        val inflat = this.layoutInflater
        val dialogView = inflat.inflate(R.layout.progress, null)

        builder.setView(dialogView)
        progress = builder.create()

        txtTitleER.text = desarrollo
        txtSubTitleER.text = persona

        imgBackStatusER.setOnClickListener { this.finish() }
    }

    private fun getStatusIncidencia(){
        progress.show()

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","ConsultaStatusIncidenciaIdApp")
            .add("Id", idStatus)
            .build()

        val request = Request.Builder().url(Constants.URL_STATUS).post(builder).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                progress.dismiss()
                snackbar(applicationContext, layParentER, e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    val jsonRes = JSONObject(response.body()!!.string())
                    Log.e("RES", jsonRes.toString())

                    if(jsonRes.getInt("Error") == 0){
                        val j = jsonRes.getJSONArray("Datos").getJSONObject(0)

                        cStatus = Status(
                            j.getString("Id"),
                            j.getString("FechaAlta"),
                            j.getString("FechaUltimaModif"),
                            j.getString("Observaciones"),
                            j.getString("IdColaborador1"),
                            j.getString("IdColaborador2"),
                            j.getString("StatusIncidencia"),
                            j.getString("Status"),
                            j.getString("Fotografia1"),
                            j.getString("Fotografia2"),
                            j.getString("Fotografia3"))

                        fillData()
                        progress.dismiss()

                    } else {
                        progress.dismiss()
                        snackbar(applicationContext, layParentER, jsonRes.getString("Mensaje"))
                    }
                }
            }
        })
    }

    private fun fillData(){
        Picasso.get().load("${Constants.URL_IMAGES_STATUS}${cStatus.Fotografia1}").fit().error(R.drawable.ic_box).into(imgPhoto1ER)
        Picasso.get().load("${Constants.URL_IMAGES_STATUS}${cStatus.Fotografia2}").fit().error(R.drawable.ic_box).into(imgPhoto2ER)
        Picasso.get().load("${Constants.URL_IMAGES_STATUS}${cStatus.Fotografia3}").fit().error(R.drawable.ic_box).into(imgPhoto3ER)

        etAltaE.setText(cStatus.FechaAlta)
        etModifER.setText(cStatus.FechaUltimaModif)
        etObservacionesER.setText(cStatus.Observaciones)
    }

}
