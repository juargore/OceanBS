package com.glass.oceanbs.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.GET_CAROUSEL
import com.glass.oceanbs.Constants.DATA
import com.glass.oceanbs.Constants.ERROR
import com.glass.oceanbs.Constants.MESSAGE
import com.glass.oceanbs.Constants.PHOTO
import com.glass.oceanbs.Constants.PHOTOS
import com.glass.oceanbs.Constants.URL_IMAGES_CAROUSEL
import com.glass.oceanbs.Constants.WEB_SERVICE
import com.glass.oceanbs.Constants.getUserName
import com.glass.oceanbs.R
import com.glass.oceanbs.adapters.NewHomeItemAdapter
import com.glass.oceanbs.extensions.getDateFormatted
import com.glass.oceanbs.extensions.showExitDialog
import com.glass.oceanbs.models.ItemNewHome
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.activity_new_main.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class NewMainActivity : BaseActivity() {

    private val photosList: ArrayList<String> = ArrayList()
    private var imageListener: ImageListener = ImageListener { position, imageView ->
        Picasso.get().load(photosList[position]).fit().into(imageView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_main)
        getTopImagesFromServer()
        setUpRecycler()
        setViews()
    }

    private fun setViews() {
        txtDate.text = getDateFormatted(this)
        txtUserName.text = getUserName(this).uppercase(Locale.getDefault())
        fabExit.setOnClickListener { showExitDialog() }
    }

    private fun setUpRecycler() {
        with(NewHomeItemAdapter(getList())) {
            rvMain.adapter = this
            this.onItemClicked = { intent, url ->
                intent?.let {
                    it.putExtra(PHOTOS, photosList)
                    startActivity(it)
                    overridePendingTransition(0, 0)
                }
                url?.let {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                }
            }
        }
    }

    private fun getTopImagesFromServer() {
        val client = OkHttpClient()
        val builder = FormBody.Builder().add(WEB_SERVICE, GET_CAROUSEL).build()
        val request = Request.Builder().url(URL_IMAGES_CAROUSEL).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    val jsonRes = JSONObject(response.body!!.string())
                    if (jsonRes.getInt(ERROR) > 0) {
                        showSnackBar(jsonRes.getString(MESSAGE))
                    } else {
                        val arr = jsonRes.getJSONArray(DATA)
                        for (i in 0 until arr.length()){
                            val j = arr.getJSONObject(i)
                            photosList.add(j.getString(PHOTO))
                        }
                        setImagesInCarousel()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                showSnackBar(e.message.toString())
            }
        })
    }

    private fun showSnackBar(str: String) {
        runOnUiThread { Constants.snackbar(this, layMain,str, Constants.Types.ERROR) }
    }

    private fun setImagesInCarousel() {
        carouselView.setImageListener(imageListener)
        carouselView.pageCount = photosList.size
    }

    // todo: get real data from Server
    private fun getList() = listOf(
        ItemNewHome(
            id = 0,
            title = "SEGUIMIENTO DE SU PROPIEDAD",
            subtitle = "Conozca los avances actuales...",
            hexColor = "#FFB264",
            openScreen = true,
            url = null
        ),
        ItemNewHome(
            id = 1,
            title = "SERVICIO DE POSTVENTA",
            subtitle = "Infórmenos sobre incidencias...",
            hexColor = "#F16622",
            openScreen = true,
            url = null
        ),
        ItemNewHome(
            id = 2,
            title = "NUEVOS DESARROLLOS",
            subtitle = "Conozca nuevas posibilidades...",
            hexColor = "#636363",
            openScreen = false,
            url = "https://developers.google.com/android/reference/com/google/mlkit/vision/face/FaceDetectorOptions?hl=es#LANDMARK_MODE_ALL"
        )
    )
}
