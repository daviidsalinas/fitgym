package com.example.fitgymkt.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitgymkt.model.ui.ClassWithSchedules
import com.example.fitgymkt.repository.FitGymRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaClases(
    alIrAInicio: () -> Unit,
    alIrADetalle: () -> Unit,
    alIrAAnalisis: () -> Unit,
    alIrAPerfil: () -> Unit,
    alAbrirMenu: () -> Unit,
    alAbrirNotificaciones: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { FitGymRepository(context) }

    val dias = listOf("Todos", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    var diaSeleccionado by remember { mutableStateOf("Todos") }
    var claseExpandida by remember { mutableStateOf<Int?>(null) }

    val clases by produceState<List<ClassWithSchedules>?>(initialValue = null, key1 = diaSeleccionado) {
        value = withContext(Dispatchers.IO) { repository.getClassesByWeekDay(diaSeleccionado) }
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
                NavigationBarItem(
                    selected = false,
                    onClick = { alIrAInicio() },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text("Clases") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { alIrAAnalisis() },
                    icon = { Icon(Icons.Default.BarChart, null) },
                    label = { Text("Análisis") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { alIrAPerfil() },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Clases Disponibles", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Reserva tu plaza", color = Color.Gray, fontSize = 14.sp)
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dias) { dia ->
                    FilterChip(
                        selected = diaSeleccionado == dia,
                        onClick = { diaSeleccionado = dia },
                        label = { Text(dia) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            if (clases == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }


            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (clases!!.isEmpty()) {
                    item { Text("No hay clases para el filtro seleccionado", color = Color.Gray) }
                }

                items(clases!!) { clase ->
                    TarjetaClaseDesplegable(
                        titulo = clase.className,
                        subtitulo = clase.description,
                        horariosCount = "${clase.schedules.size} horarios",
                        colorBase = colorByClass(clase.className),
                        estaExpandida = claseExpandida == clase.classId,
                        onExpandClick = {
                            claseExpandida = if (claseExpandida == clase.classId) null else clase.classId
                        },
                        onHorarioClick = alIrADetalle,
                        horarios = clase.schedules
                    )
                }
            }
        }
    }
}

private fun colorByClass(className: String): Color {
    return when {
        className.contains("yoga", ignoreCase = true) -> Color(0xFF1A1A1A)
        className.contains("pilates", ignoreCase = true) -> Color(0xFF455A64)
        else -> Color(0xFF37474F)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaClaseDesplegable(
    titulo: String,
    subtitulo: String,
    horariosCount: String,
    colorBase: Color,
    estaExpandida: Boolean,
    onExpandClick: () -> Unit,
    onHorarioClick: () -> Unit,
    horarios: List<com.example.fitgymkt.model.ui.ClassScheduleItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (estaExpandida) MaterialTheme.colorScheme.surfaceVariant else colorBase
        ),
        onClick = onExpandClick
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(colorBase)) {
                Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Bottom) {
                    Text(titulo, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(subtitulo, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        if (!estaExpandida) {
                            Text(horariosCount, color = Color.White, fontSize = 12.sp)
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White)
                        } else {
                            Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White)
                        }
                    }
                }
            }

            AnimatedVisibility(visible = estaExpandida) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    horarios.forEach { horario ->
                        FilaHorario(
                            hora = horario.time,
                            dia = horario.weekDay,
                            plazas = "${horario.occupiedSlots}/${horario.totalSlots}",
                            instructor = horario.instructorName,
                            esCritico = horario.totalSlots - horario.occupiedSlots <= 3,
                            onClick = onHorarioClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilaHorario(
    hora: String,
    dia: String,
    plazas: String,
    instructor: String,
    esCritico: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(18.dp))
            Column {
                Text("$hora  •  $dia", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(instructor, color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Groups, null,
                        modifier = Modifier.size(16.dp),
                        tint = if (esCritico) Color(0xFFE65100) else Color.Gray
                    )
                    Text(
                        " $plazas",
                        fontSize = 13.sp,
                        color = if (esCritico) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (esCritico) Text("¡Pocas plazas!", color = Color(0xFFE65100), fontSize = 10.sp)
            }
            Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.LightGray)
        }
    }
}