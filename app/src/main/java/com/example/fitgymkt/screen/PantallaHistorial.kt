package com.example.fitgymkt.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitgymkt.R
import com.example.fitgymkt.model.ui.UserReservationItem
import com.example.fitgymkt.model.ui.WorkoutHistoryItem
import com.example.fitgymkt.repository.FitGymRepository
import com.example.fitgymkt.ui.theme.ColoresFit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PantallaHistorial(
    userId: Int,
    alIrAInicio: () -> Unit,
    alIrAClases: () -> Unit,
    alIrAAnalisis: () -> Unit,
    alIrAPerfil: () -> Unit,
    alAbrirMenu: () -> Unit,
    alAbrirNotificaciones: () -> Unit,
    unreadNotifications: Int
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember(context) { FitGymRepository(context) }
    var tab by remember { mutableIntStateOf(0) }

    val reservations by produceState<List<UserReservationItem>?>(null, userId) {
        value = withContext(Dispatchers.IO) { repository.getUserReservations(userId) }
    }
    val workouts by produceState<List<WorkoutHistoryItem>?>(null, userId) {
        value = withContext(Dispatchers.IO) { repository.getWorkoutHistory(userId) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FitGymTopBar(
                title = androidx.compose.ui.res.stringResource(R.string.history_title),
                subtitle = androidx.compose.ui.res.stringResource(R.string.my_reservations),
                unreadCount = unreadNotifications,
                onMenuClick = alAbrirMenu,
                onNotificationsClick = alAbrirNotificaciones
            )
        },
        bottomBar = {
            FitGymBottomBar(
                current = FitGymDestination.Home,
                onHomeClick = alIrAInicio,
                onClassesClick = alIrAClases,
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
            TabRow(
                selectedTabIndex = tab,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {}
            ) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(androidx.compose.ui.res.stringResource(R.string.reservations)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(androidx.compose.ui.res.stringResource(R.string.attended_classes)) })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(androidx.compose.ui.res.stringResource(R.string.workouts)) })
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                reservations == null || workouts == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ColoresFit.Naranja)
                    }
                }
                tab == 0 -> HistorialReservas(reservations!!)
                tab == 1 -> HistorialReservas(reservations!!.filter { it.state == "completada" })
                else -> HistorialEntrenamientos(workouts!!)
            }
        }
    }
}

@Composable
private fun HistorialReservas(items: List<UserReservationItem>) {
    if (items.isEmpty()) {
        EmptyHistoryState(androidx.compose.ui.res.stringResource(R.string.no_items_yet))
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items) { item ->
            FitGymPanel(modifier = Modifier.fillMaxWidth(), bordered = true) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(item.className, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${item.date}  •  ${item.time}", color = ColoresFit.GrisTexto)
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                if (item.state == "completada") ColoresFit.NaranjaSuave else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            androidx.compose.ui.res.stringResource(R.string.status_value, item.state),
                            color = if (item.state == "completada") ColoresFit.Naranja else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorialEntrenamientos(items: List<WorkoutHistoryItem>) {
    if (items.isEmpty()) {
        EmptyHistoryState(androidx.compose.ui.res.stringResource(R.string.no_workouts_registered))
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items) { item ->
            FitGymPanel(modifier = Modifier.fillMaxWidth(), bordered = true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.date, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(androidx.compose.ui.res.stringResource(R.string.duration_minutes, item.durationMinutes), color = ColoresFit.GrisTexto)
                    }
                    Box(
                        modifier = Modifier
                            .background(ColoresFit.NaranjaSuave, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("${item.durationMinutes} min", color = ColoresFit.Naranja, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(message: String) {
    FitGymPanel(modifier = Modifier.fillMaxWidth(), bordered = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(message, color = ColoresFit.GrisTexto)
        }
    }
}
