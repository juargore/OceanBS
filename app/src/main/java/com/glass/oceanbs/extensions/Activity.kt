package com.glass.oceanbs.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.glass.oceanbs.Constants
import com.glass.oceanbs.R
import com.glass.oceanbs.activities.LoginActivity
import com.glass.oceanbs.utils.JoblabDialog

fun Activity.alert(func: JoblabDialog.() -> Unit): AlertDialog =
    JoblabDialog(this).apply { func() }.create()

fun Activity.showExitDialog() {
    val ctx = this
    alert {
        title.hide()
        message.text = getString(R.string.confirm_exit_desc)
        cancelClickListener { }
        acceptClickListener {
            Constants.setKeepLogin(ctx, false)
            startActivity(Intent(ctx, LoginActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }.show()
}