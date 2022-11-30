package com.glass.oceanbs.fragments.aftermarket.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.R
import com.glass.oceanbs.extensions.invisible
import com.glass.oceanbs.extensions.show
import com.glass.oceanbs.models.ItemSummary
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_item_summary.view.*

class SummaryItemAdapter(
    private var elements: List<ItemSummary>
): RecyclerView.Adapter<SummaryItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_summary, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount() = elements.size

    private var onItemClicked: ((ItemSummary) -> Unit)? = null

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = elements[position]

        with(holder.itemView) {
            txtTitle.text = item.title
            txtSubTitle.text = item.subtitle
            txtPercentage.text =
                if (item.percentage == "0%") "" else item.percentage

            if (item.isDone) {
                imgDone.show()
                Picasso.get().load(item.urlImgDone).into(imgDone)
            } else {
                imgDone.invisible()
            }

            viewColorBottom.setBackgroundColor(Color.parseColor(item.hexColor))
            txtPercentage.setBackgroundColor(Color.parseColor(item.hexColor))
            if (item.urlImgLeft.isNotEmpty()) {
                Picasso.get().load(item.urlImgLeft).into(imgLeft)
            }

            setOnClickListener { onItemClicked?.invoke(item) }
        }
    }
}
