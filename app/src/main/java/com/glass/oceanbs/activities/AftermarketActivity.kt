package com.glass.oceanbs.activities

import android.os.Bundle
import com.glass.oceanbs.Constants
import com.glass.oceanbs.R
import com.glass.oceanbs.extensions.showExitDialog
import com.glass.oceanbs.fragments.aftermarket.adapters.AftermarketPagerAdapter
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.activity_aftermarket.*
import kotlinx.android.synthetic.main.activity_aftermarket.carouselView

class AftermarketActivity : BaseActivity() {

    private var photosList = listOf<String>()
    private var imageListener: ImageListener = ImageListener { position, imageView ->
        Picasso.get().load(photosList[position]).fit().into(imageView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aftermarket)
        supportActionBar?.hide()
        setUpTabs()
        intent.extras?.let {
            photosList = it.getStringArrayList(Constants.PHOTOS)!!.toList()
            setImagesInCarousel()
        }
    }

    private fun setUpTabs() {
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
