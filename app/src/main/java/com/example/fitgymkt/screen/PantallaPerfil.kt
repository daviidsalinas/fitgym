package com.example.fitgymkt.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitgymkt.R
import com.example.fitgymkt.model.ui.ProfileData
import com.example.fitgymkt.repository.ActionResult
import com.example.fitgymkt.repository.FitGymRepository
import com.example.fitgymkt.ui.theme.ColoresFit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private enum class PerfilCampoEditable {
    Email,
    Telefono,
    Edad,
    Peso,
    Altura,
    Idioma,
    Password,
    Privacidad
}

@Composable
fun PantallaPerfil(
    userId: Int,
    alIrAInicio: () -> Unit,
    alIrAClases: () -> Unit,
    alIrAAnalisis: () -> Unit,
    unreadNotifications: Int,
    modoOscuroActivado: Boolean,
    onModoOscuroChanged: (Boolean) -> Unit,
    onIdiomaChanged: (String) -> Unit,
    alAbrirMenu: () -> Unit,
    alAbrirNotificaciones: () -> Unit,
    alCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { FitGymRepository(context) }

    var refreshKey by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val profileData by produceState<ProfileData?>(initialValue = null, userId, refreshKey) {
        value = withContext(Dispatchers.IO) { repository.getProfileData(userId) }
    }

    var notificacionesInternas by remember(profileData?.notificationsEnabled) {
        mutableStateOf(profileData?.notificationsEnabled ?: true)
    }
    var campoEnEdicion by remember { mutableStateOf<PerfilCampoEditable?>(null) }

    if (profileData == null) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                FitGymTopBar(
                    title = stringResource(R.string.profile_title),
                    unreadCount = unreadNotifications,
                    onMenuClick = alAbrirMenu,
                    onNotificationsClick = alAbrirNotificaciones
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColoresFit.Naranja)
            }
        }
        return
    }

    val data = profileData!!
    val languageUpdatedMessage = stringResource(R.string.profile_language_updated)

    when (campoEnEdicion) {
        PerfilCampoEditable.Email,
        PerfilCampoEditable.Telefono,
        PerfilCampoEditable.Edad,
        PerfilCampoEditable.Peso,
        PerfilCampoEditable.Altura -> {
            DialogoEditarCampo(
                data = data,
                campo = campoEnEdicion!!,
                onDismiss = { campoEnEdicion = null },
                onGuardar = { value ->
                    scope.launch {
                        val updatedEmail = if (campoEnEdicion == PerfilCampoEditable.Email) value else data.email
                        val updatedPhone = if (campoEnEdicion == PerfilCampoEditable.Telefono) value else data.phone
                        val updatedAge = if (campoEnEdicion == PerfilCampoEditable.Edad) value.toIntOrNull() ?: data.age else data.age
                        val updatedWeight = if (campoEnEdicion == PerfilCampoEditable.Peso) value.toDoubleOrNull() ?: data.weightKg else data.weightKg
                        val updatedHeight = if (campoEnEdicion == PerfilCampoEditable.Altura) value.toDoubleOrNull() ?: data.heightCm else data.heightCm

                        when (val result = withContext(Dispatchers.IO) {
                            repository.updateProfileData(userId, updatedEmail, updatedPhone, updatedAge, updatedWeight, updatedHeight)
                        }) {
                            is ActionResult.Success -> {
                                snackbarHostState.showSnackbar(result.message)
                                refreshKey += 1
                                campoEnEdicion = null
                            }
                            is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            )
        }

        PerfilCampoEditable.Password -> {
            DialogoCambiarPassword(
                onDismiss = { campoEnEdicion = null },
                onGuardar = { actual, nueva ->
                    scope.launch {
                        when (val result = withContext(Dispatchers.IO) { repository.updatePassword(userId, actual, nueva) }) {
                            is ActionResult.Success -> {
                                snackbarHostState.showSnackbar(result.message)
                                campoEnEdicion = null
                            }
                            is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            )
        }

        PerfilCampoEditable.Idioma -> {
            DialogoCambiarIdioma(
                idiomaActual = data.language,
                onDismiss = { campoEnEdicion = null },
                onSeleccionar = { idiomaSeleccionado ->
                    scope.launch {
                        val resultado = withContext(Dispatchers.IO) {
                            repository.updateProfileLanguage(userId, idiomaSeleccionado)
                        }
                        val mensaje = when (resultado) {
                            is ActionResult.Success -> {
                                refreshKey++
                                onIdiomaChanged(idiomaSeleccionado)
                                campoEnEdicion = null
                                languageUpdatedMessage
                            }
                            is ActionResult.Error -> resultado.message
                        }
                        snackbarHostState.showSnackbar(mensaje)
                    }
                }
            )
        }

        PerfilCampoEditable.Privacidad -> DialogoPrivacidad(onDismiss = { campoEnEdicion = null })
        null -> Unit
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FitGymTopBar(
                title = stringResource(R.string.profile_title),
                subtitle = stringResource(R.string.manage_account),
                unreadCount = unreadNotifications,
                onMenuClick = alAbrirMenu,
                onNotificationsClick = alAbrirNotificaciones
            )
        },
        bottomBar = {
            FitGymBottomBar(
                current = FitGymDestination.Profile,
                onHomeClick = alIrAInicio,
                onClassesClick = alIrAClases,
                onAnalysisClick = alIrAAnalisis,
                onProfileClick = {}
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            FitGymHeroPanel(modifier = Modifier.fillMaxWidth(), accent = ColoresFit.Naranja) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FotoPerfil(profilePhoto = data.profilePhoto, size = 82.dp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(data.fullName, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(nombreIdioma(code = data.language), color = Color.White.copy(alpha = 0.74f), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            SeccionPerfil(stringResource(R.string.profile_personal_info)) {
                ItemPerfil(Icons.Default.Email, stringResource(R.string.email), data.email) { campoEnEdicion = PerfilCampoEditable.Email }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline)
                ItemPerfil(Icons.Default.Phone, stringResource(R.string.phone), if (data.phone.isBlank()) stringResource(R.string.profile_no_phone) else data.phone) {
                    campoEnEdicion = PerfilCampoEditable.Telefono
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline)
                ItemPerfil(Icons.Default.CalendarToday, stringResource(R.string.profile_age), stringResource(R.string.profile_age_value, data.age)) {
                    campoEnEdicion = PerfilCampoEditable.Edad
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            SeccionPerfil(stringResource(R.string.profile_body_measurements)) {
                ItemPerfil(Icons.Default.Scale, stringResource(R.string.profile_weight), String.format(Locale.getDefault(), stringResource(R.string.profile_weight_value), data.weightKg)) {
                    campoEnEdicion = PerfilCampoEditable.Peso
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline)
                ItemPerfil(Icons.Default.Straighten, stringResource(R.string.profile_height), String.format(Locale.getDefault(), stringResource(R.string.profile_height_value), data.heightCm)) {
                    campoEnEdicion = PerfilCampoEditable.Altura
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            SeccionPerfil(stringResource(R.string.profile_settings)) {
                ItemConfiguracion(Icons.Default.LightMode, stringResource(R.string.dark_mode), stringResource(R.string.profile_app_theme)) {
                    Switch(checked = modoOscuroActivado, onCheckedChange = onModoOscuroChanged)
                }
                ItemConfiguracion(Icons.Default.Notifications, stringResource(R.string.notifications), stringResource(R.string.profile_realtime_alerts)) {
                    Switch(
                        checked = notificacionesInternas,
                        onCheckedChange = { enabled ->
                            notificacionesInternas = enabled
                            scope.launch {
                                when (val result = withContext(Dispatchers.IO) { repository.updateProfileNotifications(userId, enabled) }) {
                                    is ActionResult.Success -> snackbarHostState.showSnackbar(result.message)
                                    is ActionResult.Error -> {
                                        notificacionesInternas = !enabled
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                }
                            }
                        }
                    )
                }
                ItemPerfil(Icons.Default.Language, stringResource(R.string.language), nombreIdioma(code = data.language), mostrarFlecha = true) {
                    campoEnEdicion = PerfilCampoEditable.Idioma
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            SeccionPerfil(stringResource(R.string.profile_security)) {
                ItemPerfil(Icons.Default.Lock, stringResource(R.string.update_password), stringResource(R.string.profile_change_credentials)) {
                    campoEnEdicion = PerfilCampoEditable.Password
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline)
                ItemPerfil(Icons.Default.Shield, stringResource(R.string.data_control), stringResource(R.string.profile_view_info)) {
                    campoEnEdicion = PerfilCampoEditable.Privacidad
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = alCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColoresFit.Rojo),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.logout), fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun nombreIdioma(code: String): String = when (code.uppercase(Locale.ROOT)) {
    "EN" -> stringResource(R.string.profile_language_english)
    "DE" -> stringResource(R.string.profile_language_german)
    "PT" -> stringResource(R.string.profile_language_portuguese)
    else -> stringResource(R.string.profile_language_spanish)
}

@Composable
private fun FotoPerfil(profilePhoto: String, size: androidx.compose.ui.unit.Dp = 100.dp) {
    val context = LocalContext.current
    val drawableName = profilePhoto.substringBeforeLast('.').lowercase(Locale.getDefault())
    val resourceId = remember(profilePhoto) { context.resources.getIdentifier(drawableName, "drawable", context.packageName) }
    val fallbackColor = when (profilePhoto.lowercase(Locale.getDefault())) {
        "avatar_fire" -> Color(0xFFFF6B35)
        "avatar_ocean" -> Color(0xFF0081A7)
        "avatar_forest" -> Color(0xFF2A9D8F)
        "avatar_midnight" -> Color(0xFF3D405B)
        else -> ColoresFit.Naranja
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(fallbackColor),
        contentAlignment = Alignment.Center
    ) {
        if (profilePhoto.isNotBlank() && resourceId != 0) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = stringResource(R.string.profile_photo_desc),
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
        }
    }
}

@Composable
private fun DialogoEditarCampo(
    data: ProfileData,
    campo: PerfilCampoEditable,
    onDismiss: () -> Unit,
    onGuardar: (String) -> Unit
) {
    val (titulo, valorActual, filtro) = when (campo) {
        PerfilCampoEditable.Email -> Triple(stringResource(R.string.profile_edit_email), data.email, { s: String -> s })
        PerfilCampoEditable.Telefono -> Triple(stringResource(R.string.profile_edit_phone), data.phone, { s: String -> s.filter { it.isDigit() || it == '+' || it == ' ' } })
        PerfilCampoEditable.Edad -> Triple(stringResource(R.string.profile_edit_age), data.age.toString(), { s: String -> s.filter(Char::isDigit) })
        PerfilCampoEditable.Peso -> Triple(stringResource(R.string.profile_edit_weight), data.weightKg.toString(), { s: String -> s.filter { it.isDigit() || it == '.' } })
        PerfilCampoEditable.Altura -> Triple(stringResource(R.string.profile_edit_height), data.heightCm.toString(), { s: String -> s.filter { it.isDigit() || it == '.' } })
        else -> Triple("", "", { s: String -> s })
    }

    var valor by remember(campo, data) { mutableStateOf(valorActual) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { onGuardar(valor.trim()) }) { Text(stringResource(R.string.save)) } },
        dismissButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        title = { Text(titulo) },
        text = {
            OutlinedTextField(
                value = valor,
                onValueChange = { valor = filtro(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    )
}

@Composable
private fun DialogoCambiarPassword(
    onDismiss: () -> Unit,
    onGuardar: (String, String) -> Unit
) {
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { onGuardar(actual, nueva) }) { Text(stringResource(R.string.update)) } },
        dismissButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        title = { Text(stringResource(R.string.update_password)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = actual, onValueChange = { actual = it }, label = { Text(stringResource(R.string.current_password)) }, singleLine = true)
                OutlinedTextField(value = nueva, onValueChange = { nueva = it }, label = { Text(stringResource(R.string.new_password)) }, singleLine = true)
            }
        }
    )
}

@Composable
private fun DialogoCambiarIdioma(
    idiomaActual: String,
    onDismiss: () -> Unit,
    onSeleccionar: (String) -> Unit
) {
    val idiomas = listOf("ES", "EN", "DE", "PT")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        title = { Text(stringResource(R.string.profile_select_language)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                idiomas.forEach { code ->
                    val seleccionado = code == idiomaActual.uppercase(Locale.ROOT)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSeleccionar(code) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(nombreIdioma(code = code), modifier = Modifier.weight(1f))
                        if (seleccionado) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun DialogoPrivacidad(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.understood)) } },
        title = { Text(stringResource(R.string.data_control)) },
        text = { Text(stringResource(R.string.data_control_body)) }
    )
}

@Composable
fun SeccionPerfil(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            titulo,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        FitGymPanel(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            bordered = true
        ) {
            Column(content = contenido)
        }
    }
}

@Composable
fun ItemPerfil(
    icono: ImageVector,
    titulo: String,
    valor: String,
    mostrarFlecha: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = ColoresFit.Negro, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontSize = 11.sp, color = ColoresFit.GrisTexto)
            Text(valor, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
        if (mostrarFlecha) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = ColoresFit.GrisTexto)
        }
    }
}

@Composable
fun ItemConfiguracion(icono: ImageVector, titulo: String, subtitulo: String, control: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = ColoresFit.Negro, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitulo, fontSize = 11.sp, color = ColoresFit.GrisTexto)
        }
        control()
    }
}
