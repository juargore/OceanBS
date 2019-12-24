@file:Suppress("SpellCheckingInspection", "DEPRECATION")

package com.glass.oceanbs.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glass.oceanbs.R
import com.glass.oceanbs.models.ShortIncidencia
import kotlinx.android.synthetic.main.card_incidencias.view.*

class IncidenciaAdapter(private val context: Context,
                        private val listIncidencias: ArrayList<ShortIncidencia>,
                        private val eventClick: InterfaceOnClick,
                        private val eventLongClick: InterfaceOnLongClick)
    : androidx.recyclerview.widget.RecyclerView.Adapter<IncidenciaAdapter.ItemViewHolder>() {

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

    override fun getItemCount(): Int {
        return listIncidencias.size
    }

    override fun onBindViewHolder(p0: ItemViewHolder, pos: Int) {
        p0.setData(pos, listIncidencias[pos], eventClick, eventLongClick)
    }

    inner class ItemViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){

        @SuppressLint("DefaultLocale")
        fun setData(position: Int, incidencia: ShortIncidencia, eventItemClick: InterfaceOnClick, eventItemLongClick: InterfaceOnLongClick){

            itemView.txtClasificacion.text = incidencia.Clasificacion
            itemView.txtStatus.text = incidencia.Status
            itemView.txtVigencia.text = incidencia.Vigencia
            itemView.txtFecha.text = incidencia.FechaAlta
            itemView.txtFalla.text = incidencia.Falla

            when(incidencia.Status.toLowerCase()){
                "registrada"->{itemView.layColorStatus.setBackgroundColor(context.resources.getColor(R.color.colorIncidenceBlue))}
                "por verificar"->{itemView.layColorStatus.setBackgroundColor(context.resources.getColor(R.color.colorIncidenceOrange))}
                "aceptada", "programada"->{itemView.layColorStatus.setBackgroundColor(context.resources.getColor(R.color.colorIncidencePurple))}
                "en proceso"->{itemView.layColorStatus.setBackgroundColor(context.resources.getColor(R.color.colorIncidenceYellow))}
                "terminada"->{itemView.layColorStatus.setBackgroundColor(context.resources.getColor(R.color.colorIncidenceLightGreen))}
                "entregada"->{itemView.layColorStatus.setBackgroundColor(context.resources.getColor(R.color.colorIncidenceDarkGreen))}
                "no aceptada", "no terminada", "no entregada"->{itemView.layColorStatus.setBackgroundColor(context.resources.getColor(R.color.colorIncidencePink))}
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