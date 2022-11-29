package com.glass.oceanbs.fragments.aftermarket.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.R
import com.glass.oceanbs.models.Chat
import kotlinx.android.synthetic.main.card_item_chat_customer.view.*
import kotlinx.android.synthetic.main.card_item_chat_service.view.*

class ConversationItemAdapter(
    private var elements: List<Chat>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ItemViewHolderCustomer(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ItemViewHolderService(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return if (elements[position].isCustomer) 0 else 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> ItemViewHolderCustomer(getView(parent, R.layout.card_item_chat_service))
            else -> ItemViewHolderService(getView(parent, R.layout.card_item_chat_customer))
        }
    }

    private fun getView(parent: ViewGroup, layout: Int): View {
        return LayoutInflater.from(parent.context).inflate(layout, parent, false)
    }

    override fun getItemCount() = elements.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = elements[position]
        when (holder.itemViewType) {
            0 -> holder.itemView.txtMessageService.setText(item.message)
            2 -> holder.itemView.txtMessageCustomer.setText(item.message)
        }
    }
}
