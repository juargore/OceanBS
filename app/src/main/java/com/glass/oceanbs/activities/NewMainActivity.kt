package com.glass.oceanbs.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.glass.oceanbs.Constants.CAPTION
import com.glass.oceanbs.Constants.COLOR
import com.glass.oceanbs.Constants.CURRENT_DATE
import com.glass.oceanbs.Constants.DATA
import com.glass.oceanbs.Constants.GET_CAROUSEL
import com.glass.oceanbs.Constants.GET_MAIN_ITEMS_HOME
import com.glass.oceanbs.Constants.LINK
import com.glass.oceanbs.Constants.LINK_TYPE
import com.glass.oceanbs.Constants.MEMBER_SINCE
import com.glass.oceanbs.Constants.OPTIONS
import com.glass.oceanbs.Constants.OWNER_ID
import com.glass.oceanbs.Constants.OWNER_NAME
import com.glass.oceanbs.Constants.PHOTO
import com.glass.oceanbs.Constants.PHOTOS
import com.glass.oceanbs.Constants.TITLE
import com.glass.oceanbs.Constants.URL_IMAGES_CAROUSEL
import com.glass.oceanbs.Constants.URL_MAIN_ITEMS_HOME
import com.glass.oceanbs.Constants.WELCOME_CAPTION
import com.glass.oceanbs.Constants.getUserId
import com.glass.oceanbs.R
import com.glass.oceanbs.adapters.NewHomeItemAdapter
import com.glass.oceanbs.extensions.Parameter
import com.glass.oceanbs.extensions.getDataFromServer
import com.glass.oceanbs.extensions.showExitDialog
import com.glass.oceanbs.models.ItemNewHome
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.activity_new_main.*

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
        getDataFromServer(
            webService = GET_CAROUSEL,
            url = URL_IMAGES_CAROUSEL,
            parent = layMain
        ) { jsonRes ->
            val arr = jsonRes.getJSONArray(DATA)
            for (i in 0 until arr.length()) {
                val j = arr.getJSONObject(i)
                photosList.add(j.getString(PHOTO))
            }
            runOnUiThread {
                carouselView.setImageListener(imageListener)
                carouselView.pageCount = photosList.size
            }
        }
    }

    private fun getMainListFromServer() {
        getDataFromServer(
            webService = GET_MAIN_ITEMS_HOME,
            url = URL_MAIN_ITEMS_HOME,
            parent = layMain,
            parameters = listOf(
                Parameter(
                    key = OWNER_ID,
                    value = getUserId(this)
                )
            )
        ) { jsonRes ->
            with (jsonRes) {
                val mList = mutableListOf<ItemNewHome>()
                val strDate = getString(CURRENT_DATE)
                val strWelcome = getString(WELCOME_CAPTION)
                val strMember = getString(MEMBER_SINCE)
                val strOwner  = getString(OWNER_NAME)
                val options = getJSONArray(OPTIONS)

                for (i in 0 until options.length()) {
                    val j = options.getJSONObject(i)
                    val linkType = j.getInt(LINK_TYPE)
                    mList.add(
                        ItemNewHome(
                            title = j.getString(TITLE),
                            subtitle = j.getString(CAPTION),
                            hexColor = j.getString(COLOR),
                            openScreen = linkType == 1,
                            url = j.getString(LINK))
                    )
                }
                setupViews(strWelcome, strDate, strMember, strOwner, mList)
            }
        }
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
