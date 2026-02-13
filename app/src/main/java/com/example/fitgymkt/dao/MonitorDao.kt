package com.example.fitgymkt.dao



import com.example.fitgymkt.model.Monitor
import java.sql.Connection

class MonitorDao(private val connection: Connection) {

    fun insert(monitor: Monitor) {
        val sql = "INSERT INTO MONITOR (id_usuario, especialidad, telefono_contacto) VALUES (?, ?, ?)"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, monitor.idUsuario)
            stmt.setString(2, monitor.especialidad)
            stmt.setString(3, monitor.telefonoContacto)
            stmt.executeUpdate()
        }
    }

    fun findById(idUsuario: Int): Monitor? {
        val sql = "SELECT * FROM MONITOR WHERE id_usuario = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, idUsuario)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return Monitor(
                        idUsuario = rs.getInt("id_usuario"),
                        especialidad = rs.getString("especialidad"),
                        telefonoContacto = rs.getString("telefono_contacto")
                    )
                }
            }
        }
        return null
    }

    fun findAll(): List<Monitor> {
        val sql = "SELECT * FROM MONITOR"
        val list = mutableListOf<Monitor>()
        connection.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                while (rs.next()) {
                    list.add(
                        Monitor(
                            idUsuario = rs.getInt("id_usuario"),
                            especialidad = rs.getString("especialidad"),
                            telefonoContacto = rs.getString("telefono_contacto")
                        )
                    )
                }
            }
        }
        return list
    }

    fun update(monitor: Monitor) {
        val sql = "UPDATE MONITOR SET especialidad = ?, telefono_contacto = ? WHERE id_usuario = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, monitor.especialidad)
            stmt.setString(2, monitor.telefonoContacto)
            stmt.setInt(3, monitor.idUsuario)
            stmt.executeUpdate()
        }
    }

    fun delete(idUsuario: Int) {
        val sql = "DELETE FROM MONITOR WHERE id_usuario = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, idUsuario)
            stmt.executeUpdate()
        }
    }
}
