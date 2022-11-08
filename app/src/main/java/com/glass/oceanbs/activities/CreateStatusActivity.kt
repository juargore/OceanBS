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
import com.glass.oceanbs.database.TableUser
import com.glass.oceanbs.models.GenericObj
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
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
    private var persona = ""
    private var desarrollo = ""
    private var codigoUnidad = ""

    private var defaultImage1 = true
    private var defaultImage2 = true
    private var defaultImage3 = true

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
        persona = extras.getString("persona").toString()
        desarrollo = extras.getString("desarrollo").toString()
        codigoUnidad = extras.getString("codigoUnidad").toString()

        initComponents()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
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

        txtTitleR.text = "$desarrollo $codigoUnidad"
        txtSubTitleR.text = persona

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

        if(Constants.internetConnected(this)){
            setListeners()
            getDataForSpinners()
        } else
            Constants.showPopUpNoInternet(this)

        //setListeners()
        //getDataForSpinners()
    }

    private fun setListeners(){
        imgBackStatus.setOnClickListener { this.finish() }

        cardPhoto1.setOnClickListener { showPictureDialog(1); SELECTED = 1 }
        cardPhoto2.setOnClickListener { showPictureDialog(2); SELECTED = 2 }
        cardPhoto3.setOnClickListener { showPictureDialog(3); SELECTED = 3 }

        txtShowPhoto1.setOnClickListener { showPopPhoto(1) }
        txtShowPhoto2.setOnClickListener { showPopPhoto(2) }
        txtShowPhoto3.setOnClickListener { showPopPhoto(3) }

        btnSaveStatusR.setOnClickListener {
            when {
                spinnerRegistraR.selectedItemPosition == 0 -> {
                    snackbar(applicationContext, layParentR, "El colaborador que registra es obligatorio", Constants.Types.ERROR)
                }
                spinnerAtiendeR.selectedItemPosition == 0 -> {
                    snackbar(applicationContext, layParentR, "El colaborador que atiende es obligatorio", Constants.Types.ERROR)
                }
                else -> {
                    sendDataToServer()
                }
            }
        }
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
                        val jsonRes = JSONObject(response.body!!.string())

                        if(jsonRes.getInt("Error") == 0){

                            val arrayColab = jsonRes.getJSONArray("Datos")
                            snackbar(applicationContext, layParentR, jsonRes.getString("Mensaje"), Constants.Types.SUCCESS)

                            for (i in 0 until arrayColab.length()){
                                val j : JSONObject = arrayColab.getJSONObject(i)

                                listColaboradores1.add(GenericObj(
                                        j.getString("Id"),
                                        j.getString("Codigo"),
                                        "${j.getString("Nombre")} ${j.getString("ApellidoP")} ${j.getString("ApellidoM")}"))
                            }

                            listColaboradores2.addAll(listColaboradores1)
                            setUpSpinners()
                        }

                    }catch (e: Error){
                        snackbar(applicationContext, layParentR, e.message.toString(), Constants.Types.ERROR)
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

        val user = TableUser(this).getCurrentUserById(Constants.getUserId(this), Constants.getTipoUsuario(this))

        val userId: String = if(user.tipoUsuario == 1)
            user.idPropietario
        else
            user.idColaborador

        for(i in 0 until listColaboradores1.size){
            if(userId == listColaboradores1[i].Id)
                spinnerRegistraR.setSelection(i+1)
        }

        spinnerRegistraR.isEnabled = false
    }

    @SuppressLint("SetTextI18n")
    @Suppress("LocalVariableName", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun sendDataToServer(){
        progress.show()
        progress.setCancelable(false)
        titleProgress.text = "Enviando información"

        val client = OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS).build()

        val bitmapPhoto1 = imgPhoto1.drawable.toBitmap()
        val stream1 = ByteArrayOutputStream()
        bitmapPhoto1.compress(Bitmap.CompressFormat.JPEG, 50, stream1)
        val byteArray1 = stream1.toByteArray()

        val bitmapPhoto2 = imgPhoto2.drawable.toBitmap()
        val stream2 = ByteArrayOutputStream()
        bitmapPhoto2.compress(Bitmap.CompressFormat.JPEG, 50, stream2)
        val byteArray2 = stream2.toByteArray()

        val bitmapPhoto3 = imgPhoto3.drawable.toBitmap()
        val stream3 = ByteArrayOutputStream()
        bitmapPhoto3.compress(Bitmap.CompressFormat.JPEG, 50, stream3)
        val byteArray3 = stream3.toByteArray()

        val MEDIA_TYPE_JPG = "image/jpeg".toMediaTypeOrNull()

        // get id colaborador according spinners
        var idColaborador1 = ""
        if(spinnerAtiendeR.selectedItemPosition > 0)
            idColaborador1 = listColaboradores1[spinnerAtiendeR.selectedItemPosition-1].Id

        var idColaborador2 = ""
        if(spinnerRegistraR.selectedItemPosition > 0)
            idColaborador2 = listColaboradores2[spinnerRegistraR.selectedItemPosition-1].Id

        //val requestBody : RequestBody
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("WebService","GuardaStatusIncidencia")
            .addFormDataPart("Id", "") // empty if new | else -> ID
            .addFormDataPart("Status", "1")
            .addFormDataPart("Observaciones", etObservacionesR.text.toString())
            .addFormDataPart("IdIncidencia", incidenciaId)
            .addFormDataPart("IdColaborador1", idColaborador2)
            .addFormDataPart("IdColaborador2", idColaborador1)
            .addFormDataPart("StatusIncidencia", spinnerStatusR.selectedItemPosition.toString())

        if(defaultImage1)
            requestBody.addFormDataPart("Fotografia1", "")
        else
            requestBody.addFormDataPart("Fotografia1", "image1.jpeg", RequestBody.create(MEDIA_TYPE_JPG, byteArray1))

        if(defaultImage2)
            requestBody.addFormDataPart("Fotografia2", "")
        else
            requestBody.addFormDataPart("Fotografia2", "image2.jpeg", RequestBody.create(MEDIA_TYPE_JPG, byteArray2))

        if(defaultImage3)
            requestBody.addFormDataPart("Fotografia3", "")
        else
            requestBody.addFormDataPart("Fotografia3", "image3.jpeg", RequestBody.create(MEDIA_TYPE_JPG, byteArray3))


        val request = Request.Builder()
            .url(Constants.URL_INCIDENCIAS)
            .post(requestBody.build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body!!.string())
                        Log.e("RES",  jsonRes.toString())

                        if(jsonRes.getInt("Error") == 0){

                            snackbar(applicationContext, layParentR, jsonRes.getString("Mensaje"), Constants.Types.SUCCESS)
                            Constants.updateRefreshIncidencias(applicationContext, true)
                            Constants.updateRefreshStatus(applicationContext, true)

                            Handler().postDelayed({
                                this@CreateStatusActivity.finish()
                            }, 2000)

                        } else
                            snackbar(applicationContext, layParentR, jsonRes.getString("Mensaje"), Constants.Types.ERROR)

                        progress.dismiss()

                    } catch (e: Error){
                        progress.dismiss()
                        snackbar(applicationContext, layParentR, e.message.toString(), Constants.Types.ERROR)
                    }
                }
            }
        })
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

    @Deprecated("Deprecated in Java")
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
                        1->{imgPhoto1.setImageBitmap(bitmap); defaultImage1 = false}
                        2->{imgPhoto2.setImageBitmap(bitmap); defaultImage2 = false}
                        else->{imgPhoto3.setImageBitmap(bitmap); defaultImage3 = false}
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
                        imgPhoto1.setImageURI(uriImage); defaultImage1 = false }
                }
                2->{
                    val file = File(mCameraFileName2)
                    if (file.exists()) {
                        val uriImage = Uri.fromFile(File(mCameraFileName2))
                        imgPhoto2.setImageURI(uriImage); defaultImage2 = false }
                }
                else->{
                    val file = File(mCameraFileName3)
                    if (file.exists()) {
                        val uriImage = Uri.fromFile(File(mCameraFileName3))
                        imgPhoto3.setImageURI(uriImage); defaultImage3 = false }
                }
            }
        }
    }

}
