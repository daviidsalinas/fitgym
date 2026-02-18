package com.example.fitgymkt.repository

import android.content.ContentValues
import android.content.Context
import android.util.Patterns
import com.example.fitgymkt.data.FitGymDbHelper
import com.example.fitgymkt.model.ui.ClassScheduleItem
import com.example.fitgymkt.model.ui.ClassWithSchedules
import com.example.fitgymkt.model.ui.HomeData
import com.example.fitgymkt.model.ui.AnalysisData
import com.example.fitgymkt.model.ui.ProfileData
import com.example.fitgymkt.model.ui.ReservationDetailData
import com.example.fitgymkt.model.ui.TodayClassItem
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

        return HomeData(
            userName = userName,
            calories = calories,
            trainingHours = String.format(Locale.getDefault(), "%.1f h", hours),
            todayClasses = todayClasses
        )
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

    fun getAnalysisData(userId: Int): AnalysisData {
        val db = dbHelper.readableDatabase
        val calendar = java.util.Calendar.getInstance()
        val currentWeek = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(java.util.Calendar.YEAR)

        val streakDates = db.rawQuery(
            """
            SELECT DISTINCT fecha
            FROM actividad_usuario
            WHERE id_usuario = ?
            ORDER BY fecha DESC
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.getString(0))
                }
            }
        }

        val streakDays = calculateStreakDays(streakDates)

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

        val trainedMinutesWeek = db.rawQuery(
            """
            SELECT COALESCE(SUM(duracion_minutos), 0)
            FROM actividad_usuario
            WHERE id_usuario = ?
              AND CAST(strftime('%W', fecha) AS INTEGER) = ?
              AND CAST(strftime('%Y', fecha) AS INTEGER) = ?
            """.trimIndent(),
            arrayOf(userId.toString(), currentWeek.toString(), currentYear.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }

        val weekActivityMinutes = db.rawQuery(
            """
            SELECT CAST(strftime('%w', fecha) AS INTEGER) AS dia,
                   COALESCE(SUM(duracion_minutos), 0) AS minutos
            FROM actividad_usuario
            WHERE id_usuario = ?
              AND date(fecha) >= date('now', '-6 day')
            GROUP BY dia
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            val byDay = mutableMapOf<Int, Int>()
            while (cursor.moveToNext()) {
                byDay[cursor.getInt(0)] = cursor.getInt(1)
            }
            // Lunes..Domingo
            listOf(1, 2, 3, 4, 5, 6, 0).map { byDay[it] ?: 0 }
        }

        return AnalysisData(
            streakDays = streakDays,
            weeklyGoalHours = weeklyGoalHours,
            weeklyTrainedHours = trainedMinutesWeek / 60.0,
            weekActivityMinutes = weekActivityMinutes
        )
    }

    fun getProfileData(userId: Int): ProfileData {
        val db = dbHelper.readableDatabase

        return db.rawQuery(
            """
            SELECT
                COALESCE(u.nombre || ' ' || u.apellidos, 'Usuario') AS nombre_completo,
                COALESCE(u.email, ''),
                COALESCE((SELECT t.telefono FROM telefono_usuario t WHERE t.id_usuario = u.id_usuario ORDER BY t.id_telefono DESC LIMIT 1), ''),
                COALESCE(c.edad, 0),
                COALESCE(c.peso, 0),
                COALESCE(c.altura, 0),
                COALESCE(cfg.notificaciones, 1),
                COALESCE(cfg.idioma, 'ES')
            FROM usuario u
            LEFT JOIN cliente c ON c.id_usuario = u.id_usuario
            LEFT JOIN configuracion_usuario cfg ON cfg.id_usuario = u.id_usuario
            WHERE u.id_usuario = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
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
            } else {
                ProfileData(
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
        }
    }

    fun getReservationDetail(scheduleId: Int): ReservationDetailData? {
        val db = dbHelper.readableDatabase
        return db.rawQuery(
            """
            SELECT
                c.nombre,
                COALESCE(c.descripcion, ''),
                h.fecha,
                h.hora_inicio,
                COALESCE(u.nombre || ' ' || u.apellidos, 'Monitor'),
                s.nombre_sala,
                COALESCE(SUM(CASE WHEN r.estado = 'reservada' THEN 1 ELSE 0 END), 0) AS ocupadas,
                h.plazas_totales
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
            if (cursor.moveToFirst()) {
                ReservationDetailData(
                    className = cursor.getString(0),
                    classDescription = cursor.getString(1),
                    date = cursor.getString(2),
                    startTime = cursor.getString(3),
                    instructorName = cursor.getString(4),
                    roomName = cursor.getString(5),
                    occupiedSlots = cursor.getInt(6),
                    totalSlots = cursor.getInt(7)
                )
            } else {
                null
            }
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

    private data class LoginRow(
        val userId: Int,
        val userName: String,
        val storedPassword: String
    )


    private fun calculateStreakDays(sortedDatesDesc: List<String>): Int {
        if (sortedDatesDesc.isEmpty()) return 0

        val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
        val today = java.time.LocalDate.now()
        val firstDate = runCatching { java.time.LocalDate.parse(sortedDatesDesc.first(), formatter) }.getOrNull()
            ?: return 0

        if (firstDate != today && firstDate != today.minusDays(1)) return 0

        var streak = 1
        var previous = firstDate
        sortedDatesDesc.drop(1).forEach { rawDate ->
            val current = runCatching { java.time.LocalDate.parse(rawDate, formatter) }.getOrNull() ?: return@forEach
            if (current == previous.minusDays(1)) {
                streak += 1
                previous = current
            } else if (current != previous) {
                return streak
            }
        }
        return streak
    }
}

sealed class LoginResult {
    data class Success(val userId: Int, val userName: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

sealed class RegisterResult {
    data class Success(val userId: Int, val userName: String) : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}