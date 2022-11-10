package com.glass.oceanbs.fragments.aftermarket

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.github.chrisbanes.photoview.PhotoView
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.snackbar
import com.glass.oceanbs.R
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.circular_progress.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ConstructionFragment : Fragment() {

    private var root: View? = null
    private val photosList: ArrayList<String> = ArrayList()
    private var viewer: CarouselView? = null
    private var imageListener: ImageListener = ImageListener { position, imageView ->
        Picasso.get().load(photosList[position]).fit().into(imageView)
    }

    companion object {
        fun newInstance() = ConstructionFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View? {
        if (root == null) {
            root = infl.inflate(R.layout.fragment_construction, cont, false)
            setupViews()
            getBottomPhotosFromServer()
        }
        return root
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        val progress = 80 // todo: set real percentage from Server
        val progressBar = root?.findViewById<View>(R.id.circularProgress)
        val str = progressBar?.findViewById<TextView>(R.id.progress_tv)
        val pr = progressBar?.findViewById<ProgressBar>(R.id.circular_determinative_pb)
        str?.text = "$progress%"
        pr?.progress = progress
    }

    private fun getBottomPhotosFromServer() {
        val client = OkHttpClient()
        val builder = FormBody.Builder().add(Constants.WEB_SERVICE, Constants.GET_CAROUSEL).build()
        val request = Request.Builder().url(Constants.URL_IMAGES_CAROUSEL).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    val jsonRes = JSONObject(response.body!!.string())
                    if (jsonRes.getInt(Constants.ERROR) > 0) {
                        showSnackBar(jsonRes.getString(Constants.MESSAGE))
                    } else {
                        val arr = jsonRes.getJSONArray(Constants.DATA)
                        for (i in 0 until arr.length()){
                            val j = arr.getJSONObject(i)
                            photosList.add(j.getString(Constants.PHOTO))
                        }
                        setImagesInCarousel()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                showSnackBar(e.message.toString())
            }
        })
    }

    private fun setImagesInCarousel() {
        viewer = root?.findViewById(R.id.photosViewer)
        viewer?.setImageListener(imageListener)
        viewer?.pageCount = photosList.size
        viewer?.setImageClickListener { position ->
            showImageOnPopup(photosList[position])
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

    private fun showSnackBar(str: String) {
        root?.findViewById<View>(R.id.parent)?.let {
            activity?.runOnUiThread { snackbar(requireContext(), it, str, Constants.Types.ERROR) }
        }
    }
}
