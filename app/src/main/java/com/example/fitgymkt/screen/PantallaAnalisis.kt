package com.example.fitgymkt.screen

import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fitgymkt.R
import com.example.fitgymkt.model.ui.AnalysisData
import com.example.fitgymkt.repository.ActionResult
import com.example.fitgymkt.repository.FitGymRepository
import com.example.fitgymkt.ui.theme.ColoresFit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

@Composable
fun PantallaAnalisis(
    userId: Int,
    unreadNotifications: Int,
    alIrAInicio: () -> Unit,
    alIrAClases: () -> Unit,
    alIrAPerfil: () -> Unit,
    alAbrirMenu: () -> Unit,
    alAbrirNotificaciones: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { FitGymRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var refreshKey by remember { mutableIntStateOf(0) }
    var horasEntrenamiento by remember { mutableIntStateOf(0) }
    var minutosEntrenamiento by remember { mutableIntStateOf(30) }
    var previewEntrenamiento by remember { mutableStateOf(false) }
    var minutosOptimistas by remember { mutableIntStateOf(0) }

    val analysisData by produceState<AnalysisData?>(initialValue = null, userId, refreshKey) {
        value = withContext(Dispatchers.IO) { repository.getAnalysisData(userId) }
    }

    LaunchedEffect(analysisData) {
        minutosOptimistas = 0
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { FitGymSnackbarHost(snackbarHostState) },
        topBar = {
            FitGymTopBar(
                title = stringResource(R.string.analysis_title),
                subtitle = stringResource(R.string.analysis_subtitle),
                unreadCount = unreadNotifications,
                onMenuClick = alAbrirMenu,
                onNotificationsClick = alAbrirNotificaciones
            )
        },
        bottomBar = {
            FitGymBottomBar(
                current = FitGymDestination.Analysis,
                onHomeClick = alIrAInicio,
                onClassesClick = alIrAClases,
                onAnalysisClick = {},
                onProfileClick = alIrAPerfil
            )
        }
    ) { padding ->
        if (analysisData == null) {
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

        val data = analysisData!!
        val selectedMinutes = (horasEntrenamiento * 60) + minutosEntrenamiento
        val previewMinutes = minutosOptimistas + if (previewEntrenamiento) selectedMinutes else 0
        val previewHours = previewMinutes / 60.0
        val displayedCompletedHours = data.weeklyCompletedHours + previewHours
        val displayedWeeklyActivity = data.weeklyActivityHours.withTodayPreview(previewHours)
        val progress = if (data.weeklyGoalHours <= 0.0) 0f else (displayedCompletedHours / data.weeklyGoalHours).toFloat().coerceIn(0f, 1f)
        val remainingHours = (data.weeklyGoalHours - displayedCompletedHours).coerceAtLeast(0.0)
        val totalWeeklyHours = displayedWeeklyActivity.sum()
        val avgMinutes = if (displayedWeeklyActivity.isEmpty()) 0 else ((totalWeeklyHours / displayedWeeklyActivity.size) * 60).toInt()

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            FitGymHeroPanel(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(ColoresFit.Naranja, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.current_streak), color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(data.streakDays.toString(), color = ColoresFit.Naranja, fontSize = 62.sp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        " ${stringResource(R.string.consecutive_days)}",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(stringResource(R.string.consistency_message), color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(18.dp))

            FitGymPanel(modifier = Modifier.fillMaxWidth(), bordered = true) {
                Column(modifier = Modifier.padding(20.dp)) {
                    FitGymSectionHeader(
                        title = stringResource(R.string.analysis_weekly_goal),
                        subtitle = stringResource(R.string.analysis_training_time)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = stringResource(R.string.analysis_hours_value, displayedCompletedHours),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.analysis_remaining_hours, remainingHours),
                        color = ColoresFit.GrisTexto,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = ColoresFit.Naranja,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            FitGymPanel(modifier = Modifier.fillMaxWidth(), bordered = true) {
                Column(modifier = Modifier.padding(20.dp)) {
                    FitGymSectionHeader(
                        title = stringResource(R.string.analysis_weekly_activity),
                        subtitle = stringResource(R.string.analysis_last_7_days)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxValue = (displayedWeeklyActivity.maxOrNull() ?: 0.0).coerceAtLeast(0.1)
                        val dias = listOf(
                            stringResource(R.string.mon_short),
                            stringResource(R.string.tue_short),
                            stringResource(R.string.wed_short),
                            stringResource(R.string.thu_short),
                            stringResource(R.string.fri_short),
                            stringResource(R.string.sat_short),
                            stringResource(R.string.sun_short)
                        )

                        displayedWeeklyActivity.forEachIndexed { index, dailyHours ->
                            val altura = (dailyHours / maxValue).toFloat().coerceIn(0.1f, 1f)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .fillMaxHeight(altura)
                                        .background(
                                            color = if (dailyHours == maxValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(dias[index], fontSize = 12.sp, color = ColoresFit.GrisTexto)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MiniMetricCard(
                            title = stringResource(R.string.analysis_total),
                            value = stringResource(R.string.analysis_hours_value, totalWeeklyHours),
                            modifier = Modifier.weight(1f)
                        )
                        MiniMetricCard(
                            title = stringResource(R.string.analysis_daily_average),
                            value = stringResource(R.string.analysis_minutes_value, avgMinutes),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            FitGymPanel(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = ColoresFit.Naranja)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(stringResource(R.string.register_training), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.select_hours_minutes), style = MaterialTheme.typography.bodySmall, color = ColoresFit.GrisTexto)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SelectorTiempo(
                            titulo = stringResource(R.string.hours),
                            value = horasEntrenamiento,
                            range = 0..8,
                            onValueChange = {
                                horasEntrenamiento = it
                                previewEntrenamiento = true
                            }
                        )
                        SelectorTiempo(
                            titulo = stringResource(R.string.minutes_short),
                            value = minutosEntrenamiento,
                            range = 0..59,
                            formatter = { String.format(Locale.getDefault(), "%02d", it) },
                            onValueChange = {
                                minutosEntrenamiento = it
                                previewEntrenamiento = true
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val minutes = (horasEntrenamiento * 60) + minutosEntrenamiento
                            scope.launch {
                                when (val result = withContext(Dispatchers.IO) { repository.registerWorkout(userId, minutes) }) {
                                    is ActionResult.Success -> {
                                        minutosOptimistas += minutes
                                        previewEntrenamiento = false
                                        horasEntrenamiento = 0
                                        minutosEntrenamiento = 30
                                        refreshKey++
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                    is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.register_minutes, (horasEntrenamiento * 60) + minutosEntrenamiento))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun List<Double>.withTodayPreview(extraHours: Double): List<Double> {
    if (isEmpty() || extraHours <= 0.0) return this
    val todayIndex = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        else -> 6
    }
    return mapIndexed { index, value -> if (index == todayIndex) value + extraHours else value }
}

@Composable
private fun MiniMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    FitGymPanel(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, color = ColoresFit.GrisTexto)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SelectorTiempo(
    titulo: String,
    value: Int,
    range: IntRange,
    formatter: (Int) -> String = { it.toString() },
    onValueChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(titulo, fontSize = 12.sp, color = ColoresFit.GrisTexto)
        AndroidView(
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = range.first
                    maxValue = range.last
                    wrapSelectorWheel = true
                    descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                    setOnValueChangedListener { _, _, newVal -> onValueChange(newVal) }
                }
            },
            update = { picker ->
                picker.displayedValues = null
                picker.minValue = range.first
                picker.maxValue = range.last
                picker.displayedValues = range.map(formatter).toTypedArray()
                if (picker.value != value) picker.value = value
            },
            modifier = Modifier.size(width = 110.dp, height = 130.dp)
        )
    }
}
