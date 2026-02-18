package com.example.fitgymkt.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.fitgymkt.model.ui.HomeData
import com.example.fitgymkt.repository.FitGymRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    userId: Int,
    alAbrirMenu: () -> Unit,
    alAbrirNotificaciones: () -> Unit,
    alIrAClases: () -> Unit,
    alIrAAnalisis: () -> Unit,
    alIrAPerfil: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { FitGymRepository(context) }

    val homeData by produceState<HomeData?>(initialValue = null, userId) {
        value = withContext(Dispatchers.IO) { repository.getHomeData(userId = userId) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = alAbrirMenu) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
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
                NavigationBarItem(selected = true, onClick = { }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Inicio") })
                NavigationBarItem(selected = false, onClick = alIrAClases, icon = { Icon(Icons.Default.DateRange, null) }, label = { Text("Clases") })
                NavigationBarItem(selected = false, onClick = alIrAAnalisis, icon = { Icon(Icons.Default.BarChart, null) }, label = { Text("Análisis") })
                NavigationBarItem(selected = false, onClick = alIrAPerfil, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Perfil") })
            }
        }
    ) { padding ->
        if (homeData == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val data = homeData!!
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Bienvenida
            Text(
                "¡Hola, ${data.userName}!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text("Hoy es un buen día para entrenar", color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            // Resumen de Actividad
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CardInicioAccion("Reservar clase", "Ver clases", Icons.Default.DateRange, alIrAClases, Modifier.weight(1f))
                CardInicioAccion("Música del gym", "Abrir Spotify", Icons.Default.LibraryMusic, {}, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CardInicioAccion("Mi progreso", "Ir a análisis", Icons.Default.BarChart, alIrAAnalisis, Modifier.weight(1f))
                CardInicioAccion("Mi historial", "Clases asistidas", Icons.Default.History, alIrAClases, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Siguiente clase", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            val nextClass = data.todayClasses.firstOrNull()
            if (nextClass != null) {
                ItemClaseHoy(
                    nombre = nextClass.className,
                    hora = nextClass.startTime,
                    sala = nextClass.roomName,
                    icono = iconByClass(nextClass.className)
                )
            } else {
                Text("No tienes clases próximas", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Registro de visita", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Racha actual: ${data.streakDays} días", color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TarjetaResumen("Calorías", "${data.calories} kcal", Icons.Default.Whatshot, Color(0xFFFFEBEB), Color(0xFFFF5252), Modifier.weight(1f))
                        TarjetaResumen("Tiempo", data.trainingHours, Icons.Default.Timer, Color(0xFFEBF5FF), Color(0xFF3B82F6), Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Puedes registrar entrenamientos completos desde Análisis",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

        }
    }
}

@Composable
private fun CardInicioAccion(titulo: String, subtitulo: String, icono: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icono, null)
            Spacer(modifier = Modifier.height(10.dp))
            Text(titulo, fontWeight = FontWeight.Bold)
            Text(subtitulo, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

private fun iconByClass(className: String): ImageVector {
    return when {
        className.contains("yoga", ignoreCase = true) -> Icons.Default.SelfImprovement
        className.contains("pilates", ignoreCase = true) -> Icons.Default.AccessibilityNew
        else -> Icons.Default.FitnessCenter
    }
}

@Composable
fun TarjetaResumen(titulo: String, valor: String, icono: ImageVector, fondoIcono: Color, colorIcono: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(40.dp).background(fondoIcono, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icono, null, tint = colorIcono, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(titulo, fontSize = 14.sp, color = Color.Gray)
            Text(valor, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun SeccionCabecera(titulo: String, alClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        TextButton(onClick = alClick) {
            Text("Ver todas", color = MaterialTheme.colorScheme.primary)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun ItemClaseHoy(nombre: String, hora: String, sala: String, icono: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Fondo del icono adaptado: Negro en modo claro, Blanco/Gris en modo oscuro
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = MaterialTheme.colorScheme.surface)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$hora • $sala", color = Color.Gray, fontSize = 14.sp)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}