package com.glass.oceanbs.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.CAPTION
import com.glass.oceanbs.Constants.COLOR
import com.glass.oceanbs.Constants.CURRENT_DATE
import com.glass.oceanbs.Constants.GET_CAROUSEL
import com.glass.oceanbs.Constants.DATA
import com.glass.oceanbs.Constants.ERROR
import com.glass.oceanbs.Constants.GET_MAIN_ITEMS_HOME
import com.glass.oceanbs.Constants.LINK
import com.glass.oceanbs.Constants.LINK_TYPE
import com.glass.oceanbs.Constants.MEMBER_SINCE
import com.glass.oceanbs.Constants.MESSAGE
import com.glass.oceanbs.Constants.OPTIONS
import com.glass.oceanbs.Constants.OWNER
import com.glass.oceanbs.Constants.PHOTO
import com.glass.oceanbs.Constants.PHOTOS
import com.glass.oceanbs.Constants.TITLE
import com.glass.oceanbs.Constants.URL_IMAGES_CAROUSEL
import com.glass.oceanbs.Constants.URL_MAIN_ITEMS_HOME
import com.glass.oceanbs.Constants.WEB_SERVICE
import com.glass.oceanbs.Constants.WELCOME_CAPTION
import com.glass.oceanbs.R
import com.glass.oceanbs.adapters.NewHomeItemAdapter
import com.glass.oceanbs.extensions.showExitDialog
import com.glass.oceanbs.models.ItemNewHome
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.activity_new_main.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import kotlin.collections.ArrayList

class NewMainActivity: BaseActivity() {

    private val photosList: ArrayList<String> = ArrayList()
    private var imageListener = ImageListener { position, imageView ->
        Picasso.get().load(photosList[position]).fit().into(imageView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_main)
        getTopImagesFromServer()
        getMainListFromServer()
        fabExit.setOnClickListener { showExitDialog() }
    }

    private fun setUpRecycler(items: List<ItemNewHome>) {
        with(NewHomeItemAdapter(items)) {
            rvMain.adapter = this
            onItemClicked = { intent, url ->
                url?.let {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                }
                intent?.let {
                    it.putExtra(PHOTOS, photosList)
                    startActivity(it)
                    overridePendingTransition(0, 0)
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

            override fun onFailure(call: Call, e: IOException) {
                showSnackBar(e.message.toString())
            }
        })
    }

    private fun showSnackBar(str: String) {
        runOnUiThread { Constants.snackbar(this, layMain,str, Constants.Types.ERROR) }
    }

    private fun setImagesInCarousel() {
        runOnUiThread {
            carouselView.setImageListener(imageListener)
            carouselView.pageCount = photosList.size
        }
    }

    private fun getMainListFromServer() {
        val client = OkHttpClient()
        val builder = FormBody.Builder().add(WEB_SERVICE, GET_MAIN_ITEMS_HOME).build()
        val request = Request.Builder().url(URL_MAIN_ITEMS_HOME).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                with (JSONObject(response.body!!.string())) {
                    if (getInt(ERROR) > 0) {
                        showSnackBar(getString(MESSAGE))
                    } else {
                        val mList = mutableListOf<ItemNewHome>()
                        val strWelcome = getString(WELCOME_CAPTION)
                        val strDate = getString(CURRENT_DATE)
                        val strMember = getString(MEMBER_SINCE)
                        val strOwner  = getString(OWNER)
                        val arr = getJSONArray(OPTIONS)

                        for (i in 0 until arr.length()) {
                            val j = arr.getJSONObject(i)
                            val linkType = j.getInt(LINK_TYPE)
                            mList.add(
                                ItemNewHome(
                                    title = j.getString(TITLE),
                                    subtitle = j.getString(CAPTION),
                                    hexColor = j.getString(COLOR),
                                    openScreen = linkType == 1,
                                    url = j.getString(LINK))
                            )
                        }; setupViews(strWelcome, strDate, strMember, strOwner, mList)
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                showSnackBar(e.message.toString())
            }
        })
    }

    private fun setupViews(
        strWelcome: String,
        strDate: String,
        strMember: String,
        strOwner: String,
        items: List<ItemNewHome>
    ) {
        runOnUiThread {
            txtWelcome.text = strWelcome
            txtDate.text = strDate
            txtMemberSince.text = strMember
            txtUserName.text = strOwner
            setUpRecycler(items)
        }
    }
}
