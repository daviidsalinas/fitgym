package com.example.fitgymkt.util

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object Database {
    private const val DB_URL = "jdbc:sqlite:fitgym2.db"
    private var connection: Connection? = null

    fun connect(): Connection {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DB_URL)
                println("Conexión a SQLite establecida")
            } catch (e: SQLException) {
                println("Error al conectar a SQLite: ${e.message}")
                throw e
            }
        }
        return connection!!
    }

    fun disconnect() {
        try {
            connection?.close()
            println("Conexión cerrada")
        } catch (e: SQLException) {
            println("Error cerrando conexión: ${e.message}")
        }
    }
}
