@file:Suppress("DEPRECATION")

package com.glass.oceanbs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.alert
import org.jetbrains.anko.backgroundColor

object Constants {

    const val URL_PARENT = "http://oceanbs04.com/models/catalogos/CCatColaborador.php"


    fun snackbar(context: Context, view: View, message: String, type: types = types.GENERAL){
        val snack = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val view = snack.view

        when(type){
            types.GENERAL ->{ }
            types.ERROR ->{ view.backgroundColor = context.resources.getColor(R.color.colorAccent) }
            types.SUCCESS ->{  view.backgroundColor = context.resources.getColor(R.color.colorAccent)}
            types.INFO ->{ view.backgroundColor = context.resources.getColor(R.color.colorAccent)}
        }
        snack.show()
    }

    enum class types{
        GENERAL,
        ERROR,
        SUCCESS,
        INFO
    }

    /*Functions to check if user has active internet connection or not*/
    fun internetConnected(activity: Activity) : Boolean{
        return isNetworkConnected(activity)
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return cm!!.activeNetworkInfo != null
    }

    fun showPopUpNoInternet(activity: Activity){
        activity.alert(activity.resources.getString(R.string.desc_no_internet),
            activity.resources.getString(R.string.title_no_internet))
        {
            positiveButton(activity.resources.getString(R.string.accept)) {}
        }.show().setCancelable(true)
    }

    private val PERMISSION_ID = 42

    fun checkPermission(activity: Activity, vararg perm: String) : Boolean {
        val havePermissions = perm.toList().all {
            ContextCompat.checkSelfPermission(activity,it) ==
                    PackageManager.PERMISSION_GRANTED
        }
        if (!havePermissions) {
            if(perm.toList().any {
                    ActivityCompat.
                        shouldShowRequestPermissionRationale(activity, it)}
            ) {
                val dialog = AlertDialog.Builder(activity)
                    .setTitle("Se necesitan Permisos!!")
                    .setMessage("Es necesario otorgar permisos a esta App para su correcto funcionamiento")
                    .setPositiveButton("Sí. Conceder los permisos") { _, _ ->
                        ActivityCompat.requestPermissions(
                            activity, perm, PERMISSION_ID)
                    }
                    //.setNegativeButton("No conceder") { _, _ -> }
                    .create()
                dialog.show()
                dialog.setCancelable(false)
            } else {
                ActivityCompat.requestPermissions(activity, perm, PERMISSION_ID)
            }
            return false
        }
        return true
    }
}