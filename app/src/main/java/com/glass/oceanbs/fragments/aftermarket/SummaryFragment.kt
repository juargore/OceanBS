package com.glass.oceanbs.fragments.aftermarket

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.R
import com.glass.oceanbs.fragments.aftermarket.adapters.SummaryItemAdapter
import com.glass.oceanbs.models.ItemSummary

class SummaryFragment : Fragment() {

    private var root: View? = null

    companion object {
        fun newInstance() = SummaryFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View? {
        if (root == null) {
            root = infl.inflate(R.layout.fragment_summary, cont, false)
            setupRecycler()
        }
        return root
    }

    private fun setupRecycler() {
        with(SummaryItemAdapter(getList())) {
            root?.findViewById<RecyclerView>(R.id.rvSummary)?.let {
                it.adapter = this
                this.onItemClicked = {

                }
            }
        }
    }

    // todo: get real data from Server
    private fun getList() = listOf(
        ItemSummary(
            id = 0,
            isDone = true,
            title = "AVANCE DE OBRA",
            subtitle = "Realizado.",
            hexColor = "#ED6825",
            percentage = "100%",
            urlImgLeft = "https://cdn4.iconfinder.com/data/icons/pictype-free-vector-icons/16/home-512.png",
            urlImgDone = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c6/Sign-check-icon.png/800px-Sign-check-icon.png"
        ),
        ItemSummary(
            id = 1,
            isDone = false,
            title = "AVANCE DOCUMENTAL",
            subtitle = "En proceso...",
            hexColor = "#ED6825",
            percentage = "30%",
            urlImgLeft = "http://cdn.onlinewebfonts.com/svg/img_18374.png",
            urlImgDone = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c6/Sign-check-icon.png/800px-Sign-check-icon.png"
        ),
        ItemSummary(
            id = 2,
            isDone = false,
            title = "AVANCE DE OBRA",
            subtitle = "Pendiente.",
            hexColor = "#636363",
            percentage = "0%",
            urlImgLeft = "https://cdn-icons-png.flaticon.com/512/55/55281.png",
            urlImgDone = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c6/Sign-check-icon.png/800px-Sign-check-icon.png"
        ),
        ItemSummary(
            id = 3,
            isDone = false,
            title = "AVANCE DE OBRA",
            subtitle = "Pendiente.",
            hexColor = "#636363",
            percentage = "0%",
            urlImgLeft = "https://www.kindpng.com/picc/m/80-803313_transparent-warranty-icon-png-emblem-png-download.png",
            urlImgDone = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c6/Sign-check-icon.png/800px-Sign-check-icon.png"
        )
    )
}
