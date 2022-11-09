package com.glass.oceanbs.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.glass.oceanbs.R

@SuppressLint("InflateParams")
class JoblabDialog(val context: Context) : BaseDialogHelper() {

    override val dialogView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.popup_base, null)
    }

    override val builder: AlertDialog.Builder =
        AlertDialog.Builder(context, R.style.JoblabDialogStyle).setView(dialogView)

    var title: TextView = dialogView.findViewById(R.id.title)
    var message: TextView = dialogView.findViewById(R.id.message)

    var acceptButton: TextView = dialogView.findViewById(R.id.btnAccept)

    val cancelButton: TextView by lazy {
        dialogView.findViewById(R.id.btnCancel)
    }

    fun cancelClickListener(func: (() -> Unit)? = null) =
        with(cancelButton) {
            setClickListenerToDialogIcon(func)
        }

    fun acceptClickListener(func: (() -> Unit)? = null) =
        with(acceptButton) {
            setClickListenerToDialogIcon(func)
        }

    private fun View.setClickListenerToDialogIcon(func: (() -> Unit)?) =
        setOnClickListener {
            func?.invoke()
            dialog?.dismiss()
        }
}
