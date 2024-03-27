package com.ocean.oceanbs.fragments.aftermarket.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ocean.oceanbs.R
import com.ocean.oceanbs.models.History

class HistoryItemAdapter(
    private var elements: List<History>
): RecyclerView.Adapter<HistoryItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_history, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount() = elements.size

    var onItemClicked: ((History) -> Unit)? = null

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val txtTitle: TextView = holder.itemView.findViewById(R.id.txtTitle)
        val txtSubTitle: TextView = holder.itemView.findViewById(R.id.txtSubTitle)
        val txtUnidad: TextView = holder.itemView.findViewById(R.id.txtUnidad)
        val txtDate: TextView = holder.itemView.findViewById(R.id.txtDate)
        val viewColorBottom: View = holder.itemView.findViewById(R.id.viewColorBottom)

        val item = elements[position]
        with (holder.itemView) {
            txtTitle.text = item.title
            txtSubTitle.text = item.subtitle
            txtUnidad.text = item.unityCode
            txtDate.text = item.creationDate
            viewColorBottom.setBackgroundColor(Color.parseColor(item.hexColor))
            txtUnidad.setBackgroundColor(Color.parseColor(item.hexColor))
            setOnClickListener { onItemClicked?.invoke(item) }
        }
    }
}
