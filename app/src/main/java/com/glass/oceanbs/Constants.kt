@file:Suppress("DEPRECATION", "SpellCheckingInspection")

package com.glass.oceanbs

import android.annotation.SuppressLint
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
import com.glass.oceanbs.extensions.alert
import com.glass.oceanbs.extensions.hide
import com.google.android.material.snackbar.Snackbar

object Constants {
    //private const val URL_PARENT = "http://oceanbs01.com/"
    private const val URL_PARENT = "http://oceanbs04.com/"
    private const val DATABASE_SP = "oceanbs"

    const val URL_USER = "${URL_PARENT}models/catalogos/CCatColaborador.php"
    const val URL_SOLICITUDES = "${URL_PARENT}models/registros/CRegSolicitudAG.php"
    const val URL_SUCURSALES = "${URL_PARENT}models/catalogos/CCatSucursal.php"
    const val URL_PRODUCTO = "${URL_PARENT}models/catalogos/CCatProducto.php"
    const val URL_INCIDENCIAS = "${URL_PARENT}models/registros/CRegIncidencia.php"
    const val URL_STATUS = "${URL_PARENT}models/registros/CRegStatusIncidencia.php"
    const val URL_CLASIFICACION = "${URL_PARENT}models/catalogos/CCatValorClasificacion.php"
    const val URL_IMAGES_CAROUSEL = "${URL_PARENT}models/catalogos/CCatEmpresa.php"
    const val URL_IMAGES = "${URL_PARENT}uploads/catalogos/sucursales/"
    const val URL_IMAGES_STATUS = "${URL_PARENT}uploads/crecento/statusincidencias/"
    const val URL_MAIN_ITEMS_HOME = "${URL_PARENT}models/na_servicios/CServiciosAplicacionesConsultas.php"
    const val URL_SUMMARY_ITEMS = URL_MAIN_ITEMS_HOME
    const val URL_CONSTRUCTION_ITEMS = URL_MAIN_ITEMS_HOME
    const val URL_DOCUMENTATION_ITEMS = URL_MAIN_ITEMS_HOME
    const val URL_HISTORY_ITEMS = URL_MAIN_ITEMS_HOME
    const val URL_CHAT_ITEMS = URL_MAIN_ITEMS_HOME

    const val GET_CAROUSEL = "ConsultaCarruselApp"
    const val GET_ALL_DESARROLLOS = "ConsultaDesarrollosTodos"
    const val GET_DESARROLLOS_BY_OWNER_ID = "ConsultaDesarrollosIdPropietario"
    const val GET_MAIN_ITEMS_HOME = "ConsultaIntegralMenuPrincipal"
    const val GET_SUMMARY_ITEMS = "ConsultaIntegralSeguimientoResumen"
    const val GET_CHAT_ITEMS = "ConsultaIntegralConversacion"
    const val POST_CHAT_MESSAGE = "EnviaMensajeConversacion"
    const val GET_CONSTRUCTION_ITEMS = "ConsultaIntegralAvanceConstruccion"
    const val GET_DOCUMENTATION_ITEMS = "ConsultaIntegralAvanceDocumentacion"
    const val GET_HISTORY_ITEMS = "ConsultaIntegralAvisos"

    const val WEB_SERVICE = "WebService"
    const val ERROR = "Error"
    const val MESSAGE = "Mensaje"
    const val DATA = "Datos"
    const val NOTICES = "Avisos"
    const val NOTICE = "Aviso"
    const val OPTIONS = "Opciones"
    const val CONVERSATION = "Conversacion"
    const val UNITS = "Unidades"
    const val PHOTO = "Fotografia"
    const val PHOTO1 = "Fotografia1"
    const val PHOTO2 = "Fotografia2"
    const val PHOTO3 = "Fotografia3"
    const val PHOTOS = "photos"
    const val OWNER_ID = "IdPropietario"
    const val SELECT = "Seleccionar"
    const val WELCOME_CAPTION = "LeyendaBienvenida"
    const val CURRENT_DATE = "FechaActual"
    const val MEMBER_SINCE = "LeyendaPropietario"
    const val OWNER_NAME = "NombrePropietario"
    const val TITLE = "Titulo"
    const val CAPTION = "Leyenda"
    const val CAPTION_SUMMARY = "LeyendaResumen"
    const val COLOR = "Color"
    const val LINK_TYPE = "TipoEnlace"
    const val LINK = "Enlace"
    const val PROGRESS = "Avance"
    const val PHASE = "Fase"
    const val IMAGE = "Imagen"
    const val UNITY_ID = "IdUnidad"
    const val NOTICE_TYPE = "TipoAviso"
    const val UNITY_CODE = "CodigoUnidad"
    const val UNITY_NAME = "NombreUnidad"
    const val ESTIMATED_COMPLETION_DATE = "FechaEstimada"
    const val ADDITIONAL_INFO = "InformacionAdicional"

    fun snackbar(context: Context, view: View, message: String, type: Types) {
        val snack = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val sview = snack.view
        val tv = sview.findViewById<TextView>(R.id.snackbar_text)

        val color = when (type) {
            Types.ERROR -> R.color.colorIncidencePink
            Types.SUCCESS -> R.color.colorIncidenceLightGreen
            Types.INFO -> R.color.colorPrimary
        }
        sview.setBackgroundColor(context.resources.getColor(color))
        tv.setTextColor(Color.BLACK)
        snack.show()
    }

    enum class Types{
        ERROR,
        SUCCESS,
        INFO
    }


    fun setUserId(context: Context, userId: String){
        val editor = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE).edit()
        editor.putString("userId", userId)
        editor.apply()
    }

    fun getUserId(context: Context): String{
        val prefs = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE)
        return prefs.getString("userId", "")!!
    }

    fun setUserName(context: Context, name: String){
        val editor = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE).edit()
        editor.putString("userName", name)
        editor.apply()
    }

    fun getUserName(context: Context): String{
        val prefs = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE)
        return prefs.getString("userName", "")!!
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

    fun mustRefreshToken(context: Context): Boolean{
        val prefs = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE)
        return prefs.getBoolean("refreshToken", false)
    }

    fun updateRefreshToken(context: Context, value: Boolean){
        val editor = context.getSharedPreferences(DATABASE_SP, MODE_PRIVATE).edit()
        editor.putBoolean("refreshToken", value)
        editor.apply()
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

    @SuppressLint("MissingPermission")
    private fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return cm!!.activeNetworkInfo != null
    }

    fun showPopUpNoInternet(activity: Activity){
        activity.alert {
            title.text = activity.resources.getString(R.string.title_no_internet)
            message.text = activity.resources.getString(R.string.desc_no_internet)
            cancelButton.hide()
            acceptButton.hide()
        }.show()
    }

    /* functions to check the permissions for the general App */
    private const val PERMISSION_ID = 42

    fun checkPermission(activity: Activity, vararg perm: String) : Boolean {
        val havePermissions = perm.toList().all {
            ContextCompat.checkSelfPermission(activity,it) ==
                    PackageManager.PERMISSION_GRANTED
        }
        if (!havePermissions) {
            if (perm.toList().any {
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, it)}
            ) {
                val dialog = AlertDialog.Builder(activity)
                    .setTitle("Se necesitan Permisos!!")
                    .setMessage("Es necesario otorgar permisos a esta App para su correcto funcionamiento")
                    .setPositiveButton("Sí. Conceder los permisos") { _, _ ->
                        ActivityCompat.requestPermissions(activity, perm, PERMISSION_ID)
                    }.create()
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
