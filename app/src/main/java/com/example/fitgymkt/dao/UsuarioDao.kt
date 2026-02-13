package com.example.fitgymkt.dao



import com.example.fitgymkt.model.Usuario
import java.sql.Connection

class UsuarioDao(private val connection: Connection) {

    fun insert(usuario: Usuario): Int {
        val sql = "INSERT INTO USUARIO (nombre, apellidos, email, contrasena, fotoPerfil) VALUES (?, ?, ?, ?, ?)"
        connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS).use { stmt ->
            stmt.setString(1, usuario.nombre)
            stmt.setString(2, usuario.apellidos)
            stmt.setString(3, usuario.email)
            stmt.setString(4, usuario.contraseña)
            stmt.setString(5, usuario.fotoPerfil)
            stmt.executeUpdate()
            stmt.generatedKeys.use { rs ->
                return if (rs.next()) rs.getInt(1) else -1
            }
        }
    }

    fun findById(id: Int): Usuario? {
        val sql = "SELECT * FROM USUARIO WHERE id_usuario = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return Usuario(
                        id = rs.getInt("id_usuario"),
                        nombre = rs.getString("nombre"),
                        apellidos = rs.getString("apellidos"),
                        email = rs.getString("email"),
                        contraseña = rs.getString("contrasena"),
                        fotoPerfil = rs.getString("fotoPerfil")
                    )
                }
            }
        }
        return null
    }

    fun findAll(): List<Usuario> {
        val sql = "SELECT * FROM USUARIO"
        val usuarios = mutableListOf<Usuario>()
        connection.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { rs ->
                while (rs.next()) {
                    usuarios.add(
                        Usuario(
                            id = rs.getInt("id_usuario"),
                            nombre = rs.getString("nombre"),
                            apellidos = rs.getString("apellidos"),
                            email = rs.getString("email"),
                            contraseña = rs.getString("contrasena"),
                            fotoPerfil = rs.getString("fotoPerfil")
                        )
                    )
                }
            }
        }
        return usuarios
    }

    fun update(usuario: Usuario) {
        val sql = "UPDATE USUARIO SET nombre = ?, apellidos = ?, email = ?, contrasena = ?, fotoPerfil = ? WHERE id_usuario = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, usuario.nombre)
            stmt.setString(2, usuario.apellidos)
            stmt.setString(3, usuario.email)
            stmt.setString(4, usuario.contraseña)
            stmt.setString(5, usuario.fotoPerfil)
            stmt.setInt(6, usuario.id ?: throw IllegalArgumentException("ID no puede ser null"))
            stmt.executeUpdate()
        }
    }

    fun delete(id: Int) {
        val sql = "DELETE FROM USUARIO WHERE id_usuario = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            stmt.executeUpdate()
        }
    }
}
