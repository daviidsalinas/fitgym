package com.example.fitgymkt.repository

import android.content.Context
import android.util.Patterns
import com.example.fitgymkt.data.SupabaseRestClient
import com.example.fitgymkt.model.ui.AnalysisData
import com.example.fitgymkt.model.ui.AppNotification
import com.example.fitgymkt.model.ui.ClassScheduleItem
import com.example.fitgymkt.model.ui.ClassWithSchedules
import com.example.fitgymkt.model.ui.HomeData
import com.example.fitgymkt.model.ui.ProfileData
import com.example.fitgymkt.model.ui.PushReminder
import com.example.fitgymkt.model.ui.ReservationDetailData
import com.example.fitgymkt.model.ui.SubscriptionStatus
import com.example.fitgymkt.model.ui.TodayClassItem
import com.example.fitgymkt.model.ui.UserReservationItem
import com.example.fitgymkt.model.ui.WorkoutHistoryItem
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class FitGymRepository(context: Context) {

    private val api = SupabaseRestClient(context)

    fun getHomeData(userId: Int): HomeData {
        return runCatching {
            val user = getSingleRow("usuario", mapOf("id_usuario" to eq(userId)), "nombre")
            val userName = user?.optString("nombre").orEmpty().ifBlank { "Usuario" }

            val activities = getRows("actividad_usuario", mapOf("id_usuario" to eq(userId)), "fecha,duracion_minutos")
            val totalMinutes = activities.sumOf { it.optInt("duracion_minutos", 0) }
            val calories = activities.sumOf { it.optInt("duracion_minutos", 0) * 8 }

            val reservations = getRows(
                "reserva",
                filters = mapOf("id_usuario" to eq(userId), "estado" to inList("reservada", "completada")),
                select = "id_horario",
                orderBy = "fecha_reserva",
                ascending = true,
                limit = 3
            )

            val todayClasses = reservations.mapNotNull { row ->
                getTodayClassItem(row.optInt("id_horario", -1))
            }

            HomeData(
                userName = userName,
                calories = calories,
                trainingHours = String.format(Locale.getDefault(), "%.1f h", totalMinutes.toDouble() / 60.0),
                todayClasses = todayClasses,
                streakDays = calculateCurrentStreakDays(userId)
            )
        }.getOrElse {
            HomeData("Usuario", 0, "0.0 h", emptyList(), 0)
        }
    }

    fun getAnalysisData(userId: Int): AnalysisData {
        return runCatching {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val activities = getRows(
                "actividad_usuario",
                mapOf("id_usuario" to eq(userId)),
                "fecha,duracion_minutos",
                orderBy = "fecha",
                ascending = true
            )

            val groupedByDate = activities.groupBy { it.optString("fecha") }
                .mapValues { (_, rows) -> rows.sumOf { it.optInt("duracion_minutos", 0) } }
                .toSortedMap()

            val dates = groupedByDate.keys.mapNotNull { formatter.parse(it) }
            val streakDays = calculateCurrentStreak(dates, formatter)
            val latestDate = dates.lastOrNull() ?: Date()

            val weeklyHours = (0..6).map { offset ->
                val cal = Calendar.getInstance().apply {
                    time = latestDate
                    add(Calendar.DAY_OF_YEAR, offset - 6)
                }
                val key = formatter.format(cal.time)
                (groupedByDate[key] ?: 0) / 60.0
            }

            val cal = Calendar.getInstance().apply { time = latestDate }
            val week = cal.get(Calendar.WEEK_OF_YEAR)
            val year = cal.get(Calendar.YEAR)

            val goal = getSingleRow(
                "objetivo_semanal",
                mapOf("id_usuario" to eq(userId), "semana" to eq(week), "anio" to eq(year)),
                "horas_objetivo"
            )?.optDouble("horas_objetivo", 8.0) ?: 8.0

            AnalysisData(streakDays, goal, weeklyHours.sum(), weeklyHours)
        }.getOrElse { AnalysisData(0, 8.0, 0.0, List(7) { 0.0 }) }
    }

    fun getProfileData(userId: Int): ProfileData {
        return runCatching {
            val user = getSingleRow("usuario", mapOf("id_usuario" to eq(userId)))
            val client = getSingleRow("cliente", mapOf("id_usuario" to eq(userId)))
            val phone = getSingleRow("telefono_usuario", mapOf("id_usuario" to eq(userId)), orderBy = "id_telefono")
            val cfg = getSingleRow("configuracion_usuario", mapOf("id_usuario" to eq(userId)))

            ProfileData(
                fullName = listOf(user?.optString("nombre"), user?.optString("apellidos")).joinToString(" ").trim().ifBlank { "Usuario" },
                profilePhoto = user?.optString("fotoPerfil").orEmpty(),
                email = user?.optString("email").orEmpty(),
                phone = phone?.optString("telefono").orEmpty(),
                age = client?.optInt("edad", 0) ?: 0,
                weightKg = client?.optDouble("peso", 0.0) ?: 0.0,
                heightCm = client?.optDouble("altura", 0.0) ?: 0.0,
                notificationsEnabled = cfg?.optBoolean("notificaciones", true) ?: true,
                language = cfg?.optString("idioma").orEmpty().ifBlank { "ES" }
            )
        }.getOrElse {
            ProfileData("Usuario", "", "", "", 0, 0.0, 0.0, true, "ES")
        }
    }

    fun updatePassword(userId: Int, currentPassword: String, newPassword: String): ActionResult {
        if (currentPassword.isBlank() || newPassword.isBlank()) return ActionResult.Error("Completa ambos campos de password")
        if (newPassword.length < 6) return ActionResult.Error("La nueva password debe tener al menos 6 caracteres")
        if (!api.isConfigured()) return ActionResult.Error("Supabase no está configurado. Revisa SUPABASE_URL y SUPABASE_ANON_KEY")

        return runCatching {
            val user = getSingleRow("usuario", mapOf("id_usuario" to eq(userId)))
                ?: return ActionResult.Error("Usuario no encontrado")
            val passwordField = resolvePasswordField(user)
            if (passwordField == null) return ActionResult.Error("No se encontró el campo de password en usuario")
            if (user.optString(passwordField) != currentPassword) return ActionResult.Error("La password actual no es correcta")

            api.update("usuario", mapOf("id_usuario" to eq(userId)), JSONObject().put(passwordField, newPassword))
            ActionResult.Success("password actualizada correctamente")
        }.getOrElse { ActionResult.Error("No se pudo actualizar la password: ${it.message.orEmpty()}") }
    }

    fun getReservationDetail(userId: Int): ReservationDetailData? {
        val reservation = getRows(
            "reserva",
            mapOf("id_usuario" to eq(userId), "estado" to inList("reservada", "completada")),
            "id_horario",
            orderBy = "fecha_reserva",
            ascending = true,
            limit = 1
        ).firstOrNull() ?: return null

        return getReservationDetailForSchedule(reservation.optInt("id_horario", -1))
    }

    fun getReservationDetailForSchedule(scheduleId: Int): ReservationDetailData? {
        return runCatching {
            if (scheduleId <= 0) return null
            val schedule = getSingleRow("horario_clase", mapOf("id_horario" to eq(scheduleId))) ?: return null
            val classRow = getSingleRow("clase", mapOf("id_clase" to eq(schedule.optInt("id_clase"))))
            val room = getSingleRow("sala", mapOf("id_sala" to eq(schedule.optInt("id_sala"))))
            val monitorUser = getSingleRow("usuario", mapOf("id_usuario" to eq(schedule.optInt("id_monitor"))))
            val occupied = getRows("reserva", mapOf("id_horario" to eq(scheduleId), "estado" to eq("reservada")), "id_reserva").size

            ReservationDetailData(
                className = classRow?.optString("nombre").orEmpty(),
                classDescription = classRow?.optString("descripcion").orEmpty().ifBlank { "Sin descripción" },
                date = schedule.optString("fecha"),
                startTime = schedule.optString("hora_inicio"),
                instructorName = listOf(monitorUser?.optString("nombre"), monitorUser?.optString("apellidos")).joinToString(" ").trim(),
                roomName = room?.optString("nombre_sala").orEmpty(),
                totalSlots = schedule.optInt("plazas_totales", 0),
                occupiedSlots = occupied
            )
        }.getOrNull()
    }

    fun reserveClass(userId: Int, scheduleId: Int): ActionResult {
        return runCatching {
            val existing = getRows(
                "reserva",
                mapOf("id_usuario" to eq(userId), "id_horario" to eq(scheduleId), "estado" to neq("cancelada")),
                "id_reserva",
                limit = 1
            )
            if (existing.isNotEmpty()) return ActionResult.Error("Ya tienes una reserva para esta clase")

            val schedule = getSingleRow("horario_clase", mapOf("id_horario" to eq(scheduleId)), "plazas_totales")
                ?: return ActionResult.Error("Horario no encontrado")
            val occupied = getRows("reserva", mapOf("id_horario" to eq(scheduleId), "estado" to eq("reservada")), "id_reserva").size
            if (occupied >= schedule.optInt("plazas_totales", 0)) return ActionResult.Error("No hay plazas disponibles")

            api.insert(
                "reserva",
                JSONObject()
                    .put("id_usuario", userId)
                    .put("id_horario", scheduleId)
                    .put("estado", "reservada")
                    .put("fecha_reserva", currentDateIso())
            )
            ActionResult.Success("¡Reserva confirmada!")
        }.getOrElse { ActionResult.Error("No se pudo completar la reserva") }
    }

    fun registerWorkout(userId: Int, minutes: Int): ActionResult {
        if (minutes <= 0) return ActionResult.Error("Introduce una duración válida")
        return runCatching {
            api.insert(
                "actividad_usuario",
                JSONObject()
                    .put("id_usuario", userId)
                    .put("fecha", currentDateIso())
                    .put("duracion_minutos", minutes)
            )
            ActionResult.Success("Entrenamiento registrado")
        }.getOrElse { ActionResult.Error("No se pudo registrar el entrenamiento") }
    }

    fun getUserReservations(userId: Int): List<UserReservationItem> {
        return runCatching {
            getRows("reserva", mapOf("id_usuario" to eq(userId)), orderBy = "fecha_reserva", ascending = false)
                .mapNotNull { row ->
                    val schedule = getSingleRow("horario_clase", mapOf("id_horario" to eq(row.optInt("id_horario")))) ?: return@mapNotNull null
                    val classRow = getSingleRow("clase", mapOf("id_clase" to eq(schedule.optInt("id_clase"))), "nombre")
                    UserReservationItem(
                        className = classRow?.optString("nombre").orEmpty(),
                        date = schedule.optString("fecha"),
                        time = schedule.optString("hora_inicio"),
                        state = row.optString("estado")
                    )
                }
        }.getOrDefault(emptyList())
    }

    fun getWorkoutHistory(userId: Int): List<WorkoutHistoryItem> {
        return runCatching {
            getRows(
                "actividad_usuario",
                mapOf("id_usuario" to eq(userId)),
                "fecha,duracion_minutos",
                orderBy = "fecha",
                ascending = false
            ).map {
                WorkoutHistoryItem(it.optString("fecha"), it.optInt("duracion_minutos", 0))
            }
        }.getOrDefault(emptyList())
    }

    fun getCurrentSubscription(userId: Int): SubscriptionStatus? {
        return runCatching {
            val row = getSingleRow(
                "suscripcion",
                mapOf("id_usuario" to eq(userId)),
                "tipo,fecha_fin,fecha_inicio,estado",
                orderBy = "fecha_inicio",
                ascending = false
            ) ?: return null

            SubscriptionStatus(
                type = row.optString("tipo"),
                endDate = row.optString("fecha_fin"),
                status = row.optString("estado")
            )
        }.getOrNull()
    }

    fun getNotifications(userId: Int): List<AppNotification> {
        val notifications = mutableListOf<AppNotification>()
        val subscription = getCurrentSubscription(userId)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        if (subscription?.endDate?.isNotBlank() == true) {
            val endDate = formatter.parse(subscription.endDate)
            if (endDate != null) {
                val daysLeft = TimeUnit.MILLISECONDS.toDays(endDate.time - Date().time)
                notifications += AppNotification(
                    id = "subscription",
                    title = "Suscripción",
                    description = if (daysLeft >= 0) "Tu plan ${subscription.type} vence en $daysLeft días" else "Tu suscripción ${subscription.type} está vencida",
                    timestamp = "Hoy",
                    read = daysLeft > 7
                )
            }
        }

        val nextClass = getRows(
            "reserva",
            mapOf("id_usuario" to eq(userId), "estado" to eq("reservada")),
            "id_horario",
            orderBy = "fecha_reserva",
            ascending = true,
            limit = 1
        ).firstOrNull()

        nextClass?.optInt("id_horario")?.let { scheduleId ->
            val schedule = getSingleRow("horario_clase", mapOf("id_horario" to eq(scheduleId)))
            val classRow = schedule?.let { getSingleRow("clase", mapOf("id_clase" to eq(it.optInt("id_clase"))), "nombre") }
            if (schedule != null && classRow != null) {
                notifications += AppNotification(
                    id = "next-class",
                    title = "Clase reservada",
                    description = "${classRow.optString("nombre")} el ${schedule.optString("fecha")} a las ${schedule.optString("hora_inicio")}",
                    timestamp = "Programada",
                    read = false
                )
            }
        }

        val workoutsThisWeek = getWorkoutHistory(userId).take(7).sumOf { it.durationMinutes }
        notifications += AppNotification(
            id = "workout-reminder",
            title = "Registrar entrenamiento",
            description = "Llevas ${workoutsThisWeek} min esta semana. ¡Sigue así!",
            timestamp = "Resumen",
            read = workoutsThisWeek >= 180
        )

        return notifications
    }

    fun updateProfileData(
        userId: Int,
        email: String,
        phone: String,
        age: Int,
        weightKg: Double,
        heightCm: Double
    ): ActionResult {
        val normalizedEmail = email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) return ActionResult.Error("Email no válido")

        return runCatching {
            val inUse = getRows(
                "usuario",
                mapOf("email" to "eq.$normalizedEmail", "id_usuario" to neq(userId)),
                "id_usuario",
                limit = 1
            )
            if (inUse.isNotEmpty()) return ActionResult.Error("Ese email ya está en uso")

            api.update("usuario", mapOf("id_usuario" to eq(userId)), JSONObject().put("email", normalizedEmail))
            api.update(
                "cliente",
                mapOf("id_usuario" to eq(userId)),
                JSONObject().put("edad", age).put("peso", weightKg).put("altura", heightCm)
            )

            api.delete("telefono_usuario", mapOf("id_usuario" to eq(userId)))
            if (phone.isNotBlank()) {
                api.insert("telefono_usuario", JSONObject().put("id_usuario", userId).put("telefono", phone.trim()))
            }

            ActionResult.Success("Perfil actualizado")
        }.getOrElse { ActionResult.Error("No se pudo actualizar el perfil") }
    }

    fun updateProfileLanguage(userId: Int, languageCode: String): ActionResult {
        val normalizedCode = languageCode.trim().uppercase(Locale.ROOT)
        if (normalizedCode !in setOf("ES", "EN", "DE", "PT")) return ActionResult.Error("Idioma no soportado")

        return runCatching {
            val exists = getSingleRow("configuracion_usuario", mapOf("id_usuario" to eq(userId)), "id_configuracion")
            if (exists != null) {
                api.update("configuracion_usuario", mapOf("id_usuario" to eq(userId)), JSONObject().put("idioma", normalizedCode))
            } else {
                api.insert(
                    "configuracion_usuario",
                    JSONObject().put("id_usuario", userId).put("idioma", normalizedCode).put("notificaciones", true)
                )
            }
            ActionResult.Success("Idioma actualizado")
        }.getOrElse { ActionResult.Error("No se pudo actualizar el idioma") }
    }

    fun updateProfilePhoto(userId: Int, avatarKey: String): ActionResult {
        if (avatarKey !in setOf("avatar_fire", "avatar_ocean", "avatar_forest", "avatar_midnight")) {
            return ActionResult.Error("Avatar no válido")
        }

        return runCatching {
            val updated = api.update(
                "usuario",
                mapOf("id_usuario" to eq(userId)),
                JSONObject().put("fotoPerfil", avatarKey)
            )
            if (updated.length() > 0) ActionResult.Success("Foto de perfil actualizada")
            else ActionResult.Error("No se encontró el usuario")
        }.getOrElse { ActionResult.Error("No se pudo actualizar la foto") }
    }

    fun updateProfileNotifications(userId: Int, enabled: Boolean): ActionResult {
        return runCatching {
            val exists = getSingleRow("configuracion_usuario", mapOf("id_usuario" to eq(userId)), "id_configuracion")
            if (exists != null) {
                api.update("configuracion_usuario", mapOf("id_usuario" to eq(userId)), JSONObject().put("notificaciones", enabled))
            } else {
                api.insert(
                    "configuracion_usuario",
                    JSONObject().put("id_usuario", userId).put("idioma", "ES").put("notificaciones", enabled)
                )
            }
            ActionResult.Success("Preferencias de notificaciones actualizadas")
        }.getOrElse { ActionResult.Error("No se pudieron actualizar las notificaciones") }
    }

    fun getUpcomingClassReminder(userId: Int): PushReminder? {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val now = Date()

        val next = getRows(
            "reserva",
            mapOf("id_usuario" to eq(userId), "estado" to eq("reservada")),
            "id_horario",
            orderBy = "fecha_reserva",
            ascending = true,
            limit = 1
        ).firstOrNull() ?: return null

        val scheduleId = next.optInt("id_horario", -1)
        val schedule = getSingleRow("horario_clase", mapOf("id_horario" to eq(scheduleId))) ?: return null
        val classRow = getSingleRow("clase", mapOf("id_clase" to eq(schedule.optInt("id_clase"))), "nombre") ?: return null

        val dateTime = formatter.parse("${schedule.optString("fecha")} ${schedule.optString("hora_inicio")}") ?: return null
        val hoursUntilClass = TimeUnit.MILLISECONDS.toHours(dateTime.time - now.time)
        if (hoursUntilClass !in 0..24) return null

        return PushReminder(
            uniqueId = "$scheduleId-${schedule.optString("fecha")}-${schedule.optString("hora_inicio")}",
            title = "Recordatorio de clase",
            message = "Te esperamos en ${classRow.optString("nombre")} hoy a las ${schedule.optString("hora_inicio")}"
        )
    }

    fun getClassesByWeekDay(dayFilter: String): List<ClassWithSchedules> {
        return runCatching {
            val schedules = getRows("horario_clase", orderBy = "fecha", ascending = true)
            val filtered = schedules.filter { row -> dayFilter == "Todos" || sqlDateToWeekDay(row.optString("fecha")) == dayFilter }

            val grouped = filtered.groupBy { it.optInt("id_clase") }
            grouped.mapNotNull { (classId, classSchedules) ->
                val classRow = getSingleRow("clase", mapOf("id_clase" to eq(classId))) ?: return@mapNotNull null
                val items = classSchedules.map { schedule ->
                    val occupied = getRows(
                        "reserva",
                        mapOf("id_horario" to eq(schedule.optInt("id_horario")), "estado" to eq("reservada")),
                        "id_reserva"
                    ).size
                    val monitorUser = getSingleRow("usuario", mapOf("id_usuario" to eq(schedule.optInt("id_monitor"))))
                    ClassScheduleItem(
                        scheduleId = schedule.optInt("id_horario"),
                        time = schedule.optString("hora_inicio"),
                        weekDay = sqlDateToWeekDay(schedule.optString("fecha")),
                        occupiedSlots = occupied,
                        totalSlots = schedule.optInt("plazas_totales"),
                        instructorName = listOf(monitorUser?.optString("nombre"), monitorUser?.optString("apellidos")).joinToString(" ").trim()
                    )
                }

                ClassWithSchedules(
                    classId = classId,
                    className = classRow.optString("nombre"),
                    description = classRow.optString("descripcion"),
                    schedules = items
                )
            }.sortedBy { it.className }
        }.getOrDefault(emptyList())
    }

    fun login(email: String, password: String): LoginResult {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank() || password.isBlank()) return LoginResult.Error("Introduce email y password")
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) return LoginResult.Error("Formato de email no válido")
        if (!api.isConfigured()) return LoginResult.Error("Supabase no está configurado. Revisa SUPABASE_URL y SUPABASE_ANON_KEY")

        return runCatching {
            val user = getSingleRow("usuario", mapOf("email" to "eq.$normalizedEmail"))
            val userPassword = user?.let(::readPasswordValue)
            when {
                user == null -> LoginResult.Error("No existe una cuenta con ese email")
                userPassword == null -> LoginResult.Error("No se encontró el campo de password en usuario")
                userPassword != password -> LoginResult.Error("password incorrecta")
                else -> LoginResult.Success(user.optInt("id_usuario"), user.optString("nombre"))
            }
        }.getOrElse { LoginResult.Error("No se pudo acceder a Supabase: ${it.message.orEmpty()}") }
    }

    fun register(nombreCompleto: String, email: String, telefono: String, password: String): RegisterResult {
        val normalizedEmail = email.trim()
        val fullName = nombreCompleto.trim().replace(Regex("\\s+"), " ")

        if (fullName.isBlank() || normalizedEmail.isBlank() || password.isBlank()) {
            return RegisterResult.Error("Completa todos los campos obligatorios")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) return RegisterResult.Error("Formato de email no válido")
        if (password.length < 6) return RegisterResult.Error("La password debe tener al menos 6 caracteres")
        if (!api.isConfigured()) return RegisterResult.Error("Supabase no está configurado. Revisa SUPABASE_URL y SUPABASE_ANON_KEY")

        return runCatching {
            val emailExists = getRows("usuario", mapOf("email" to "eq.$normalizedEmail"), "id_usuario", limit = 1)
            if (emailExists.isNotEmpty()) return RegisterResult.Error("Ya existe una cuenta con ese email")

            val parts = fullName.split(" ", limit = 2)
            val nombre = parts.first()
            val apellidos = if (parts.size > 1) parts[1] else ""

            val inserted = insertUserWithCompatiblePasswordField(nombre, apellidos, normalizedEmail, password)
            if (inserted.length() == 0) return RegisterResult.Error("No se pudo registrar el usuario")

            val userId = inserted.getJSONObject(0).optInt("id_usuario")
            api.insert(
                "cliente",
                JSONObject().put("id_usuario", userId).put("edad", 0).put("peso", 0.0).put("altura", 0.0)
            )
            if (telefono.isNotBlank()) {
                api.insert("telefono_usuario", JSONObject().put("id_usuario", userId).put("telefono", telefono.trim()))
            }
            RegisterResult.Success(userId, nombre)
        }.getOrElse { RegisterResult.Error("No se pudo registrar el usuario: ${it.message.orEmpty()}") }
    }

    private fun insertUserWithCompatiblePasswordField(
        nombre: String,
        apellidos: String,
        email: String,
        password: String
    ): org.json.JSONArray {
        val payloadBase = JSONObject()
            .put("nombre", nombre)
            .put("apellidos", apellidos)
            .put("email", email)
            .put("fotoPerfil", "")

        val passwordCandidates = listOf("password", "contrasena", "contraseña")
        var lastError: Throwable? = null

        for (candidate in passwordCandidates) {
            runCatching {
                val payload = JSONObject(payloadBase.toString()).put(candidate, password)
                api.insert("usuario", payload)
            }.onSuccess { return it }
                .onFailure { error -> lastError = error }
        }

        throw IllegalStateException(lastError?.message ?: "No se encontró una columna de password compatible")
    }

    private fun getTodayClassItem(scheduleId: Int): TodayClassItem? {
        if (scheduleId <= 0) return null
        val schedule = getSingleRow("horario_clase", mapOf("id_horario" to eq(scheduleId))) ?: return null
        val classRow = getSingleRow("clase", mapOf("id_clase" to eq(schedule.optInt("id_clase"))), "nombre")
        val room = getSingleRow("sala", mapOf("id_sala" to eq(schedule.optInt("id_sala"))), "nombre_sala")
        return TodayClassItem(
            className = classRow?.optString("nombre").orEmpty(),
            startTime = schedule.optString("hora_inicio"),
            roomName = room?.optString("nombre_sala").orEmpty()
        )
    }

    private fun getRows(
        table: String,
        filters: Map<String, String> = emptyMap(),
        select: String = "*",
        orderBy: String? = null,
        ascending: Boolean = true,
        limit: Int? = null
    ): List<JSONObject> {
        return if (!api.isConfigured()) emptyList() else api.select(table, select, filters, orderBy, ascending, limit)
            .let { array -> List(array.length()) { idx -> array.getJSONObject(idx) } }
    }

    private fun getSingleRow(
        table: String,
        filters: Map<String, String>,
        select: String = "*",
        orderBy: String? = null,
        ascending: Boolean = true
    ): JSONObject? = getRows(table, filters, select, orderBy, ascending, limit = 1).firstOrNull()

    private fun currentDateIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun weekDayFromDate(date: Date): String {
        val calendar = Calendar.getInstance().apply { time = date }
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lunes"
            Calendar.TUESDAY -> "Martes"
            Calendar.WEDNESDAY -> "Miércoles"
            Calendar.THURSDAY -> "Jueves"
            Calendar.FRIDAY -> "Viernes"
            Calendar.SATURDAY -> "Sábado"
            else -> "Domingo"
        }
    }

    private fun sqlDateToWeekDay(date: String): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val parsed = formatter.parse(date) ?: return ""
        return weekDayFromDate(parsed)
    }

    private fun calculateCurrentStreak(dates: List<Date>, formatter: SimpleDateFormat): Int {
        if (dates.isEmpty()) return 0
        val sorted = dates.distinct().sortedDescending()
        var streak = 1

        for (index in 1 until sorted.size) {
            val expected = Calendar.getInstance().apply {
                time = sorted[index - 1]
                add(Calendar.DAY_OF_YEAR, -1)
            }
            if (formatter.format(sorted[index]) == formatter.format(expected.time)) streak++ else break
        }

        return streak
    }

    private fun calculateCurrentStreakDays(userId: Int): Int {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dates = getRows(
            "actividad_usuario",
            mapOf("id_usuario" to eq(userId)),
            select = "fecha",
            orderBy = "fecha",
            ascending = false
        ).mapNotNull { formatter.parse(it.optString("fecha")) }

        return calculateCurrentStreak(dates, formatter)
    }

    private fun eq(value: Any): String = "eq.$value"
    private fun neq(value: Any): String = "neq.$value"
    private fun inList(vararg values: String): String = "in.(${values.joinToString(",")})"

    private fun resolvePasswordField(user: JSONObject): String? =
        listOf("password", "contrasena", "contraseña").firstOrNull { user.has(it) }

    private fun readPasswordValue(user: JSONObject): String? =
        resolvePasswordField(user)?.let { key -> user.optString(key).ifBlank { null } }
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