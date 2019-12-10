@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.glass.oceanbs.models.User

class TableUser(context: Context) {

    companion object{
        const val TABLE_USER = "table_user"

        const val USER_ID = "id"
        const val COLABORADOR = "colaborador"
        const val CODIGO = "codigo"
        const val NOMBRE = "nombre"
        const val APELLIDO_P = "apellido_p"
        const val APELLIDO_M = "apellido_m"

        const val CREATE_TABLE_USER = "create table $TABLE_USER (" +
                "$USER_ID TEXT not null, " +
                "$COLABORADOR TEXT not null, " +
                "$CODIGO TEXT not null, " +
                "$NOMBRE TEXT, " +
                "$APELLIDO_P TEXT, "+
                "$APELLIDO_M TEXT);"
    }

    private val helper = DbHelper(context)
    private val db: SQLiteDatabase = helper.writableDatabase
    private val allColumns = arrayOf(USER_ID, COLABORADOR, CODIGO, NOMBRE, APELLIDO_P, APELLIDO_M)

    private fun generateCV(userId: String, colaborador: String, codigo: String, nombre: String, apellidoP: String, apellidoM: String) : ContentValues{
        val values = ContentValues()
        values.put(USER_ID, userId)
        values.put(COLABORADOR, colaborador)
        values.put(CODIGO, codigo)
        values.put(NOMBRE, nombre)
        values.put(APELLIDO_P, apellidoP)
        values.put(APELLIDO_M, apellidoM)

        return values
    }

    @SuppressLint("DefaultLocale")
    fun insertNewOrExistingUser(user: User){

        // first, delete all data linked to current User
        val res = deleteUserById(user.id)
        Log.e("--", res.toString())

        // now, add the current User to database
        db.insert(TABLE_USER, null, generateCV(
            user.id, user.colaborador.toString().toLowerCase(), user.codigo, user.nombre, user.apellidoP, user.apellidoM))
    }

    private fun deleteUserById(userId: String) : Int{
        return db.delete(TABLE_USER, "$USER_ID =? ", arrayOf(userId))
    }

    fun getCurrentUserById(userId: String) : User {
        val cursor = db.query(TABLE_USER, allColumns, "$USER_ID =? ", arrayOf(userId), null, null, null)
        lateinit var cUser: User

        if(cursor.count > 0){
            cursor.use { c->
                if(c.moveToFirst()){
                    do{
                        cUser = User(
                            c.getString(c.getColumnIndex(USER_ID)),
                            c.getString(c.getColumnIndex(COLABORADOR))!!.toBoolean(),
                            c.getString(c.getColumnIndex(CODIGO)),
                            c.getString(c.getColumnIndex(NOMBRE)),
                            c.getString(c.getColumnIndex(APELLIDO_P)),
                            c.getString(c.getColumnIndex(APELLIDO_M))
                        )
                    } while (c.moveToNext())
                }
            }
        } else{
            return User("", false, "", "", "", "")
        }

        return cUser
    }
}