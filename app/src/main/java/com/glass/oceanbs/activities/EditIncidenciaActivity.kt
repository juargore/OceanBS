@file:Suppress("SpellCheckingInspection", "PrivatePropertyName", "DEPRECATION")

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
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.snackbar
import com.glass.oceanbs.R
import com.glass.oceanbs.adapters.BitacoraStatusAdapter
import com.glass.oceanbs.database.TableUser
import com.glass.oceanbs.extensions.alert
import com.glass.oceanbs.extensions.hide
import com.glass.oceanbs.models.*
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
import kotlin.collections.ArrayList

class EditIncidenciaActivity : BaseActivity() {

    private lateinit var progress : AlertDialog
    private lateinit var titleProgress: TextView
    private lateinit var layParentEdIn: LinearLayout
    private lateinit var txtTitleDesarrolloEdIn: TextView
    private lateinit var txtSubTitleDesarrolloEdIn: TextView
    private lateinit var imgBackRegistroIncidenciaEdIn: ImageView

    private lateinit var txtShowPhotoEd: TextView
    private lateinit var cardPhotoEd: CardView
    private lateinit var imgPhotoEd: ImageView

    private lateinit var spinner3mEd: Spinner
    private lateinit var spinner6mEd: Spinner
    private lateinit var spinner1aEd: Spinner
    private lateinit var etFechaAltaEdI: EditText
    private lateinit var etFechaModifEdI: EditText

    private lateinit var etFallaReportadaEdI: EditText
    private lateinit var etFallaRealEdI: EditText
    private lateinit var etObservacionesEdI: EditText
    private lateinit var btnUpdateIncidenciaEdI: Button
    private lateinit var txtBitacoraStatusEd: TextView

    private var listSpinner3m: ArrayList<GenericObj> = ArrayList()
    private var listSpinner6m: ArrayList<GenericObj> = ArrayList()
    private var listSpinner1a: ArrayList<GenericObj> = ArrayList()
    private var listRegistroStatus: ArrayList<ShortStatus> = ArrayList()

    private val GALLERY = 1
    private val CAMERA = 2

    private lateinit var cIncidencia: Incidencia

    private var mCameraFileName = ""
    private var idSolicitud = ""
    private var idIncidencia = ""
    private var persona = ""
    private var desarrollo = ""
    private var codigoUnidad = ""
    private var defaultImage = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_incidencia)

        supportActionBar?.hide()

        Constants.checkPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        val extras = intent.extras
        idSolicitud = extras!!.getString("solicitudId").toString()
        idIncidencia = extras.getString("incidenciaId").toString()
        persona = extras.getString("persona").toString()
        desarrollo = extras.getString("desarrollo").toString()
        codigoUnidad = extras.getString("codigoUnidad").toString()

        initComponents()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun initComponents(){
        layParentEdIn = findViewById(R.id.layParentEdIn)
        txtTitleDesarrolloEdIn = findViewById(R.id.txtTitleDesarrolloEdIn)
        txtSubTitleDesarrolloEdIn = findViewById(R.id.txtSubTitleDesarrolloEdIn)
        imgBackRegistroIncidenciaEdIn = findViewById(R.id.imgBackRegistroIncidenciaEdIn)
        txtBitacoraStatusEd = findViewById(R.id.txtBitacoraStatusEd)

        txtTitleDesarrolloEdIn.text = "$desarrollo $codigoUnidad"
        txtSubTitleDesarrolloEdIn.text = persona

        spinner3mEd = findViewById(R.id.spinner3mEd)
        spinner6mEd = findViewById(R.id.spinner6mEd)
        spinner1aEd = findViewById(R.id.spinner1aEd)
        etFechaAltaEdI = findViewById(R.id.etFechaAltaEdI)
        etFechaModifEdI = findViewById(R.id.etFechaModifEdI)

        etFallaReportadaEdI = findViewById(R.id.etFallaReportadaEdI)
        etFallaRealEdI = findViewById(R.id.etFallaRealEdI)
        etObservacionesEdI = findViewById(R.id.etObservacionesEdI)
        btnUpdateIncidenciaEdI = findViewById(R.id.btnUpdateIncidenciaEdI)

        txtShowPhotoEd = findViewById(R.id.txtShowPhotoEd)
        cardPhotoEd = findViewById(R.id.cardPhotoEd)
        imgPhotoEd = findViewById(R.id.imgPhotoEd)

        // set up progress dialg
        val builder = AlertDialog.Builder(this, R.style.HalfDialogTheme)
        val inflat = this.layoutInflater
        val dialogView = inflat.inflate(R.layout.progress, null)

        titleProgress = dialogView.findViewById(R.id.loading_title)

        builder.setView(dialogView)
        progress = builder.create()
        progress.setCancelable(false)

        imgBackRegistroIncidenciaEdIn.setOnClickListener { this.finish() }
        txtShowPhotoEd.setOnClickListener { showPopPhoto() }
        cardPhotoEd.setOnClickListener { showPictureDialog() }
        txtBitacoraStatusEd.setOnClickListener { getStatus() }

        btnUpdateIncidenciaEdI.setOnClickListener {
            if(validateFullFields())
                sendDataToServer()
        }

        if(Constants.internetConnected(this)){
            getDataForSpinners(1)
            getPhotoIncidencia()
            getIncidencia()
        } else
            Constants.showPopUpNoInternet(this)
    }

    private fun getPhotoIncidencia(){
        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","GetFotografiaIdIncidenciaNumStatus")
            .add("IdIncidencia", idIncidencia)
            .add("StatusIncidencia", "1")
            .add("Campo", "Fotografia1")
            .build()

        val request = Request.Builder().url(Constants.URL_STATUS).post(builder).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progress.dismiss()
                }
            }
            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jRes = JSONObject(response.body!!.string())
                        Log.e("FOTO", jRes.toString())

                        if(jRes.getInt("Error") == 0){
                            defaultImage = jRes.getString("Fotografia").replace(" ","") == ""

                            Picasso.get().load("${Constants.URL_IMAGES_STATUS}${jRes.getString("Fotografia")}")
                                .placeholder(resources.getDrawable(R.drawable.ic_loading))
                                .memoryPolicy(MemoryPolicy.NO_CACHE )
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .error(R.drawable.ic_box).into(imgPhotoEd)
                        }
                    }catch (_: Error){

                    }
                }
            }
        })
    }

    private fun getDataForSpinners(value: Int){

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","ConsultaValoresClasificacionIdClasificacion")
            .add("IdClasificacion", value.toString())
            .build()

        val request = Request.Builder().url(Constants.URL_CLASIFICACION).post(builder).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progress.dismiss()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body!!.string())

                        if(jsonRes.getInt("Error") == 0){
                            val arrayClasif = jsonRes.getJSONArray("Datos")

                            for(i in 0 until arrayClasif.length()){
                                val j : JSONObject = arrayClasif.getJSONObject(i)

                                when(value){
                                    1->{ listSpinner3m.add(GenericObj(
                                        j.getString("Id"),
                                        j.getString("Codigo"),
                                        j.getString("Nombre")))
                                    }
                                    2->{ listSpinner6m.add(GenericObj(
                                        j.getString("Id"),
                                        j.getString("Codigo"),
                                        j.getString("Nombre")))
                                    }
                                    3->{ listSpinner1a.add(GenericObj(
                                        j.getString("Id"),
                                        j.getString("Codigo"),
                                        j.getString("Nombre")))
                                    }
                                }
                            }
                        }

                        when(value){
                            1-> getDataForSpinners(2)
                            2-> getDataForSpinners(3)
                            3 -> setUpSpinners()
                        }

                    } catch (_: Error){}
                }
            }
        })
    }

    private fun getIncidencia(){
        progress.show()

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","ConsultaIncidenciaIdApp")
            .add("Id", idIncidencia)
            .build()

        val request = Request.Builder().url(Constants.URL_INCIDENCIAS).post(builder).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progress.dismiss()
                    snackbar(applicationContext, layParentEdIn, e.message.toString(), Constants.Types.ERROR)
                }
            }
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body!!.string())
                        Log.e("INC", jsonRes.toString())

                        if(jsonRes.getInt("Error") == 0){
                            val j = jsonRes.getJSONArray("Datos").getJSONObject(0)
                            snackbar(applicationContext, layParentEdIn, jsonRes.getString("Mensaje"), Constants.Types.SUCCESS)

                            cIncidencia = Incidencia(
                                j.getString("Id"),
                                j.getString("FechaAlta"),
                                j.getString("FechaUltimaModif"),
                                j.getString("Observaciones"),
                                j.getString("IdValorClasif1"),
                                j.getString("IdValorClasif2"),
                                j.getString("IdValorClasif3"),
                                j.getString("FallaReportada"),
                                j.getString("FallaReal"))

                            fillData()
                        } else
                            snackbar(applicationContext, layParentEdIn, jsonRes.getString("Mensaje"), Constants.Types.ERROR)

                    }catch (e: Error){
                        snackbar(applicationContext, layParentEdIn, e.message.toString(), Constants.Types.ERROR)
                    }
                    progress.dismiss()
                }
            }
        })
    }

    private fun fillData(){
        //etFechaAltaEdI.setText(cIncidencia.FechaAlta)
        //etFechaModifEdI.setText(cIncidencia.FechaUltimaModif)
        //etFallaRealEdI.setText(cIncidencia.FallaReal)
        etFallaReportadaEdI.setText(cIncidencia.FallaReportada)
        etObservacionesEdI.setText(cIncidencia.Observaciones)

        if (Constants.getTipoUsuario(this) == OWNER) {

            // propietario -> disable all fields
            imgPhotoEd.isEnabled = false
            imgPhotoEd.isClickable = false

            spinner3mEd.isEnabled = false
            spinner3mEd.isClickable = false

            spinner6mEd.isEnabled = false
            spinner6mEd.isClickable = false

            spinner1aEd.isEnabled = false
            spinner1aEd.isClickable = false

            etFallaReportadaEdI.isEnabled = false
            etFallaReportadaEdI.isClickable = false

            etObservacionesEdI.isEnabled = false
            etObservacionesEdI.isClickable = false

            btnUpdateIncidenciaEdI.isEnabled = false
            btnUpdateIncidenciaEdI.isClickable = false
        }
    }

    private fun setUpSpinners(){

        val list3m: ArrayList<String> = ArrayList()
        list3m.add(0, "Seleccionar")

        for (i in listSpinner3m)
            list3m.add(i.Nombre)

        val adapter3m = ArrayAdapter(this, R.layout.spinner_text, list3m)
        spinner3mEd.adapter = adapter3m

        spinner3mEd.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if(pos > 0){
                    spinner6mEd.setSelection(0)
                    spinner1aEd.setSelection(0)
                }
            }
        }

        val list6m: ArrayList<String> = ArrayList()
        list6m.add(0, "Seleccionar")

        for (i in listSpinner6m)
            list6m.add(i.Nombre)

        val adapter6m = ArrayAdapter(this, R.layout.spinner_text, list6m)
        spinner6mEd.adapter = adapter6m

        spinner6mEd.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if(pos > 0){
                    spinner3mEd.setSelection(0)
                    spinner1aEd.setSelection(0)
                }
            }
        }


        val list1a: ArrayList<String> = ArrayList()
        list1a.add(0, "Seleccionar")

        for (i in listSpinner1a)
            list1a.add(i.Nombre)

        val adapter1a = ArrayAdapter(this, R.layout.spinner_text, list1a)
        spinner1aEd.adapter = adapter1a

        spinner1aEd.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if(pos > 0){
                    spinner3mEd.setSelection(0)
                    spinner6mEd.setSelection(0)
                }
            }
        }

        fillSpinners()
    }

    private fun fillSpinners(){

        when {
            cIncidencia.IdValorClasif1.toInt() != 0 -> {

                // value is in first spinner
                for(i in 0 until listSpinner3m.size){
                    if(cIncidencia.IdValorClasif1 == listSpinner3m[i].Id)
                        spinner3mEd.setSelection(i+1)
                }
            }
            cIncidencia.IdValorClasif2.toInt() != 0 -> {

                // value is in second spinner
                for(i in 0 until listSpinner6m.size){
                    if(cIncidencia.IdValorClasif2 == listSpinner6m[i].Id)
                        spinner6mEd.setSelection(i+1)
                }
            }
            cIncidencia.IdValorClasif3.toInt() != 0 -> {

                // value is in third spinner
                for(i in 0 until listSpinner1a.size){
                    if(cIncidencia.IdValorClasif3 == listSpinner1a[i].Id)
                        spinner1aEd.setSelection(i+1)
                }
            }
        }

    }

    private fun validateFullFields(): Boolean {
        val msg = "Este campo no puede estar vacío"
        return when {
            TextUtils.isEmpty(etFallaReportadaEdI.text.toString()) -> {
                etFallaReportadaEdI.error = msg; false }
            else -> true
        }
    }

    // send values to server to create a new incidencia
    @SuppressLint("SetTextI18n")
    private fun sendDataToServer(){
        progress.show()
        titleProgress.text = "Enviando información"

        val user = TableUser(this).getCurrentUserById(Constants.getUserId(this), Constants.getTipoUsuario(this))
        val userId= user.idColaborador

        val client = OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS).build()

        var valor3m = ""
        if(spinner3mEd.selectedItemPosition > 0)
            valor3m = listSpinner3m[spinner3mEd.selectedItemPosition-1].Id

        var valor6m = ""
        if(spinner6mEd.selectedItemPosition > 0)
            valor6m = listSpinner6m[spinner6mEd.selectedItemPosition-1].Id

        var valor1a = ""
        if(spinner1aEd.selectedItemPosition > 0)
            valor1a = listSpinner1a[spinner1aEd.selectedItemPosition-1].Id

        val bitmapPhoto1 = imgPhotoEd.drawable.toBitmap()
        val stream1 = ByteArrayOutputStream()
        bitmapPhoto1.compress(Bitmap.CompressFormat.JPEG, 50, stream1)
        val byteArray1 = stream1.toByteArray()

        val MEDIA_TYPE_JPG = "image/jpeg".toMediaTypeOrNull()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("WebService","GuardaIncidencia")
            .addFormDataPart("Id", cIncidencia.Id) // empty if new | else -> ID
            .addFormDataPart("Status", "1")
            .addFormDataPart("Observaciones", etObservacionesEdI.text.toString())
            .addFormDataPart("IdColaboradorAlta", userId)
            .addFormDataPart("IdSolicitudAG", idSolicitud)
            .addFormDataPart("IdValorClasif1", valor3m)
            .addFormDataPart("IdValorClasif2", valor6m)
            .addFormDataPart("IdValorClasif3", valor1a)
            .addFormDataPart("FallaReportada", etFallaReportadaEdI.text.toString())
            .addFormDataPart("FallaReal", etFallaRealEdI.text.toString())

        if(defaultImage){
            requestBody.addFormDataPart("Fotografia1", "")
            Log.e("--","No se envio la imagen")
        } else {
            requestBody.addFormDataPart("Fotografia1", "image1.jpeg", RequestBody.create(MEDIA_TYPE_JPG, byteArray1))
            Log.e("--","Si se envio la imagen")
        }


        val request = Request.Builder().url(Constants.URL_INCIDENCIAS).post(requestBody.build()).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body!!.string())

                        if(jsonRes.getInt("Error") == 0){
                            snackbar(applicationContext, layParentEdIn, jsonRes.getString("Mensaje"), Constants.Types.SUCCESS)
                            Constants.updateRefreshIncidencias(applicationContext, true)
                            getStatus()
                        } else
                            snackbar(applicationContext, layParentEdIn, jsonRes.getString("Mensaje"), Constants.Types.ERROR)

                        progress.dismiss()

                    } catch (e: Error){
                        snackbar(applicationContext, layParentEdIn, e.message.toString(), Constants.Types.ERROR)
                        progress.dismiss()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    snackbar(applicationContext, layParentEdIn, e.message.toString(), Constants.Types.ERROR)
                    progress.dismiss()
                }
            }
        })
    }

    private fun showPopPhoto(){
        val dialog = Dialog(this, R.style.FullDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.pop_image)

        val image = dialog.findViewById<ImageView>(R.id.imgCenter)

        val b = imgPhotoEd.drawable.toBitmap()
        image.setImageBitmap(b)

        dialog.show()
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Obtener fotografía")

        val pictureDialogItems = arrayOf("Seleccionar foto de la galería", "Capturar foto con la cámara")
        pictureDialog.setItems(pictureDialogItems) { _, which ->
            when (which) {
                0 -> choosePhotoFromGallary()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)
    }

    @SuppressLint("SdCardPath")
    private fun takePhotoFromCamera() {
        val intent = Intent()
        intent.action = MediaStore.ACTION_IMAGE_CAPTURE

        val date = Date()
        val df = SimpleDateFormat("-mm-ss", Locale.getDefault())

        val newPicFile = df.format(date) + ".jpg"
        val outPath = "/sdcard/$newPicFile"
        val outfile = File(outPath)

        mCameraFileName = outfile.toString()
        val outUri = Uri.fromFile(outfile)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri)
        startActivityForResult(intent, CAMERA)
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
                    imgPhotoEd.setImageBitmap(bitmap)
                    defaultImage = false

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        else if (requestCode == CAMERA)
        {
            val file = File(mCameraFileName)

            if (file.exists()) {
                val uriImage = Uri.fromFile(File(mCameraFileName))
                imgPhotoEd.setImageURI(uriImage)
                defaultImage = false
            }
        }
    }

    private fun getStatus(){
        progress.show()

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","ConsultaStatusIncidenciaIdIncidenciaApp")
            .add("IdIncidencia", idIncidencia)
            .build()

        val request = Request.Builder().url(Constants.URL_STATUS).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    snackbar(applicationContext, layParentEdIn, e.message.toString(), Constants.Types.ERROR)
                    progress.dismiss()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body!!.string())
                        listRegistroStatus.clear()

                        if(jsonRes.getInt("Error") > 0)
                            snackbar(applicationContext, layParentEdIn, jsonRes.getString("Mensaje"), Constants.Types.ERROR)
                        else{

                            // create status object and iterate json array
                            val arrayStatus = jsonRes.getJSONArray("Datos")

                            for(i in 0 until arrayStatus.length()){
                                val j : JSONObject = arrayStatus.getJSONObject(i)

                                listRegistroStatus.add(ShortStatus(
                                    j.getString("Id"),
                                    j.getString("FechaAlta"),
                                    j.getString("StatusIncidencia"),
                                    j.getString("Status")))
                            }

                            showPopBitacoraStatus()
                        }

                        Constants.updateRefreshStatus(applicationContext, false)
                        progress.dismiss()

                    }catch (e: Error){
                        snackbar(applicationContext, layParentEdIn, e.message.toString(), Constants.Types.ERROR)
                        progress.dismiss()
                    }
                }
            }
        })
    }

    private fun showPopBitacoraStatus(){
        val dialog = Dialog(this, R.style.FullDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.pop_bitacora_status)

        val btnAddIncidencia = dialog.findViewById<Button>(R.id.btnAddPopIncidencias)
        btnAddIncidencia.setOnClickListener {
            dialog.dismiss()

            val intent = Intent(applicationContext, CreateIncidenciaActivity::class.java)
            intent.putExtra("persona", persona)
            intent.putExtra("desarrollo", desarrollo)
            intent.putExtra("solicitudId", idSolicitud)
            intent.putExtra("codigoUnidad", codigoUnidad)
            startActivity(intent)
        }

        val btnAddStatus = dialog.findViewById<Button>(R.id.btnAddPopStatusIncidencias)
        btnAddStatus.setOnClickListener {
            val intent = Intent(applicationContext, CreateStatusActivity::class.java)
            intent.putExtra("incidenciaId", idIncidencia)
            intent.putExtra("persona",persona)
            intent.putExtra("desarrollo",desarrollo)
            intent.putExtra("codigoUnidad",codigoUnidad)
            startActivity(intent)

            dialog.dismiss()
        }

        //show | hide button according user or colaborator
        if (Constants.getTipoUsuario(this) == OWNER)
            btnAddStatus.visibility = View.GONE


        val btnExit = dialog.findViewById<TextView>(R.id.txtCancelP)
        btnExit.setOnClickListener {
            dialog.dismiss()
            this@EditIncidenciaActivity.finish()
        }

        val rvBitacoraStatusIncidencia = dialog.findViewById<RecyclerView>(R.id.rvBitacoraStatusIncidencia)
        rvBitacoraStatusIncidencia.layoutManager = LinearLayoutManager(this)

        val adapter = BitacoraStatusAdapter(this, listRegistroStatus, object : BitacoraStatusAdapter.InterfaceOnClick{
            override fun onItemClick(pos: Int) {
                val intent = Intent(applicationContext, EditStatusActivity::class.java)
                intent.putExtra("persona",persona)
                intent.putExtra("desarrollo",desarrollo)
                intent.putExtra("idStatus", listRegistroStatus[pos].Id)
                intent.putExtra("incidenciaId", idIncidencia)
                intent.putExtra("codigoUnidad",codigoUnidad)
                startActivity(intent)
            }
        }, object : BitacoraStatusAdapter.InterfaceOnLongClick{
            override fun onItemLongClick(pos: Int) {
                if (Constants.getTipoUsuario(applicationContext) == COLLABORATOR)
                    showDeleteDialog(layParentEdIn, listRegistroStatus[pos].Id)
            }
        })

        rvBitacoraStatusIncidencia.adapter = adapter

        dialog.show()
        dialog.setCancelable(false)
    }

    private fun showDeleteDialog(view: View, idStatus: String){
        alert {
            title.hide()
            message.text = getString(R.string.msg_confirm_deletion)
            acceptClickListener {
                deleteStatusRegistro(view, idStatus)
            }
            cancelClickListener { }
        }.show()

        /*alert(resources.getString(R.string.msg_confirm_deletion),
            "")
        {
            positiveButton(resources.getString(R.string.accept)) {
                deleteStatusRegistro(view, idStatus)
            }
            negativeButton(resources.getString(R.string.cancel)){}
        }.show().apply {
            getButton(AlertDialog.BUTTON_POSITIVE)?.let { it.textColor = resources.getColor(R.color.colorBlack) }
            getButton(AlertDialog.BUTTON_NEGATIVE)?.let { it.textColor = resources.getColor(R.color.colorAccent) }
        }*/
    }


    private fun deleteStatusRegistro(view: View, idStatus: String){
        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","EliminaStatusIncidencia")
            .add("Id", idStatus)
            .build()

        val request = Request.Builder().url(Constants.URL_INCIDENCIAS).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    val msg = "No es posible eliminar esta status!"
                    snackbar(applicationContext, view, msg, Constants.Types.ERROR)
                }
            }
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread{
                    try{
                        val jsonRes = JSONObject(response.body!!.string())
                        Log.e("--", jsonRes.toString())

                        if(jsonRes.getInt("Error") == 0){

                            // successfully deleted on Server -> refresh list
                            listRegistroStatus.clear()

                            snackbar(applicationContext, view, jsonRes.getString("Mensaje"), Constants.Types.SUCCESS)
                            Constants.updateRefreshIncidencias(applicationContext, true)
                            getStatus()
                        } else{
                            snackbar(applicationContext, view, jsonRes.getString("Mensaje"), Constants.Types.ERROR)
                        }

                    } catch (e: Error){
                        snackbar(applicationContext, view, e.message.toString(), Constants.Types.ERROR)
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if(Constants.mustRefreshStatus(this)){
            listRegistroStatus.clear()
            getStatus()
        }
    }

}
