package com.glass.oceanbs.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_SCHEME_VERSION) {

    companion object{
        private const val DB_NAME = "OCEANBS_DB"
        private const val DB_SCHEME_VERSION = 7
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TableUser.CREATE_TABLE_USER)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS "+TableUser.TABLE_USER)
        onCreate(db)
    }
}