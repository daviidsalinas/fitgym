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
        createAndSeedDatabase(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)

        if (!hasRequiredSchema(db)) {
            recreateDatabase(db)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        recreateDatabase(db)
    }

    private fun hasRequiredSchema(db: SQLiteDatabase): Boolean {
        val hasUsuarioTable = db.rawQuery(
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'usuario' LIMIT 1",
            null
        ).use { cursor ->
            cursor.moveToFirst()
        }

        if (!hasUsuarioTable) return false

        val usuarioColumns = db.rawQuery("PRAGMA table_info(usuario)", null).use { cursor ->
            buildSet {
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameIndex))
                }
            }
        }

        return REQUIRED_USUARIO_COLUMNS.all { it in usuarioColumns }
    }

    private fun recreateDatabase(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            TABLES_IN_DROP_ORDER.forEach { table ->
                db.execSQL("DROP TABLE IF EXISTS $table")
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        createAndSeedDatabase(db)
    }

    private fun createAndSeedDatabase(db: SQLiteDatabase) {
        executeSqlScript(db, DDL_ASSET_PATH)
        executeSqlScript(db, DML_ASSET_PATH)
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

        private val REQUIRED_USUARIO_COLUMNS = setOf(
            "id_usuario",
            "nombre",
            "apellidos",
            "email",
            "contraseña"
        )

        private val TABLES_IN_DROP_ORDER = listOf(
            "configuracion_usuario",
            "suscripcion",
            "favorito",
            "objetivo_semanal",
            "actividad_usuario",
            "reserva",
            "horario_clase",
            "sala",
            "clase",
            "direccion_usuario",
            "telefono_usuario",
            "monitor",
            "cliente",
            "usuario"
        )
    }
}
