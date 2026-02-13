package com.example.fitgymkt

import com.example.fitgymkt.dao.ClaseDao
import com.example.fitgymkt.dao.ClienteDao
import com.example.fitgymkt.dao.MonitorDao
import com.example.fitgymkt.model.Usuario
import com.example.fitgymkt.dao.UsuarioDao
import dao.HorarioClaseDao
import java.sql.Connection
import java.sql.DriverManager

fun main() {
    // =============================
    // 1. Conexión a SQLite
    // =============================
    val dbUrl = "jdbc:sqlite:fitgym2.db" // Cambia la ruta si es necesario
    val connection: Connection = DriverManager.getConnection(dbUrl)
    println("Conectado a la base de datos")

    // =============================
    // 2. Crear DAOs
    // =============================
    val usuarioDao = UsuarioDao(connection)
    val clienteDao = ClienteDao(connection)
    val monitorDao = MonitorDao(connection)
    val claseDao = ClaseDao(connection)
    val horarioClaseDao = HorarioClaseDao(connection)

    // =============================
    // 3. CRUD de ejemplo - Usuario
    // =============================
    try {
        println("\n--- INSERTAR USUARIO ---")
        val nuevoUsuario = Usuario(
            id = null,
            nombre = "Laura",
            apellidos = "Gomez",
            email = "laura@gmail.com",
            contraseña = "pass123",
            fotoPerfil = "laura.jpg"
        )
        val idNuevoUsuario = usuarioDao.insert(nuevoUsuario)
        println("Usuario insertado con ID = $idNuevoUsuario")

        println("\n--- BUSCAR USUARIO POR ID ---")
        val usuario = usuarioDao.findById(idNuevoUsuario)
        println(usuario)

        println("\n--- LISTAR TODOS LOS USUARIOS ---")
        val usuarios = usuarioDao.findAll()
        usuarios.forEach { println(it) }

        println("\n--- ACTUALIZAR USUARIO ---")
        usuario?.let {
            val actualizado = it.copy(nombre = "Laura Actualizada")
            usuarioDao.update(actualizado)
            println("Usuario actualizado: ${usuarioDao.findById(it.id!!)}")
        }

        println("\n--- BORRAR USUARIO ---")
        usuarioDao.delete(idNuevoUsuario)
        println("Usuario borrado")
    } catch (e: Exception) {
        println("Error en operaciones de Usuario: ${e.message}")
    }

    // =============================
    // 4. CRUD de ejemplo - Cliente
    // =============================
    try {
        println("\n--- LISTAR CLIENTES ---")
        val clientes = clienteDao.findAll()
        clientes.forEach { println(it) }

        println("\n--- BUSCAR CLIENTE POR ID ---")
        val cliente = clienteDao.findById(10) // Asegúrate que 10 existe
        println(cliente)
    } catch (e: Exception) {
        println("Error en operaciones de Cliente: ${e.message}")
    }

    // =============================
    // 5. CRUD de ejemplo - Clase
    // =============================
    try {
        println("\n--- LISTAR CLASES ---")
        val clases = claseDao.findAll()
        clases.forEach { println(it) }

        println("\n--- BUSCAR CLASE POR ID ---")
        val clase = claseDao.findById(1)
        println(clase)
    } catch (e: Exception) {
        println("Error en operaciones de Clase: ${e.message}")
    }

    // =============================
    // 6. CRUD de ejemplo - HorarioClase
    // =============================
    try {
        println("\n--- LISTAR HORARIOS ---")
        val horarios = horarioClaseDao.findAll()
        horarios.forEach { println(it) }

        println("\n--- BUSCAR HORARIO POR ID ---")
        val horario = horarioClaseDao.findById(1)
        println(horario)
    } catch (e: Exception) {
        println("Error en operaciones de HorarioClase: ${e.message}")
    }

    // =============================
    // 7. Cerrar conexión
    // =============================
    connection.close()
    println("\nConexión cerrada")
}
