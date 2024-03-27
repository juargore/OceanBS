package com.ocean.oceanbs.activities

import android.os.Bundle
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.ocean.oceanbs.Constants.PHOTOS
import com.ocean.oceanbs.Constants.getUserName
import com.ocean.oceanbs.R
import com.ocean.oceanbs.extensions.getUserTypeStr
import com.ocean.oceanbs.extensions.showExitDialog
import com.ocean.oceanbs.fragments.aftermarket.adapters.AftermarketPagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener
import java.util.*

class AftermarketActivity : BaseActivity() {

    private lateinit var txtUserName: TextView
    private lateinit var txtOwner: TextView
    private lateinit var fabExit: FloatingActionButton
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var carouselView: CarouselView

    private var photosList = listOf<String>()
    private var imageListener: ImageListener = ImageListener { position, imageView ->
        Picasso.get().load(photosList[position]).fit().into(imageView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aftermarket)

        txtUserName = findViewById(R.id.txtUserName)
        txtOwner = findViewById(R.id.txtOwner)
        fabExit = findViewById(R.id.fabExit)
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        carouselView = findViewById(R.id.carouselView)

        setUpTabs()

        intent.extras?.let {
            photosList = it.getStringArrayList(PHOTOS)!!.toList()
            setImagesInCarousel()
        }
    }

    private fun setUpTabs() {
        txtUserName.text = getUserName(this).uppercase(Locale.getDefault())
        txtOwner.text = getUserTypeStr(this)
        fabExit.setOnClickListener { showExitDialog() }
        viewPager.adapter = AftermarketPagerAdapter(this, supportFragmentManager)
        viewPager.offscreenPageLimit = 2
        tabLayout.setupWithViewPager(viewPager)

        // actionScreen == 1.0 -> MainActivity::class.java
        // actionScreen == 2.0 -> AfterMarketActivity::class.java
        // actionScreen == 2.1 -> AfterMarketActivity::class.java + ConstructionFragment
        // actionScreen == 2.2 -> AfterMarketActivity::class.java + DocumentationFragment
        // actionScreen == 3.0 -> AfterMarketActivity::class.java + MainConversationFragment
        if (NotificationActivity.actionScreen == 3.0f) {
            changeToConversationTab()
            NotificationActivity.actionScreen = 0.0f
        }
    }

    fun changeToConversationTab() {
        viewPager.currentItem = 1
    }

    private fun setImagesInCarousel() {
        carouselView.setImageListener(imageListener)
        carouselView.pageCount = photosList.size
    }

    override fun onStop() {
        super.onStop()
        NotificationActivity.actionScreen = 0.0f
    }
}
