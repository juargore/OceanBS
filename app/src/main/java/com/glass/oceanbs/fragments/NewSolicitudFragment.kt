@file:Suppress("SpellCheckingInspection", "DEPRECATION")

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
import android.widget.Button
import android.widget.TextView
import com.glass.oceanbs.R
import com.glass.oceanbs.activities.IncidenciasActivity
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.textColor


class NewSolicitudFragment : Fragment() {

    private lateinit var btnSaveSolicitud: Button
    private lateinit var txtInstructionN: TextView

    private var solicitudId: String? = "0"

    companion object{
        fun newInstance(): NewSolicitudFragment {
            return NewSolicitudFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_new_solicitud, container, false)

        initComponents(rootView)

        return rootView
    }

    @SuppressLint("SetTextI18n")
    private fun initComponents(view: View){
        txtInstructionN = view.findViewById(R.id.txtInstructionN)
        btnSaveSolicitud = view.findViewById(R.id.btnSaveSolicitud)

        val isEdit = arguments?.getBoolean("isEdit")
        solicitudId = arguments?.getString("solicitudId")

        if(isEdit != null && isEdit){
            txtInstructionN.text = "Llene los campos para actualizar la solicitud"
        }

        btnSaveSolicitud.setOnClickListener { showConfirmDialog(context!!) }
    }

    private fun showConfirmDialog(context: Context){
        alert(resources.getString(R.string.msg_confirm_creation),
            "Confirmar Solicitud")
        {
            positiveButton(resources.getString(R.string.accept)) {
                showResumeDialog(context)
            }
            negativeButton(resources.getString(R.string.cancel)){}
        }.show().apply {
            getButton(AlertDialog.BUTTON_POSITIVE)?.let { it.textColor = resources.getColor(R.color.colorBlack) }
            getButton(AlertDialog.BUTTON_NEGATIVE)?.let { it.textColor = resources.getColor(R.color.colorAccent) }
        }
    }

    private fun showResumeDialog(context: Context){
        val dialog = Dialog(context, R.style.FullDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.pop_data_solicitud)

        val btnAdd = dialog.findViewById<Button>(R.id.btnAddIncidencias)
        btnAdd.setOnClickListener {
            dialog.dismiss()
            
            val intent = Intent(activity, IncidenciasActivity::class.java)
            startActivity(intent)
        }

        dialog.show()
    }

}
