package com.glass.oceanbs.fragments.aftermarket

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.R
import com.glass.oceanbs.extensions.hide
import com.glass.oceanbs.extensions.show
import com.glass.oceanbs.fragments.aftermarket.adapters.HistoryItemAdapter
import com.glass.oceanbs.models.History

class HistoryFragment : Fragment() {

    private var root: View? = null

    companion object {
        fun newInstance() = HistoryFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View? {
        root = infl.inflate(R.layout.fragment_history, cont, false)
        initValidation()
        return root
    }

    private fun initValidation() {
        val parent = root?.findViewById<ConstraintLayout>(R.id.layParentHistory)
        val desarrolloId = MainTracingFragment.desarrolloId

        println("AQUI: Id en History: $desarrolloId")

        if (desarrolloId != null) {
            parent?.show()
            setUpRecycler()
        } else {
            parent?.hide()
        }
    }

    private fun setUpRecycler() {
        with (HistoryItemAdapter(getList())) {
            root?.findViewById<RecyclerView>(R.id.rvHistory)?.let {
                it.adapter = this
                this.onItemClicked = {

                }
            }
        }
    }

    // todo: get real data from Server
    private fun getList() = listOf(
        History(
            id = 0,
            title = "AVISO DE CONSTRUCCIÓN",
            subtitle = "50% de avance",
            hexColor = "#FFB264",
            unityCode = "PS103",
            date = "2020-04-03"
        ),
        History(
            id = 1,
            title = "AVISO DE CONSTRUCCIÓN",
            subtitle = "40% de avance",
            hexColor = "#FFB264",
            unityCode = "PS102",
            date = "2020-04-03"
        ),
        History(
            id = 2,
            title = "AVISO DE CONSTRUCCIÓN",
            subtitle = "30% de avance",
            hexColor = "#FFB264",
            unityCode = "PS101",
            date = "2020-04-03"
        ),
    )
}
