package com.glass.oceanbs.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.glass.oceanbs.R
import com.glass.oceanbs.adapters.ParentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var tabsParent: TabLayout
    private lateinit var viewPagerParent: ViewPager
    private lateinit var carouselView: CarouselView

    private val bitmapList: ArrayList<Bitmap> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        initComponents()
        setUpTabs()
        setImagesInCarousel()
    }

    private fun initComponents(){
        carouselView = findViewById(R.id.carouselView)
        tabsParent = findViewById(R.id.tabsParent)
        viewPagerParent = findViewById(R.id.viewPagerParent)

        bitmapList.add(BitmapFactory.decodeResource(resources,
            R.drawable.photo1))
        bitmapList.add(BitmapFactory.decodeResource(resources,
            R.drawable.photo2))
        bitmapList.add(BitmapFactory.decodeResource(resources,
            R.drawable.photo3))
        bitmapList.add(BitmapFactory.decodeResource(resources,
            R.drawable.photo4))
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
}
