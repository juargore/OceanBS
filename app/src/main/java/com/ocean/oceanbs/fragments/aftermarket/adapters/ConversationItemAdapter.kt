package com.ocean.oceanbs.fragments.aftermarket.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ocean.oceanbs.R
import com.ocean.oceanbs.models.Chat
import com.ocean.oceanbs.models.MessageType

class ConversationItemAdapter(
    private var elements: List<Chat>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val customerId = 0
    private val noCustomerId = 2

    class ItemViewHolderCustomer(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMessageCustomer: TextView = itemView.findViewById(R.id.txtMessageCustomer)
        val txtDateCustomer: TextView = itemView.findViewById(R.id.txtDateCustomer)
    }

    class ItemViewHolderService(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMessageService: TextView = itemView.findViewById(R.id.txtMessageService)
        val txtWorkerNameService: TextView = itemView.findViewById(R.id.txtWorkerNameService)
        val txtDateService: TextView = itemView.findViewById(R.id.txtDateService)
        val viewColorBottomS: View = itemView.findViewById(R.id.viewColorBottomS)
    }

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
                (holder as ItemViewHolderCustomer).txtMessageCustomer.text = item.message
                holder.txtDateCustomer.text = item.hour
                holder.txtMessageCustomer.setOnLongClickListener {
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
                (holder as ItemViewHolderService).txtMessageService.text = item.message
                if (item.workerName == "null") {
                    holder.txtWorkerNameService.visibility = View.GONE
                }
                holder.txtWorkerNameService.text = item.workerName
                holder.txtDateService.text = item.hour

                // hide hour text to avoid extra margin
                if (item.hour.isEmpty()) {
                    holder.txtDateService.visibility = View.GONE
                }

                // automatic response -> paint text and view as blue
                if (item.type == MessageType.AUTOMATED) {
                    holder.txtWorkerNameService.setTextColor(Color.BLUE)
                    holder.viewColorBottomS.setBackgroundColor(Color.BLUE)
                }
                holder.txtMessageService.setOnLongClickListener {
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
