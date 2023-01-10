package com.glass.oceanbs.fragments.aftermarket.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.R
import com.glass.oceanbs.models.Chat
import com.glass.oceanbs.models.MessageType
import kotlinx.android.synthetic.main.card_item_chat_customer.view.*
import kotlinx.android.synthetic.main.card_item_chat_service.view.*

class ConversationItemAdapter(
    private var elements: List<Chat>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val customerId = 0
    private val noCustomerId = 2

    class ItemViewHolderCustomer(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ItemViewHolderService(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return when (elements[position].type) {
            MessageType.CUSTOMER -> customerId
            else -> noCustomerId // worker or automated
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            customerId -> ItemViewHolderCustomer(getView(parent, R.layout.card_item_chat_customer))
            else -> ItemViewHolderService(getView(parent, R.layout.card_item_chat_service))
        }
    }

    private fun getView(parent: ViewGroup, layout: Int): View {
        return LayoutInflater.from(parent.context).inflate(layout, parent, false)
    }

    override fun getItemCount() = elements.size

    var onLongClicked: ((String) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = elements[position]
        when (holder.itemViewType) {
            customerId -> {
                // customer card
                holder.itemView.txtMessageCustomer.setText(item.message)
                holder.itemView.txtDateCustomer.text = item.hour
                holder.itemView.txtMessageCustomer.setOnLongClickListener {
                    onLongClicked?.invoke(item.message)
                    true
                }
                holder.itemView.setOnLongClickListener {
                    onLongClicked?.invoke(item.message)
                    true
                }
            }
            noCustomerId -> {
                // worker card
                holder.itemView.txtMessageService.setText(item.message)
                if (item.workerName == "null") {
                    holder.itemView.txtWorkerNameService.visibility = View.GONE
                }
                holder.itemView.txtWorkerNameService.text = item.workerName
                holder.itemView.txtDateService.text = item.hour

                // hide hour text to avoid extra margin
                if (item.hour.isEmpty()) {
                    holder.itemView.txtDateService.visibility = View.GONE
                }

                // automatic response -> paint text and view as blue
                if (item.type == MessageType.AUTOMATED) {
                    holder.itemView.txtWorkerNameService.setTextColor(Color.BLUE)
                    holder.itemView.viewColorBottomS.setBackgroundColor(Color.BLUE)
                }
                holder.itemView.txtMessageService.setOnLongClickListener {
                    onLongClicked?.invoke(item.message)
                    true
                }
                holder.itemView.setOnLongClickListener {
                    onLongClicked?.invoke(item.message)
                    true
                }
            }
        }
    }
}
