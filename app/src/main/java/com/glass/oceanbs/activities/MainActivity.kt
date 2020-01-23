package com.glass.oceanbs.activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.snackbar
import com.glass.oceanbs.R
import com.glass.oceanbs.adapters.ParentPagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener
import okhttp3.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.textColor
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {


    private lateinit var carouselView: CarouselView
    private lateinit var fabExit : FloatingActionButton
    private lateinit var layParentMain: RelativeLayout

    private val bitmapList: ArrayList<Bitmap> = ArrayList()

    companion object{
        private lateinit var tabsParent: TabLayout
        private lateinit var viewPagerParent: ViewPager

        fun goToFirstTab(){
            viewPagerParent.currentItem = 0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        initComponents()
        setUpTabs()
        setImagesInCarousel()
    }

    private fun initComponents(){
        layParentMain = findViewById(R.id.layParentMain)
        carouselView = findViewById(R.id.carouselView)
        tabsParent = findViewById(R.id.tabsParent)
        viewPagerParent = findViewById(R.id.viewPagerParent)
        fabExit = findViewById(R.id.fabExit)

        bitmapList.add(BitmapFactory.decodeResource(resources,
            R.drawable.photo1))
        bitmapList.add(BitmapFactory.decodeResource(resources,
            R.drawable.photo2))
        bitmapList.add(BitmapFactory.decodeResource(resources,
            R.drawable.photo3))
        bitmapList.add(BitmapFactory.decodeResource(resources,
            R.drawable.photo4))

        fabExit.setOnClickListener { showExitDialog() }
    }

    private fun setUpTabs(){
        val adapter = ParentPagerAdapter(applicationContext, supportFragmentManager)

        viewPagerParent.adapter = adapter
        viewPagerParent.offscreenPageLimit = 2
        tabsParent.setupWithViewPager(viewPagerParent)
    }

    private fun setImagesInCarousel(){
        carouselView.setImageListener(imageListener)
        carouselView.pageCount = bitmapList.size
    }

    private var imageListener: ImageListener = ImageListener { position, imageView ->
        try {
            imageView.setImageBitmap(bitmapList[position])
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getImagesFromServer(){

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","ConsultaSolicitudesAGIdUsuario")
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body()!!.string())
                        Log.e("RES", jsonRes.toString())

                        if(jsonRes.getInt("Error") > 0)
                            snackbar(applicationContext, layParentMain, jsonRes.getString("Mensaje"), Constants.Types.ERROR)
                        else{
                            // TODO
                        }

                    } catch (e: Error){
                        snackbar(applicationContext!!, layParentMain, e.message.toString(), Constants.Types.ERROR)
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    snackbar(applicationContext!!, layParentMain, e.message.toString(), Constants.Types.ERROR)
                }
            }
        })
    }

    private fun showExitDialog(){
        alert("¿Está seguro que desea salir de la aplicación?",
            "")
        {
            positiveButton(resources.getString(R.string.accept)) {
                Constants.setKeepLogin(applicationContext, false)
                val intent = Intent(applicationContext, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            negativeButton(resources.getString(R.string.cancel)){}
        }.show().apply {
            getButton(AlertDialog.BUTTON_POSITIVE)?.let { it.textColor = resources.getColor(R.color.colorBlack) }
            getButton(AlertDialog.BUTTON_NEGATIVE)?.let { it.textColor = resources.getColor(R.color.colorAccent) }
        }
    }
}
