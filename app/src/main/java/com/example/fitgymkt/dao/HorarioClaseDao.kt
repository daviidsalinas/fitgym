package dao



import com.example.fitgymkt.model.HorarioClase
import java.sql.Connection

class HorarioClaseDao(private val connection: Connection) {

    fun insert(h: HorarioClase): Int {
        val sql = "INSERT INTO HORARIO_CLASE (id_clase, id_monitor, id_sala, fecha, hora_inicio, hora_fin, plazas_totales) VALUES (?, ?, ?, ?, ?, ?, ?)"
        connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setInt(1, h.idClase)
            stmt.setInt(2, h.idMonitor)
            stmt.setInt(3, h.idSala)
            stmt.setString(4, h.fecha)       // formato "YYYY-MM-DD"
            stmt.setString(5, h.horaInicio) // formato "HH:MM"
            stmt.setString(6, h.horaFin)
            stmt.setInt(7, h.plazasTotales)
            stmt.executeUpdate()
            stmt.generatedKeys.use { rs ->
                return if (rs.next()) rs.getInt(1) else -1
            }
        }
    }

    fun findById(idHorario: Int): HorarioClase? {
        val sql = "SELECT * FROM HORARIO_CLASE WHERE id_horario = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, idHorario)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return HorarioClase(
                        idHorario = rs.getInt("id_horario"),
                        idClase = rs.getInt("id_clase"),
                        idMonitor = rs.getInt("id_monitor"),
                        idSala = rs.getInt("id_sala"),
                        fecha = rs.getString("fecha"),
                        horaInicio = rs.getString("hora_inicio"),
                        horaFin = rs.getString("hora_fin"),
                        plazasTotales = rs.getInt("plazas_totales")
                    )
                }
            }
        }
        return null
    }

    fun findAll(): List<HorarioClase> {
        val list = mutableListOf<HorarioClase>()
        val sql = "SELECT * FROM HORARIO_CLASE"
        connection.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                while (rs.next()) {
                    list.add(
                        HorarioClase(
                            idHorario = rs.getInt("id_horario"),
                            idClase = rs.getInt("id_clase"),
                            idMonitor = rs.getInt("id_monitor"),
                            idSala = rs.getInt("id_sala"),
                            fecha = rs.getString("fecha"),
                            horaInicio = rs.getString("hora_inicio"),
                            horaFin = rs.getString("hora_fin"),
                            plazasTotales = rs.getInt("plazas_totales")
                        )
                    )
                }
            }
        }
        return list
    }

    fun update(h: HorarioClase) {
        val sql = "UPDATE HORARIO_CLASE SET id_clase = ?, id_monitor = ?, id_sala = ?, fecha = ?, hora_inicio = ?, hora_fin = ?, plazas_totales = ? WHERE id_horario = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, h.idClase)
            stmt.setInt(2, h.idMonitor)
            stmt.setInt(3, h.idSala)
            stmt.setString(4, h.fecha)
            stmt.setString(5, h.horaInicio)
            stmt.setString(6, h.horaFin)
            stmt.setInt(7, h.plazasTotales)
            stmt.setInt(8, h.idHorario ?: throw IllegalArgumentException("ID no puede ser null"))
            stmt.executeUpdate()
        }
    }

    fun delete(idHorario: Int) {
        val sql = "DELETE FROM HORARIO_CLASE WHERE id_horario = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, idHorario)
            stmt.executeUpdate()
        }
    }
}


