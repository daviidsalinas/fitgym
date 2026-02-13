package com.example.fitgymkt.dao


import com.example.fitgymkt.model.Reserva
import java.sql.Connection
import java.sql.ResultSet

class ReservaDao(private val connection: Connection) {

    fun crear(reserva: Reserva): Boolean {
        val sql = "INSERT INTO RESERVA (id_usuario, id_horario, estado, fecha_reserva) VALUES (?, ?, ?, ?)"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, reserva.idUsuario)
            stmt.setInt(2, reserva.idHorario)
            stmt.setString(3, reserva.estado)
            stmt.setString(4, reserva.fechaReserva)
            return stmt.executeUpdate() > 0
        }
    }

    fun leer(id: Int): Reserva? {
        val sql = "SELECT * FROM RESERVA WHERE id_reserva=?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            val rs = stmt.executeQuery()
            return if (rs.next()) mapear(rs) else null
        }
    }

    fun actualizar(reserva: Reserva): Boolean {
        val sql = "UPDATE RESERVA SET id_usuario=?, id_horario=?, estado=?, fecha_reserva=? WHERE id_reserva=?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, reserva.idUsuario)
            stmt.setInt(2, reserva.idHorario)
            stmt.setString(3, reserva.estado)
            stmt.setString(4, reserva.fechaReserva)
            stmt.setInt(5, reserva.idReserva)
            return stmt.executeUpdate() > 0
        }
    }

    fun eliminar(id: Int): Boolean {
        val sql = "DELETE FROM RESERVA WHERE id_reserva=?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            return stmt.executeUpdate() > 0
        }
    }

    fun listarTodos(): List<Reserva> {
        val lista = mutableListOf<Reserva>()
        val sql = "SELECT * FROM RESERVA"
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery(sql)
            while (rs.next()) lista.add(mapear(rs))
        }
        return lista
    }

    private fun mapear(rs: ResultSet): Reserva =
        Reserva(
            idReserva = rs.getInt("id_reserva"),
            idUsuario = rs.getInt("id_usuario"),
            idHorario = rs.getInt("id_horario"),
            estado = rs.getString("estado"),
            fechaReserva = rs.getString("fecha_reserva")
        )
}
