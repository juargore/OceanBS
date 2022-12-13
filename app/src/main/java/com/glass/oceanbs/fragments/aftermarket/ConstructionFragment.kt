package com.glass.oceanbs.fragments.aftermarket

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.github.chrisbanes.photoview.PhotoView
import com.glass.oceanbs.Constants.ADDITIONAL_INFO
import com.glass.oceanbs.Constants.CAPTION
import com.glass.oceanbs.Constants.ESTIMATED_COMPLETION_DATE
import com.glass.oceanbs.Constants.GET_CONSTRUCTION_ITEMS
import com.glass.oceanbs.Constants.NOTICE
import com.glass.oceanbs.Constants.PHOTO1
import com.glass.oceanbs.Constants.PHOTO2
import com.glass.oceanbs.Constants.PHOTO3
import com.glass.oceanbs.Constants.PROGRESS
import com.glass.oceanbs.Constants.TITLE
import com.glass.oceanbs.Constants.UNITY_ID
import com.glass.oceanbs.Constants.URL_CONSTRUCTION_ITEMS
import com.glass.oceanbs.R
import com.glass.oceanbs.extensions.*
import com.glass.oceanbs.fragments.aftermarket.MainTracingFragment.Companion.desarrolloId
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.circular_progress.*
import okhttp3.*

class ConstructionFragment : Fragment() {

    private lateinit var layParentConstruction: LinearLayout
    private val photosList: ArrayList<String> = ArrayList()
    private var imageListener: ImageListener = ImageListener { position, imageView ->
        Picasso.get().load(photosList[position]).fit().into(imageView)
    }

    companion object {
        fun newInstance() = ConstructionFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View? {
        val root = infl.inflate(R.layout.fragment_construction, cont, false)
        initValidation(root)
        return root
    }

    private fun initValidation(root: View) {
        layParentConstruction = root.findViewById(R.id.layParentConstruction)

        if (desarrolloId != null) {
            layParentConstruction.show()
            getDataFromServer(root)
        } else {
            layParentConstruction.hide()
        }
    }

    private fun getDataFromServer(root: View) {
        activity?.getDataFromServer(
            webService = GET_CONSTRUCTION_ITEMS,
            url = URL_CONSTRUCTION_ITEMS,
            parent = layParentConstruction,
            parameters = listOf(Parameter(
                key = UNITY_ID,
                value = desarrolloId
            ))
        ) { jsonRes ->
            var progress = 0
            var estimatedDate = ""
            var additionalInfo = ""
            val title = jsonRes.getString(TITLE)
            val subtitle = jsonRes.getString(CAPTION)
            val arr = jsonRes.getJSONArray(NOTICE)

            if (arr.length() > 0) {
                val obj = arr.getJSONObject(0)
                progress = obj.getInt(PROGRESS)
                estimatedDate = obj.getString(ESTIMATED_COMPLETION_DATE)
                additionalInfo = obj.getString(ADDITIONAL_INFO)

                val photo1 = obj.getString(PHOTO1)
                val photo2 = obj.getString(PHOTO2)
                val photo3 = obj.getString(PHOTO3)

                if (photo1.isNotEmpty()) {
                    photosList.add(photo1)
                }
                if (photo2.isNotEmpty()) {
                    photosList.add(photo2)
                }
                if (photo3.isNotEmpty()) {
                    photosList.add(photo3)
                }
            }

            runOnUiThread {
                setupViews(
                    root = root,
                    title = title,
                    subtitle = subtitle,
                    progress = progress,
                    estimatedDate = estimatedDate,
                    additionalInfo = additionalInfo
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews(
        root: View,
        title: String,
        subtitle: String,
        progress: Int,
        estimatedDate: String,
        additionalInfo: String
    ) {
        val etDate = root.findViewById<EditText>(R.id.etDate)
        val etInfo = root.findViewById<EditText>(R.id.etInfo)
        val mTitle = root.findViewById<TextView>(R.id.txtTitle)
        val mSubtitle = root.findViewById<TextView>(R.id.txtSubtitle)
        val mProgressBar = root.findViewById<View>(R.id.circularProgress)
        val str = mProgressBar?.findViewById<TextView>(R.id.progress_tv)
        val pr = mProgressBar?.findViewById<ProgressBar>(R.id.circular_determinative_pb)

        mTitle?.text = title
        mSubtitle?.text = subtitle
        etDate?.setText(estimatedDate)
        etInfo?.setText(additionalInfo)
        str?.text = "$progress%"
        pr?.progress = progress

        setImagesInCarousel(root)
    }

    private fun setImagesInCarousel(root: View) {
        root.findViewById<CarouselView>(R.id.photosViewer).apply {
            setImageListener(imageListener)
            pageCount = photosList.size
            setImageClickListener { position ->
                showImageOnPopup(photosList[position])
            }
        }
    }

    private fun showImageOnPopup(url: String) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.popup_image_view, null)
        dialogBuilder.setView(dialogView)

        val image = dialogView.findViewById<PhotoView>(R.id.imageViewPop)
        Picasso.get().load(url).into(image)

        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.show()
    }
}
