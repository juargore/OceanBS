@file:Suppress("SpellCheckingInspection", "DEPRECATION")

package com.glass.oceanbs.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glass.oceanbs.R
import com.glass.oceanbs.models.ShortStatus
import kotlinx.android.synthetic.main.card_status_incidencia.view.*

class BitacoraStatusAdapter(private val context: Context,
                            private val listStatus: ArrayList<ShortStatus>,
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
        return listStatus.size
    }

    override fun onBindViewHolder(p0: ItemViewHolder, pos: Int) {
        p0.setData(pos, listStatus[pos], eventClick, eventLongClick)
    }

    inner class ItemViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){

        @SuppressLint("DefaultLocale", "UseCompatLoadingForDrawables")
        fun setData(position: Int, status: ShortStatus, eventItemClick: InterfaceOnClick, eventItemLongClick: InterfaceOnLongClick){

            itemView.txtFechaAlta.text = status.FechaAlta
            itemView.txtFullName.text = status.Status

            val fColor : Drawable = when(status.StatusIncidencia.toInt()){
                1-> { context.resources.getDrawable(R.drawable.progress_registrada) }
                2-> { context.resources.getDrawable(R.drawable.progress_por_verificar) }
                3, 4-> { context.resources.getDrawable(R.drawable.progress_aceptada_programada) }
                5-> { context.resources.getDrawable(R.drawable.progress_en_proceso) }
                6-> { context.resources.getDrawable(R.drawable.progress_terminada) }
                7-> { context.resources.getDrawable(R.drawable.progress_entregada) }
                8, 9, 10 -> { context.resources.getDrawable(R.drawable.progress_pink) }
                else-> { context.resources.getDrawable(R.drawable.progress_base) }
            }
            itemView.layFill.setBackgroundDrawable(fColor)

            itemView.layBack.post {
                val totalWidth = itemView.layBack.width
                val layParams = itemView.layFill.layoutParams

                // when
                val newWidth : Int = when(status.StatusIncidencia.toInt()){
                    1-> 4
                    2-> 5
                    3, 4-> 6
                    5-> 8
                    6-> 9
                    7-> 10
                    8, 9, 10 -> 10
                    else-> 10
                }

                layParams.width = (totalWidth/10) * newWidth

                itemView.layFill.layoutParams = layParams
            }


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