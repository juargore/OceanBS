@file:Suppress("SpellCheckingInspection", "PrivatePropertyName", "DEPRECATION",
    "UNUSED_ANONYMOUS_PARAMETER"
)

package com.glass.oceanbs.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toBitmap
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.snackbar
import com.glass.oceanbs.R
import com.glass.oceanbs.models.GenericObj
import okhttp3.*
import org.jetbrains.anko.alert
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Error
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class CreateStatusActivity : AppCompatActivity() {

    private lateinit var progress : AlertDialog
    private lateinit var titleProgress: TextView
    private lateinit var layParentR: LinearLayout
    private lateinit var txtTitleR: TextView
    private lateinit var txtSubTitleR: TextView
    private lateinit var imgBackStatus: ImageView

    private lateinit var txtShowPhoto1: TextView
    private lateinit var cardPhoto1: CardView
    private lateinit var imgPhoto1: ImageView

    private lateinit var txtShowPhoto2: TextView
    private lateinit var cardPhoto2: CardView
    private lateinit var imgPhoto2: ImageView

    private lateinit var txtShowPhoto3: TextView
    private lateinit var cardPhoto3: CardView
    private lateinit var imgPhoto3: ImageView

    private lateinit var spinnerStatusR: Spinner
    private lateinit var spinnerRegistraR: Spinner
    private lateinit var spinnerAtiendeR: Spinner
    private lateinit var etObservacionesR: EditText
    private lateinit var btnSaveStatusR: Button

    private var listColaboradores1 : ArrayList<GenericObj> = ArrayList()
    private var listColaboradores2 : ArrayList<GenericObj> = ArrayList()

    private val GALLERY = 1
    private val CAMERA = 2
    private var SELECTED = 0

    private var mCameraFileName1 = ""
    private var mCameraFileName2 = ""
    private var mCameraFileName3 = ""

    private var incidenciaId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_status)

        supportActionBar?.hide()

        Constants.checkPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        val extras = intent.extras
        incidenciaId = extras!!.getString("incidenciaId").toString()

        initComponents()
    }

    private fun initComponents(){
        layParentR = findViewById(R.id.layParentR)
        imgBackStatus = findViewById(R.id.imgBackStatusR)
        txtTitleR = findViewById(R.id.txtTitleR)
        txtSubTitleR = findViewById(R.id.txtSubTitleR)

        txtShowPhoto1 = findViewById(R.id.txtShowPhoto1)
        cardPhoto1 = findViewById(R.id.cardPhoto1)
        imgPhoto1 = findViewById(R.id.imgPhoto1)

        txtShowPhoto2 = findViewById(R.id.txtShowPhoto2)
        cardPhoto2 = findViewById(R.id.cardPhoto2)
        imgPhoto2 = findViewById(R.id.imgPhoto2)

        txtShowPhoto3 = findViewById(R.id.txtShowPhoto3)
        cardPhoto3 = findViewById(R.id.cardPhoto3)
        imgPhoto3 = findViewById(R.id.imgPhoto3)

        spinnerStatusR = findViewById(R.id.spinnerStatusR)
        spinnerRegistraR = findViewById(R.id.spinnerRegistraR)
        spinnerAtiendeR = findViewById(R.id.spinnerAtiendeR)
        etObservacionesR = findViewById(R.id.etObservacionesR)
        btnSaveStatusR = findViewById(R.id.btnSaveStatusR)

        // set up progress dialg
        val builder = AlertDialog.Builder(this, R.style.HalfDialogTheme)
        val inflat = this.layoutInflater
        val dialogView = inflat.inflate(R.layout.progress, null)

        titleProgress = dialogView.findViewById(R.id.loading_title)

        builder.setView(dialogView)
        progress = builder.create()
        setListeners()
        setUpSpinners()
    }

    private fun setListeners(){
        imgBackStatus.setOnClickListener { this.finish() }

        cardPhoto1.setOnClickListener { showPictureDialog(1); SELECTED = 1 }
        cardPhoto2.setOnClickListener { showPictureDialog(2); SELECTED = 2 }
        cardPhoto3.setOnClickListener { showPictureDialog(3); SELECTED = 3 }

        txtShowPhoto1.setOnClickListener { showPopPhoto(1) }
        txtShowPhoto2.setOnClickListener { showPopPhoto(2) }
        txtShowPhoto3.setOnClickListener { showPopPhoto(3) }

        btnSaveStatusR.setOnClickListener { sendDataToServer() }
    }

    private fun getDataForSpinners(){

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","")
            .add("Id", "")
            .build()

        val request = Request.Builder().url(Constants.URL_CLASIFICACION).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body()!!.string())

                        if(jsonRes.getInt("Error") == 0){

                        }

                    }catch (e: Error){
                        snackbar(applicationContext, layParentR, e.message.toString())
                    }
                }
            }
        })

    }

    private fun setUpSpinners(){

        val statusList = arrayOf(
            "Seleccione una opción",
            "Registrada",
            "Por Verificar",
            "Aceptada",
            "Programada",
            "En Proceso",
            "Terminada",
            "Entregada",
            "No Aceptada",
            "No Terminada",
            "No Entregada")

        val adapterRelation = ArrayAdapter(this, R.layout.spinner_text, statusList)
        spinnerStatusR.adapter = adapterRelation


        val listColab1: ArrayList<String> = ArrayList()
        listColab1.add(0, "Seleccionar")

        for (i in listColaboradores1)
            listColab1.add(i.Nombre)

        val adapterColab1 = ArrayAdapter(this, R.layout.spinner_text, listColab1)
        spinnerRegistraR.adapter = adapterColab1

        val listColab2: ArrayList<String> = ArrayList()
        listColab2.add(0, "Seleccionar")

        for (i in listColaboradores2)
            listColab2.add(i.Nombre)

        val adapterColab2 = ArrayAdapter(this, R.layout.spinner_text, listColab2)
        spinnerAtiendeR.adapter = adapterColab2
    }

    private fun sendDataToServer(){
        progress.show()
        progress.setCancelable(false)
        titleProgress.text = "Enviando Información"

        val client = OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS).build()

        val bitmap = imgPhoto1.drawable.toBitmap()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()

        val MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");

        val requestBody : RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("WebService","GuardaStatusIncidencia")
            .addFormDataPart("Id", "") // empty if new | else -> ID
            .addFormDataPart("Status", "1")
            .addFormDataPart("Observaciones", etObservacionesR.text.toString())
            .addFormDataPart("IdIncidencia", incidenciaId)
            .addFormDataPart("IdColaborador1", "1")
            .addFormDataPart("IdColaborador2", "2")
            .addFormDataPart("StatusIncidencia", spinnerStatusR.selectedItemPosition.toString())
            .addFormDataPart("Fotografia1", "image.jpeg", RequestBody.create(MEDIA_TYPE_JPG, byteArray))
            .addFormDataPart("Fotografia2", "")
            .addFormDataPart("Fotografia3", "")
            .build()

        val builder = FormBody.Builder()
            .add("WebService","GuardaStatusIncidencia")
            .add("Id", "") // empty if new | else -> ID
            .add("Status", "1")
            .add("Observaciones", etObservacionesR.text.toString())
            .add("IdIncidencia", incidenciaId)
            .add("IdColaborador1", "1")
            .add("IdColaborador2", "2")
            .add("StatusIncidencia", spinnerStatusR.selectedItemPosition.toString())
            .add("Fotografia1", "")
            .add("Fotografia2", "")
            .add("Fotografia3", "")
            .build()

        val request = Request.Builder()
            .url(Constants.URL_INCIDENCIAS)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {

                    try {
                        val jsonRes = JSONObject(response.body()!!.string())
                        Log.e("RES",  jsonRes.toString())

                        if(jsonRes.getInt("Error") == 0){
                            showSuccessDialog()
                            Constants.updateRefreshIncidencias(applicationContext, true)
                        } else
                            snackbar(applicationContext, layParentR, jsonRes.getString("Mensaje"))

                        progress.dismiss()

                    } catch (e: Error){
                        progress.dismiss()
                        snackbar(applicationContext, layParentR, e.message.toString())
                    }
                }
            }
        })
    }

    private fun showSuccessDialog(){
        alert("Se ha guardado el Status de la incidencia satisfactoriamente",
            "Guardado exitoso!")
        {
            positiveButton(resources.getString(R.string.accept)) {
                this@CreateStatusActivity.finish()
            }
        }.show().setCancelable(false)
    }

    private fun showPictureDialog(photo: Int) {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Obtener fotografía")

        val pictureDialogItems = arrayOf("Seleccionar foto de la galería", "Capturar foto con la cámara")
        pictureDialog.setItems(pictureDialogItems) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallary()
                1 -> takePhotoFromCamera(photo)
            }
        }
        pictureDialog.show()
    }

    private fun showPopPhoto(photo: Int){
        val dialog = Dialog(this, R.style.FullDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.pop_image)

        val image = dialog.findViewById<ImageView>(R.id.imgCenter)

        val b: Bitmap = when(photo){
            1->{ imgPhoto1.drawable.toBitmap() }
            2->{ imgPhoto2.drawable.toBitmap() }
            else->{ imgPhoto3.drawable.toBitmap() }
        }
        image.setImageBitmap(b)

        dialog.show()
    }

    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)
    }

    @SuppressLint("SdCardPath")
    private fun takePhotoFromCamera(photo: Int) {
        val intent = Intent()
        intent.action = MediaStore.ACTION_IMAGE_CAPTURE

        val date = Date()
        val df = SimpleDateFormat("-mm-ss", Locale.getDefault())

        val outUri: Uri
        when(photo){
            1->{
                val newPicFile = df.format(date) + ".jpg"
                val outPath = "/sdcard/$newPicFile"
                val outfile = File(outPath)

                mCameraFileName1 = outfile.toString()
                outUri = Uri.fromFile(outfile)
            }
            2->{
                val newPicFile = df.format(date) + ".jpg"
                val outPath = "/sdcard/$newPicFile"
                val outfile = File(outPath)

                mCameraFileName2 = outfile.toString()
                outUri = Uri.fromFile(outfile)}
            else->{
                val newPicFile = df.format(date) + ".jpg"
                val outPath = "/sdcard/$newPicFile"
                val outfile = File(outPath)

                mCameraFileName3 = outfile.toString()
                outUri = Uri.fromFile(outfile)
            }
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri)
        startActivityForResult(intent, CAMERA)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    public override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY)
        {
            if (data != null)
            {
                val contentURI = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                    when(SELECTED){
                        1->{imgPhoto1.setImageBitmap(bitmap)}
                        2->{imgPhoto2.setImageBitmap(bitmap)}
                        else->{imgPhoto3.setImageBitmap(bitmap)}
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        else if (requestCode == CAMERA)
        {
            when(SELECTED){
                1->{
                    val file = File(mCameraFileName1)
                    if (file.exists()) {
                        val uriImage = Uri.fromFile(File(mCameraFileName1))
                        imgPhoto1.setImageURI(uriImage) }
                }
                2->{
                    val file = File(mCameraFileName2)
                    if (file.exists()) {
                        val uriImage = Uri.fromFile(File(mCameraFileName2))
                        imgPhoto2.setImageURI(uriImage) }
                }
                else->{
                    val file = File(mCameraFileName3)
                    if (file.exists()) {
                        val uriImage = Uri.fromFile(File(mCameraFileName3))
                        imgPhoto3.setImageURI(uriImage) }
                }
            }
        }
    }

}
