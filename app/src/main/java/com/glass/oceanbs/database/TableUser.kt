package com.glass.oceanbs.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

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

    /*
    * fun insertNewOrExistingUser(user: User){

        //First, delete all data linked to current user
        val res = deleteUserById(user.userId)
        //Log.e("--", "User deleted: $res")

        //Now, add the current user to Database
        db.insert(TABLE_USER, null, generateCV(user.userId, user.firstName, user.lastName, user.uniqueName, user.groupId, user.groupName,
            user.email, user.password, user.ttContext, user.scopeFlow, user.canAddSubs, user.accessToken, user.gcmToken, user.expires_in, user.dateTokenSaved, user.refreshToken, user.kid))
    }

    private fun deleteUserById(userId: String) : Int{
        return db.delete(TABLE_USER, "$USER_ID =? ", arrayOf(userId))
    }

    fun updateTokensByUserId(user:User, userId: String){
        val cv = ContentValues()
        cv.put(ACCESS_TOKEN, user.accessToken)
        cv.put(EXPIRES_IN, user.expires_in)
        cv.put(REFRESH_TOKEN, user.refreshToken)

        db.update(TABLE_USER, cv, "$USER_ID=${user.userId}", null)
    }

    fun getCurrentUserById(userId: String) : User{
        val cursor = db.query(TABLE_USER, allColumns, "$USER_ID =? ", arrayOf(userId), null, null, null)
        lateinit var currentUser : User

        if(cursor.count > 0){
            cursor.use {c->
                if(c.moveToFirst()){
                    do{
                        currentUser = User(
                            c.getString(c.getColumnIndex(USER_ID)),
                            c.getString(c.getColumnIndex(FIRST_NAME)),
                            c.getString(c.getColumnIndex(LAST_NAME)),
                            c.getString(c.getColumnIndex(UNIQUE_NAME)),
                            c.getString(c.getColumnIndex(GROUP_ID)),
                            c.getString(c.getColumnIndex(GROUP_NAME)),
                            c.getString(c.getColumnIndex(EMAIL)),
                            c.getString(c.getColumnIndex(PASSWORD)),
                            c.getString(c.getColumnIndex(TRACK_TRACE_CONTEXT)),
                            c.getString(c.getColumnIndex(SCOPE_FLOW)),
                            c.getString(c.getColumnIndex(CAN_ADD_SUBS)),
                            c.getString(c.getColumnIndex(ACCESS_TOKEN)),
                            c.getString(c.getColumnIndex(GCM_TOKEN)),
                            c.getString(c.getColumnIndex(EXPIRES_IN)),
                            c.getString(c.getColumnIndex(DATE_TOKEN_SAVED)),
                            c.getString(c.getColumnIndex(REFRESH_TOKEN)),
                            c.getString(c.getColumnIndex(KID))
                        )
                    } while (c.moveToNext())
                }
            }
        } else{
            return User("","","","","","","", "","", "",
            "","","","","","","")
        }
        cursor.close()

        return currentUser
    }*/
}