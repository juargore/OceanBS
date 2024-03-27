@file:Suppress("SpellCheckingInspection", "DEPRECATION")

package com.ocean.oceanbs.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.ocean.oceanbs.R
import com.ocean.oceanbs.models.ShortIncidencia
import java.util.*
import kotlin.collections.ArrayList

class IncidenciaAdapter(
    private val context: Context,
    private val listIncidencias: ArrayList<ShortIncidencia>,
    private val eventClick: InterfaceOnClick,
    private val eventLongClick: InterfaceOnLongClick
): androidx.recyclerview.widget.RecyclerView.Adapter<IncidenciaAdapter.ItemViewHolder>() {

    interface InterfaceOnClick {
        fun onItemClick(pos: Int)
    }

    interface InterfaceOnLongClick {
        fun onItemLongClick(pos: Int)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.card_incidencias, p0, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount() = listIncidencias.size

    override fun onBindViewHolder(p0: ItemViewHolder, pos: Int) {
        p0.setData(pos, listIncidencias[pos], eventClick, eventLongClick)
    }

    inner class ItemViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        private val txtClasificacion: TextView = itemView.findViewById(R.id.txtClasificacion)
        private val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        private val txtVigencia: TextView = itemView.findViewById(R.id.txtVigencia)
        private val txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
        private val txtFalla: TextView = itemView.findViewById(R.id.txtFalla)
        private val layColorStatus: RelativeLayout = itemView.findViewById(R.id.layColorStatus)

        fun setData(
            position: Int,
            incidencia: ShortIncidencia,
            eventItemClick: InterfaceOnClick,
            eventItemLongClick: InterfaceOnLongClick
        ) {
            txtClasificacion.text = incidencia.Clasificacion
            txtStatus.text = incidencia.Status
            txtVigencia.text = incidencia.Vigencia
            txtFecha.text = incidencia.FechaAlta
            txtFalla.text = incidencia.Falla

            when (incidencia.Status.toLowerCase(Locale.getDefault())) {
                "registrada"-> setColorStatus(layColorStatus, R.color.colorIncidenceBlue)
                "por verificar"-> setColorStatus(layColorStatus, R.color.colorIncidenceOrange)
                "aceptada", "programada"-> setColorStatus(layColorStatus, R.color.colorIncidencePurple)
                "en proceso"-> setColorStatus(layColorStatus, R.color.colorIncidenceYellow)
                "terminada"-> setColorStatus(layColorStatus, R.color.colorIncidenceLightGreen)
                "entregada"-> setColorStatus(layColorStatus, R.color.colorIncidenceDarkGreen)
                "no aceptada", "no terminada", "no entregada"-> setColorStatus(layColorStatus, R.color.colorIncidencePink)
            }

            itemView.setOnClickListener {
                eventItemClick.onItemClick(position)
            }

            itemView.setOnLongClickListener {
                eventItemLongClick.onItemLongClick(position)
                true
            }
        }

        private fun setColorStatus(v: RelativeLayout, color: Int) {
            v.setBackgroundColor(context.resources.getColor(color))
        }
    }
}
