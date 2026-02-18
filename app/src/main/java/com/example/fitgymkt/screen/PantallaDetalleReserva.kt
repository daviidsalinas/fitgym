package com.example.fitgymkt.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitgymkt.model.ui.ReservationDetailData
import com.example.fitgymkt.repository.FitGymRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleReserva(
    scheduleId: Int,
    alVolverAClases: () -> Unit,
    alIrAInicio: () -> Unit,
    alIrAAnalisis: () -> Unit,
    alIrAPerfil: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { FitGymRepository(context) }

    val detalle = produceState<ReservationDetailData?>(initialValue = null, key1 = scheduleId) {
        value = withContext(Dispatchers.IO) { repository.getReservationDetail(scheduleId) }
    }.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = { IconButton(onClick = {}) { Icon(Icons.Default.Menu, null) } },
                actions = {
                    BadgedBox(badge = { Badge { Text("2") } }) {
                        Icon(Icons.Default.Notifications, null)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(selected = false, onClick = { alIrAInicio() }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Inicio") })
                NavigationBarItem(selected = true, onClick = { alVolverAClases() }, icon = { Icon(Icons.Default.DateRange, null) }, label = { Text("Clases") })
                NavigationBarItem(selected = false, onClick = { alIrAAnalisis() }, icon = { Icon(Icons.Default.BarChart, null) }, label = { Text("Análisis") })
                NavigationBarItem(selected = false, onClick = { alIrAPerfil() }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Perfil") })
            }
        }
    ) { padding ->
        if (detalle == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Cabecera con Imagen y Botón Volver
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.DarkGray)) {
                Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Bottom) {
                    IconButton(
                        onClick = { alVolverAClases() },
                        modifier = Modifier.align(Alignment.Start).background(Color.Black.copy(0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(detalle.className, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(detalle.classDescription, color = Color.White.copy(0.8f), fontSize = 16.sp)
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
                    Text("Detalles de la Clase", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    ItemDetalle(Icons.Default.CalendarMonth, "Día y Hora", "${detalle.date}\n${detalle.startTime}")
                    ItemDetalle(Icons.Default.Person, "Instructor", detalle.instructorName)
                    ItemDetalle(Icons.Default.LocationOn, "Ubicación", detalle.roomName)
                    ItemDetalle(Icons.Default.Groups, "Disponibilidad", "${detalle.occupiedSlots} de ${detalle.totalSlots} plazas")
                }
            }

            // 2. Sección: Qué necesitas
            Card(
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Qué Necesitas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    FilaRequisito("Ropa deportiva cómoda")
                    FilaRequisito("Botella de agua")
                    FilaRequisito("Toalla personal")
                    FilaRequisito("Llegar 10 minutos antes")
                }
            }

            // 3. Sección: Política de Cancelación
            Card(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Política de Cancelación", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Puedes cancelar tu reserva hasta 2 horas antes del inicio de la clase sin penalización.",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }

            // Botón de Acción Final
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Reservar Plaza", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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