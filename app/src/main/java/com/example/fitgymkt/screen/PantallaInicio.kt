package com.example.fitgymkt.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitgymkt.R
import com.example.fitgymkt.model.ui.HomeData
import com.example.fitgymkt.repository.ActionResult
import com.example.fitgymkt.repository.FitGymRepository
import com.example.fitgymkt.ui.theme.ColoresFit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PantallaInicio(
    userId: Int,
    unreadNotifications: Int,
    alAbrirMenu: () -> Unit,
    alAbrirNotificaciones: () -> Unit,
    alIrAClases: () -> Unit,
    alIrAAnalisis: () -> Unit,
    alIrAPerfil: () -> Unit,
    alIrAHistorial: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { FitGymRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var refreshKey by remember { mutableIntStateOf(0) }
    var horasEntrenamiento by remember { mutableIntStateOf(0) }
    var minutosEntrenamiento by remember { mutableIntStateOf(30) }

    val homeData by produceState<HomeData?>(initialValue = null, userId, refreshKey) {
        value = withContext(Dispatchers.IO) { repository.getHomeData(userId = userId) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { FitGymSnackbarHost(snackbarHostState) },
        topBar = {
            FitGymTopBar(
                unreadCount = unreadNotifications,
                onMenuClick = alAbrirMenu,
                onNotificationsClick = alAbrirNotificaciones
            )
        },
        bottomBar = {
            FitGymBottomBar(
                current = FitGymDestination.Home,
                onHomeClick = {},
                onClassesClick = alIrAClases,
                onAnalysisClick = alIrAAnalisis,
                onProfileClick = alIrAPerfil
            )
        }
    ) { padding ->
        if (homeData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColoresFit.Naranja)
            }
            return@Scaffold
        }

        val data = homeData!!
        val nextClass = data.todayClasses.firstOrNull()
        val noClassesMessage = stringResource(R.string.no_classes_available_now)
        val showNoClassesDisclaimer: () -> Unit = {
            scope.launch { snackbarHostState.showSnackbar(noClassesMessage) }
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            FitGymSectionHeader(
                title = stringResource(R.string.home_greeting, data.userName),
                subtitle = stringResource(R.string.home_motivation)
            )

            Spacer(modifier = Modifier.height(18.dp))

            HeroSiguienteClase(
                nextClass = nextClass,
                onVerClases = if (nextClass != null) alIrAClases else showNoClassesDisclaimer
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (nextClass == null) {
                NoClassesDisclaimer()
                Spacer(modifier = Modifier.height(18.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CardInicioAccion(
                    titulo = stringResource(R.string.book_class),
                    subtitulo = if (nextClass != null) stringResource(R.string.see_classes) else stringResource(R.string.no_classes_available_short),
                    icono = Icons.Default.DateRange,
                    onClick = if (nextClass != null) alIrAClases else showNoClassesDisclaimer,
                    enabled = nextClass != null,
                    modifier = Modifier.weight(1f)
                )
                CardInicioAccion(
                    titulo = stringResource(R.string.gym_music),
                    subtitulo = stringResource(R.string.open_spotify),
                    icono = Icons.Default.LibraryMusic,
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://open.spotify.com/playlist/37i9dQZF1DXaxEKcoCdWHD?si=32f691f5ae2c4c86")
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CardInicioAccion(
                    titulo = stringResource(R.string.my_progress),
                    subtitulo = stringResource(R.string.go_to_analysis),
                    icono = Icons.Default.BarChart,
                    onClick = alIrAAnalisis,
                    modifier = Modifier.weight(1f)
                )
                CardInicioAccion(
                    titulo = stringResource(R.string.my_history),
                    subtitulo = stringResource(R.string.my_reservations),
                    icono = Icons.Default.History,
                    onClick = alIrAHistorial,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            if (nextClass != null) {
                FitGymSectionHeader(
                    title = stringResource(R.string.next_class),
                    subtitle = stringResource(R.string.reserve_spot)
                )
                Spacer(modifier = Modifier.height(12.dp))
                ItemClaseHoy(
                    nombre = nextClass.className,
                    hora = nextClass.startTime,
                    sala = nextClass.roomName,
                    imageUrl = nextClass.imageUrl
                )
                Spacer(modifier = Modifier.height(22.dp))
            }

            TarjetaRegistroEntrenamiento(
                horasEntrenamiento = horasEntrenamiento,
                minutosEntrenamiento = minutosEntrenamiento,
                onHorasChange = { horasEntrenamiento = it },
                onMinutosChange = { minutosEntrenamiento = it },
                onRegistrar = {
                    scope.launch {
                        when (val result = withContext(Dispatchers.IO) {
                            repository.registerWorkout(userId, (horasEntrenamiento * 60) + minutosEntrenamiento)
                        }) {
                            is ActionResult.Success -> {
                                horasEntrenamiento = 0
                                minutosEntrenamiento = 30
                                refreshKey++
                                snackbarHostState.showSnackbar(result.message)
                            }
                            is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HeroSiguienteClase(
    nextClass: com.example.fitgymkt.model.ui.TodayClassItem?,
    onVerClases: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            coil.compose.SubcomposeAsyncImage(
                model = nextClass?.imageUrl?.takeIf { it.isNotBlank() },
                contentDescription = nextClass?.className ?: stringResource(R.string.next_class),
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ColoresFit.Naranja)
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            iconByClass(nextClass?.className.orEmpty()),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(22.dp)
            ) {
                Text(
                    text = stringResource(R.string.next_class),
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = nextClass?.className ?: stringResource(R.string.no_upcoming_classes),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (nextClass != null) "${nextClass.startTime} - ${nextClass.roomName}" else stringResource(R.string.no_classes_available_now),
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onVerClases,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(if (nextClass != null) stringResource(R.string.book_class) else stringResource(R.string.understood))
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        if (nextClass != null) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.Default.Info,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun NoClassesDisclaimer() {
    FitGymPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.no_classes_available_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = stringResource(R.string.no_classes_available_now),
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.72f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun TarjetaRegistroEntrenamiento(
    horasEntrenamiento: Int,
    minutosEntrenamiento: Int,
    onHorasChange: (Int) -> Unit,
    onMinutosChange: (Int) -> Unit,
    onRegistrar: () -> Unit
) {
    FitGymPanel(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            FitGymSectionHeader(
                title = stringResource(R.string.register_training),
                subtitle = stringResource(R.string.register_training_hint)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AjustadorNumero(
                    label = stringResource(R.string.hours),
                    value = horasEntrenamiento,
                    range = 0..8,
                    onChange = onHorasChange,
                    modifier = Modifier.weight(1f)
                )
                AjustadorNumero(
                    label = stringResource(R.string.minutes_short),
                    value = minutosEntrenamiento,
                    range = 0..59,
                    onChange = onMinutosChange,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onRegistrar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.register_minutes, (horasEntrenamiento * 60) + minutosEntrenamiento))
            }
        }
    }
}

@Composable
private fun AjustadorNumero(
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FitGymPanel(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { onChange((value - 1).coerceAtLeast(range.first)) }) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, fontSize = 12.sp, color = ColoresFit.GrisTexto)
                Text(
                    value.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = { onChange((value + 1).coerceAtMost(range.last)) }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardInicioAccion(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        onClick = onClick,
        enabled = enabled
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(titulo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitulo, fontSize = 12.sp, color = ColoresFit.GrisTexto)
        }
    }
}

private fun iconByClass(className: String): ImageVector = when {
    className.contains("yoga", ignoreCase = true) -> Icons.Default.SelfImprovement
    className.contains("pilates", ignoreCase = true) -> Icons.Default.AccessibilityNew
    else -> Icons.Default.FitnessCenter
}

@Composable
fun ItemClaseHoy(nombre: String, hora: String, sala: String, imageUrl: String) {
    FitGymPanel(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                coil.compose.SubcomposeAsyncImage(
                    model = imageUrl.ifBlank { null },
                    contentDescription = nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    error = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(iconByClass(nombre), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = ColoresFit.GrisTexto, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$hora  •  $sala", color = ColoresFit.GrisTexto, fontSize = 13.sp)
                }
            }
            Box(
                modifier = Modifier
                    .background(ColoresFit.NaranjaSuave, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(stringResource(R.string.book_class), color = ColoresFit.Naranja, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
