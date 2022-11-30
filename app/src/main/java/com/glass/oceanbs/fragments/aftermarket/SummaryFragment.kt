package com.glass.oceanbs.fragments.aftermarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.Constants.CAPTION
import com.glass.oceanbs.Constants.COLOR
import com.glass.oceanbs.Constants.GET_SUMMARY_ITEMS
import com.glass.oceanbs.Constants.IMAGE
import com.glass.oceanbs.Constants.OPTIONS
import com.glass.oceanbs.Constants.PROGRESS
import com.glass.oceanbs.Constants.TITLE
import com.glass.oceanbs.Constants.URL_SUMMARY_ITEMS
import com.glass.oceanbs.R
import com.glass.oceanbs.extensions.getDataFromServer
import com.glass.oceanbs.extensions.hide
import com.glass.oceanbs.extensions.runOnUiThread
import com.glass.oceanbs.extensions.show
import com.glass.oceanbs.fragments.aftermarket.adapters.SummaryItemAdapter
import com.glass.oceanbs.models.ItemSummary

class SummaryFragment : Fragment() {

    private lateinit var root: View
    private lateinit var txtTop: TextView
    private lateinit var layParentSummary: LinearLayout

    companion object {
        fun newInstance() = SummaryFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View {
        root = infl.inflate(R.layout.fragment_summary, cont, false)
        initValidation()
        return root
    }

    private fun initValidation() {
        layParentSummary = root.findViewById(R.id.layParentSummary)
        txtTop = root.findViewById(R.id.txtTop)
        val desarrolloId = MainTracingFragment.desarrolloId
        if (desarrolloId != null) {
            layParentSummary.show()
            getItemsFromServer()
        } else {
            layParentSummary.hide()
        }
    }

    private fun getItemsFromServer() {
        activity?.getDataFromServer(
            webService = GET_SUMMARY_ITEMS,
            url = URL_SUMMARY_ITEMS,
            parent = layParentSummary
        ) { jsonRes ->
            val caption = jsonRes.getString(CAPTION)
            val arr = jsonRes.getJSONArray(OPTIONS)
            val mList = mutableListOf<ItemSummary>()

            for (i in 0 until arr.length()) {
                val j = arr.getJSONObject(i)
                val progress = j.getString(PROGRESS)
                mList.add(
                    ItemSummary(
                        isDone = progress == "100%",
                        title = j.getString(TITLE),
                        subtitle = j.getString(CAPTION),
                        hexColor = j.getString(COLOR),
                        percentage = progress,
                        urlImgLeft = j.getString(IMAGE)
                    )
                )
            }
            setupViews(caption, mList)
        }
    }

    private fun setupViews(caption: String, list: List<ItemSummary>) {
        runOnUiThread {
            txtTop.text = caption
            setupRecycler(list)
        }
    }

    private fun setupRecycler(list: List<ItemSummary>) {
        root.findViewById<RecyclerView>(R.id.rvSummary)?.let {
            it.adapter = SummaryItemAdapter(list)
        }
    }
}
