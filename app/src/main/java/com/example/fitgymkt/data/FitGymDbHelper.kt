package com.example.fitgymkt.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Helper para trabajar con una base de datos pre-cargada en assets.
 */
class FitGymDbHelper(
    private val appContext: Context
) : SQLiteOpenHelper(appContext, DB_NAME, null, DB_VERSION) {

    init {
        ensureDatabaseCopied()
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.i(TAG, "onCreate() invocado: se usa base pre-cargada en assets")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.i(TAG, "Actualizando BD de versión $oldVersion a $newVersion")
        db.close()
        replaceDatabaseFromAssets()
    }

    fun openReadableDatabase(): SQLiteDatabase {
        ensureDatabaseCopied()
        return try {
            super.getReadableDatabase()
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo abrir readableDatabase", e)
            throw SQLiteException("No se pudo abrir la base de datos en modo lectura", e)
        }
    }

    fun openWritableDatabase(): SQLiteDatabase {
        ensureDatabaseCopied()
        return try {
            super.getWritableDatabase()
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo abrir writableDatabase", e)
            throw SQLiteException("No se pudo abrir la base de datos en modo escritura", e)
        }
    }

    override fun getReadableDatabase(): SQLiteDatabase = openReadableDatabase()
    override fun getWritableDatabase(): SQLiteDatabase = openWritableDatabase()

    @Synchronized
    private fun ensureDatabaseCopied() {
        val dbFile = appContext.getDatabasePath(DB_NAME)
        val shouldCopy = !dbFile.exists() || !isCopiedVersionCurrent()

        if (!shouldCopy) return

        if (!dbFile.exists()) {
            Log.i(TAG, "BD no encontrada en almacenamiento interno. Copiando desde assets...")
        } else {
            Log.i(TAG, "Versión de BD desactualizada. Reemplazando archivo local...")
        }

        copyDatabaseFromAssets(dbFile)
        markCopiedVersion(DB_VERSION)
    }

    @Synchronized
    private fun replaceDatabaseFromAssets() {
        val dbFile = appContext.getDatabasePath(DB_NAME)
        if (dbFile.exists() && !dbFile.delete()) {
            Log.w(TAG, "No se pudo borrar la BD anterior en ${dbFile.absolutePath}. Se intentará sobrescribir.")
        }
        copyDatabaseFromAssets(dbFile)
        markCopiedVersion(DB_VERSION)
    }

    private fun copyDatabaseFromAssets(destination: File) {
        destination.parentFile?.let { parent ->
            if (!parent.exists() && !parent.mkdirs()) {
                throw IllegalStateException("No se pudo crear carpeta de databases: ${parent.absolutePath}")
            }
        }

        val tmpFile = File(destination.parentFile, "$DB_NAME.tmp")

        try {
            appContext.assets.open(DB_NAME).use { input ->
                FileOutputStream(tmpFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                    output.fd.sync()
                }
            }

            if (destination.exists() && !destination.delete()) {
                throw IOException("No se pudo eliminar BD previa en ${destination.absolutePath}")
            }

            if (!tmpFile.renameTo(destination)) {
                throw IOException("No se pudo mover archivo temporal a ${destination.absolutePath}")
            }

            Log.i(TAG, "BD copiada correctamente a ${destination.absolutePath}")

        } catch (e: Exception) {
            Log.e(TAG, "Error al copiar BD desde assets", e)
            if (tmpFile.exists()) tmpFile.delete()
            throw IllegalStateException("No se pudo preparar la base de datos", e)
        }
    }

    private fun isCopiedVersionCurrent(): Boolean {
        val stored = appContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_DB_COPIED_VERSION, -1)
        return stored == DB_VERSION
    }

    private fun markCopiedVersion(version: Int) {
        appContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_DB_COPIED_VERSION, version)
            .apply()
    }

    companion object {
        private const val TAG = "FitGymDbHelper"
        private const val DB_NAME = "fitgym.db"
        private const val DB_VERSION = 2
        private const val PREFS_NAME = "fitgym_db_prefs"
        private const val KEY_DB_COPIED_VERSION = "copied_db_version"
    }
}
