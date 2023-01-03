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
import androidx.core.widget.NestedScrollView
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

    data class PicassoImage(
        val url: String?,
        val resource: Int?
    )

    private lateinit var root: View
    private lateinit var layParentConstruction: LinearLayout
    private lateinit var select: TextView
    private lateinit var scroll: NestedScrollView
    private val photosList: ArrayList<PicassoImage> = ArrayList()
    private var imageListener: ImageListener = ImageListener { position, imageView ->
        photosList[position].url?.let {
            Picasso.get().load(it).fit().into(imageView)
        }
        photosList[position].resource?.let {
            Picasso.get().load(it).fit().into(imageView)
        }
    }

    companion object {
        fun newInstance() = ConstructionFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View {
        root = infl.inflate(R.layout.fragment_construction, cont, false)
        layParentConstruction = root.findViewById(R.id.layParentConstruction)
        select = root.findViewById(R.id.txtSelectC)
        scroll = root.findViewById(R.id.scrollC)
        return root
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (desarrolloId != null) {
                scroll.show()
                select.hide()
                val item = MainTracingFragment.itemConstruction
                if (item != null) {
                    val photo1 = item.photo1
                    val photo2 = item.photo2
                    val photo3 = item.photo3
                    photosList.clear()

                    if (photo1.isNotEmpty()) {
                        photosList.add(PicassoImage(url = photo1, resource = null))
                    } else {
                        photosList.add(PicassoImage(url = null, resource = R.drawable.img_no_photo))
                    }
                    if (photo2.isNotEmpty()) {
                        photosList.add(PicassoImage(url = photo2, resource = null))
                    } else {
                        photosList.add(PicassoImage(url = null, resource = R.drawable.img_no_photo))
                    }
                    if (photo3.isNotEmpty()) {
                        photosList.add(PicassoImage(url = photo3, resource = null))
                    } else {
                        photosList.add(PicassoImage(url = null, resource = R.drawable.img_no_photo))
                    }

                    setupViews(
                        title = item.title,
                        subtitle = item.subtitle,
                        progress = item.progress,
                        estimatedDate = item.estimatedDate,
                        additionalInfo = item.additionalInfo
                    )
                } else {
                    getDataFromServer()
                }
            } else {
                scroll.hide()
                select.show()
            }
        }
    }

    private fun getDataFromServer() {
        activity?.getDataFromServer(
            showLoader = false,
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
                photosList.clear()

                /*photosList.add(PicassoImage(
                    url = "https://img.gtsstatic.net/reno/imagereader.aspx?imageurl=https%3A%2F%2Fsir.azureedge.net%2F1194i215%2F4yxhc7f2612047pche26ey8z57i215&option=N&h=472&permitphotoenlargement=false",
                    resource = null
                ))*/

                if (photo1.isNotEmpty()) {
                    photosList.add(PicassoImage(url = photo1, resource = null))
                } else {
                    photosList.add(PicassoImage(url = null, resource = R.drawable.img_no_photo))
                }
                if (photo2.isNotEmpty()) {
                    photosList.add(PicassoImage(url = photo2, resource = null))
                } else {
                    photosList.add(PicassoImage(url = null, resource = R.drawable.img_no_photo))
                }
                if (photo3.isNotEmpty()) {
                    photosList.add(PicassoImage(url = photo3, resource = null))
                } else {
                    photosList.add(PicassoImage(url = null, resource = R.drawable.img_no_photo))
                }
            }

            runOnUiThread {
                setupViews(
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

        setImagesInCarousel()
    }

    private fun setImagesInCarousel() {
        root.findViewById<CarouselView>(R.id.photosViewer).apply {
            setImageListener(imageListener)
            pageCount = photosList.size
            setImageClickListener { position ->
                photosList[position].url?.let {
                    showImageOnPopup(it)
                }
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

        alertDialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }
}
