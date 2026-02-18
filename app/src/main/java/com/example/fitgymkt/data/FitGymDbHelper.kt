package com.example.fitgymkt.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FitGymDbHelper(private val context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    init {
        // Al crear el helper, aseguramos que la BD exista
        prepareDatabase()
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // No usamos onCreate para crear BD, ya está pre-creada
        Log.d(TAG, "onCreate llamado, la BD ya debería estar copiada")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade de $oldVersion a $newVersion")
        // Para tu proyecto, simplemente borra y copia de nuevo si cambias DB_VERSION
        recreateDatabase(db)
    }

    // --------------------
    // PREPARACIÓN DE LA BD
    // --------------------
    private fun prepareDatabase() {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) {
            Log.d(TAG, "BD no existe, copiando desde assets...")
            copyDatabaseFromAssets(dbFile)
        } else {
            Log.d(TAG, "BD ya existe en: ${dbFile.path}")
        }
    }

    private fun copyDatabaseFromAssets(dbFile: File) {
        dbFile.parentFile?.mkdirs()
        try {
            context.assets.open(DB_NAME).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.d(TAG, "Base de datos copiada correctamente a ${dbFile.path}")
        } catch (e: IOException) {
            Log.e(TAG, "Error copiando BD desde assets: ${e.message}")
            e.printStackTrace()
        }
    }

    // --------------------
    // RECREAR BD (opcional)
    // --------------------
    private fun recreateDatabase(db: SQLiteDatabase) {
        try {
            db.beginTransaction()
            TABLES_TO_DROP.forEach { table ->
                db.execSQL("DROP TABLE IF EXISTS $table")
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, "Error recreando la BD: ${e.message}")
        } finally {
            db.endTransaction()
        }
        // Luego podrías volver a copiar la BD pre-creada
        prepareDatabase()
    }

    companion object {
        private const val TAG = "FitGymDbHelper"
        private const val DB_NAME = "fitgym.db"
        private const val DB_VERSION = 2

        private val TABLES_TO_DROP = listOf(
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
