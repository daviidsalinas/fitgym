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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.fitgymkt.model.ui.ReservationDetailData
import com.example.fitgymkt.repository.ActionResult
import com.example.fitgymkt.repository.FitGymRepository
import com.example.fitgymkt.ui.theme.ColoresFit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FitGymTopBar(
                onBackClick = alVolverAClases,
                unreadCount = 2,
                onNotificationsClick = alAbrirNotificaciones
            )
        },
        bottomBar = {
            FitGymBottomBar(
                current = FitGymDestination.Classes,
                onHomeClick = alIrAInicio,
                onClassesClick = alVolverAClases,
                onAnalysisClick = alIrAAnalisis,
                onProfileClick = alIrAPerfil
            )
        }
    ) { padding ->
        if (reservationDetail == null) {
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

        val detail = reservationDetail!!

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            FitGymHeroPanel(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.class_details),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = detail.className,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = detail.classDescription,
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            FitGymPanel(modifier = Modifier.fillMaxWidth(), bordered = true) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(stringResource(R.string.class_details), style = MaterialTheme.typography.titleLarge)
                    ItemDetalle(Icons.Default.CalendarMonth, stringResource(R.string.day_and_time), "${detail.date}\n${detail.startTime}")
                    ItemDetalle(Icons.Default.Person, stringResource(R.string.instructor), detail.instructorName)
                    ItemDetalle(Icons.Default.LocationOn, stringResource(R.string.location), detail.roomName)
                    ItemDetalle(
                        Icons.Default.Groups,
                        stringResource(R.string.availability),
                        stringResource(R.string.detail_availability_value, detail.occupiedSlots, detail.totalSlots)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FitGymPanel(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.what_you_need), style = MaterialTheme.typography.titleLarge)
                    FilaRequisito(stringResource(R.string.requirement_clothes))
                    FilaRequisito(stringResource(R.string.requirement_water))
                    FilaRequisito(stringResource(R.string.requirement_towel))
                    FilaRequisito(stringResource(R.string.requirement_arrive_early))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FitGymPanel(
                modifier = Modifier.fillMaxWidth(),
                containerColor = ColoresFit.NaranjaSuave
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.cancellation_policy), style = MaterialTheme.typography.titleMedium, color = ColoresFit.Negro)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.cancellation_policy_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColoresFit.AzulFit
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColoresFit.Negro),
                shape = RoundedCornerShape(20.dp),
                enabled = !reservando && scheduleId > 0
            ) {
                if (reservando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.book_spot), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ItemDetalle(icono: ImageVector, titulo: String, info: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = ColoresFit.Negro)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(titulo, color = ColoresFit.GrisTexto, fontSize = 12.sp)
            Text(info, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun FilaRequisito(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ColoresFit.Verde, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(texto, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
