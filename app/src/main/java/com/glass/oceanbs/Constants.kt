package com.glass.oceanbs

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object Constants {

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