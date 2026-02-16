package com.example.fitgymkt.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FitGymDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    private val appContext = context.applicationContext

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        executeSqlScript(db, DDL_ASSET_PATH)
        executeSqlScript(db, DML_ASSET_PATH)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS configuracion_usuario")
        db.execSQL("DROP TABLE IF EXISTS suscripcion")
        db.execSQL("DROP TABLE IF EXISTS favorito")
        db.execSQL("DROP TABLE IF EXISTS objetivo_semanal")
        db.execSQL("DROP TABLE IF EXISTS actividad_usuario")
        db.execSQL("DROP TABLE IF EXISTS reserva")
        db.execSQL("DROP TABLE IF EXISTS horario_clase")
        db.execSQL("DROP TABLE IF EXISTS sala")
        db.execSQL("DROP TABLE IF EXISTS clase")
        db.execSQL("DROP TABLE IF EXISTS direccion_usuario")
        db.execSQL("DROP TABLE IF EXISTS telefono_usuario")
        db.execSQL("DROP TABLE IF EXISTS monitor")
        db.execSQL("DROP TABLE IF EXISTS cliente")
        db.execSQL("DROP TABLE IF EXISTS usuario")
        onCreate(db)
    }

    private fun executeSqlScript(db: SQLiteDatabase, assetPath: String) {
        val script = appContext.assets.open(assetPath).bufferedReader().use { it.readText() }
        val statements = script
            .lineSequence()
            .map { it.substringBefore("--").trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        db.beginTransaction()
        try {
            statements.forEach { statement ->
                db.execSQL(statement)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    companion object {
        private const val DB_NAME = "fitgym_app.db"
        private const val DB_VERSION = 2
        private const val DDL_ASSET_PATH = "sql/fitgym_ddl.sql"
        private const val DML_ASSET_PATH = "sql/fitgym_dml.sql"
    }
}
