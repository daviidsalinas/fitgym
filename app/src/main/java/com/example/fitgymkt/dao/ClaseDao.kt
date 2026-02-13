package com.example.fitgymkt.dao

import com.example.fitgymkt.model.Clase
import java.sql.Connection

class ClaseDao(private val connection: Connection) {

    fun insert(clase: Clase): Int {
        val sql = "INSERT INTO CLASE (nombre, descripcion, imagen_url) VALUES (?, ?, ?)"
        connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, clase.nombre)
            stmt.setString(2, clase.descripcion)
            stmt.setString(3, clase.imagenUrl)
            stmt.executeUpdate()
            stmt.generatedKeys.use { rs ->
                return if (rs.next()) rs.getInt(1) else -1
            }
        }
    }

    fun findById(idClase: Int): Clase? {
        val sql = "SELECT * FROM CLASE WHERE id_clase = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, idClase)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return Clase(
                        idClase = rs.getInt("id_clase"),
                        nombre = rs.getString("nombre"),
                        descripcion = rs.getString("descripcion"),
                        imagenUrl = rs.getString("imagen_url")
                    )
                }
            }
        }
        return null
    }

    fun findAll(): List<Clase> {
        val sql = "SELECT * FROM CLASE"
        val list = mutableListOf<Clase>()
        connection.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                while (rs.next()) {
                    list.add(
                        Clase(
                            idClase = rs.getInt("id_clase"),
                            nombre = rs.getString("nombre"),
                            descripcion = rs.getString("descripcion"),
                            imagenUrl = rs.getString("imagen_url")
                        )
                    )
                }
            }
        }
        return list
    }

    fun update(clase: Clase) {
        val sql = "UPDATE CLASE SET nombre = ?, descripcion = ?, imagen_url = ? WHERE id_clase = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, clase.nombre)
            stmt.setString(2, clase.descripcion)
            stmt.setString(3, clase.imagenUrl)
            stmt.setInt(4, clase.idClase ?: throw IllegalArgumentException("ID no puede ser null"))
            stmt.executeUpdate()
        }
    }

    fun delete(idClase: Int) {
        val sql = "DELETE FROM CLASE WHERE id_clase = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, idClase)
            stmt.executeUpdate()
        }
    }
}
