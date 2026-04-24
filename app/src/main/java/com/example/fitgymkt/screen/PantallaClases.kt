package com.example.fitgymkt.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitgymkt.R
import com.example.fitgymkt.model.ui.ClassWithSchedules
import com.example.fitgymkt.repository.FitGymRepository
import com.example.fitgymkt.ui.theme.ColoresFit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PantallaClases(
    userId: Int,
    alIrAInicio: () -> Unit,
    alIrADetalle: (Int) -> Unit,
    alIrAAnalisis: () -> Unit,
    alIrAPerfil: () -> Unit,
    alAbrirMenu: () -> Unit,
    alAbrirNotificaciones: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { FitGymRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    val dias = listOf(
        "Todos" to stringResource(R.string.all_days),
        "Lunes" to stringResource(R.string.monday),
        "Martes" to stringResource(R.string.tuesday),
        "Miércoles" to stringResource(R.string.wednesday),
        "Jueves" to stringResource(R.string.thursday),
        "Viernes" to stringResource(R.string.friday),
        "Sábado" to stringResource(R.string.saturday),
        "Domingo" to stringResource(R.string.sunday)
    )
    var diaSeleccionado by remember { mutableStateOf("Todos") }
    var claseExpandida by remember { mutableStateOf<Int?>(null) }

    val clases by produceState<List<ClassWithSchedules>?>(initialValue = null, key1 = diaSeleccionado, key2 = userId) {
        value = withContext(Dispatchers.IO) { repository.getClassesByWeekDay(diaSeleccionado) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FitGymTopBar(
                title = stringResource(R.string.nav_classes),
                subtitle = stringResource(R.string.reserve_spot),
                unreadCount = 2,
                onMenuClick = alAbrirMenu,
                onNotificationsClick = alAbrirNotificaciones
            )
        },
        bottomBar = {
            FitGymBottomBar(
                current = FitGymDestination.Classes,
                onHomeClick = alIrAInicio,
                onClassesClick = {},
                onAnalysisClick = alIrAAnalisis,
                onProfileClick = alIrAPerfil
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(dias) { (diaValor, diaTexto) ->
                    val seleccionado = diaSeleccionado == diaValor
                    AssistChip(
                        onClick = { diaSeleccionado = diaValor },
                        label = {
                            Text(
                                diaTexto,
                                color = if (seleccionado) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (seleccionado) ColoresFit.Negro else MaterialTheme.colorScheme.surface,
                            labelColor = if (seleccionado) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = if (seleccionado) ColoresFit.Negro else MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (clases == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ColoresFit.Naranja)
                }
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (clases!!.isEmpty()) {
                    item {
                        FitGymPanel(modifier = Modifier.fillMaxWidth(), bordered = true) {
                            Text(
                                text = stringResource(R.string.no_classes_for_filter),
                                color = ColoresFit.GrisTexto,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                }

                items(clases!!) { clase ->
                    TarjetaClaseDesplegable(
                        titulo = clase.className,
                        subtitulo = clase.description,
                        horariosCount = stringResource(R.string.class_schedules_count, clase.schedules.size),
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

private fun colorByClass(className: String): Color = when {
    className.contains("yoga", ignoreCase = true) -> Color(0xFF1F2937)
    className.contains("pilates", ignoreCase = true) -> Color(0xFF273449)
    else -> Color(0xFF111827)
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
    onHorarioClick: (Int) -> Unit,
    horarios: List<com.example.fitgymkt.model.ui.ClassScheduleItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        onClick = onExpandClick
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(164.dp)
                    .background(colorBase)
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 20.dp, top = 18.dp)
                        .size(86.dp)
                        .background(Color.White.copy(alpha = 0.06f), CircleShape)
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(22.dp)
                ) {
                    Text(titulo, color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        subtitulo,
                        color = Color.White.copy(alpha = 0.74f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(ColoresFit.Naranja, RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(horariosCount, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = if (estaExpandida) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            AnimatedVisibility(visible = estaExpandida) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    horarios.forEach { horario ->
                        FilaHorario(
                            hora = horario.time,
                            dia = horario.weekDay,
                            plazas = "${horario.occupiedSlots}/${horario.totalSlots}",
                            instructor = horario.instructorName,
                            scheduleId = horario.scheduleId,
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
    scheduleId: Int,
    plazas: String,
    instructor: String,
    esCritico: Boolean = false,
    onClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = { onClick(scheduleId) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = ColoresFit.Negro)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("$hora  •  $dia", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(2.dp))
                Text(instructor, color = ColoresFit.GrisTexto, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Groups,
                        contentDescription = null,
                        tint = if (esCritico) ColoresFit.Naranja else ColoresFit.GrisTexto,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        plazas,
                        color = if (esCritico) ColoresFit.Naranja else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                if (esCritico) {
                    Text(stringResource(R.string.few_spots), color = ColoresFit.Naranja, fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = ColoresFit.GrisTexto)
        }
    }
}
