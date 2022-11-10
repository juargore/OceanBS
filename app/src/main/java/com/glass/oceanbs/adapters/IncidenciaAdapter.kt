@file:Suppress("SpellCheckingInspection", "DEPRECATION")

package com.glass.oceanbs.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.glass.oceanbs.R
import com.glass.oceanbs.models.ShortIncidencia
import kotlinx.android.synthetic.main.card_incidencias.view.*
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
        fun setData(
            position: Int,
            incidencia: ShortIncidencia,
            eventItemClick: InterfaceOnClick,
            eventItemLongClick: InterfaceOnLongClick
        ) {
            itemView.txtClasificacion.text = incidencia.Clasificacion
            itemView.txtStatus.text = incidencia.Status
            itemView.txtVigencia.text = incidencia.Vigencia
            itemView.txtFecha.text = incidencia.FechaAlta
            itemView.txtFalla.text = incidencia.Falla

            val layout = itemView.layColorStatus
            when (incidencia.Status.toLowerCase(Locale.getDefault())) {
                "registrada"-> setColorStatus(layout, R.color.colorIncidenceBlue)
                "por verificar"-> setColorStatus(layout, R.color.colorIncidenceOrange)
                "aceptada", "programada"-> setColorStatus(layout, R.color.colorIncidencePurple)
                "en proceso"-> setColorStatus(layout, R.color.colorIncidenceYellow)
                "terminada"-> setColorStatus(layout, R.color.colorIncidenceLightGreen)
                "entregada"-> setColorStatus(layout, R.color.colorIncidenceDarkGreen)
                "no aceptada", "no terminada", "no entregada"-> setColorStatus(layout, R.color.colorIncidencePink)
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
