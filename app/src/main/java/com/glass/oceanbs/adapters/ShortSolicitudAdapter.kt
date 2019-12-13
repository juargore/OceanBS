@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glass.oceanbs.R
import com.glass.oceanbs.models.ShortSolicitud
import kotlinx.android.synthetic.main.card_solicitudes.view.*

class ShortSolicitudAdapter(private val context: Context,
                            private val listSolicitudes: ArrayList<ShortSolicitud>,
                            private val eventClick: InterfaceOnClick,
                            private val eventLongClick: InterfaceOnLongClick)
    : androidx.recyclerview.widget.RecyclerView.Adapter<ShortSolicitudAdapter.ItemViewHolder>() {

    interface InterfaceOnClick {
        fun onItemClick(pos: Int)
    }

    interface InterfaceOnLongClick {
        fun onItemLongClick(pos: Int)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.card_solicitudes, p0, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listSolicitudes.size
    }

    override fun onBindViewHolder(p0: ItemViewHolder, pos: Int) {
        p0.setData(pos, listSolicitudes[pos], eventClick, eventLongClick)
    }

    inner class ItemViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){

        @SuppressLint("DefaultLocale", "SetTextI18n")
        fun setData(position: Int, shortSolicitud: ShortSolicitud, eventItemClick: InterfaceOnClick, eventItemLongClick: InterfaceOnLongClick){

            itemView.txtNombreDesarrolloS.text = shortSolicitud.NombreDesarrollo
            itemView.txtNombrePRS.text = shortSolicitud.NombrePR
            itemView.txtCodigoUnidadS.text = shortSolicitud.CodigoUnidad
            itemView.txtFechaAltaS.text = "Fecha: ${shortSolicitud.FechaAlta}"

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