package com.example.fitgymkt.repository

import android.content.ContentValues
import android.content.Context
import android.util.Patterns
import com.example.fitgymkt.data.FitGymDbHelper
import com.example.fitgymkt.model.ui.AnalysisData
import com.example.fitgymkt.model.ui.ClassScheduleItem
import com.example.fitgymkt.model.ui.ClassWithSchedules
import com.example.fitgymkt.model.ui.HomeData
import com.example.fitgymkt.model.ui.ProfileData
import com.example.fitgymkt.model.ui.ReservationDetailData
import com.example.fitgymkt.model.ui.TodayClassItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FitGymRepository(context: Context) {

    private val dbHelper = FitGymDbHelper(context.applicationContext)

    fun getHomeData(userId: Int): HomeData {
        val db = dbHelper.readableDatabase

        val userName = db.rawQuery(
            "SELECT nombre FROM usuario WHERE id_usuario = ?",
            arrayOf(userId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else "Usuario"
        }

        val calories = db.rawQuery(
            """
            SELECT COALESCE(SUM(duracion_minutos * 8), 0)
            FROM actividad_usuario
            WHERE id_usuario = ?
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }

        val totalMinutes = db.rawQuery(
            """
            SELECT COALESCE(SUM(duracion_minutos), 0)
            FROM actividad_usuario
            WHERE id_usuario = ?
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }

        val todayClasses = db.rawQuery(
            """
            SELECT c.nombre, h.hora_inicio, s.nombre_sala
            FROM reserva r
            INNER JOIN horario_clase h ON h.id_horario = r.id_horario
            INNER JOIN clase c ON c.id_clase = h.id_clase
            INNER JOIN sala s ON s.id_sala = h.id_sala
            WHERE r.id_usuario = ?
              AND r.estado IN ('reservada', 'completada')
            ORDER BY h.fecha ASC, h.hora_inicio ASC
            LIMIT 3
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        TodayClassItem(
                            className = cursor.getString(0),
                            startTime = cursor.getString(1),
                            roomName = cursor.getString(2)
                        )
                    )
                }
            }
        }

        val hours = totalMinutes.toDouble() / 60.0
        val streakDays = calculateCurrentStreakDays(userId)

        return HomeData(
            userName = userName,
            calories = calories,
            trainingHours = String.format(Locale.getDefault(), "%.1f h", hours),
            todayClasses = todayClasses,
            streakDays = streakDays
        )
    }

    fun getAnalysisData(userId: Int): AnalysisData {
        val db = dbHelper.readableDatabase
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val activityRows = db.rawQuery(
            """
            SELECT fecha, COALESCE(SUM(duracion_minutos), 0)
            FROM actividad_usuario
            WHERE id_usuario = ?
            GROUP BY fecha
            ORDER BY fecha ASC
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val date = dateFormatter.parse(cursor.getString(0)) ?: continue
                    val minutes = cursor.getInt(1)
                    add(date to minutes)
                }
            }
        }

        val streakDays = calculateCurrentStreak(activityRows.map { it.first }, dateFormatter)

        val latestDate = activityRows.lastOrNull()?.first ?: Date()
        val weeklyHours = (0..6).map { offset ->
            val calendar = Calendar.getInstance().apply {
                time = latestDate
                add(Calendar.DAY_OF_YEAR, offset - 6)
            }
            val day = dateFormatter.format(calendar.time)
            val minutes = activityRows.firstOrNull { dateFormatter.format(it.first) == day }?.second ?: 0
            minutes / 60.0
        }

        val weeklyCompletedHours = weeklyHours.sum()

        val calendar = Calendar.getInstance().apply { time = latestDate }
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        val weeklyGoalHours = db.rawQuery(
            """
            SELECT horas_objetivo
            FROM objetivo_semanal
            WHERE id_usuario = ? AND semana = ? AND anio = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(userId.toString(), currentWeek.toString(), currentYear.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getDouble(0) else 8.0
        }

        return AnalysisData(
            streakDays = streakDays,
            weeklyGoalHours = weeklyGoalHours,
            weeklyCompletedHours = weeklyCompletedHours,
            weeklyActivityHours = weeklyHours
        )
    }

    fun getProfileData(userId: Int): ProfileData {
        val db = dbHelper.readableDatabase

        return db.rawQuery(
            """
            SELECT
                u.nombre || ' ' || u.apellidos AS full_name,
                u.email,
                COALESCE(t.telefono, ''),
                COALESCE(c.edad, 0),
                COALESCE(c.peso, 0),
                COALESCE(c.altura, 0),
                COALESCE(cfg.notificaciones, 1),
                COALESCE(cfg.idioma, 'ES')
            FROM usuario u
            LEFT JOIN cliente c ON c.id_usuario = u.id_usuario
            LEFT JOIN telefono_usuario t ON t.id_usuario = u.id_usuario
            LEFT JOIN configuracion_usuario cfg ON cfg.id_usuario = u.id_usuario
            WHERE u.id_usuario = ?
            ORDER BY t.id_telefono ASC
            LIMIT 1
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                return@use ProfileData(
                    fullName = "Usuario",
                    email = "",
                    phone = "",
                    age = 0,
                    weightKg = 0.0,
                    heightCm = 0.0,
                    notificationsEnabled = true,
                    language = "ES"
                )
            }

            ProfileData(
                fullName = cursor.getString(0),
                email = cursor.getString(1),
                phone = cursor.getString(2),
                age = cursor.getInt(3),
                weightKg = cursor.getDouble(4),
                heightCm = cursor.getDouble(5),
                notificationsEnabled = cursor.getInt(6) == 1,
                language = cursor.getString(7)
            )
        }
    }

    fun getReservationDetail(userId: Int): ReservationDetailData? {
        val db = dbHelper.readableDatabase

        return db.rawQuery(
            """
            SELECT
                c.nombre,
                c.descripcion,
                h.fecha,
                h.hora_inicio,
                u.nombre || ' ' || u.apellidos AS instructor,
                s.nombre_sala,
                h.plazas_totales,
                COALESCE(SUM(CASE WHEN r2.estado = 'reservada' THEN 1 ELSE 0 END), 0) AS ocupadas
            FROM reserva r
            INNER JOIN horario_clase h ON h.id_horario = r.id_horario
            INNER JOIN clase c ON c.id_clase = h.id_clase
            INNER JOIN sala s ON s.id_sala = h.id_sala
            INNER JOIN monitor m ON m.id_usuario = h.id_monitor
            INNER JOIN usuario u ON u.id_usuario = m.id_usuario
            LEFT JOIN reserva r2 ON r2.id_horario = h.id_horario
            WHERE r.id_usuario = ?
              AND r.estado IN ('reservada', 'completada')
            GROUP BY h.id_horario
            ORDER BY h.fecha ASC, h.hora_inicio ASC
            LIMIT 1
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) return@use null

            ReservationDetailData(
                className = cursor.getString(0),
                classDescription = cursor.getString(1) ?: "Sin descripción",
                date = cursor.getString(2),
                startTime = cursor.getString(3),
                instructorName = cursor.getString(4),
                roomName = cursor.getString(5),
                totalSlots = cursor.getInt(6),
                occupiedSlots = cursor.getInt(7)
            )
        }
    }

    fun getReservationDetailForSchedule(scheduleId: Int): ReservationDetailData? {
        val db = dbHelper.readableDatabase

        return db.rawQuery(
            """
            SELECT
                c.nombre,
                c.descripcion,
                h.fecha,
                h.hora_inicio,
                u.nombre || ' ' || u.apellidos AS instructor,
                s.nombre_sala,
                h.plazas_totales,
                COALESCE(SUM(CASE WHEN r.estado = 'reservada' THEN 1 ELSE 0 END), 0) AS ocupadas
            FROM horario_clase h
            INNER JOIN clase c ON c.id_clase = h.id_clase
            INNER JOIN sala s ON s.id_sala = h.id_sala
            INNER JOIN monitor m ON m.id_usuario = h.id_monitor
            INNER JOIN usuario u ON u.id_usuario = m.id_usuario
            LEFT JOIN reserva r ON r.id_horario = h.id_horario
            WHERE h.id_horario = ?
            GROUP BY h.id_horario
            LIMIT 1
            """.trimIndent(),
            arrayOf(scheduleId.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) return@use null

            ReservationDetailData(
                className = cursor.getString(0),
                classDescription = cursor.getString(1) ?: "Sin descripción",
                date = cursor.getString(2),
                startTime = cursor.getString(3),
                instructorName = cursor.getString(4),
                roomName = cursor.getString(5),
                totalSlots = cursor.getInt(6),
                occupiedSlots = cursor.getInt(7)
            )
        }
    }

    fun reserveClass(userId: Int, scheduleId: Int): ActionResult {
        val db = dbHelper.writableDatabase

        val alreadyReserved = db.rawQuery(
            "SELECT 1 FROM reserva WHERE id_usuario = ? AND id_horario = ? AND estado != 'cancelada' LIMIT 1",
            arrayOf(userId.toString(), scheduleId.toString())
        ).use { it.moveToFirst() }

        if (alreadyReserved) {
            return ActionResult.Error("Ya tienes una reserva para esta clase")
        }

        val capacityRow = db.rawQuery(
            """
            SELECT h.plazas_totales, COALESCE(SUM(CASE WHEN r.estado = 'reservada' THEN 1 ELSE 0 END), 0)
            FROM horario_clase h
            LEFT JOIN reserva r ON r.id_horario = h.id_horario
            WHERE h.id_horario = ?
            GROUP BY h.id_horario
            """.trimIndent(),
            arrayOf(scheduleId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) to cursor.getInt(1) else null
        } ?: return ActionResult.Error("Horario no encontrado")

        if (capacityRow.second >= capacityRow.first) {
            return ActionResult.Error("No hay plazas disponibles")
        }

        val values = ContentValues().apply {
            put("id_usuario", userId)
            put("id_horario", scheduleId)
            put("estado", "reservada")
            put("fecha_reserva", currentDateIso())
        }

        return try {
            db.insertOrThrow("reserva", null, values)
            ActionResult.Success("¡Reserva confirmada!")
        } catch (_: Exception) {
            ActionResult.Error("No se pudo completar la reserva")
        }
    }

    fun registerWorkout(userId: Int, minutes: Int): ActionResult {
        if (minutes <= 0) return ActionResult.Error("Introduce una duración válida")

        val values = ContentValues().apply {
            put("id_usuario", userId)
            put("fecha", currentDateIso())
            put("duracion_minutos", minutes)
        }

        return try {
            dbHelper.writableDatabase.insertOrThrow("actividad_usuario", null, values)
            ActionResult.Success("Entrenamiento registrado")
        } catch (_: Exception) {
            ActionResult.Error("No se pudo registrar el entrenamiento")
        }
    }

    private fun currentDateIso(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun updateProfileData(
        userId: Int,
        email: String,
        phone: String,
        age: Int,
        weightKg: Double,
        heightCm: Double
    ): ActionResult {
        val normalizedEmail = email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            return ActionResult.Error("Email no válido")
        }

        val db = dbHelper.writableDatabase
        db.beginTransaction()

        return try {
            val emailInUse = db.rawQuery(
                "SELECT 1 FROM usuario WHERE lower(email)=lower(?) AND id_usuario != ? LIMIT 1",
                arrayOf(normalizedEmail, userId.toString())
            ).use { it.moveToFirst() }

            if (emailInUse) {
                ActionResult.Error("Ese email ya está en uso")
            } else {
                db.update("usuario", ContentValues().apply { put("email", normalizedEmail) }, "id_usuario = ?", arrayOf(userId.toString()))
                db.update("cliente", ContentValues().apply {
                    put("edad", age)
                    put("peso", weightKg)
                    put("altura", heightCm)
                }, "id_usuario = ?", arrayOf(userId.toString()))

                db.delete("telefono_usuario", "id_usuario = ?", arrayOf(userId.toString()))
                if (phone.isNotBlank()) {
                    db.insertOrThrow("telefono_usuario", null, ContentValues().apply {
                        put("id_usuario", userId)
                        put("telefono", phone.trim())
                    })
                }

                db.setTransactionSuccessful()
                ActionResult.Success("Perfil actualizado")
            }
        } catch (_: Exception) {
            ActionResult.Error("No se pudo actualizar el perfil")
        } finally {
            db.endTransaction()
        }
    }

    fun getClassesByWeekDay(dayFilter: String): List<ClassWithSchedules> {
        val db = dbHelper.readableDatabase
        val query =
            """
            SELECT
                c.id_clase,
                c.nombre,
                c.descripcion,
                h.id_horario,
                h.hora_inicio,
                h.fecha,
                h.plazas_totales,
                COALESCE(SUM(CASE WHEN r.estado = 'reservada' THEN 1 ELSE 0 END), 0) AS plazas_ocupadas,
                u.nombre || ' ' || u.apellidos AS instructor
            FROM clase c
            INNER JOIN horario_clase h ON h.id_clase = c.id_clase
            INNER JOIN monitor m ON m.id_usuario = h.id_monitor
            INNER JOIN usuario u ON u.id_usuario = m.id_usuario
            LEFT JOIN reserva r ON r.id_horario = h.id_horario
            WHERE (? = 'Todos' OR strftime('%w', h.fecha) = ?)
            GROUP BY h.id_horario
            ORDER BY c.nombre ASC, h.fecha ASC, h.hora_inicio ASC
            """.trimIndent()

        val filterCode = weekDayToSqlCode(dayFilter)
        val rows = db.rawQuery(query, arrayOf(dayFilter, filterCode)).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        RawScheduleRow(
                            classId = cursor.getInt(0),
                            className = cursor.getString(1),
                            description = cursor.getString(2) ?: "",
                            scheduleId = cursor.getInt(3),
                            startTime = cursor.getString(4),
                            date = cursor.getString(5),
                            totalSlots = cursor.getInt(6),
                            occupiedSlots = cursor.getInt(7),
                            instructorName = cursor.getString(8)
                        )
                    )
                }
            }
        }

        return rows
            .groupBy { it.classId }
            .map { (_, classRows) ->
                val first = classRows.first()
                ClassWithSchedules(
                    classId = first.classId,
                    className = first.className,
                    description = first.description,
                    schedules = classRows.map { row ->
                        ClassScheduleItem(
                            scheduleId = row.scheduleId,
                            time = row.startTime,
                            weekDay = sqlDateToWeekDay(row.date),
                            occupiedSlots = row.occupiedSlots,
                            totalSlots = row.totalSlots,
                            instructorName = row.instructorName
                        )
                    }
                )
            }
    }

    fun login(email: String, password: String): LoginResult {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank() || password.isBlank()) {
            return LoginResult.Error("Introduce email y contraseña")
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            return LoginResult.Error("Formato de email no válido")
        }

        val loginRow = try {
            val db = dbHelper.readableDatabase
            db.rawQuery(
                "SELECT id_usuario, nombre, contraseña FROM usuario WHERE lower(email) = lower(?) LIMIT 1",
                arrayOf(normalizedEmail)
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    LoginRow(
                        userId = cursor.getInt(0),
                        userName = cursor.getString(1),
                        storedPassword = cursor.getString(2)
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            return LoginResult.Error("No se pudo acceder a la base de datos")
        }

        return when {
            loginRow == null -> LoginResult.Error("No existe una cuenta con ese email")
            loginRow.storedPassword != password -> LoginResult.Error("Contraseña incorrecta")
            else -> LoginResult.Success(userId = loginRow.userId, userName = loginRow.userName)
        }
    }

    fun register(nombreCompleto: String, email: String, telefono: String, password: String): RegisterResult {

        val normalizedEmail = email.trim()
        val fullName = nombreCompleto.trim().replace(Regex("\\s+"), " ")

        if (fullName.isBlank() || normalizedEmail.isBlank() || password.isBlank()) {
            return RegisterResult.Error("Completa todos los campos obligatorios")
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            return RegisterResult.Error("Formato de email no válido")
        }

        if (password.length < 6) {
            return RegisterResult.Error("La contraseña debe tener al menos 6 caracteres")
        }

        val db = try {
            dbHelper.writableDatabase
        } catch (e: Exception) {
            return RegisterResult.Error("No se pudo acceder a la base de datos")
        }

        val emailExists = try {
            db.rawQuery(
                "SELECT 1 FROM usuario WHERE lower(email) = lower(?) LIMIT 1",
                arrayOf(normalizedEmail)
            ).use { it.moveToFirst() }
        } catch (e: Exception) {
            return RegisterResult.Error("No se pudo acceder a la base de datos")
        }

        if (emailExists) {
            return RegisterResult.Error("Ya existe una cuenta con ese email")
        }

        val nameParts = fullName.split(" ", limit = 2)
        val nombre = nameParts.first()
        val apellidos = if (nameParts.size > 1) nameParts[1] else ""

        db.beginTransaction()
        return try {
            val userValues = ContentValues().apply {
                put("nombre", nombre)
                put("apellidos", apellidos)
                put("email", normalizedEmail)
                put("contraseña", password)
                put("fotoPerfil", "")
            }
            val userId = db.insertOrThrow("usuario", null, userValues)

            val clienteValues = ContentValues().apply {
                put("id_usuario", userId)
                put("edad", 0)
                put("peso", 0.0)
                put("altura", 0.0)
            }
            db.insertOrThrow("cliente", null, clienteValues)

            if (telefono.isNotBlank()) {
                val telefonoValues = ContentValues().apply {
                    put("id_usuario", userId)
                    put("telefono", telefono.trim())
                }
                db.insertOrThrow("telefono_usuario", null, telefonoValues)
            }

            db.setTransactionSuccessful()
            RegisterResult.Success(userId.toInt(), nombre)
        } catch (e: Exception) {
            RegisterResult.Error("No se pudo registrar el usuario")
        } finally {
            db.endTransaction()
        }
    }

    private fun weekDayToSqlCode(day: String): String {
        return when (day) {
            "Domingo" -> "0"
            "Lunes" -> "1"
            "Martes" -> "2"
            "Miércoles" -> "3"
            "Jueves" -> "4"
            "Viernes" -> "5"
            "Sábado" -> "6"
            else -> "-1"
        }
    }

    private fun sqlDateToWeekDay(date: String): String {
        val parts = date.split("-")
        if (parts.size != 3) return ""

        val year = parts[0].toIntOrNull() ?: return ""
        val month = parts[1].toIntOrNull() ?: return ""
        val day = parts[2].toIntOrNull() ?: return ""

        val calendar = java.util.Calendar.getInstance().apply {
            set(year, month - 1, day)
        }

        return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Lunes"
            java.util.Calendar.TUESDAY -> "Martes"
            java.util.Calendar.WEDNESDAY -> "Miércoles"
            java.util.Calendar.THURSDAY -> "Jueves"
            java.util.Calendar.FRIDAY -> "Viernes"
            java.util.Calendar.SATURDAY -> "Sábado"
            else -> "Domingo"
        }
    }

    private data class RawScheduleRow(
        val classId: Int,
        val className: String,
        val description: String,
        val scheduleId: Int,
        val startTime: String,
        val date: String,
        val totalSlots: Int,
        val occupiedSlots: Int,
        val instructorName: String
    )

    private fun calculateCurrentStreak(dates: List<Date>, formatter: SimpleDateFormat): Int {
        if (dates.isEmpty()) return 0

        val sorted = dates.distinct().sortedDescending()
        var streak = 1

        for (index in 1 until sorted.size) {
            val expectedCalendar = Calendar.getInstance().apply {
                time = sorted[index - 1]
                add(Calendar.DAY_OF_YEAR, -1)
            }

            if (formatter.format(sorted[index]) == formatter.format(expectedCalendar.time)) {
                streak++
            } else {
                break
            }
        }

        return streak
    }

    private fun calculateCurrentStreakDays(userId: Int): Int {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dates = dbHelper.readableDatabase.rawQuery(
            "SELECT DISTINCT fecha FROM actividad_usuario WHERE id_usuario = ? ORDER BY fecha DESC",
            arrayOf(userId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    formatter.parse(cursor.getString(0))?.let { add(it) }
                }
            }
        }
        return calculateCurrentStreak(dates, formatter)
    }

    private data class LoginRow(
        val userId: Int,
        val userName: String,
        val storedPassword: String
    )
}

sealed class LoginResult {
    data class Success(val userId: Int, val userName: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

sealed class RegisterResult {
    data class Success(val userId: Int, val userName: String) : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}

sealed class ActionResult {
    data class Success(val message: String) : ActionResult()
    data class Error(val message: String) : ActionResult()
}