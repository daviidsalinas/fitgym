package com.example.fitgymkt.dao


import com.example.fitgymkt.model.Cliente
import java.sql.Connection

class ClienteDao(private val connection: Connection) {

    fun insert(cliente: Cliente) {
        val sql = "INSERT INTO CLIENTE (id_usuario, edad, peso, altura) VALUES (?, ?, ?, ?)"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, cliente.idUsuario)
            stmt.setInt(2, cliente.edad)
            stmt.setDouble(3, cliente.peso)
            stmt.setDouble(4, cliente.altura)
            stmt.executeUpdate()
        }
    }

    fun findById(idUsuario: Int): Cliente? {
        val sql = "SELECT * FROM CLIENTE WHERE id_usuario = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, idUsuario)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return Cliente(
                        idUsuario = rs.getInt("id_usuario"),
                        edad = rs.getInt("edad"),
                        peso = rs.getDouble("peso"),
                        altura = rs.getDouble("altura")
                    )
                }
            }
        }
        return null
    }

    fun findAll(): List<Cliente> {
        val clientes = mutableListOf<Cliente>()
        val sql = "SELECT * FROM CLIENTE"
        connection.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                while (rs.next()) {
                    clientes.add(
                        Cliente(
                            idUsuario = rs.getInt("id_usuario"),
                            edad = rs.getInt("edad"),
                            peso = rs.getDouble("peso"),
                            altura = rs.getDouble("altura")
                        )
                    )
                }
            }
        }
        return clientes
    }

    fun update(cliente: Cliente) {
        val sql = "UPDATE CLIENTE SET edad = ?, peso = ?, altura = ? WHERE id_usuario = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, cliente.edad)
            stmt.setDouble(2, cliente.peso)
            stmt.setDouble(3, cliente.altura)
            stmt.setInt(4, cliente.idUsuario)
            stmt.executeUpdate()
        }
    }

    fun delete(idUsuario: Int) {
        val sql = "DELETE FROM CLIENTE WHERE id_usuario = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, idUsuario)
            stmt.executeUpdate()
        }
    }
}
