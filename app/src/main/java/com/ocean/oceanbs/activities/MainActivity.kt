package com.ocean.oceanbs.activities

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.ocean.oceanbs.Constants.PHOTOS
import com.ocean.oceanbs.R
import com.ocean.oceanbs.adapters.ParentPagerAdapter
import com.ocean.oceanbs.extensions.showExitDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener

class MainActivity : BaseActivity() {

    private var photosList = listOf<String>()
    private var imageListener: ImageListener = ImageListener { position, imageView ->
        Picasso.get().load(photosList[position]).fit().into(imageView)
    }

    companion object {
        private lateinit var mTabsParent: TabLayout
        private lateinit var mViewPagerParent: ViewPager
        fun goToFirstTab() { mViewPagerParent.currentItem = 0 }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initComponents()
        setUpTabs()

        intent.extras?.let {
            photosList = it.getStringArrayList(PHOTOS)!!.toList()
            setImagesInCarousel()
        }
    }

    private fun initComponents() {
        mTabsParent = findViewById(R.id.tabsParent)
        mViewPagerParent = findViewById(R.id.viewPagerParent)
        val fabExit: FloatingActionButton = findViewById(R.id.fabExit)
        fabExit.setOnClickListener { showExitDialog() }
    }

    private fun setUpTabs() {
        mViewPagerParent.adapter = ParentPagerAdapter(this, supportFragmentManager)
        mViewPagerParent.offscreenPageLimit = 2

        val viewPagerParent: ViewPager = findViewById(R.id.viewPagerParent)
        mTabsParent.setupWithViewPager(viewPagerParent)
    }

    private fun setImagesInCarousel() {
        val carouselView: CarouselView = findViewById(R.id.carouselView)
        carouselView.setImageListener(imageListener)
        carouselView.pageCount = photosList.size
    }
}
