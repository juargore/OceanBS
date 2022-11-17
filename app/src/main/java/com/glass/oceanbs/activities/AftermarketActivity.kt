package com.glass.oceanbs.activities

import android.os.Bundle
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.getUserName
import com.glass.oceanbs.R
import com.glass.oceanbs.extensions.getUserTypeStr
import com.glass.oceanbs.extensions.showExitDialog
import com.glass.oceanbs.fragments.aftermarket.adapters.AftermarketPagerAdapter
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.activity_aftermarket.*
import kotlinx.android.synthetic.main.activity_aftermarket.carouselView
import java.util.*

class AftermarketActivity : BaseActivity() {

    private var photosList = listOf<String>()
    private var imageListener: ImageListener = ImageListener { position, imageView ->
        Picasso.get().load(photosList[position]).fit().into(imageView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aftermarket)
        setUpTabs()
        intent.extras?.let {
            photosList = it.getStringArrayList(Constants.PHOTOS)!!.toList()
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
    }

    private fun setImagesInCarousel() {
        carouselView.setImageListener(imageListener)
        carouselView.pageCount = photosList.size
    }
}
