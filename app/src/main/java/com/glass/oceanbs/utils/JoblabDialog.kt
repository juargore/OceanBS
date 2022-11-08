package com.glass.oceanbs.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.glass.oceanbs.R
import com.glass.oceanbs.extensions.hide
import com.glass.oceanbs.extensions.show

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

    fun setTypeDialog(type: TYPES) {
        when (type) {
            TYPES.DOUBLE -> {
                cancelButton.show()
                acceptButton.show()
            }
            TYPES.SIMPLE -> {
                cancelButton.hide()
                acceptButton.show()
            }
            TYPES.EMPTY -> {
                cancelButton.hide()
                acceptButton.hide()
            }
        }
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

    /*fun errorDialog(body: ErrorGenericResponse) {
        setTypeDialog(TYPES.SIMPLE)
        title.text = detailButton.context.getString(R.string.dialog_title_error)
        message.text = body.message
        acceptClickListener {}
        if (!body.details.isNullOrEmpty()) {
            detailsClickListener()
            detailButton.show()
            detailMessage.text = getDetailsFromList(body.details)
        }
    }

    fun errorDialogEmpty(msg: String) {
        setTypeDialog(TYPES.EMPTY)
        title.text = context.getString(R.string.dialog_title_error)
        message.text = msg
    }

    fun getDetailsFromList(details: List<Detail>): String {
        val descriptions = details.map { it.description }
        return context.getString(
            R.string.dialog_message_details, descriptions.joinToString(". ")
        )
    }

    fun setUpAccutestErrorDialog(
        bodyMessage: String,
        showDetails: Boolean,
        detail: String,
        action: () -> Unit
    ) {
        setTypeDialog(TYPES.SIMPLE)
        cancelable = false
        title.text = detailButton.context.getString(R.string.dialog_title_accutest)
        message.text = bodyMessage
        acceptClickListener {
            action()
        }
        if (showDetails) {
            detailsClickListener()
            detailButton.show()
            detailMessage.text = detail
        }
    }*/
}

enum class TYPES {
    DOUBLE,
    SIMPLE,
    EMPTY
}