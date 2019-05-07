package com.hbcx.user.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

/**
 *
 */
class DBHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "DZ_db") {
    companion object {
        const val HISTORY_TABLE_NAME = "history_table"
        const val HISTORY_RENT_TABLE_NAME = "rent_history_table"
        const val HISTORY_TICKET_TABLE_NAME = "ticket_history_table"
        private var instance: DBHelper? = null
        @Synchronized
        fun getInstance(ctx: Context): DBHelper {
            if (instance == null) {
                instance = DBHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.createTable(HISTORY_TABLE_NAME, true, "id" to INTEGER + PRIMARY_KEY ,
                "name" to TEXT,
                "lat" to TEXT,
                "lng" to TEXT,
                "district" to TEXT,
                "address" to TEXT,
                "time" to TEXT
        )
        db?.createTable(HISTORY_RENT_TABLE_NAME, true, "id" to INTEGER + PRIMARY_KEY ,
                "name" to TEXT,
                "lat" to TEXT,
                "lng" to TEXT,
                "district" to TEXT,
                "address" to TEXT,
                "time" to TEXT
        )
        db?.createTable(HISTORY_TICKET_TABLE_NAME, true, "id" to INTEGER + PRIMARY_KEY ,
                "start" to TEXT,
                "end" to TEXT,
                "time" to TEXT,
                "start_code" to TEXT,
                "end_code" to TEXT,
                "start_id" to INTEGER,
                "start_station_id" to INTEGER,
                "end_station_id" to INTEGER,
                "line_type" to INTEGER
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}

val Context.database: DBHelper
    get() = DBHelper.getInstance(applicationContext)