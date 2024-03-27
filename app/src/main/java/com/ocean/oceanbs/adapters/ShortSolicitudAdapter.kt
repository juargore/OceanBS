@file:Suppress("SpellCheckingInspection")

package com.ocean.oceanbs.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ocean.oceanbs.R
import com.ocean.oceanbs.models.ShortSolicitud

class ShortSolicitudAdapter(
    private val context: Context,
    private val listSolicitudes: ArrayList<ShortSolicitud>,
    private val eventClick: InterfaceOnClick,
    private val eventLongClick: InterfaceOnLongClick
): androidx.recyclerview.widget.RecyclerView.Adapter<ShortSolicitudAdapter.ItemViewHolder>() {

    interface InterfaceOnClick {
        fun onItemClick(pos: Int)
    }

    interface InterfaceOnLongClick {
        fun onItemLongClick(pos: Int)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.card_solicitudes, p0, false))
    }

    override fun getItemCount() = listSolicitudes.size

    override fun onBindViewHolder(p0: ItemViewHolder, pos: Int) {
        p0.setData(pos, listSolicitudes[pos], eventClick, eventLongClick)
    }

    inner class ItemViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){

        private val txtNombreDesarrolloS: TextView = itemView.findViewById(R.id.txtNombreDesarrolloS)
        private val txtNombrePRS: TextView = itemView.findViewById(R.id.txtNombrePRS)
        private val txtCodigoUnidadS: TextView = itemView.findViewById(R.id.txtCodigoUnidadS)
        private val txtFechaAltaS: TextView = itemView.findViewById(R.id.txtFechaAltaS)

        @SuppressLint("DefaultLocale", "SetTextI18n")
        fun setData(position: Int, shortSolicitud: ShortSolicitud, eventItemClick: InterfaceOnClick, eventItemLongClick: InterfaceOnLongClick){

            txtNombreDesarrolloS.text = shortSolicitud.NombreDesarrollo
            txtNombrePRS.text = shortSolicitud.NombrePropietario
            txtCodigoUnidadS.text = shortSolicitud.CodigoUnidad
            txtFechaAltaS.text = "Fecha: ${shortSolicitud.FechaAlta}"

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
