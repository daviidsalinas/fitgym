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

                // 2. Diálogo Global de Notificaciones
                if (mostrarNotificaciones) {
                    DialogoNotificacionesGlobal(onDismiss = { mostrarNotificaciones = false })
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ContenidoMenuLateral(
                            alCerrar = { scope.launch { drawerState.close() } },
                            alCerrarSesion = { scope.launch { drawerState.close() } }
                        )
                    }
                ) {
                    NavegacionPrincipal(
                        modoOscuroActual = esModoOscuro,
                        alCambiarModoOscuro = { esModoOscuro = it },
                        alAbrirMenu = { scope.launch { drawerState.open() } },
                        alAbrirNotificaciones = { mostrarNotificaciones = true } // Acción para la campana
                    )
                }
            }
        }
    }
}

@Composable
fun NavegacionPrincipal(
    modoOscuroActual: Boolean,
    alCambiarModoOscuro: (Boolean) -> Unit,
    alAbrirMenu: () -> Unit,
    alAbrirNotificaciones: () -> Unit // Nuevo parámetro
) {
    val controladorNavegacion = rememberNavController()

    NavHost(navController = controladorNavegacion, startDestination = "login") {
        composable("login") {
            PantallaLogin(
                alIrARegistro = { controladorNavegacion.navigate("registro") },
                alEntrarApp = {
                    controladorNavegacion.navigate("inicio") { popUpTo("login") { inclusive = true } }
                }
            )
        }
        composable("registro") {
            PantallaRegistro(alVolverAlLogin = { controladorNavegacion.popBackStack() })
        }
        composable("inicio") {
            PantallaInicio(
                alAbrirMenu = alAbrirMenu,
                alAbrirNotificaciones = alAbrirNotificaciones,
                alIrAClases = { controladorNavegacion.navigate("clases") },
                alIrAAnalisis = { controladorNavegacion.navigate("analisis") },
                alIrAPerfil = { controladorNavegacion.navigate("perfil") }
            )
        }
        composable("clases") {
            PantallaClases(
                alAbrirMenu = alAbrirMenu,
                alAbrirNotificaciones = alAbrirNotificaciones,
                alIrAInicio = { controladorNavegacion.navigate("inicio") },
                alIrAAnalisis = { controladorNavegacion.navigate("analisis") },
                alIrAPerfil = { controladorNavegacion.navigate("perfil") },
                alIrADetalle = { controladorNavegacion.navigate("detalle_reserva") }
            )
        }
        composable("analisis") {
            PantallaAnalisis(
                alAbrirMenu = alAbrirMenu,
                alAbrirNotificaciones = alAbrirNotificaciones,
                alIrAInicio = { controladorNavegacion.navigate("inicio") },
                alIrAClases = { controladorNavegacion.navigate("clases") },
                alIrAPerfil = { controladorNavegacion.navigate("perfil") }
            )
        }
        composable("perfil") {
            PantallaPerfil(
                alAbrirMenu = alAbrirMenu,
                alAbrirNotificaciones = alAbrirNotificaciones,
                alIrAInicio = { controladorNavegacion.navigate("inicio") },
                alIrAClases = { controladorNavegacion.navigate("clases") },
                alIrAAnalisis = { controladorNavegacion.navigate("analisis") },
                modoOscuroActivado = modoOscuroActual,
                onModoOscuroChanged = alCambiarModoOscuro
            )
        }
        composable("detalle_reserva") {
            PantallaDetalleReserva(
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
fun ContenidoMenuLateral(alCerrar: () -> Unit, alCerrarSesion: () -> Unit) {
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
                    Text("Carlos Martínez", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Plan Premium", color = Color.Gray, fontSize = 12.sp)
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