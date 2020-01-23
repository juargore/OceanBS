@file:Suppress("DEPRECATION", "SpellCheckingInspection")

package com.glass.oceanbs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.alert
import org.jetbrains.anko.backgroundColor

object Constants {

    private const val URL_PARENT = "http://oceanbs01.com/"
    //private const val URL_PARENT = "http://oceanbs04.com/"

    const val URL_USER = "${URL_PARENT}models/catalogos/CCatColaborador.php"
    const val URL_SOLICITUDES = "${URL_PARENT}models/registros/CRegSolicitudAG.php"
    const val URL_SUCURSALES = "${URL_PARENT}models/catalogos/CCatSucursal.php"
    const val URL_PRODUCTO = "${URL_PARENT}models/catalogos/CCatProducto.php"
    const val URL_INCIDENCIAS = "${URL_PARENT}models/registros/CRegIncidencia.php"
    const val URL_STATUS = "${URL_PARENT}models/registros/CRegStatusIncidencia.php"
    const val URL_CLASIFICACION = "${URL_PARENT}models/catalogos/CCatValorClasificacion.php"

    const val URL_IMAGES = "${URL_PARENT}uploads/catalogos/sucursales/"
    //const val URL_IMAGES_STATUS = "http://oceanbs01.com/uploads/crecento/statusincidencias/"
    const val URL_IMAGES_STATUS = "${URL_PARENT}uploads/crecento/statusincidencias/"

    private const val DATABASE_SP = "oceanbs"

    fun snackbar(context: Context, view: View, message: String, type: Types = Types.GENERAL){
        val snack = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val sview = snack.view
        val tv = sview.findViewById<TextView>(R.id.snackbar_text)

        when(type){
            Types.GENERAL ->{ }
            Types.ERROR ->{ sview.backgroundColor = context.resources.getColor(R.color.colorIncidencePink) }
            Types.SUCCESS ->{  sview.backgroundColor = context.resources.getColor(R.color.colorIncidenceLightGreen)}
            Types.INFO ->{ sview.backgroundColor = context.resources.getColor(R.color.colorPrimary)}
        }

        tv.setTextColor(Color.BLACK)
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

    fun setTipoUsuario(context: Context, tipoUsuario: Int){
        val editor = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE).edit()
        editor.putInt("tipo", tipoUsuario)
        editor.apply()
    }

    fun getTipoUsuario(context: Context): Int{
        val prefs = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE)
        return prefs.getInt("tipo", 1)
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

    fun mustRefreshIncidencias(context: Context): Boolean{
        val prefs = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE)
        return prefs.getBoolean("refreshIncidentes", false)
    }

    fun updateRefreshIncidencias(context: Context, value: Boolean){
        val editor = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE).edit()
        editor.putBoolean("refreshIncidentes", value)
        editor.apply()
    }

    fun mustRefreshStatus(context: Context): Boolean{
        val prefs = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE)
        return prefs.getBoolean("refreshStatus", false)
    }

    fun updateRefreshStatus(context: Context, value: Boolean){
        val editor = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE).edit()
        editor.putBoolean("refreshStatus", value)
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