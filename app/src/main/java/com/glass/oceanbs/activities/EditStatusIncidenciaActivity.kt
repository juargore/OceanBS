@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.activities

import android.Manifest
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.widget.*
import androidx.cardview.widget.CardView
import com.glass.oceanbs.Constants
import com.glass.oceanbs.R

class EditStatusIncidenciaActivity : AppCompatActivity() {

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

    private lateinit var spinnerStatusER: Spinner
    private lateinit var spinnerRegistraER: Spinner
    private lateinit var spinnerAtiendeER: Spinner
    private lateinit var etObservacionesER: EditText
    private lateinit var btnSaveStatusER: Button

    private val GALLERY = 1
    private val CAMERA = 2
    private var SELECTED = 0

    private var mCameraFileName1 = ""
    private var mCameraFileName2 = ""
    private var mCameraFileName3 = ""

    private var idSolicitud = ""
    private var idIncidencia = ""
    private var persona = ""
    private var desarrollo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_status_incidencia)

        supportActionBar?.hide()

        Constants.checkPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        val extras = intent.extras
        //idSolicitud = extras!!.getString("solicitudId").toString()
        //idIncidencia = extras.getString("incidenciaId").toString()
        persona = extras!!.getString("persona").toString()
        desarrollo = extras.getString("desarrollo").toString()

        initComponents()
    }

    private fun initComponents(){
        txtTitleER = findViewById(R.id.txtTitleER)
        txtSubTitleER = findViewById(R.id.txtSubTitleER)

        txtTitleER.text = desarrollo
        txtSubTitleER.text = persona

    }

}
