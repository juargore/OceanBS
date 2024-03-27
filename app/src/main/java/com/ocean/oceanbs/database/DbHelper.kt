package com.ocean.oceanbs.database

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ocean.oceanbs.Constants
import com.ocean.oceanbs.activities.LoginActivity

class DbHelper(val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_SCHEME_VERSION) {

    companion object{
        private const val DB_NAME = "OCEANBS_DB"
        private const val DB_SCHEME_VERSION = 11
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TableUser.CREATE_TABLE_USER)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {

        // Log out user to access again
        Constants.setKeepLogin(context, false)
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)

        db.execSQL("DROP TABLE IF EXISTS "+TableUser.TABLE_USER)
        onCreate(db)
    }
}