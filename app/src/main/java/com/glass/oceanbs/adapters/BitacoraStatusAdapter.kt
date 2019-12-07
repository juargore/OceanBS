@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glass.oceanbs.R

class BitacoraStatusAdapter(private val context: Context,
                            private val eventClick: InterfaceOnClick,
                            private val eventLongClick: InterfaceOnLongClick)
    : androidx.recyclerview.widget.RecyclerView.Adapter<BitacoraStatusAdapter.ItemViewHolder>() {

    interface InterfaceOnClick {
        fun onItemClick(pos: Int)
    }

    interface InterfaceOnLongClick {
        fun onItemLongClick(pos: Int)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.card_status_incidencia, p0, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 5
    }

    override fun onBindViewHolder(p0: ItemViewHolder, pos: Int) {
        p0.setData(pos, eventClick, eventLongClick)
    }

    inner class ItemViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){

        @SuppressLint("DefaultLocale")
        fun setData(position: Int, eventItemClick: InterfaceOnClick, eventItemLongClick: InterfaceOnLongClick){

            itemView.setOnClickListener {
                eventItemClick.onItemClick(position)
            }

            itemView.setOnLongClickListener {
                eventItemLongClick.onItemLongClick(position)
                true
            }
        }
    }

}