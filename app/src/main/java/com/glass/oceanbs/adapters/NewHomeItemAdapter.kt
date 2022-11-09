package com.glass.oceanbs.adapters

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.R
import com.glass.oceanbs.activities.MainActivity
import com.glass.oceanbs.models.ItemNewHome
import kotlinx.android.synthetic.main.card_item_new_home.view.*

class NewHomeItemAdapter(
    private var elements: List<ItemNewHome>
) : RecyclerView.Adapter<NewHomeItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_new_home, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount() = elements.size

    var onItemClicked: ((Intent?, String?) -> Unit)? = null

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = elements[position]

        with(holder.itemView) {
            txtTitle.text = item.title
            txtSubTitle.text = item.subtitle
            viewColorBottom.setBackgroundColor(Color.parseColor(item.hexColor))
            viewText.setBackgroundColor(Color.parseColor(item.hexColor))

            var intent: Intent? = null
            var url: String? = null

            when (position) {
                0 -> intent = Intent(context, MainActivity::class.java)
                1 -> intent = Intent(context, MainActivity::class.java)
                else -> url = item.url
            }

            setOnClickListener {
                onItemClicked?.invoke(intent, url)
            }
        }
    }
}