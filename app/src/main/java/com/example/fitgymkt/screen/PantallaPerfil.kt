package com.example.fitgymkt.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitgymkt.model.ui.ProfileData
import com.example.fitgymkt.repository.FitGymRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    alAbrirNotificaciones: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { FitGymRepository(context) }

    val perfil = produceState<ProfileData?>(initialValue = null, key1 = userId) {
        value = withContext(Dispatchers.IO) { repository.getProfileData(userId) }
    }.value

    var notificacionesInternas by remember(perfil?.notificationsEnabled) {
        mutableStateOf(perfil?.notificationsEnabled ?: true)
    }

    Scaffold(
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
                NavigationBarItem(selected = false, onClick = { alIrAInicio() }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Inicio") })
                NavigationBarItem(selected = false, onClick = { alIrAClases() }, icon = { Icon(Icons.Default.DateRange, null) }, label = { Text("Clases") })
                NavigationBarItem(selected = false, onClick = { alIrAAnalisis() }, icon = { Icon(Icons.Default.BarChart, null) }, label = { Text("Análisis") })
                NavigationBarItem(selected = true, onClick = { }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Perfil") })
            }
        }
    ) { padding ->
        if (perfil == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
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
                            onClick = { },
                            modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surface, CircleShape).border(1.dp, Color.LightGray, CircleShape)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(perfil.fullName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Información Personal
            SeccionPerfil("Información Personal") {
                ItemPerfil(Icons.Default.Email, "Email", perfil.email)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ItemPerfil(Icons.Default.Phone, "Teléfono", perfil.phone.ifBlank { "No registrado" })
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ItemPerfil(Icons.Default.CalendarToday, "Edad", "${perfil.age} años")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Medidas Corporales
            SeccionPerfil("Medidas Corporales") {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        ItemPerfil(Icons.Default.Scale, "Peso", "${perfil.weightKg} kg", mostrarFlecha = false)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ItemPerfil(Icons.Default.Straighten, "Altura", "${perfil.heightCm} cm", mostrarFlecha = false)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Configuración (Switch de Modo Oscuro)
            SeccionPerfil("Configuración") {
                ItemConfiguracion(Icons.Default.LightMode, "Modo Oscuro", "Tema de la aplicación") {
                    Switch(
                        checked = modoOscuroActivado,
                        onCheckedChange = { onModoOscuroChanged(it) }
                    )
                }
                ItemConfiguracion(Icons.Default.Notifications, "Notificaciones", "Alertas en tiempo real") {
                    Switch(checked = notificacionesInternas, onCheckedChange = { notificacionesInternas = it })
                }
                ItemPerfil(Icons.Default.Language, "Idioma", perfil.language)
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
                onClick = { /* Lógica logout */ },
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