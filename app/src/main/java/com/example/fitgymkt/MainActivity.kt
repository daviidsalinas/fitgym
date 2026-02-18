package com.example.fitgymkt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.*
import com.example.fitgymkt.screen.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Estados Globales
            var esModoOscuro by remember { mutableStateOf(false) }
            var mostrarNotificaciones by remember { mutableStateOf(false) } // Estado para el diálogo

            val colores = if (esModoOscuro) darkColorScheme() else lightColorScheme()

            MaterialTheme(colorScheme = colores) {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var usuarioSesion by remember { mutableStateOf<UsuarioSesion?>(null) }
                var rutaActual by remember { mutableStateOf("splash") }
                val drawerHabilitado = usuarioSesion != null && rutaActual !in setOf("splash", "login", "registro")


                // 2. Diálogo Global de Notificaciones
                if (mostrarNotificaciones) {
                    DialogoNotificacionesGlobal(onDismiss = { mostrarNotificaciones = false })
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = drawerHabilitado,
                    drawerContent = {
                        ContenidoMenuLateral(
                            nombreUsuario = usuarioSesion?.userName ?: "Invitado",
                            alCerrar = { scope.launch { drawerState.close() } },
                            alCerrarSesion = {
                                usuarioSesion = null
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                ) {
                    NavegacionPrincipal(
                        usuarioSesion = usuarioSesion,
                        onRutaCambiada = { rutaActual = it },
                        alCambiarSesion = { usuarioSesion = it },
                        modoOscuroActual = esModoOscuro,
                        alCambiarModoOscuro = { esModoOscuro = it },
                        alAbrirMenu = {
                            if (drawerHabilitado) {
                                scope.launch { drawerState.open() }
                            }
                        },
                        alAbrirNotificaciones = { mostrarNotificaciones = true } // Acción para la campana
                    )
                }
            }
        }
    }
}

@Composable
fun NavegacionPrincipal(
    usuarioSesion: UsuarioSesion?,
    onRutaCambiada: (String) -> Unit,
    alCambiarSesion: (UsuarioSesion?) -> Unit,
    modoOscuroActual: Boolean,
    alCambiarModoOscuro: (Boolean) -> Unit,
    alAbrirMenu: () -> Unit,
    alAbrirNotificaciones: () -> Unit
) {
    val controladorNavegacion = rememberNavController()

    LaunchedEffect(controladorNavegacion) {
        controladorNavegacion.currentBackStackEntryFlow.collectLatest { entry ->
            onRutaCambiada(entry.destination.route ?: "")
        }
    }

    LaunchedEffect(usuarioSesion) {
        // Solo redirige al login si ya pasó el splash (no está en "splash" ni en "login")
        val rutaActual = controladorNavegacion.currentDestination?.route
        if (usuarioSesion == null && rutaActual != "login" && rutaActual != "splash") {
            controladorNavegacion.navigate("login") {
                popUpTo(controladorNavegacion.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(navController = controladorNavegacion, startDestination = "splash") {
        composable("splash") {
            PantallaSplash(
                alTerminar = {
                    val destino = if (usuarioSesion == null) "login" else "inicio"
                    controladorNavegacion.navigate(destino) {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                delayMs = 2500L
            )
        }

        composable("login") {
            PantallaLogin(
                alIrARegistro = { controladorNavegacion.navigate("registro") },
                alEntrarApp = { userId, userName ->
                    alCambiarSesion(UsuarioSesion(userId = userId, userName = userName))
                    controladorNavegacion.navigate("inicio") { popUpTo("login") { inclusive = true } }
                }
            )
        }
        composable("registro") {
            PantallaRegistro(
                alVolverAlLogin = { controladorNavegacion.popBackStack() },
                alRegistroCompletado = { userId, userName ->
                    alCambiarSesion(UsuarioSesion(userId = userId, userName = userName))
                    controladorNavegacion.navigate("inicio") { popUpTo("login") { inclusive = true } }
                }

            )
        }
        composable("inicio") {
            PantallaInicio(
                userId = usuarioSesion?.userId ?: 1,
                alAbrirMenu = alAbrirMenu,
                alAbrirNotificaciones = alAbrirNotificaciones,
                alIrAClases = { controladorNavegacion.navigate("clases") },
                alIrAAnalisis = { controladorNavegacion.navigate("analisis") },
                alIrAPerfil = { controladorNavegacion.navigate("perfil") }
            )
        }
        composable("clases") {
            PantallaClases(
                userId = usuarioSesion?.userId ?: 1,
                alAbrirMenu = alAbrirMenu,
                alAbrirNotificaciones = alAbrirNotificaciones,
                alIrAInicio = { controladorNavegacion.navigate("inicio") },
                alIrAAnalisis = { controladorNavegacion.navigate("analisis") },
                alIrAPerfil = { controladorNavegacion.navigate("perfil") },
                alIrADetalle = { scheduleId -> controladorNavegacion.navigate("detalle_reserva/$scheduleId") }
            )
        }
        composable("analisis") {
            PantallaAnalisis(
                userId = usuarioSesion?.userId ?: 1,
                alAbrirMenu = alAbrirMenu,
                alAbrirNotificaciones = alAbrirNotificaciones,
                alIrAInicio = { controladorNavegacion.navigate("inicio") },
                alIrAClases = { controladorNavegacion.navigate("clases") },
                alIrAPerfil = { controladorNavegacion.navigate("perfil") }
            )
        }
        composable("perfil") {
            PantallaPerfil(
                userId = usuarioSesion?.userId ?: 1,
                alAbrirMenu = alAbrirMenu,
                alAbrirNotificaciones = alAbrirNotificaciones,
                alIrAInicio = { controladorNavegacion.navigate("inicio") },
                alIrAClases = { controladorNavegacion.navigate("clases") },
                alIrAAnalisis = { controladorNavegacion.navigate("analisis") },
                modoOscuroActivado = modoOscuroActual,
                onModoOscuroChanged = alCambiarModoOscuro,
                alCerrarSesion = {
                    alCambiarSesion(null)
                    controladorNavegacion.navigate("login") { popUpTo(controladorNavegacion.graph.id) { inclusive = true } }
                }
            )
        }
        composable("detalle_reserva/{scheduleId}") { backStackEntry ->
            PantallaDetalleReserva(
                userId = usuarioSesion?.userId ?: 1,
                scheduleId = backStackEntry.arguments?.getString("scheduleId")?.toIntOrNull() ?: -1,
                alAbrirMenu = alAbrirMenu,
                alAbrirNotificaciones = alAbrirNotificaciones,
                alVolverAClases = { controladorNavegacion.popBackStack() },
                alIrAInicio = { controladorNavegacion.navigate("inicio") },
                alIrAAnalisis = { controladorNavegacion.navigate("analisis") },
                alIrAPerfil = { controladorNavegacion.navigate("perfil") }
            )
        }
    }
}

// --- Componente de Notificaciones adaptado de tu diseño ---
@Composable
fun DialogoNotificacionesGlobal(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notificaciones", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("2 sin leer", color = Color.Gray, fontSize = 12.sp)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Text(" Marcar todas como leídas", fontSize = 13.sp)
                }

                ItemNoti("Clase próxima", "Tu clase de Yoga Flow comienza en 30 min", "Hace 5 min", true)
                ItemNoti("¡Logro desbloqueado!", "Has completado 7 días consecutivos", "Hace 1 hora", true)
                ItemNoti("Recordatorio", "No olvides registrar tu peso semanal", "Hace 3 horas", false)
            }
        }
    }
}

@Composable
fun ItemNoti(titulo: String, desc: String, tiempo: String, esNueva: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Box(modifier = Modifier.size(40.dp).background(Color.Black, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.NotificationsActive, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (esNueva) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(6.dp).background(Color.Black, CircleShape))
                }
            }
            Text(desc, color = Color.Gray, fontSize = 13.sp)
            Text(tiempo, color = Color.LightGray, fontSize = 11.sp)
        }
    }
}

@Composable
fun ContenidoMenuLateral(nombreUsuario: String, alCerrar: () -> Unit, alCerrarSesion: () -> Unit) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            IconButton(onClick = alCerrar, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(60.dp).background(Color.Black, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(nombreUsuario, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            ItemMenuLateral(Icons.Default.FavoriteBorder, "Mis Favoritos")
            ItemMenuLateral(Icons.Default.CreditCard, "Suscripción")
            ItemMenuLateral(Icons.Default.HeadsetMic, "Contacto y Soporte")
            ItemMenuLateral(Icons.Default.History, "Historial")
            Spacer(modifier = Modifier.weight(1f))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            TextButton(onClick = alCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

data class UsuarioSesion(
    val userId: Int,
    val userName: String
)

@Composable
fun ItemMenuLateral(icono: ImageVector, texto: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(20.dp))
        Text(texto, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}