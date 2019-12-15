@file:Suppress("DEPRECATION", "SpellCheckingInspection")

package com.glass.oceanbs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.alert
import org.jetbrains.anko.backgroundColor

object Constants {

    private const val URL_PARENT = "http://oceanbs01.com/models/"
    const val URL_USER = "${URL_PARENT}catalogos/CCatColaborador.php"
    const val URL_SOLICITUDES = "${URL_PARENT}registros/CRegSolicitudAG.php"
    const val URL_SUCURSALES = "${URL_PARENT}catalogos/CCatSucursal.php"
    const val URL_PRODUCTO = "${URL_PARENT}catalogos/CCatProducto.php"

    private const val DATABASE_SP = "oceanbs"

    fun snackbar(context: Context, view: View, message: String, type: Types = Types.GENERAL){
        val snack = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val sview = snack.view

        when(type){
            Types.GENERAL ->{ }
            Types.ERROR ->{ sview.backgroundColor = context.resources.getColor(R.color.colorAccent) }
            Types.SUCCESS ->{  sview.backgroundColor = context.resources.getColor(R.color.colorAccent)}
            Types.INFO ->{ sview.backgroundColor = context.resources.getColor(R.color.colorAccent)}
        }
        snack.show()
    }

    enum class Types{
        GENERAL,
        ERROR,
        SUCCESS,
        INFO
    }


    /* functions to get and set the User ID */

    fun setUserId(context: Context, userId: String){
        val editor = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE).edit()
        editor.putString("userId", userId)
        editor.apply()
    }

    fun getUserId(context: Context): String{
        val prefs = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE)
        return prefs.getString("userId", "")!!
    }

    fun setKeepLogin(context: Context, value: Boolean){
        val editor = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE).edit()
        editor.putBoolean("remember", value)
        editor.apply()
    }

    fun getKeepLogin(context: Context) : Boolean{
        val prefs = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE)
        return prefs.getBoolean("remember", false)
    }

    fun mustRefreshSolicitudes(context: Context): Boolean{
        val prefs = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE)
        return prefs.getBoolean("refreshSolicitudes", false)
    }

    fun updateRefreshSolicitudes(context: Context, value: Boolean){
        val editor = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE).edit()
        editor.putBoolean("refreshSolicitudes", value)
        editor.apply()
    }


    /* functions to check if User has active internet connection or not */

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

    /* functions to check the permissions for the general App */
    private const val PERMISSION_ID = 42

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