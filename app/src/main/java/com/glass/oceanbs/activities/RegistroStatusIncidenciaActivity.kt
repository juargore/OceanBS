@file:Suppress("SpellCheckingInspection", "PrivatePropertyName", "DEPRECATION",
    "UNUSED_ANONYMOUS_PARAMETER"
)

package com.glass.oceanbs.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toBitmap
import com.glass.oceanbs.Constants
import com.glass.oceanbs.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegistroStatusIncidenciaActivity : AppCompatActivity() {

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

    private val GALLERY = 1
    private val CAMERA = 2
    private var SELECTED = 0

    private var mCameraFileName1 = ""
    private var mCameraFileName2 = ""
    private var mCameraFileName3 = ""

    companion object {
        private const val IMAGE_DIRECTORY = "/demonuts"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_status_incidencia)

        supportActionBar?.hide()

        Constants.checkPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        initComponents()
    }

    private fun initComponents(){
        imgBackStatus = findViewById(R.id.imgBackStatus)

        txtShowPhoto1 = findViewById(R.id.txtShowPhoto1)
        cardPhoto1 = findViewById(R.id.cardPhoto1)
        imgPhoto1 = findViewById(R.id.imgPhoto1)

        txtShowPhoto2 = findViewById(R.id.txtShowPhoto2)
        cardPhoto2 = findViewById(R.id.cardPhoto2)
        imgPhoto2 = findViewById(R.id.imgPhoto2)

        txtShowPhoto3 = findViewById(R.id.txtShowPhoto3)
        cardPhoto3 = findViewById(R.id.cardPhoto3)
        imgPhoto3 = findViewById(R.id.imgPhoto3)

        setListeners()
    }

    private fun setListeners(){
        imgBackStatus.setOnClickListener { this.finish() }

        cardPhoto1.setOnClickListener { showPictureDialog(1); SELECTED = 1 }
        cardPhoto2.setOnClickListener { showPictureDialog(2); SELECTED = 2 }
        cardPhoto3.setOnClickListener { showPictureDialog(3); SELECTED = 3 }

        txtShowPhoto1.setOnClickListener { showPopPhoto(1) }
        txtShowPhoto2.setOnClickListener { showPopPhoto(2) }
        txtShowPhoto3.setOnClickListener { showPopPhoto(3) }
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
                        imgPhoto1.setImageURI(uriImage)

                        //val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uriImage);
                        //mCameraFileName1 = saveImage(bitmap)
                    }
                }
                2->{
                    val file = File(mCameraFileName2)
                    if (file.exists()) {
                        val uriImage = Uri.fromFile(File(mCameraFileName2))
                        imgPhoto2.setImageURI(uriImage)

                        //val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uriImage);
                        //mCameraFileName1 = saveImage(bitmap)
                    }
                }
                else->{
                    val file = File(mCameraFileName3)
                    if (file.exists()) {
                        val uriImage = Uri.fromFile(File(mCameraFileName3))
                        imgPhoto3.setImageURI(uriImage)

                        //val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uriImage);
                        //mCameraFileName1 = saveImage(bitmap)
                    }
                }
            }
        }
    }

}
