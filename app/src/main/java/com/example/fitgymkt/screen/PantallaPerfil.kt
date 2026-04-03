package com.example.fitgymkt.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitgymkt.model.ui.ProfileData
import com.example.fitgymkt.R
import com.example.fitgymkt.repository.ActionResult
import com.example.fitgymkt.repository.FitGymRepository
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
    Foto,
    Idioma,
    Password,
    Privacidad
}

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = alAbrirMenu) {
                        Icon(Icons.Default.Menu, null)
                    }
                },
                actions = {
                    IconButton(onClick = alAbrirNotificaciones) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifications > 0) {
                                    Badge { Text(unreadNotifications.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, null)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(selected = false, onClick = alIrAInicio, icon = { Icon(Icons.Default.Home, null) }, label = { Text(stringResource(R.string.nav_home)) })
                NavigationBarItem(selected = false, onClick = alIrAClases, icon = { Icon(Icons.Default.DateRange, null) }, label = { Text(stringResource(R.string.nav_classes)) })
                NavigationBarItem(selected = false, onClick = alIrAAnalisis, icon = { Icon(Icons.Default.BarChart, null) }, label = { Text(stringResource(R.string.nav_analysis)) })
                NavigationBarItem(selected = true, onClick = { }, icon = { Icon(Icons.Default.Person, null) }, label = { Text(stringResource(R.string.nav_profile)) })
            }
        }
    ) { padding ->
        if (profileData == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
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

            PerfilCampoEditable.Foto -> {
                DialogoCambiarAvatar(
                    avatarActual = data.profilePhoto,
                    onDismiss = { campoEnEdicion = null },
                    onSeleccionar = { avatar ->
                        scope.launch {
                            when (val result = withContext(Dispatchers.IO) {
                                repository.updateProfilePhoto(userId, avatar)
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


            PerfilCampoEditable.Privacidad -> {
                DialogoPrivacidad(onDismiss = { campoEnEdicion = null })
            }

            null -> Unit
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.profile_title), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(stringResource(R.string.manage_account), color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    FotoPerfil(profilePhoto = data.profilePhoto, size = 100.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(data.fullName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.profile_change_photo),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { campoEnEdicion = PerfilCampoEditable.Foto }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            //Información Personal
            SeccionPerfil(stringResource(R.string.profile_personal_info)) {
                ItemPerfil(Icons.Default.Email, stringResource(R.string.email), data.email) { campoEnEdicion = PerfilCampoEditable.Email }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ItemPerfil(Icons.Default.Phone, stringResource(R.string.phone), if (data.phone.isBlank()) stringResource(R.string.profile_no_phone) else data.phone) { campoEnEdicion = PerfilCampoEditable.Telefono }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ItemPerfil(Icons.Default.CalendarToday, stringResource(R.string.profile_age), stringResource(R.string.profile_age_value, data.age)) { campoEnEdicion = PerfilCampoEditable.Edad }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Medidas Corporales
            SeccionPerfil(stringResource(R.string.profile_body_measurements)) {
                ItemPerfil(Icons.Default.Scale, stringResource(R.string.profile_weight), String.format(Locale.getDefault(), stringResource(R.string.profile_weight_value), data.weightKg)) { campoEnEdicion = PerfilCampoEditable.Peso }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ItemPerfil(Icons.Default.Straighten, stringResource(R.string.profile_height), String.format(Locale.getDefault(), stringResource(R.string.profile_height_value), data.heightCm)) { campoEnEdicion = PerfilCampoEditable.Altura }            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Configuración (Switch de Modo Oscuro)
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
                                when (val result = withContext(Dispatchers.IO) {
                                    repository.updateProfileNotifications(userId, enabled)
                                }) {
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
                ItemPerfil(
                    Icons.Default.Language,
                    stringResource(R.string.language),
                    nombreIdioma(code = data.language),
                    mostrarFlecha = true
                ) { campoEnEdicion = PerfilCampoEditable.Idioma }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 5. Seguridad
            SeccionPerfil(stringResource(R.string.profile_security)) {
                ItemPerfil(Icons.Default.Lock, stringResource(R.string.update_password), stringResource(R.string.profile_change_credentials)) { campoEnEdicion = PerfilCampoEditable.Password }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ItemPerfil(Icons.Default.Shield, stringResource(R.string.data_control), stringResource(R.string.profile_view_info)) { campoEnEdicion = PerfilCampoEditable.Privacidad }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 6. Botón Cerrar Sesión
            Button(
                onClick = alCerrarSesion,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3D00)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Logout, null, tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.logout), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
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
        else -> MaterialTheme.colorScheme.primary
    }
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(fallbackColor),
        contentAlignment = Alignment.Center
    ) {
        if (profilePhoto.isNotBlank() && resourceId != 0) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = stringResource(R.string.profile_photo_desc),
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(50.dp))
        }
    }
}

@Composable
private fun DialogoCambiarAvatar(
    avatarActual: String,
    onDismiss: () -> Unit,
    onSeleccionar: (String) -> Unit
) {
    val opciones = listOf("avatar_fire", "avatar_ocean", "avatar_forest", "avatar_midnight")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        title = { Text(stringResource(R.string.profile_choose_avatar)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                opciones.forEach { avatar ->
                    val seleccionado = avatar == avatarActual
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSeleccionar(avatar) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FotoPerfil(profilePhoto = avatar, size = 40.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = avatar.removePrefix("avatar_").replaceFirstChar { it.uppercaseChar() },
                            modifier = Modifier.weight(1f)
                        )
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

// --- SUB-COMPONENTES AUXILIARES ---

@Composable
private fun DialogoPrivacidad(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.understood)) } },
        title = { Text(stringResource(R.string.data_control)) },
        text = {
            Text(stringResource(R.string.data_control_body))
        }
    )
}

@Composable
fun SeccionPerfil(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            titulo,
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Card(
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) { Column(content = contenido) }
    }
}

@Composable
fun ItemPerfil(icono: ImageVector, titulo: String, valor: String, mostrarFlecha: Boolean = true, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontSize = 11.sp, color = Color.Gray)
            Text(valor, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
        if (mostrarFlecha) {
            Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun ItemConfiguracion(icono: ImageVector, titulo: String, subtitulo: String, control: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitulo, fontSize = 11.sp, color = Color.Gray)
        }
        control()
    }
}