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
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.Constants
import com.glass.oceanbs.R
import com.glass.oceanbs.adapters.BitacoraStatusAdapter
import com.squareup.picasso.Picasso
import okhttp3.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.textColor
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Error
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NuevaIncidenciaActivity : AppCompatActivity() {

    private lateinit var layParentIn: LinearLayout
    private lateinit var txtTitleDesarrolloIn: TextView
    private lateinit var txtSubTitleDesarrolloIn: TextView
    private lateinit var imgBackRegistroIncidencia: ImageView

    private lateinit var txtShowPhoto: TextView
    private lateinit var cardPhoto: CardView
    private lateinit var imgPhoto: ImageView

    private lateinit var etFallaReportadaI: EditText
    private lateinit var etFallaRealI: EditText
    private lateinit var etObservacionesI: EditText
    private lateinit var btnSaveIncidenciaI: Button

    private lateinit var txtBitacoraStatus: TextView

    private val GALLERY = 1
    private val CAMERA = 2
    private var mCameraFileName = ""
    private var idSolicitud = ""

    companion object {
        private val IMAGE_DIRECTORY = "/demonuts"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_incidencia)

        supportActionBar?.hide()

        Constants.checkPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        val extras = intent.extras
        idSolicitud = extras!!.getString("solicitudId").toString()

        initComponents()
    }

    private fun initComponents(){
        layParentIn = findViewById(R.id.layParentIn)
        txtTitleDesarrolloIn = findViewById(R.id.txtTitleDesarrolloIn)
        txtSubTitleDesarrolloIn = findViewById(R.id.txtSubTitleDesarrolloIn)
        imgBackRegistroIncidencia = findViewById(R.id.imgBackRegistroIncidencia)
        txtBitacoraStatus = findViewById(R.id.txtBitacoraStatus)

        etFallaReportadaI = findViewById(R.id.etFallaReportadaI)
        etFallaRealI = findViewById(R.id.etFallaRealI)
        etObservacionesI = findViewById(R.id.etObservacionesI)
        btnSaveIncidenciaI = findViewById(R.id.btnSaveIncidenciaI)

        txtShowPhoto = findViewById(R.id.txtShowPhoto)
        cardPhoto = findViewById(R.id.cardPhoto)
        imgPhoto = findViewById(R.id.imgPhoto)

        imgBackRegistroIncidencia.setOnClickListener { this.finish() }
        txtShowPhoto.setOnClickListener { showPopPhoto() }
        cardPhoto.setOnClickListener { showPictureDialog() }
        txtBitacoraStatus.setOnClickListener { showPopBitacoraStatus() }
        btnSaveIncidenciaI.setOnClickListener { sendDataToServer() }
    }

    // send values to server to create a new solicitud
    @SuppressLint("SetTextI18n")
    private fun sendDataToServer(){
        /*progress.show()
        progress.setCancelable(false)
        titleProgress.text = "Enviando Información"*/
        val userId = Constants.getUserId(this)
        val client = OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS).build()

        val builder = FormBody.Builder()
            .add("WebService","GuardaIncidencia")
            .add("Id", "") // empty if new
            .add("Status", "1")
            .add("Observaciones", etObservacionesI.text.toString())
            .add("IdSolicitudAG", idSolicitud)
            .add("IdValorClasif1", "")
            .add("IdValorClasif2", "3")
            .add("IdValorClasif3", "")
            .add("FallaReportada", etFallaReportadaI.text.toString())
            .add("FallaReal", etFallaRealI.text.toString())
            .build()

        val request = Request.Builder().url(Constants.URL_INCIDENCIAS).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body()!!.string())

                        if(jsonRes.getInt("Error") == 0){
                            Constants.snackbar(applicationContext, layParentIn, jsonRes.getString("Mensaje"))
                        } else
                            Constants.snackbar(applicationContext, layParentIn, jsonRes.getString("Mensaje"))

                        //progress.dismiss()

                    } catch (e: Error){
                        Constants.snackbar(applicationContext, layParentIn, e.message.toString())
                        //progress.dismiss()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Constants.snackbar(applicationContext, layParentIn, e.message.toString())
                    //progress.dismiss()
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

        val b = imgPhoto.drawable.toBitmap()
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
        val galleryIntent = Intent(Intent.ACTION_PICK,
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

    public override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY)
        {
            if (data != null)
            {
                val contentURI = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    imgPhoto.setImageBitmap(bitmap)

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
                imgPhoto.setImageURI(uriImage)

                //val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uriImage);
                //mCameraFileName = saveImage(bitmap)
            }
        }
    }

    private fun saveImage(myBitmap: Bitmap):String {
        val bytes = ByteArrayOutputStream()

        val a = Bitmap.createScaledBitmap(myBitmap, myBitmap.width/3, myBitmap.height/3, true)
        a.compress(Bitmap.CompressFormat.JPEG, 80, bytes)

        val wallpaperDirectory = File(
            (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY)

        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs()
        }

        try
        {
            val f = File(wallpaperDirectory, ("A"+(Calendar.getInstance()
                .timeInMillis).toString() + ".jpg"))
            f.createNewFile()

            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this,
                arrayOf(f.path),
                arrayOf("image/jpeg"), null)
            fo.close()

            return f.absolutePath
        }
        catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }

    private fun showPopBitacoraStatus(){
        val dialog = Dialog(this, R.style.FullDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.pop_bitacora_status)

        val btnAdd = dialog.findViewById<Button>(R.id.btnAddPopIncidencias)
        btnAdd.setOnClickListener {
            val intent = Intent(applicationContext, RegistroStatusIncidenciaActivity::class.java)
            startActivity(intent) }

        val btnExit = dialog.findViewById<TextView>(R.id.txtCancelP)
        btnExit.setOnClickListener { dialog.dismiss() }

        val rvBitacoraStatusIncidencia = dialog.findViewById<RecyclerView>(R.id.rvBitacoraStatusIncidencia)
        rvBitacoraStatusIncidencia.layoutManager = LinearLayoutManager(this)

        val adapter = BitacoraStatusAdapter(this, object : BitacoraStatusAdapter.InterfaceOnClick{
            override fun onItemClick(pos: Int) {
                val intent = Intent(applicationContext, RegistroStatusIncidenciaActivity::class.java)
                startActivity(intent)
            }
        }, object : BitacoraStatusAdapter.InterfaceOnLongClick{
            override fun onItemLongClick(pos: Int) {
                showDeleteDialog()
            }
        })

        rvBitacoraStatusIncidencia.adapter = adapter

        dialog.show()
    }

    private fun showDeleteDialog(){
        alert(resources.getString(R.string.msg_confirm_deletion),
            "Eliminar Status de Incidencia")
        {
            positiveButton(resources.getString(R.string.accept)) {

            }
            negativeButton(resources.getString(R.string.cancel)){}
        }.show().apply {
            getButton(AlertDialog.BUTTON_POSITIVE)?.let { it.textColor = resources.getColor(R.color.colorBlack) }
            getButton(AlertDialog.BUTTON_NEGATIVE)?.let { it.textColor = resources.getColor(R.color.colorAccent) }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}
