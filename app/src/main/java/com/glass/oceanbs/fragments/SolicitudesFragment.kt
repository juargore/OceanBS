@file:Suppress("DEPRECATION", "SpellCheckingInspection")
package com.glass.oceanbs.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.adapters.SolicitudAdapter
import com.glass.oceanbs.R
import com.glass.oceanbs.activities.IncidenciasActivity
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.textColor

class SolicitudesFragment : Fragment() {

    private lateinit var rvSolicitudes: RecyclerView

    companion object{
        fun newInstance(): SolicitudesFragment {
            return SolicitudesFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_solicitudes, container, false)

        rvSolicitudes = rootView.findViewById(R.id.rvSolicitudes)

        rvSolicitudes.layoutManager = LinearLayoutManager(context)

        val adapter = SolicitudAdapter(context!!, object : SolicitudAdapter.InterfaceOnClick{
            override fun onItemClick(pos: Int) {
                val intent = Intent(activity!!, IncidenciasActivity::class.java)
                startActivity(intent)
            }
        }, object : SolicitudAdapter.InterfaceOnLongClick{
            override fun onItemLongClick(pos: Int) {
                showPopOptions(context!!)
            }
        })

        rvSolicitudes.adapter = adapter

        return rootView
    }

    @SuppressLint("SetTextI18n")
    private fun showPopOptions(context: Context){
        val dialog = Dialog(context, R.style.FullDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.pop_options)

        val title = dialog.findViewById<TextView>(R.id.txtOpTitle)
        val delete = dialog.findViewById<TextView>(R.id.txtOpDel)
        val edit = dialog.findViewById<TextView>(R.id.txtOpEdit)
        val cancel = dialog.findViewById<TextView>(R.id.txtOpCan)

        title.text = "Opciones de Solicitud"
        delete.text = "Eliminar Solicitud"
        edit.text = "Editar Solicitud"

        cancel.setOnClickListener { dialog.dismiss() }
        delete.setOnClickListener { showDeleteDialog(); dialog.dismiss() }

        dialog.show()
    }

    private fun showDeleteDialog(){
        alert(resources.getString(R.string.msg_confirm_deletion),
            "Eliminar Solicitud")
        {
            positiveButton(resources.getString(R.string.accept)) {

            }
            negativeButton(resources.getString(R.string.cancel)){}
        }.show().apply {
            getButton(AlertDialog.BUTTON_POSITIVE)?.let { it.textColor = resources.getColor(R.color.colorBlack) }
            getButton(AlertDialog.BUTTON_NEGATIVE)?.let { it.textColor = resources.getColor(R.color.colorAccent) }
        }
    }

}
