@file:Suppress("SpellCheckingInspection", "DEPRECATION", "PrivatePropertyName")

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
import android.os.Handler
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
import com.glass.oceanbs.models.Status
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import okhttp3.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class EditStatusActivity : AppCompatActivity() {

    private lateinit var progress : AlertDialog
    private lateinit var titleProgress: TextView
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

    private var listColaboradores1 : ArrayList<GenericObj> = ArrayList()
    private var listColaboradores2 : ArrayList<GenericObj> = ArrayList()
    private lateinit var statusList: Array<String>

    private val GALLERY = 1
    private val CAMERA = 2
    private var SELECTED = 0

    private var mCameraFileName1 = ""
    private var mCameraFileName2 = ""
    private var mCameraFileName3 = ""

    private var incidenciaId = ""
    private var idStatus = ""
    private var persona = ""
    private var desarrollo = ""
    private var codigoUnidad = ""

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
        incidenciaId = extras.getString("incidenciaId").toString()
        persona = extras.getString("persona").toString()
        desarrollo = extras.getString("desarrollo").toString()
        codigoUnidad = extras.getString("codigoUnidad").toString()

        initComponents()
    }

    @SuppressLint("SetTextI18n", "InflateParams")
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

        titleProgress = dialogView.findViewById(R.id.loading_title)

        builder.setView(dialogView)
        progress = builder.create()

        txtTitleER.text = "$desarrollo $codigoUnidad"
        txtSubTitleER.text = persona

        setListeners()
        getDataForSpinners()
    }

    private fun setListeners(){
        imgBackStatusER.setOnClickListener { this.finish() }

        cardPhoto1ER.setOnClickListener { showPictureDialog(1); SELECTED = 1 }
        cardPhoto2ER.setOnClickListener { showPictureDialog(2); SELECTED = 2 }
        cardPhoto3ER.setOnClickListener { showPictureDialog(3); SELECTED = 3 }

        txtShowPhoto1ER.setOnClickListener { showPopPhoto(1) }
        txtShowPhoto2ER.setOnClickListener { showPopPhoto(2) }
        txtShowPhoto3ER.setOnClickListener { showPopPhoto(3) }

        btnUpdateStatusER.setOnClickListener { sendDataToServer() }
    }

    private fun getDataForSpinners(){

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","ConsultaColaboradoresTodosApp")
            .build()

        val request = Request.Builder().url(Constants.URL_USER).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body()!!.string())

                        if(jsonRes.getInt("Error") == 0){

                            val arrayColab = jsonRes.getJSONArray("Datos")

                            for (i in 0 until arrayColab.length()){
                                val j : JSONObject = arrayColab.getJSONObject(i)

                                listColaboradores1.add(
                                    GenericObj(
                                        j.getString("Id"),
                                        j.getString("Codigo"),
                                        "${j.getString("Nombre")} ${j.getString("ApellidoP")} ${j.getString("ApellidoM")}")
                                )
                            }

                            listColaboradores2.addAll(listColaboradores1)
                            setUpSpinners()
                        }

                    }catch (e: Error){
                        snackbar(applicationContext, layParentER, e.message.toString(), Constants.Types.ERROR)
                    }
                }
            }
        })

    }

    private fun setUpSpinners(){

        statusList = arrayOf(
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
        spinnerStatusER.adapter = adapterRelation


        val listColab1: ArrayList<String> = ArrayList()
        listColab1.add(0, "Seleccionar")

        for (i in listColaboradores1)
            listColab1.add(i.Nombre)

        val adapterColab1 = ArrayAdapter(this, R.layout.spinner_text, listColab1)
        spinnerRegistraER.adapter = adapterColab1

        val listColab2: ArrayList<String> = ArrayList()
        listColab2.add(0, "Seleccionar")

        for (i in listColaboradores2)
            listColab2.add(i.Nombre)

        val adapterColab2 = ArrayAdapter(this, R.layout.spinner_text, listColab2)
        spinnerAtiendeER.adapter = adapterColab2

        getStatusIncidencia()
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
                snackbar(applicationContext, layParentER, e.message.toString(), Constants.Types.ERROR)
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    val jsonRes = JSONObject(response.body()!!.string())
                    Log.e("RES", jsonRes.toString())

                    if(jsonRes.getInt("Error") == 0){
                        val j = jsonRes.getJSONArray("Datos").getJSONObject(0)
                        snackbar(applicationContext, layParentER, jsonRes.getString("Mensaje"), Constants.Types.SUCCESS)

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
                        snackbar(applicationContext, layParentER, jsonRes.getString("Mensaje"), Constants.Types.ERROR)
                    }
                }
            }
        })
    }

    private fun fillData(){
        Picasso.get().load("${Constants.URL_IMAGES_STATUS}${cStatus.Fotografia1}")
            .placeholder(resources.getDrawable(R.drawable.ic_loading))
            .memoryPolicy(MemoryPolicy.NO_CACHE )
            .networkPolicy(NetworkPolicy.NO_CACHE)
            .error(R.drawable.ic_box).into(imgPhoto1ER)

        Picasso.get().load("${Constants.URL_IMAGES_STATUS}${cStatus.Fotografia2}")
            .placeholder(resources.getDrawable(R.drawable.ic_loading))
            .memoryPolicy(MemoryPolicy.NO_CACHE )
            .networkPolicy(NetworkPolicy.NO_CACHE)
            .error(R.drawable.ic_box).into(imgPhoto2ER)

        Picasso.get().load("${Constants.URL_IMAGES_STATUS}${cStatus.Fotografia3}")
            .placeholder(resources.getDrawable(R.drawable.ic_loading))
            .memoryPolicy(MemoryPolicy.NO_CACHE )
            .networkPolicy(NetworkPolicy.NO_CACHE)
            .error(R.drawable.ic_box).into(imgPhoto3ER)

        //etAltaE.setText(cStatus.FechaAlta)
        //etModifER.setText(cStatus.FechaUltimaModif)
        etObservacionesER.setText(cStatus.Observaciones)

        fillSpinners()
    }

    private fun fillSpinners(){
        // value is in first spinner
        for(i in 0 until listColaboradores1.size){
            if(cStatus.IdColaborador1 == listColaboradores1[i].Id)
                spinnerRegistraER.setSelection(i+1)
        }

        spinnerRegistraER.isEnabled = false

        // value is in second spinner
        for(i in 0 until listColaboradores2.size){
            if(cStatus.IdColaborador2 == listColaboradores2[i].Id)
                spinnerAtiendeER.setSelection(i+1)
        }

        // value is in third spinner
        for(i in statusList.indices){
            if(cStatus.StatusIncidencia.toInt() == i){
                spinnerStatusER.setSelection(i)
                break
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("LocalVariableName", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun sendDataToServer(){
        progress.show()
        progress.setCancelable(false)
        titleProgress.text = "Enviando información"

        val client = OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS).build()

        val bitmapPhoto1 = imgPhoto1ER.drawable.toBitmap()
        val stream1 = ByteArrayOutputStream()
        bitmapPhoto1.compress(Bitmap.CompressFormat.JPEG, 50, stream1)
        val byteArray1 = stream1.toByteArray()

        val bitmapPhoto2 = imgPhoto2ER.drawable.toBitmap()
        val stream2 = ByteArrayOutputStream()
        bitmapPhoto2.compress(Bitmap.CompressFormat.JPEG, 50, stream2)
        val byteArray2 = stream2.toByteArray()

        val bitmapPhoto3 = imgPhoto3ER.drawable.toBitmap()
        val stream3 = ByteArrayOutputStream()
        bitmapPhoto3.compress(Bitmap.CompressFormat.JPEG, 50, stream3)
        val byteArray3 = stream3.toByteArray()

        val MEDIA_TYPE_JPG = MediaType.parse("image/jpeg")

        // get id colaborador according spinners
        var idColaborador1 = ""
        if(spinnerAtiendeER.selectedItemPosition > 0)
            idColaborador1 = listColaboradores1[spinnerAtiendeER.selectedItemPosition-1].Id

        var idColaborador2 = ""
        if(spinnerRegistraER.selectedItemPosition > 0)
            idColaborador2 = listColaboradores2[spinnerRegistraER.selectedItemPosition-1].Id

        //val requestBody : RequestBody
        val requestBody : RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("WebService","GuardaStatusIncidencia")
            .addFormDataPart("Id", idStatus) // empty if new | else -> ID
            .addFormDataPart("Status", "1")
            .addFormDataPart("Observaciones", etObservacionesER.text.toString())
            .addFormDataPart("IdIncidencia", incidenciaId)
            .addFormDataPart("IdColaborador1", idColaborador2)
            .addFormDataPart("IdColaborador2", idColaborador1)
            .addFormDataPart("StatusIncidencia", spinnerStatusER.selectedItemPosition.toString())
            .addFormDataPart("Fotografia1", "image1.jpeg", RequestBody.create(MEDIA_TYPE_JPG, byteArray1))
            .addFormDataPart("Fotografia2", "image2.jpeg", RequestBody.create(MEDIA_TYPE_JPG, byteArray2))
            .addFormDataPart("Fotografia3", "image3.jpeg", RequestBody.create(MEDIA_TYPE_JPG, byteArray3))
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

                            snackbar(applicationContext, layParentER, jsonRes.getString("Mensaje"), Constants.Types.ERROR)
                            Constants.updateRefreshIncidencias(applicationContext, true)
                            Constants.updateRefreshStatus(applicationContext, true)

                            Handler().postDelayed({
                                this@EditStatusActivity.finish()
                            }, 2000)
                        } else
                            snackbar(applicationContext, layParentER, jsonRes.getString("Mensaje"), Constants.Types.ERROR)

                        progress.dismiss()

                    } catch (e: Error){
                        progress.dismiss()
                        snackbar(applicationContext, layParentER, e.message.toString(), Constants.Types.ERROR)
                    }
                }
            }
        })
    }

    private fun showPictureDialog(photo: Int) {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Obtener fotografía")

        val pictureDialogItems = arrayOf("Seleccionar foto de la galería", "Capturar foto con la cámara")
        pictureDialog.setItems(pictureDialogItems) { _, which ->
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
            1->{ imgPhoto1ER.drawable.toBitmap() }
            2->{ imgPhoto2ER.drawable.toBitmap() }
            else->{ imgPhoto3ER.drawable.toBitmap() }
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
                        1->{imgPhoto1ER.setImageBitmap(bitmap)}
                        2->{imgPhoto2ER.setImageBitmap(bitmap)}
                        else->{imgPhoto3ER.setImageBitmap(bitmap)}
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
                        imgPhoto1ER.setImageURI(uriImage) }
                }
                2->{
                    val file = File(mCameraFileName2)
                    if (file.exists()) {
                        val uriImage = Uri.fromFile(File(mCameraFileName2))
                        imgPhoto2ER.setImageURI(uriImage) }
                }
                else->{
                    val file = File(mCameraFileName3)
                    if (file.exists()) {
                        val uriImage = Uri.fromFile(File(mCameraFileName3))
                        imgPhoto3ER.setImageURI(uriImage) }
                }
            }
        }
    }

}
