package com.glass.oceanbs.fragments.aftermarket.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.R
import com.glass.oceanbs.models.History
import kotlinx.android.synthetic.main.card_item_history.view.*

class HistoryItemAdapter(
    private var elements: List<History>
): RecyclerView.Adapter<HistoryItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_history, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount() = elements.size

    private var onItemClicked: ((History) -> Unit)? = null

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = elements[position]
        with (holder.itemView) {
            txtTitle.text = item.title
            txtSubTitle.text = item.subtitle
            txtUnidad.text = item.unityCode
            txtDate.text = item.date
            viewColorBottom.setBackgroundColor(Color.parseColor(item.hexColor))
            txtUnidad.setBackgroundColor(Color.parseColor(item.hexColor))
            setOnClickListener { onItemClicked?.invoke(item) }
        }
    }
}
