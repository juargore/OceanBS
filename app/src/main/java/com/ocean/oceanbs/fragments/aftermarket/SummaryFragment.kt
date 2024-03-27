package com.ocean.oceanbs.fragments.aftermarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ocean.oceanbs.Constants
import com.ocean.oceanbs.Constants.CAPTION
import com.ocean.oceanbs.Constants.COLOR
import com.ocean.oceanbs.Constants.GET_SUMMARY_ITEMS
import com.ocean.oceanbs.Constants.IMAGE
import com.ocean.oceanbs.Constants.OPTIONS
import com.ocean.oceanbs.Constants.PROGRESS
import com.ocean.oceanbs.Constants.TITLE
import com.ocean.oceanbs.Constants.URL_SUMMARY_ITEMS
import com.ocean.oceanbs.R
import com.ocean.oceanbs.extensions.*
import com.ocean.oceanbs.fragments.aftermarket.MainTracingFragment.Companion.desarrolloId
import com.ocean.oceanbs.fragments.aftermarket.adapters.SummaryItemAdapter
import com.ocean.oceanbs.models.ItemSummary

class SummaryFragment : Fragment() {

    private lateinit var txtTop: TextView
    private lateinit var layParentSummary: LinearLayout

    companion object {
        fun newInstance() = SummaryFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View {
        val root = infl.inflate(R.layout.fragment_summary, cont, false)
        initValidation(root)
        return root
    }

    private fun initValidation(root: View) {
        layParentSummary = root.findViewById(R.id.layParentSummary)
        txtTop = root.findViewById(R.id.txtTop)
        val rv = root.findViewById<RecyclerView>(R.id.rvSummary)
        val txtSelect = root.findViewById<TextView>(R.id.txtSelect)

        if (desarrolloId != null) {
            rv.show()
            txtTop.show()
            txtSelect.hide()
            getItemsFromServer(root)
        } else {
            rv.hide()
            txtTop.hide()
            txtSelect.show()
        }
    }

    private fun getItemsFromServer(root: View) {
        activity?.getDataFromServer(
            webService = GET_SUMMARY_ITEMS,
            url = URL_SUMMARY_ITEMS,
            parent = layParentSummary,
            parameters = listOf(
                Parameter(Constants.UNITY_ID, desarrolloId)
            )
        ) { jsonRes ->
            val caption = jsonRes.getString(CAPTION)
            val arr = jsonRes.getJSONArray(OPTIONS)
            val mList = mutableListOf<ItemSummary>()

            for (i in 0 until arr.length()) {
                val j = arr.getJSONObject(i)
                val progress = j.getString(PROGRESS) // Avance
                val isDone = progress == "100%" || progress == "Fase 5"
                mList.add(
                    ItemSummary(
                        isDone = isDone,
                        title = j.getString(TITLE), // Titulo
                        subtitle = j.getString(CAPTION), // Leyenda
                        hexColor = j.getString(COLOR), // Color
                        percentage = progress,
                        urlImgLeft = j.getString(IMAGE) // Imagen
                    )
                )
            }
            setupViews(root, caption, mList)
        }
    }

    private fun setupViews(root: View, caption: String, list: List<ItemSummary>) {
        runOnUiThread {
            txtTop.text = caption
            setupRecycler(root, list)
        }
    }

    private fun setupRecycler(root: View, list: List<ItemSummary>) {
        root.findViewById<RecyclerView>(R.id.rvSummary)?.let {
            val mAdapter = SummaryItemAdapter(list)
            it.adapter = mAdapter
            mAdapter.onItemClicked = { item ->
                if (item.title.contains("CONSTRUCCI")) {
                    (parentFragment as MainTracingFragment).changeToConstructionTab(null)
                }
                if (item.title.contains("DOCUMENTACI")) {
                    (parentFragment as MainTracingFragment).changeToDocumentationTab(null)
                }
            }
        }
    }
}