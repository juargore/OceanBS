@file:Suppress("SpellCheckingInspection", "DEPRECATION", "PrivatePropertyName")

package com.ocean.oceanbs.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toBitmap
import com.ocean.oceanbs.Constants
import com.ocean.oceanbs.Constants.snackbar
import com.ocean.oceanbs.R
import com.ocean.oceanbs.models.GenericObj
import com.ocean.oceanbs.models.OWNER
import com.ocean.oceanbs.models.Status
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class EditStatusActivity : BaseActivity() {

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

    private var defaultImage1 = true
    private var defaultImage2 = true
    private var defaultImage3 = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_status)
        intent.extras?.let {
            idStatus = it.getString("idStatus").toString()
            incidenciaId = it.getString("incidenciaId").toString()
            persona = it.getString("persona").toString()
            desarrollo = it.getString("desarrollo").toString()
            codigoUnidad = it.getString("codigoUnidad").toString()
        }
        initComponents()
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun initComponents() {
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

        if (Constants.internetConnected(this)) {
            setListeners()
            getDataForSpinners()
        } else
            Constants.showPopUpNoInternet(this)
    }

    private fun setListeners() {
        imgBackStatusER.setOnClickListener { this.finish() }

        cardPhoto1ER.setOnClickListener { showPictureDialog(1); SELECTED = 1 }
        cardPhoto2ER.setOnClickListener { showPictureDialog(2); SELECTED = 2 }
        cardPhoto3ER.setOnClickListener { showPictureDialog(3); SELECTED = 3 }

        txtShowPhoto1ER.setOnClickListener { showPopPhoto(1) }
        txtShowPhoto2ER.setOnClickListener { showPopPhoto(2) }
        txtShowPhoto3ER.setOnClickListener { showPopPhoto(3) }

        btnUpdateStatusER.setOnClickListener {
            when {
                spinnerRegistraER.selectedItemPosition == 0 -> {
                    snackbar(applicationContext, layParentER, "El colaborador que registra es obligatorio", Constants.Types.ERROR)
                }
                spinnerAtiendeER.selectedItemPosition == 0 -> {
                    snackbar(applicationContext, layParentER, "El colaborador que atiende es obligatorio", Constants.Types.ERROR)
                }
                else -> {
                    sendDataToServer()
                }
            }
        }
    }

    private fun getDataForSpinners() {

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

                        if (jsonRes.getInt("Error") == 0) {

                            val arrayColab = jsonRes.getJSONArray("Datos")

                            for (i in 0 until arrayColab.length()) {
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

                    }catch (e: Error) {
                        snackbar(applicationContext, layParentER, e.message.toString(), Constants.Types.ERROR)
                    }
                }
            }
        })

    }

    private fun setUpSpinners() {

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

    private fun getStatusIncidencia() {
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
                    val jsonRes = JSONObject(response.body!!.string())
                    Log.e("RES", jsonRes.toString())

                    if (jsonRes.getInt("Error") == 0) {
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun fillData() {
        defaultImage1 = cStatus.Fotografia1.replace(" ","") == ""
        defaultImage2 = cStatus.Fotografia2.replace(" ","") == ""
        defaultImage3 = cStatus.Fotografia3.replace(" ","") == ""

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

        etObservacionesER.setText(cStatus.Observaciones)
        fillSpinners()
    }

    private fun fillSpinners() {
        // value is in first spinner
        for (i in 0 until listColaboradores1.size) {
            if (cStatus.IdColaborador1 == listColaboradores1[i].Id)
                spinnerRegistraER.setSelection(i+1)
        }

        spinnerRegistraER.isEnabled = false

        // value is in second spinner
        for (i in 0 until listColaboradores2.size) {
            if (cStatus.IdColaborador2 == listColaboradores2[i].Id)
                spinnerAtiendeER.setSelection(i+1)
        }

        // value is in third spinner
        for (i in statusList.indices) {
            if (cStatus.StatusIncidencia.toInt() == i) {
                spinnerStatusER.setSelection(i)
                break
            }
        }

        if (Constants.getTipoUsuario(this) == OWNER)
            disableFieldsIfPropietario()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun disableFieldsIfPropietario() {
        cardPhoto1ER.isEnabled = false
        cardPhoto2ER.isEnabled = false
        cardPhoto3ER.isEnabled = false

        spinnerStatusER.isEnabled = false
        spinnerStatusER.background = resources.getDrawable(R.drawable.rectangle_round_corner_gray_fill)
        spinnerAtiendeER.isEnabled = false
        spinnerAtiendeER.background = resources.getDrawable(R.drawable.rectangle_round_corner_gray_fill)
        etObservacionesER.isEnabled = false
        etObservacionesER.background = resources.getDrawable(R.drawable.rectangle_round_corner_gray_fill)
        etObservacionesER.setTextColor(resources.getColor(R.color.colorBlack))

        btnUpdateStatusER.visibility = View.GONE
    }


    @SuppressLint("SetTextI18n")
    @Suppress("LocalVariableName", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun sendDataToServer() {
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

        val MEDIA_TYPE_JPG = "image/jpeg".toMediaTypeOrNull()

        // get id colaborador according spinners
        var idColaborador1 = ""
        if (spinnerAtiendeER.selectedItemPosition > 0)
            idColaborador1 = listColaboradores1[spinnerAtiendeER.selectedItemPosition-1].Id

        var idColaborador2 = ""
        if (spinnerRegistraER.selectedItemPosition > 0)
            idColaborador2 = listColaboradores2[spinnerRegistraER.selectedItemPosition-1].Id

        //val requestBody : RequestBody
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("WebService","GuardaStatusIncidencia")
            .addFormDataPart("Id", idStatus) // empty if new | else -> ID
            .addFormDataPart("Status", "1")
            .addFormDataPart("Observaciones", etObservacionesER.text.toString())
            .addFormDataPart("IdIncidencia", incidenciaId)
            .addFormDataPart("IdColaborador1", idColaborador2)
            .addFormDataPart("IdColaborador2", idColaborador1)
            .addFormDataPart("StatusIncidencia", spinnerStatusER.selectedItemPosition.toString())

        if (defaultImage1)
            requestBody.addFormDataPart("Fotografia1", "")
        else
            requestBody.addFormDataPart("Fotografia1", "image1.jpeg", RequestBody.create(MEDIA_TYPE_JPG, byteArray1))

        if (defaultImage2)
            requestBody.addFormDataPart("Fotografia2", "")
        else
            requestBody.addFormDataPart("Fotografia2", "image2.jpeg", RequestBody.create(MEDIA_TYPE_JPG, byteArray2))

        if (defaultImage3)
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

                        if (jsonRes.getInt("Error") == 0) {

                            snackbar(applicationContext, layParentER, jsonRes.getString("Mensaje"), Constants.Types.SUCCESS)
                            Constants.updateRefreshIncidencias(applicationContext, true)
                            Constants.updateRefreshStatus(applicationContext, true)

                            Handler().postDelayed({
                                this@EditStatusActivity.finish()
                            }, 2000)
                        } else
                            snackbar(applicationContext, layParentER, jsonRes.getString("Mensaje"), Constants.Types.ERROR)

                        progress.dismiss()

                    } catch (e: Error) {
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

    private fun showPopPhoto(photo: Int) {
        val dialog = Dialog(this, R.style.FullDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.pop_image)

        val image = dialog.findViewById<ImageView>(R.id.imgCenter)
        val b: Bitmap = when(photo) {
            1 -> imgPhoto1ER.drawable.toBitmap()
            2 -> imgPhoto2ER.drawable.toBitmap()
            else -> imgPhoto3ER.drawable.toBitmap()
        }
        image.setImageBitmap(b)
        dialog.show()
    }

    private fun choosePhotoFromGallary() {
        startActivityForResult(
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI), GALLERY
        )
    }

    @SuppressLint("SdCardPath")
    private fun takePhotoFromCamera(photo: Int) {
        val date = Date()
        val df = SimpleDateFormat("-mm-ss", Locale.getDefault())
        val outUri: Uri
        val intent = Intent()
        intent.action = MediaStore.ACTION_IMAGE_CAPTURE

        when (photo) {
            1 -> {
                val newPicFile = df.format(date) + ".jpg"
                val outPath = "/sdcard/$newPicFile"
                val outfile = File(outPath)

                mCameraFileName1 = outfile.toString()
                outUri = Uri.fromFile(outfile)
            }
            2 -> {
                val newPicFile = df.format(date) + ".jpg"
                val outPath = "/sdcard/$newPicFile"
                val outfile = File(outPath)

                mCameraFileName2 = outfile.toString()
                outUri = Uri.fromFile(outfile)}
            else -> {
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

    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    when(SELECTED) {
                        1 -> { imgPhoto1ER.setImageBitmap(bitmap); defaultImage1 = false }
                        2 -> { imgPhoto2ER.setImageBitmap(bitmap); defaultImage2 = false }
                        else -> { imgPhoto3ER.setImageBitmap(bitmap); defaultImage3 = false }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == CAMERA) {
            when(SELECTED) {
                1 -> {
                    val file = File(mCameraFileName1)
                    if (file.exists()) {
                        val uriImage = Uri.fromFile(File(mCameraFileName1))
                        imgPhoto1ER.setImageURI(uriImage)
                        defaultImage1 = false}
                }
                2 -> {
                    val file = File(mCameraFileName2)
                    if (file.exists()) {
                        val uriImage = Uri.fromFile(File(mCameraFileName2))
                        imgPhoto2ER.setImageURI(uriImage)
                        defaultImage2 = false}
                }
                else -> {
                    val file = File(mCameraFileName3)
                    if (file.exists()) {
                        val uriImage = Uri.fromFile(File(mCameraFileName3))
                        imgPhoto3ER.setImageURI(uriImage)
                        defaultImage3 = false }
                }
            }
        }
    }
}
