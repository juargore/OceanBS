package com.ocean.oceanbs.extensions

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.ocean.oceanbs.Constants
import com.ocean.oceanbs.R
import com.ocean.oceanbs.activities.LoginActivity
import com.ocean.oceanbs.utils.JoblabDialog
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

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

fun Fragment?.runOnUiThread(action: () -> Unit) {
    this ?: return
    if (!isAdded) return // Fragment not attached to an Activity
    activity?.runOnUiThread(action)
}

private var progress: android.app.AlertDialog? = null

data class Parameter(
    val key: String,
    val value: String?
)

fun Activity.getDataFromServer(
    showLoader: Boolean = true,
    webService: String,
    url: String,
    parameters: List<Parameter>? = null,
    parent: View,
    func: (JSONObject) -> Unit
) {
    if (showLoader) {
        if (progress == null) {
            val mBuilder = android.app.AlertDialog.Builder(this, R.style.HalfDialogTheme)
            val inflat = layoutInflater
            val dialogView = inflat.inflate(R.layout.progress, null)

            mBuilder.setView(dialogView)
            progress = mBuilder.create()
            progress?.setCancelable(false)
        }
        progress?.let {
            if (!it.isShowing) {
                it.show()
            }
        }
    }

    val client = OkHttpClient()
    val builder = FormBody.Builder()
        .add(Constants.WEB_SERVICE, webService)

    parameters?.forEach { param ->
        param.value?.let {
            builder.add(param.key, it)
        }
    }

    val request = Request.Builder().url(url).post(builder.build()).build()

    client.newCall(request).enqueue(object: Callback {
        override fun onResponse(call: Call, response: Response) {
            try {
                val res = response.body!!.string()
                println(res)
                val jsonRes = JSONObject(res)
                if (jsonRes.getInt(Constants.ERROR) > 0) {
                    showSnackBar(jsonRes.getString(Constants.MESSAGE), parent)
                } else {
                    progress?.dismiss()
                    progress = null
                    func.invoke(jsonRes)
                }
            } catch (e: Exception) {
                showSnackBar(e.message.toString(), parent)
                progress?.dismiss()
                progress = null
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            showSnackBar(e.message.toString(), parent)
            progress?.dismiss()
            progress = null
        }
    })
}

private fun Activity.showSnackBar(str: String, parent: View) {
    runOnUiThread {
        Constants.snackbar(
            this,
            parent,
            str,
            Constants.Types.ERROR
        )
    }
}