package com.example.fitgymkt.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitgymkt.R
import com.example.fitgymkt.model.ui.UserReservationItem
import com.example.fitgymkt.model.ui.WorkoutHistoryItem
import com.example.fitgymkt.repository.FitGymRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
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
    val context = LocalContext.current
    val repository = remember(context) { FitGymRepository(context) }
    var tab by remember { mutableIntStateOf(0) }

    val reservations by produceState<List<UserReservationItem>?>(null, userId) {
        value = withContext(Dispatchers.IO) { repository.getUserReservations(userId) }
    }
    val workouts by produceState<List<WorkoutHistoryItem>?>(null, userId) {
        value = withContext(Dispatchers.IO) { repository.getWorkoutHistory(userId) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = alAbrirMenu) { Icon(Icons.Default.Menu, null) }
                },
                actions = {
                    IconButton(onClick = alAbrirNotificaciones) {
                        BadgedBox(badge = { if (unreadNotifications > 0) Badge { Text(unreadNotifications.toString()) } }) {
                            Icon(Icons.Default.Notifications, null)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = false, onClick = alIrAInicio, icon = { Icon(Icons.Default.Home, null) }, label = { Text(stringResource(R.string.nav_home)) })
                NavigationBarItem(selected = false, onClick = alIrAClases, icon = { Icon(Icons.Default.DateRange, null) }, label = { Text(stringResource(R.string.nav_classes)) })
                NavigationBarItem(selected = false, onClick = alIrAAnalisis, icon = { Icon(Icons.Default.BarChart, null) }, label = { Text(stringResource(R.string.nav_analysis)) })
                NavigationBarItem(selected = false, onClick = alIrAPerfil, icon = { Icon(Icons.Default.Person, null) }, label = { Text(stringResource(R.string.nav_profile)) })
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.reservations)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.attended_classes)) })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(stringResource(R.string.workouts)) })
            }

            when {
                reservations == null || workouts == null -> Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator() }
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
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.no_items_yet), color = Color.Gray)
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items) { item ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(item.className, fontWeight = FontWeight.Bold)
                    Text("${item.date} • ${item.time}")
                    Text(stringResource(R.string.status_value, item.state), color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun HistorialEntrenamientos(items: List<WorkoutHistoryItem>) {
    if (items.isEmpty()) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.no_workouts_registered), color = Color.Gray)
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items) { item ->
            Card {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column {
                        Text(item.date, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.duration_minutes, item.durationMinutes), color = Color.Gray)
                    }
                }
            }
        }
    }
}