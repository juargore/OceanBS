package com.glass.oceanbs.fragments.aftermarket

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.Constants.GET_HISTORY_ITEMS
import com.glass.oceanbs.Constants.NOTICES
import com.glass.oceanbs.Constants.NOTICE_TYPE
import com.glass.oceanbs.Constants.UNITY_ID
import com.glass.oceanbs.Constants.URL_HISTORY_ITEMS
import com.glass.oceanbs.R
import com.glass.oceanbs.extensions.*
import com.glass.oceanbs.fragments.aftermarket.MainTracingFragment.Companion.desarrolloCode
import com.glass.oceanbs.fragments.aftermarket.MainTracingFragment.Companion.desarrolloId
import com.glass.oceanbs.fragments.aftermarket.adapters.HistoryItemAdapter
import com.glass.oceanbs.models.History
import com.glass.oceanbs.models.HistorySpinner

class HistoryFragment : Fragment() {

    companion object {
        fun newInstance() = HistoryFragment()
    }

    private val historySpinnerList: ArrayList<HistorySpinner> = ArrayList()
    private lateinit var layParentHistory: ConstraintLayout
    private lateinit var spinnerHistory: Spinner
    private lateinit var txtEmpty: TextView

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View? {
        val root = infl.inflate(R.layout.fragment_history, cont, false)
        initValidation(root)
        return root
    }

    private fun initValidation(root: View) {
        layParentHistory = root.findViewById(R.id.layParentHistory)

        if (desarrolloId != null) {
            layParentHistory.show()
            fillSpinnerData(root)
        } else {
            layParentHistory.hide()
        }
    }

    private fun fillSpinnerData(root: View) {
        historySpinnerList.clear()
        historySpinnerList.add(HistorySpinner(0, "Todos los Avisos"))
        historySpinnerList.add(HistorySpinner(1, "Avisos de Construcción"))
        historySpinnerList.add(HistorySpinner(2, "Avisos de Documentación"))

        val mList = mutableListOf<String>()
        historySpinnerList.forEach { mList.add(it.name) }
        spinnerHistory = root.findViewById(R.id.spinnerHistory)
        txtEmpty = root.findViewById(R.id.txtEmpty)

        with(spinnerHistory) {
            adapter = ArrayAdapter(requireContext(), R.layout.spinner_text, mList)
            onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    getDataFromServer(root, historySpinnerList[pos].id)
                }
            }
        }
    }

    // todo: truena cuando data viene null o empty -> verificar
    private fun getDataFromServer(root: View, noticeInt: Int) {
        activity?.getDataFromServer(
            webService = GET_HISTORY_ITEMS,
            url = URL_HISTORY_ITEMS,
            parent = layParentHistory,
            parameters = listOf(
                Parameter(UNITY_ID, desarrolloId),
                Parameter(NOTICE_TYPE, noticeInt.toString())
            )
        ) { jsonRes ->
            val arr = jsonRes.getJSONArray(NOTICES)
            val mList = mutableListOf<History>()
            for (i in 0 until arr.length()) {
                val j = arr.getJSONObject(i)
                mList.add(
                    History(
                        id = j.getInt("Id"),
                        title = j.getString("Titulo"),
                        subtitle = j.getString("LeyendaAvance"),
                        hexColor = "#FFB264",
                        unityCode = desarrolloCode.toString(),
                        date = j.getString("FechaEstimada"),
                        additionalInfo = j.getString("InformacionAdicional"),
                        progress = j.getInt("Avance"),
                        phase = j.getInt("Fase"),
                        photo1 = j.getString("Fotografia1"),
                        photo2 = j.getString("Fotografia2"),
                        photo3 = j.getString("Fotografia3"),
                    )
                )
            }

            runOnUiThread {
                if (mList.isEmpty()) {
                    txtEmpty.show()
                } else {
                    txtEmpty.hide()
                }
                setUpRecycler(root, mList)
            }
        }
    }

    private fun setUpRecycler(root: View, list: List<History>) {
        with (HistoryItemAdapter(list)) {
            root.findViewById<RecyclerView>(R.id.rvHistory)?.let {
                it.adapter = this
                onItemClicked = { item ->
                    if (item.title.contains("CONSTRUCCI")) {
                        (parentFragment as MainTracingFragment).changeToConstructionTab(item)
                    }
                    if (item.title.contains("DOCUMENTACI")) {
                        (parentFragment as MainTracingFragment).changeToDocumentationTab(item)
                    }
                }
            }
        }
    }

    @Suppress("unused")
    private fun getTestList() = listOf(
        History(
            id = 0,
            title = "AVISO DE CONSTRUCCIÓN",
            subtitle = "50% de avance",
            hexColor = "#FFB264",
            unityCode = "PS103",
            date = "2020-04-03",
            additionalInfo = "no info",
            progress = 0,
            phase = 0,
            photo1 = "",
            photo2 = "",
            photo3 = "",
        )
    )
}
