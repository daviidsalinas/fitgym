package com.example.fitgymkt

import android.Manifest
import android.os.Bundle
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.*
import com.example.fitgymkt.model.ui.AppNotification
import com.example.fitgymkt.model.ui.SubscriptionStatus
import com.example.fitgymkt.notifications.PushNotificationScheduler
import com.example.fitgymkt.repository.FitGymRepository
import com.example.fitgymkt.screen.*
import com.example.fitgymkt.ui.theme.ColoresFit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savedLanguage = getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "ES")
            ?: "ES"
        applyAppLanguage(this, savedLanguage)
        setContent {
            var esModoOscuro by remember { mutableStateOf(false) }
            var mostrarNotificaciones by remember { mutableStateOf(false) }
            var mostrarSuscripcion by remember { mutableStateOf(false) }
            var mostrarContacto by remember { mutableStateOf(false) }
            var notificaciones by remember { mutableStateOf(listOf<AppNotification>()) }
            var suscripcion by remember { mutableStateOf<SubscriptionStatus?>(null) }

            val colores = if (esModoOscuro) {
                darkColorScheme(
                    primary = Color(0xFFE5E7EB),
                    secondary = Color(0xFFCBD5E1),
                    tertiary = Color(0xFF94A3B8),
                    background = Color(0xFF111827),
                    surface = Color(0xFF111827),
                    surfaceVariant = Color(0xFF1F2937),
                    onPrimary = Color(0xFF111827),
                    onSecondary = Color(0xFF111827),
                    onTertiary = Color(0xFF111827),
                    onBackground = Color.White,
                    onSurface = Color.White,
                    onSurfaceVariant = Color(0xFFD1D5DB)
                )
            } else {
                lightColorScheme(
                    primary = ColoresFit.Negro,
                    secondary = Color(0xFF475569),
                    tertiary = Color(0xFF64748B),
                    background = ColoresFit.Blanco,
                    surface = ColoresFit.Blanco,
                    surfaceVariant = Color(0xFFF8FAFC),
                    onPrimary = ColoresFit.Blanco,
                    onSecondary = ColoresFit.Blanco,
                    onTertiary = ColoresFit.Blanco,
                    onBackground = ColoresFit.Negro,
                    onSurface = ColoresFit.Negro,
                    onSurfaceVariant = Color(0xFF64748B)
                )
            }

            MaterialTheme(colorScheme = colores) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val requestNotificationPermission = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { }
                val repository = remember(context) { FitGymRepository(context) }
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var usuarioSesion by remember { mutableStateOf<UsuarioSesion?>(null) }
                var usuarioProgramadoId by remember { mutableStateOf<Int?>(null) }
                var rutaSolicitada by remember { mutableStateOf<String?>(null) }
                var rutaActual by remember { mutableStateOf("splash") }
                val drawerHabilitado = usuarioSesion?.role == "cliente" && rutaActual !in setOf("splash", "login", "registro")

                LaunchedEffect(usuarioSesion?.userId) {
                    val userId = usuarioSesion?.userId
                    if (userId == null) {
                        if (usuarioProgramadoId != null) {
                            PushNotificationScheduler.cancel(context, usuarioProgramadoId!!)
                            usuarioProgramadoId = null
                        }
                        notificaciones = emptyList()
                        suscripcion = null
                        return@LaunchedEffect
                    }
                    if (usuarioProgramadoId != null && usuarioProgramadoId != userId) {
                        PushNotificationScheduler.cancel(context, usuarioProgramadoId!!)
                    }
                    PushNotificationScheduler.schedule(context, userId)
                    usuarioProgramadoId = userId

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    val (notificationsResult, subscriptionResult) = withContext(Dispatchers.IO) {
                        repository.getNotifications(userId) to repository.getCurrentSubscription(userId)
                    }
                    notificaciones = notificationsResult
                    suscripcion = subscriptionResult
                }

                if (mostrarNotificaciones) {
                    DialogoNotificacionesGlobal(
                        notificaciones = notificaciones,
                        onDismiss = { mostrarNotificaciones = false },
                        onMarcarLeidas = { notificaciones = notificaciones.map { it.copy(read = true) } }
                    )
                }

                if (mostrarSuscripcion) {
                    DialogoSuscripcionGlobal(suscripcion = suscripcion, onDismiss = { mostrarSuscripcion = false })
                }

                if (mostrarContacto) {
                    DialogoContactoSoporte(onDismiss = { mostrarContacto = false })
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = drawerHabilitado,
                    drawerContent = {
                        ContenidoMenuLateral(
                            nombreUsuario = usuarioSesion?.userName ?: "Invitado",
                            subscription = suscripcion,
                            alCerrar = { scope.launch { drawerState.close() } },
                            alVerSuscripcion = { mostrarSuscripcion = true },
                            alVerContacto = { mostrarContacto = true },
                            alVerHistorial = {
                                rutaSolicitada = "historial"
                                scope.launch { drawerState.close() }
                            },
                            alCerrarSesion = {
                                usuarioSesion = null
                                mostrarNotificaciones = false
                                mostrarSuscripcion = false
                                mostrarContacto = false
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
                            if (drawerHabilitado) scope.launch { drawerState.open() }
                        },
                        alAbrirNotificaciones = { mostrarNotificaciones = true },
                        unreadNotifications = notificaciones.count { !it.read },
                        rutaSolicitada = rutaSolicitada,
                        alConsumirRutaSolicitada = { rutaSolicitada = null }
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
    alAbrirNotificaciones: () -> Unit,
    unreadNotifications: Int,
    rutaSolicitada: String?,
    alConsumirRutaSolicitada: () -> Unit
) {
    val context = LocalContext.current
    val controladorNavegacion = rememberNavController()

    LaunchedEffect(rutaSolicitada) {
        rutaSolicitada?.let {
            controladorNavegacion.navigate(it)
            alConsumirRutaSolicitada()
        }
    }

    LaunchedEffect(controladorNavegacion) {
        controladorNavegacion.currentBackStackEntryFlow.collectLatest { entry ->
            onRutaCambiada(entry.destination.route ?: "")
        }
    }

    LaunchedEffect(usuarioSesion) {
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
                    val destino = when {
                        usuarioSesion == null -> "login"
                        usuarioSesion.role == "admin" -> "admin"
                        else -> "inicio"
                    }
                    controladorNavegacion.navigate(destino) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            PantallaLogin(
                alIrARegistro = { controladorNavegacion.navigate("registro") },
                alEntrarApp = { userId, userName, role ->
                    alCambiarSesion(UsuarioSesion(userId = userId, userName = userName, role = role))
                    val destination = if (role == "admin") "admin" else "inicio"
                    controladorNavegacion.navigate(destination) { popUpTo("login") { inclusive = true } }
                }
            )
        }
        composable("registro") {
            PantallaRegistro(
                alVolverAlLogin = { controladorNavegacion.popBackStack() },
                alRegistroCompletado = { userId, userName ->
                    alCambiarSesion(UsuarioSesion(userId = userId, userName = userName, role = "cliente"))
                    controladorNavegacion.navigate("inicio") { popUpTo("login") { inclusive = true } }
                }
            )
        }
        composable("admin") {
            PantallaAdmin(
                adminName = usuarioSesion?.userName ?: "Admin",
                alCerrarSesion = {
                    alCambiarSesion(null)
                    controladorNavegacion.navigate("login") {
                        popUpTo(controladorNavegacion.graph.id) { inclusive = true }
                    }
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
                alIrAPerfil = { controladorNavegacion.navigate("perfil") },
                alIrAHistorial = { controladorNavegacion.navigate("historial") }
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
                unreadNotifications = unreadNotifications,
                modoOscuroActivado = modoOscuroActual,
                onModoOscuroChanged = alCambiarModoOscuro,
                onIdiomaChanged = { codigo ->
                    applyAppLanguage(context, codigo)
                    controladorNavegacion.navigate("perfil") {
                        popUpTo("perfil") { inclusive = true }
                    }
                },
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
        composable("historial") {
            PantallaHistorial(
                userId = usuarioSesion?.userId ?: 1,
                alAbrirMenu = alAbrirMenu,
                alAbrirNotificaciones = alAbrirNotificaciones,
                alIrAInicio = { controladorNavegacion.navigate("inicio") },
                alIrAClases = { controladorNavegacion.navigate("clases") },
                alIrAAnalisis = { controladorNavegacion.navigate("analisis") },
                alIrAPerfil = { controladorNavegacion.navigate("perfil") },
                unreadNotifications = unreadNotifications
            )
        }
    }
}

private const val APP_PREFS = "fitgym_app_prefs"
private const val KEY_LANGUAGE = "selected_language"

private fun applyAppLanguage(context: Context, languageCode: String) {
    val locale = Locale(languageCode.lowercase(Locale.ROOT))
    Locale.setDefault(locale)

    val resources = context.resources
    val configuration = resources.configuration
    configuration.setLocale(locale)
    resources.updateConfiguration(configuration, resources.displayMetrics)

    context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_LANGUAGE, languageCode.uppercase(Locale.ROOT))
        .apply()
}

@Composable
fun DialogoNotificacionesGlobal(
    notificaciones: List<AppNotification>,
    onDismiss: () -> Unit,
    onMarcarLeidas: () -> Unit
) {
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
                        Text(stringResource(R.string.notifications), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(stringResource(R.string.unread_count, notificaciones.count { !it.read }), color = Color.Gray, fontSize = 12.sp)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }

                Button(
                    onClick = onMarcarLeidas,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Text(" ${stringResource(R.string.mark_all_as_read)}", fontSize = 13.sp)

                }

                notificaciones.forEach {
                    ItemNoti(it.title, it.description, it.timestamp, !it.read)
                }
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
fun ContenidoMenuLateral(
    nombreUsuario: String,
    subscription: SubscriptionStatus?,
    alCerrar: () -> Unit,
    alVerSuscripcion: () -> Unit,
    alVerContacto: () -> Unit,
    alVerHistorial: () -> Unit,
    alCerrarSesion: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            IconButton(onClick = alCerrar, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
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
            ItemMenuLateral(Icons.Default.CreditCard, stringResource(R.string.subscription), onClick = alVerSuscripcion)
            Text(subscription?.let { stringResource(R.string.subscription_expires, it.endDate) } ?: stringResource(R.string.no_active_subscription), fontSize = 12.sp, color = Color.Gray)
            ItemMenuLateral(Icons.Default.HeadsetMic, stringResource(R.string.contact_support), onClick = alVerContacto)
            ItemMenuLateral(Icons.Default.History, stringResource(R.string.nav_history), onClick = alVerHistorial)
            Spacer(modifier = Modifier.weight(1f))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            TextButton(onClick = alCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(stringResource(R.string.logout), color = Color.Red, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

data class UsuarioSesion(
    val userId: Int,
    val userName: String,
    val role: String
)

@Composable
fun ItemMenuLateral(icono: ImageVector, texto: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(20.dp))
        Text(texto, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DialogoSuscripcionGlobal(suscripcion: SubscriptionStatus?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
        title = { Text(stringResource(R.string.subscription)) },
        text = {
            Text(
                suscripcion?.let { "Plan ${it.type}\nEstado: ${it.status}\nVence: ${it.endDate}" }
                    ?: "No tienes una suscripción activa"
            )
        }
    )
}

@Composable
fun DialogoContactoSoporte(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
        title = { Text(stringResource(R.string.contact_support)) },
        text = { Text(stringResource(R.string.contact_support_body)) }
    )
}
