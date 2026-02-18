package com.example.fitgymkt.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitgymkt.model.ui.ProfileData
import com.example.fitgymkt.repository.ActionResult
import com.example.fitgymkt.repository.FitGymRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    userId: Int,
    alIrAInicio: () -> Unit,
    alIrAClases: () -> Unit,
    alIrAAnalisis: () -> Unit,
    modoOscuroActivado: Boolean,
    onModoOscuroChanged: (Boolean) -> Unit,
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

    var mostrandoEditor by remember { mutableStateOf(false) }

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
                    // 2. Conectamos la campana con el diálogo global
                    IconButton(onClick = alAbrirNotificaciones) {
                        BadgedBox(badge = { Badge { Text("2") } }) {
                            Icon(Icons.Default.Notifications, null)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(selected = false, onClick = alIrAInicio, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Inicio") })
                NavigationBarItem(selected = false, onClick = alIrAClases, icon = { Icon(Icons.Default.DateRange, null) }, label = { Text("Clases") })
                NavigationBarItem(selected = false, onClick = alIrAAnalisis, icon = { Icon(Icons.Default.BarChart, null) }, label = { Text("Análisis") })
                NavigationBarItem(selected = true, onClick = { }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Perfil") })
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

        if (mostrandoEditor) {
            DialogoEditarPerfil(
                data = data,
                onDismiss = { mostrandoEditor = false },
                onGuardar = { email, phone, age, weight, height ->
                    scope.launch {
                        when (val result = withContext(Dispatchers.IO) {
                            repository.updateProfileData(userId, email, phone, age, weight, height)
                        }) {
                            is ActionResult.Success -> {
                                snackbarHostState.showSnackbar(result.message)
                                refreshKey += 1
                                mostrandoEditor = false
                            }
                            is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Mi Perfil", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text("Gestiona tu cuenta", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // 1. Cabecera de Perfil
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(50.dp))
                        }
                        IconButton(
                            onClick = { mostrandoEditor = true },
                            modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surface, CircleShape).border(1.dp, Color.LightGray, CircleShape)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(data.fullName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Información Personal
            SeccionPerfil("Información Personal") {
                ItemPerfil(Icons.Default.Email, "Email", data.email)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ItemPerfil(Icons.Default.Phone, "Teléfono", if (data.phone.isBlank()) "Sin teléfono" else data.phone)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ItemPerfil(Icons.Default.CalendarToday, "Edad", "${data.age} años")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Medidas Corporales
            SeccionPerfil("Medidas Corporales") {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        ItemPerfil(Icons.Default.Scale, "Peso", String.format(Locale.getDefault(), "%.1f kg", data.weightKg), mostrarFlecha = false)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ItemPerfil(Icons.Default.Straighten, "Altura", String.format(Locale.getDefault(), "%.0f cm", data.heightCm), mostrarFlecha = false)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Configuración (Switch de Modo Oscuro)
            SeccionPerfil("Configuración") {
                ItemConfiguracion(Icons.Default.LightMode, "Modo Oscuro", "Tema de la aplicación") {
                    Switch(checked = modoOscuroActivado, onCheckedChange = onModoOscuroChanged)
                }
                ItemConfiguracion(Icons.Default.Notifications, "Notificaciones", "Alertas en tiempo real") {
                    Switch(checked = notificacionesInternas, onCheckedChange = { notificacionesInternas = it })
                }
                ItemPerfil(Icons.Default.Language, "Idioma", if (data.language == "ES") "Español" else data.language)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Seguridad
            SeccionPerfil("Seguridad") {
                ItemPerfil(Icons.Default.Lock, "Cambiar contraseña", "Actualiza tu contraseña")
                ItemPerfil(Icons.Default.Shield, "Privacidad", "Control de datos")
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
                Text("Cerrar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DialogoEditarPerfil(
    data: ProfileData,
    onDismiss: () -> Unit,
    onGuardar: (String, String, Int, Double, Double) -> Unit
) {
    var email by remember(data.email) { mutableStateOf(data.email) }
    var phone by remember(data.phone) { mutableStateOf(data.phone) }
    var age by remember(data.age) { mutableStateOf(data.age.toString()) }
    var weight by remember(data.weightKg) { mutableStateOf(data.weightKg.toString()) }
    var height by remember(data.heightCm) { mutableStateOf(data.heightCm.toString()) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onGuardar(
                    email,
                    phone,
                    age.toIntOrNull() ?: 0,
                    weight.toDoubleOrNull() ?: 0.0,
                    height.toDoubleOrNull() ?: 0.0
                )
            }) { Text("Guardar") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Editar perfil") },
        text = {
            Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") })
                OutlinedTextField(value = age, onValueChange = { age = it.filter(Char::isDigit) }, label = { Text("Edad") })
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Peso (kg)") })
                OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Altura (cm)") })
            }
        }
    )
}

// --- SUB-COMPONENTES AUXILIARES ---

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
        ) {
            Column(content = contenido)
        }
    }
}

@Composable
fun ItemPerfil(icono: ImageVector, titulo: String, valor: String, mostrarFlecha: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(titulo, fontSize = 11.sp, color = Color.Gray)
            Text(valor, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
        if (mostrarFlecha) {
            Spacer(modifier = Modifier.weight(1f))
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