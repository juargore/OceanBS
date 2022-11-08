@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.glass.oceanbs.models.User
import java.util.*

class TableUser(context: Context) {

    companion object{
        const val TABLE_USER = "table_user"

        const val TIPO_USUARIO = "tipo_usuario"
        const val ID_PROPIETARIO = "id_propietario"
        const val ID_COLABORADOR = "id_colaborador"
        const val CODIGO = "codigo"
        const val NOMBRE = "nombre"
        const val APELLIDO_P = "apellido_p"
        const val APELLIDO_M = "apellido_m"

        const val CREATE_TABLE_USER = "create table $TABLE_USER (" +
                "$TIPO_USUARIO TEXT not null, " +
                "$ID_PROPIETARIO TEXT not null, " +
                "$ID_COLABORADOR TEXT not null, " +
                "$CODIGO TEXT not null, " +
                "$NOMBRE TEXT, " +
                "$APELLIDO_P TEXT, "+
                "$APELLIDO_M TEXT);"
    }

    private val helper = DbHelper(context)
    private val db: SQLiteDatabase = helper.writableDatabase
    private val allColumns = arrayOf(TIPO_USUARIO, ID_PROPIETARIO, ID_COLABORADOR, CODIGO, NOMBRE, APELLIDO_P, APELLIDO_M)

    private fun generateCV(tipoUsuario: Int, idPropietario: String, idColaborador: String, codigo: String, nombre: String, apellidoP: String, apellidoM: String) : ContentValues{
        val values = ContentValues()
        values.put(TIPO_USUARIO, tipoUsuario)
        values.put(ID_PROPIETARIO, idPropietario)
        values.put(ID_COLABORADOR, idColaborador)
        values.put(CODIGO, codigo)
        values.put(NOMBRE, nombre)
        values.put(APELLIDO_P, apellidoP)
        values.put(APELLIDO_M, apellidoM)

        return values
    }

    @SuppressLint("DefaultLocale")
    fun insertNewOrExistingUser(user: User, tipoUsuario: Int){

        // first, delete all data linked to current User
        if(tipoUsuario == 1)
            deleteUserById(user.idPropietario, tipoUsuario)
        else
            deleteUserById(user.idColaborador, tipoUsuario)

        // now, add the current User to database
        db.insert(TABLE_USER, null, generateCV(
            user.tipoUsuario, user.idPropietario, user.idColaborador, user.codigo, user.nombre, user.apellidoP, user.apellidoM))
    }

    private fun deleteUserById(userId: String, tipoUsuario: Int){
        if(tipoUsuario == 1)
            db.delete(TABLE_USER, "$ID_PROPIETARIO =? ", arrayOf(userId))
        else
            db.delete(TABLE_USER, "$ID_COLABORADOR =? ", arrayOf(userId))
    }

    @SuppressLint("Recycle", "Range")
    fun getCurrentUserById(userId: String, tipoUusario: Int) : User {

        // 1- Propietario | 2- Colaborador
        val cursor  = if(tipoUusario == 1)
            db.query(TABLE_USER, allColumns, "$ID_PROPIETARIO =? ", arrayOf(userId), null, null, null)
        else
            db.query(TABLE_USER, allColumns, "$ID_COLABORADOR =? ", arrayOf(userId), null, null, null)

        lateinit var cUser: User

        if(cursor.count > 0){
            cursor.use { c->
                if(c.moveToFirst()){
                    do{
                        cUser = User(
                            c.getInt(c.getColumnIndex(TIPO_USUARIO)),
                            c.getString(c.getColumnIndex(ID_PROPIETARIO)),
                            c.getString(c.getColumnIndex(ID_COLABORADOR)),
                            c.getString(c.getColumnIndex(CODIGO)),
                            c.getString(c.getColumnIndex(NOMBRE)),
                            c.getString(c.getColumnIndex(APELLIDO_P)),
                            c.getString(c.getColumnIndex(APELLIDO_M))
                        )
                    } while (c.moveToNext())
                }
            }
        } else{
            return User(0, "","", "", "", "", "")
        }

        return cUser
    }
}