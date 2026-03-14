package com.example.fitgymkt.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.fitgymkt.model.ui.ReservationDetailData
import com.example.fitgymkt.R
import com.example.fitgymkt.repository.ActionResult
import com.example.fitgymkt.repository.FitGymRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleReserva(
    userId: Int,
    scheduleId: Int,
    alAbrirMenu: () -> Unit,
    alAbrirNotificaciones: () -> Unit,
    alVolverAClases: () -> Unit,
    alIrAInicio: () -> Unit,
    alIrAAnalisis: () -> Unit,
    alIrAPerfil: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { FitGymRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var reservando by remember { mutableStateOf(false) }

    val reservationDetail by produceState<ReservationDetailData?>(initialValue = null, scheduleId) {
        value = withContext(Dispatchers.IO) {
            if (scheduleId > 0) repository.getReservationDetailForSchedule(scheduleId)
            else repository.getReservationDetail(userId)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = { IconButton(onClick = alAbrirMenu) { Icon(Icons.Default.Menu, null) } },
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
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(selected = false, onClick = alIrAInicio, icon = { Icon(Icons.Default.Home, null) }, label = { Text(stringResource(R.string.nav_home)) })
                NavigationBarItem(selected = true, onClick = alVolverAClases, icon = { Icon(Icons.Default.DateRange, null) }, label = { Text(stringResource(R.string.nav_classes)) })
                NavigationBarItem(selected = false, onClick = alIrAAnalisis, icon = { Icon(Icons.Default.BarChart, null) }, label = { Text(stringResource(R.string.nav_analysis)) })
                NavigationBarItem(selected = false, onClick = alIrAPerfil, icon = { Icon(Icons.Default.Person, null) }, label = { Text(stringResource(R.string.nav_profile)) })
            }
        }
    ) { padding ->
        if (reservationDetail == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val detail = reservationDetail!!

        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.DarkGray)) {
                Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Bottom) {
                    IconButton(
                        onClick = alVolverAClases,
                        modifier = Modifier.align(Alignment.Start).background(Color.Black.copy(0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(detail.className, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(detail.classDescription, color = Color.White.copy(0.8f), fontSize = 16.sp)
                }
            }

            // 1. Tarjeta de Detalles principales
            Card(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(stringResource(R.string.class_details), fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    ItemDetalle(Icons.Default.CalendarMonth, stringResource(R.string.day_and_time), "${detail.date}\n${detail.startTime}")
                    ItemDetalle(Icons.Default.Person, stringResource(R.string.instructor), detail.instructorName)
                    ItemDetalle(Icons.Default.LocationOn, stringResource(R.string.location), detail.roomName)
                    ItemDetalle(Icons.Default.Groups, stringResource(R.string.availability), stringResource(R.string.detail_availability_value, detail.occupiedSlots, detail.totalSlots))
                }
            }

            // 2. Sección: Qué necesitas
            Card(
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.what_you_need), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    FilaRequisito(stringResource(R.string.requirement_clothes))
                    FilaRequisito(stringResource(R.string.requirement_water))
                    FilaRequisito(stringResource(R.string.requirement_towel))
                    FilaRequisito(stringResource(R.string.requirement_arrive_early))
                }
            }

            // 3. Sección: Política de Cancelación
            Card(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.cancellation_policy), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.cancellation_policy_body),
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }

            // Botón de Acción Final
            Button(
                onClick = {
                    scope.launch {
                        reservando = true
                        val result = withContext(Dispatchers.IO) { repository.reserveClass(userId, scheduleId) }
                        reservando = false
                        when (result) {
                            is ActionResult.Success -> snackbarHostState.showSnackbar(result.message)
                            is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                enabled = !reservando && scheduleId > 0            ) {
                if (reservando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.book_spot), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ItemDetalle(icono: ImageVector, titulo: String, info: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(48.dp).background(Color(0xFFF0F4F8), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, null, tint = Color(0xFF1A1A1A))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(titulo, color = Color.Gray, fontSize = 12.sp)
            Text(info, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp)
        }
    }
}

@Composable
fun FilaRequisito(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(texto, fontSize = 14.sp)
    }
}