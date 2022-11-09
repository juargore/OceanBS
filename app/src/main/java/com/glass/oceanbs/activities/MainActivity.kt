package com.glass.oceanbs.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.glass.oceanbs.Constants.PHOTOS
import com.glass.oceanbs.R
import com.glass.oceanbs.adapters.ParentPagerAdapter
import com.glass.oceanbs.extensions.showExitDialog
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var photosList = listOf<String>()

    companion object {
        private lateinit var mTabsParent: TabLayout
        private lateinit var mViewPagerParent: ViewPager

        fun goToFirstTab() {
            mViewPagerParent.currentItem = 0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
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
        fabExit.setOnClickListener { showExitDialog() }
    }

    private fun setUpTabs() {
        mViewPagerParent.adapter = ParentPagerAdapter(this, supportFragmentManager)
        mViewPagerParent.offscreenPageLimit = 2
        mTabsParent.setupWithViewPager(viewPagerParent)
    }

    private var imageListener: ImageListener = ImageListener { position, imageView ->
        Picasso.get().load(photosList[position]).fit().into(imageView)
    }

    private fun setImagesInCarousel() {
        carouselView.setImageListener(imageListener)
        carouselView.pageCount = photosList.size
    }
}
