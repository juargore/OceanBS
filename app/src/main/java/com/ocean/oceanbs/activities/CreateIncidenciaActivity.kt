@file:Suppress("SpellCheckingInspection", "DEPRECATION", "PrivatePropertyName", "LocalVariableName")

package com.ocean.oceanbs.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.ocean.oceanbs.Constants
import com.ocean.oceanbs.Constants.ERROR
import com.ocean.oceanbs.Constants.SELECT
import com.ocean.oceanbs.Constants.WEB_SERVICE
import com.ocean.oceanbs.Constants.getTipoUsuario
import com.ocean.oceanbs.Constants.getUserId
import com.ocean.oceanbs.Constants.internetConnected
import com.ocean.oceanbs.Constants.showPopUpNoInternet
import com.ocean.oceanbs.Constants.snackbar
import com.ocean.oceanbs.Constants.updateRefreshIncidencias
import com.ocean.oceanbs.R
import com.ocean.oceanbs.adapters.BitacoraStatusAdapter
import com.ocean.oceanbs.database.TableUser
import com.ocean.oceanbs.extensions.alert
import com.ocean.oceanbs.extensions.hide
import com.ocean.oceanbs.models.COLLABORATOR
import com.ocean.oceanbs.models.GenericObj
import com.ocean.oceanbs.models.OWNER
import com.ocean.oceanbs.models.ShortStatus
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CreateIncidenciaActivity : BaseActivity() {

    private lateinit var progress : AlertDialog
    private lateinit var titleProgress: TextView
    private lateinit var layParentIn: LinearLayout
    private lateinit var txtTitleDesarrolloIn: TextView
    private lateinit var txtSubTitleDesarrolloIn: TextView
    private lateinit var imgBackRegistroIncidencia: ImageView

    private lateinit var txtShowPhoto: TextView
    private lateinit var cardPhoto: CardView
    private lateinit var imgPhoto: ImageView

    private lateinit var spinner3m: Spinner
    private lateinit var spinner6m: Spinner
    private lateinit var spinner1a: Spinner

    private lateinit var etFallaReportadaI: EditText
    private lateinit var etFallaRealI: EditText
    private lateinit var etObservacionesI: EditText
    private lateinit var btnSaveIncidenciaI: Button
    private lateinit var txtBitacoraStatus: TextView

    private var listSpinner3m: ArrayList<GenericObj> = ArrayList()
    private var listSpinner6m: ArrayList<GenericObj> = ArrayList()
    private var listSpinner1a: ArrayList<GenericObj> = ArrayList()
    private var listRegistroStatus: ArrayList<ShortStatus> = ArrayList()

    private val GALLERY = 1
    private val CAMERA = 2
    private var idSolicitud = ""
    private var idIncidencia = ""
    private var persona = ""
    private var desarrollo = ""
    private var codigoUnidad = ""
    private var defaultImage = true

    private var pictureAsFile : File? = null
    private var mBitmap: Bitmap? = null
    private val PERMISSION_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_incidencia)
        intent.extras?.let {
            idSolicitud = it.getString("solicitudId").toString()
            persona = it.getString("persona").toString()
            desarrollo = it.getString("desarrollo").toString()
            codigoUnidad = it.getString("codigoUnidad").toString()
        }
        initComponents()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun initComponents() {
        layParentIn = findViewById(R.id.layParentIn)
        txtTitleDesarrolloIn = findViewById(R.id.txtTitleDesarrolloIn)
        txtSubTitleDesarrolloIn = findViewById(R.id.txtSubTitleDesarrolloIn)
        imgBackRegistroIncidencia = findViewById(R.id.imgBackRegistroIncidencia)
        txtBitacoraStatus = findViewById(R.id.txtBitacoraStatus)

        txtTitleDesarrolloIn.text = "$desarrollo $codigoUnidad"
        txtSubTitleDesarrolloIn.text = persona

        spinner3m = findViewById(R.id.spinner3m)
        spinner6m = findViewById(R.id.spinner6m)
        spinner1a = findViewById(R.id.spinner1a)

        etFallaReportadaI = findViewById(R.id.etFallaReportadaI)
        etFallaRealI = findViewById(R.id.etFallaRealI)
        etObservacionesI = findViewById(R.id.etObservacionesI)
        btnSaveIncidenciaI = findViewById(R.id.btnSaveIncidenciaI)

        txtShowPhoto = findViewById(R.id.txtShowPhoto)
        cardPhoto = findViewById(R.id.cardPhoto)
        imgPhoto = findViewById(R.id.imgPhoto)

        // set up progress dialg
        val builder = AlertDialog.Builder(this, R.style.HalfDialogTheme)
        val inflat = layoutInflater
        val dialogView = inflat.inflate(R.layout.progress, null)

        titleProgress = dialogView.findViewById(R.id.loading_title)

        builder.setView(dialogView)
        progress = builder.create()
        progress.setCancelable(false)

        imgBackRegistroIncidencia.setOnClickListener { this.finish() }
        txtShowPhoto.setOnClickListener { showPopPhoto() }
        cardPhoto.setOnClickListener { showPictureDialog() }

        btnSaveIncidenciaI.setOnClickListener {
            if (validateFullFields())
                sendDataToServer()
        }

        if (internetConnected(this))
            getDataForSpinners(1)
        else
            showPopUpNoInternet(this)
    }

    private fun getDataForSpinners(value: Int) {

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add(WEB_SERVICE,"ConsultaValoresClasificacionIdClasificacion")
            .add("IdClasificacion", value.toString())
            .build()

        val request = Request.Builder().url(Constants.URL_CLASIFICACION).post(builder).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body!!.string())

                        if (jsonRes.getInt(ERROR) == 0) {
                            val arrayClasif = jsonRes.getJSONArray("Datos")

                            for(i in 0 until arrayClasif.length()) {
                                val j = arrayClasif.getJSONObject(i)
                                val generic = GenericObj(
                                    j.getString("Id"),
                                    j.getString("Codigo"),
                                    j.getString("Nombre"))
                                when(value) {
                                    1 -> listSpinner3m.add(generic)
                                    2 -> listSpinner6m.add(generic)
                                    3 -> listSpinner1a.add(generic)
                                }
                            }
                        }

                        when (value) {
                            1-> getDataForSpinners(2)
                            2-> getDataForSpinners(3)
                            3 -> setUpSpinners()
                        }

                    } catch (_: Error) {}
                }
            }
        })
    }

    private fun setUpSpinners() {
        val list3m: ArrayList<String> = ArrayList()
        list3m.add(0, SELECT)

        for (i in listSpinner3m)
            list3m.add(i.Nombre)

        val adapter3m = ArrayAdapter(this, R.layout.spinner_text, list3m)
        spinner3m.adapter = adapter3m

        spinner3m.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if (pos > 0) {
                    spinner6m.setSelection(0)
                    spinner1a.setSelection(0)
                }
            }
        }

        val list6m: ArrayList<String> = ArrayList()
        list6m.add(0, SELECT)

        for (i in listSpinner6m)
            list6m.add(i.Nombre)

        val adapter6m = ArrayAdapter(this, R.layout.spinner_text, list6m)
        spinner6m.adapter = adapter6m

        spinner6m.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if (pos > 0) {
                    spinner3m.setSelection(0)
                    spinner1a.setSelection(0)
                }
            }
        }

        val list1a: ArrayList<String> = ArrayList()
        list1a.add(0, SELECT)

        for (i in listSpinner1a)
            list1a.add(i.Nombre)

        val adapter1a = ArrayAdapter(this, R.layout.spinner_text, list1a)
        spinner1a.adapter = adapter1a

        spinner1a.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if (pos > 0) {
                    spinner3m.setSelection(0)
                    spinner6m.setSelection(0)
                }
            }
        }
    }

    private fun validateFullFields(): Boolean {
        val msg = "Este campo no puede estar vacío"
        return when {
            TextUtils.isEmpty(etFallaReportadaI.text.toString()) -> {
                etFallaReportadaI.error = msg; false }
            else -> true
        }
    }

    // send values to server to create a new incidencia
    @SuppressLint("SetTextI18n")
    private fun sendDataToServer() {
        progress.show()
        titleProgress.text = "Enviando información"

        val client = OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS).build()
        val user = TableUser(this).getCurrentUserById(getUserId(this), getTipoUsuario(this))
        val userId= user.idColaborador
        var valor1a = ""
        var valor3m = ""
        var valor6m = ""

        if (spinner3m.selectedItemPosition > 0)
            valor3m = listSpinner3m[spinner3m.selectedItemPosition-1].Id

        if (spinner6m.selectedItemPosition > 0)
            valor6m = listSpinner6m[spinner6m.selectedItemPosition-1].Id

        if (spinner1a.selectedItemPosition > 0)
            valor1a = listSpinner1a[spinner1a.selectedItemPosition-1].Id

        val bitmapPhoto1 = imgPhoto.drawable.toBitmap()
        val stream1 = ByteArrayOutputStream()
        bitmapPhoto1.compress(Bitmap.CompressFormat.JPEG, 50, stream1)
        val byteArray1 = stream1.toByteArray()
        val MEDIA_TYPE_JPG = "image/jpeg".toMediaTypeOrNull()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("WebService","GuardaIncidencia")
            .addFormDataPart("Id", "") // empty if new | else -> ID
            .addFormDataPart("Status", "1")
            .addFormDataPart("Observaciones", etObservacionesI.text.toString())
            .addFormDataPart("IdColaboradorAlta", userId)
            .addFormDataPart("IdSolicitudAG", idSolicitud)
            .addFormDataPart("IdValorClasif1", valor3m)
            .addFormDataPart("IdValorClasif2", valor6m)
            .addFormDataPart("IdValorClasif3", valor1a)
            .addFormDataPart("FallaReportada", etFallaReportadaI.text.toString())
            .addFormDataPart("FallaReal", etFallaRealI.text.toString())

        if (defaultImage)
            requestBody.addFormDataPart("Fotografia1", "")
        else
            requestBody.addFormDataPart("Fotografia1", "image1.jpeg", RequestBody.create(MEDIA_TYPE_JPG, byteArray1))

        val request = Request.Builder()
            .url(Constants.URL_INCIDENCIAS)
            .post(requestBody.build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body!!.string())
                        if (jsonRes.getInt("Error") == 0) {
                            idIncidencia = jsonRes.getString("Id")
                            snackbar(applicationContext, layParentIn, jsonRes.getString("Mensaje"), Constants.Types.SUCCESS)
                            updateRefreshIncidencias(applicationContext, true)
                            getStatus()
                        } else {
                            snackbar(applicationContext, layParentIn, jsonRes.getString("Mensaje"), Constants.Types.ERROR)
                        }
                        progress.dismiss()
                    } catch (e: Error) {
                        snackbar(applicationContext, layParentIn, e.message.toString(), Constants.Types.ERROR)
                        progress.dismiss()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    snackbar(applicationContext, layParentIn, e.message.toString(), Constants.Types.ERROR)
                    progress.dismiss()
                }
            }
        })
    }

    private fun showPopPhoto() {
        Dialog(this, R.style.FullDialogTheme).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.pop_image)
            val b = imgPhoto.drawable.toBitmap()
            val image = findViewById<ImageView>(R.id.imgCenter)
            image.setImageBitmap(b)
        }.show()
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Obtener fotografía")

        val pictureDialogItems = arrayOf("Seleccionar foto de la galería", "Capturar foto con la cámara")
        pictureDialog.setItems(pictureDialogItems) { _, which ->
            when (which) {
                0 -> choosePhotoFromGallary()
                1 -> verifyPermissionAndRedirectToCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallary() {
        startActivityForResult(Intent(
            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), GALLERY
        )
    }

    @SuppressLint("InlinedApi", "ObsoleteSdkInt")
    private fun verifyPermissionAndRedirectToCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_DENIED  ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_MEDIA_LOCATION
                )
                == PackageManager.PERMISSION_DENIED) {

                //permission was not enabled
                val permission = arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_MEDIA_LOCATION
                )

                //show popup to request permission
                requestPermissions(permission, PERMISSION_CODE)
            } else{
                //permission already granted
                openCamera()
            }
        } else{
            // system os is < marshmallow
            openCamera()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error while creating the File
                    Log.e("--", "Error creating file: ${ex.message}")
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also { file ->
                    val filePath = file.path
                    BitmapFactory.decodeFile(filePath)?.let {
                        mBitmap = Bitmap.createScaledBitmap(it, 500, 500, false)
                    }
                    pictureAsFile = file
                    val photoURI = FileProvider.getUriForFile(
                        Objects.requireNonNull(this),
                        "com.ocean.oceanbs" + ".provider", file)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA)
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, contentURI)
                    imgPhoto.setImageBitmap(bitmap)
                    defaultImage = false
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == CAMERA) {
            val filePath = pictureAsFile?.path
            val preBitmap = BitmapFactory.decodeFile(filePath)
            val matrix = Matrix()
            matrix.postRotate(90f)
            val rotatedBitmap = Bitmap.createBitmap(preBitmap, 0, 0, preBitmap.width, preBitmap.height, matrix, true)
            imgPhoto.setImageBitmap(rotatedBitmap)
            defaultImage = false
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(code: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (code) {
            PERMISSION_CODE -> {
                for (i in permissions.indices) {
                    if (permissions[i] == Manifest.permission.CAMERA) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            openCamera()
                        } else {
                            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
                        }
                        break
                    }
                }
            }
        }
    }

    private fun getStatus() {
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
                    snackbar(applicationContext, layParentIn, e.message.toString(), Constants.Types.ERROR)
                    progress.dismiss()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body!!.string())
                        listRegistroStatus.clear()

                        if (jsonRes.getInt("Error") > 0)
                            snackbar(applicationContext, layParentIn, jsonRes.getString("Mensaje"), Constants.Types.ERROR)
                        else {
                            // create status object and iterate json array
                            val arrayStatus = jsonRes.getJSONArray("Datos")

                            for(i in 0 until arrayStatus.length()) {
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
                    }catch (e: Error) {
                        snackbar(applicationContext, layParentIn, e.message.toString(), Constants.Types.ERROR)
                        progress.dismiss()
                    }
                }
            }
        })
    }

    private fun showPopBitacoraStatus() {
        val dialog = Dialog(this, R.style.FullDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.pop_bitacora_status)

        val btnAddIncidencia = dialog.findViewById<Button>(R.id.btnAddPopIncidencias)
        btnAddIncidencia.setOnClickListener {
            dialog.dismiss()
            finish()
            startActivity(intent)
        }

        val btnAddStatus = dialog.findViewById<Button>(R.id.btnAddPopStatusIncidencias)
        btnAddStatus.setOnClickListener {
            startActivity(
                Intent(applicationContext, CreateStatusActivity::class.java).apply {
                    putExtra("incidenciaId", idIncidencia)
                    putExtra("persona",persona)
                    putExtra("desarrollo",desarrollo)
                    putExtra("codigoUnidad",codigoUnidad)
                }
            )
            dialog.dismiss()
        }

        //show | hide button according user or colaborator
        if (getTipoUsuario(this) == OWNER)
            btnAddStatus.visibility = View.GONE

        val btnExit = dialog.findViewById<TextView>(R.id.txtCancelP)
        btnExit.setOnClickListener {
            dialog.dismiss()
            this@CreateIncidenciaActivity.finish()
        }

        val rvBitacoraStatusIncidencia = dialog.findViewById<RecyclerView>(R.id.rvBitacoraStatusIncidencia)
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
                if (getTipoUsuario(applicationContext) == COLLABORATOR) //colaborador
                    showDeleteDialog(layParentIn, listRegistroStatus[pos].Id)
            }
        })

        rvBitacoraStatusIncidencia.adapter = adapter
        dialog.show()
        dialog.setCancelable(false)
    }

    private fun showDeleteDialog(view: View, idStatus: String) {
        alert {
            title.hide()
            message.text = getString(R.string.msg_confirm_deletion)
            acceptClickListener {
                deleteStatusRegistro(view, idStatus)
            }
            cancelClickListener { }
        }.show()
    }

    private fun deleteStatusRegistro(view: View, idStatus: String) {
        progress.show()

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

                        if (jsonRes.getInt("Error") == 0) {

                            // successfully deleted on Server -> refresh list
                            listRegistroStatus.clear()

                            snackbar(applicationContext, view, jsonRes.getString("Mensaje"), Constants.Types.SUCCESS)
                            updateRefreshIncidencias(applicationContext, true)
                            getStatus()
                        } else{
                            snackbar(
                                applicationContext,
                                view,
                                jsonRes.getString("Mensaje"),
                                Constants.Types.ERROR)
                        }

                    } catch (e: Error) {
                        snackbar(
                            applicationContext,
                            view,
                            e.message.toString(),
                            Constants.Types.ERROR
                        )
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (Constants.mustRefreshStatus(this)) {
            listRegistroStatus.clear()
            getStatus()
        }
    }
}
