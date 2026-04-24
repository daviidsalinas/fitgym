package com.example.fitgymkt.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.fitgymkt.R
import com.example.fitgymkt.model.ui.AdminClassItem
import com.example.fitgymkt.model.ui.AdminBookingItem
import com.example.fitgymkt.model.ui.AdminDashboardData
import com.example.fitgymkt.model.ui.AdminScheduleItem
import com.example.fitgymkt.model.ui.AdminUserItem
import com.example.fitgymkt.repository.ActionResult
import com.example.fitgymkt.repository.FitGymRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.Search

private enum class AdminSection(
    val titleRes: Int,
    val icon: ImageVector
) {
    DASHBOARD(R.string.admin_dashboard_title, Icons.Default.Dashboard),
    USERS(R.string.admin_users_title, Icons.Default.Groups),
    MANAGEMENT(R.string.admin_management_title, Icons.Default.CalendarMonth)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAdmin(
    adminName: String,
    alCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { FitGymRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedSection by remember { mutableStateOf(AdminSection.DASHBOARD) }
    var refreshKey by remember { mutableStateOf(0) }
    var showCreateClassDialog by remember { mutableStateOf(false) }
    var showCreateScheduleDialog by remember { mutableStateOf(false) }

    val dashboardData by produceState<AdminDashboardData?>(initialValue = null, refreshKey) {
        value = withContext(Dispatchers.IO) { repository.getAdminDashboardData() }
    }
    val users by produceState<List<AdminUserItem>>(initialValue = emptyList(), refreshKey) {
        value = withContext(Dispatchers.IO) { repository.getAdminUsers() }
    }
    val classes by produceState<List<AdminClassItem>>(initialValue = emptyList(), refreshKey) {
        value = withContext(Dispatchers.IO) { repository.getAdminClasses() }
    }
    val schedules by produceState<List<AdminScheduleItem>>(initialValue = emptyList(), refreshKey) {
        value = withContext(Dispatchers.IO) { repository.getAdminSchedules() }
    }
    val bookings by produceState<List<AdminBookingItem>>(initialValue = emptyList(), refreshKey) {
        value = withContext(Dispatchers.IO) { repository.getAdminBookings() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.admin_panel_title), fontWeight = FontWeight.Bold)
                        Text(
                            text = adminName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = alCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(R.string.logout))
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                AdminSection.entries.forEach { section ->
                    NavigationBarItem(
                        selected = selectedSection == section,
                        onClick = { selectedSection = section },
                        icon = {
                            if (section == AdminSection.MANAGEMENT && dashboardData != null && dashboardData!!.todayReservations > 0) {
                                androidx.compose.material3.BadgedBox(
                                    badge = { Badge { Text(dashboardData!!.todayReservations.toString()) } }
                                ) {
                                    Icon(section.icon, contentDescription = null)
                                }
                            } else {
                                Icon(section.icon, contentDescription = null)
                            }
                        },
                        label = { Text(stringResource(section.titleRes)) }
                    )
                }
            }
        }
    ) { padding ->
        if (dashboardData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when (selectedSection) {
                AdminSection.DASHBOARD -> AdminDashboardContent(dashboardData = dashboardData!!)
                AdminSection.USERS -> AdminUsersContent(
                    users = users,
                    onToggleActive = { user ->
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                repository.updateUserActiveStatus(user.id, !user.active)
                            }
                            when (result) {
                                is ActionResult.Success -> {
                                    snackbarHostState.showSnackbar(result.message)
                                    refreshKey++
                                }
                                is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                            }
                        }
                    }
                )
                AdminSection.MANAGEMENT -> AdminManagementContent(
                    classes = classes,
                    schedules = schedules,
                    bookings = bookings,
                    onCreateClass = { showCreateClassDialog = true },
                    onCreateSchedule = { showCreateScheduleDialog = true }
                )
            }
        }

        if (showCreateClassDialog) {
            CreateClassDialog(
                onDismiss = { showCreateClassDialog = false },
                onCreate = { name, description, imageUri ->
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            repository.createAdminClass(name, description, imageUri)
                        }
                        when (result) {
                            is ActionResult.Success -> {
                                snackbarHostState.showSnackbar(result.message)
                                showCreateClassDialog = false
                                refreshKey++
                            }
                            is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            )
        }

        if (showCreateScheduleDialog) {
            CreateScheduleDialog(
                classes = classes,
                onDismiss = { showCreateScheduleDialog = false },
                onCreate = { classId, date, startTime, durationMinutes ->
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            repository.createAdminSchedule(classId, date, startTime, durationMinutes)
                        }
                        when (result) {
                            is ActionResult.Success -> {
                                snackbarHostState.showSnackbar(result.message)
                                showCreateScheduleDialog = false
                                refreshKey++
                            }
                            is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun AdminDashboardContent(dashboardData: AdminDashboardData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            AdminMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.admin_metric_users),
                value = dashboardData.totalUsers.toString(),
                icon = Icons.Default.Groups
            )
            AdminMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.admin_metric_active_users),
                value = dashboardData.activeUsers.toString(),
                icon = Icons.Default.Person
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            AdminMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.admin_metric_classes),
                value = dashboardData.totalClasses.toString(),
                icon = Icons.Default.DateRange
            )
            AdminMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.admin_metric_today_schedules),
                value = dashboardData.todaySchedules.toString(),
                icon = Icons.Default.Schedule
            )
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.admin_recent_users_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.admin_recent_users_subtitle, dashboardData.todayReservations),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                dashboardData.recentUsers.forEach { user ->
                    AdminUserRow(user = user, onToggleActive = null)
                }
            }
        }
    }
}

@Composable
private fun AdminMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Text(text = value, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(text = title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AdminUsersContent(
    users: List<AdminUserItem>,
    onToggleActive: (AdminUserItem) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filteredUsers = remember(users, query) {
        val normalized = query.trim()
        if (normalized.isBlank()) users
        else users.filter { it.fullName.contains(normalized, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.admin_users_summary, filteredUsers.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text(stringResource(R.string.admin_users_search_placeholder)) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
        items(filteredUsers, key = { it.id }) { user ->
            AdminUserRow(user = user, onToggleActive = { onToggleActive(user) })
        }
    }
}

@Composable
private fun AdminManagementContent(
    classes: List<AdminClassItem>,
    schedules: List<AdminScheduleItem>,
    bookings: List<AdminBookingItem>,
    onCreateClass: () -> Unit,
    onCreateSchedule: () -> Unit
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.admin_management_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.admin_management_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            AdminManagementHeader(
                title = stringResource(R.string.admin_classes_title),
                summary = stringResource(R.string.admin_classes_summary, classes.size),
                actionLabel = stringResource(R.string.admin_create_class),
                onAction = onCreateClass
            )
        }
        items(classes, key = { "class_${it.id}" }) { classItem ->
            AdminClassCard(classItem = classItem)
        }

        item {
            AdminManagementHeader(
                title = stringResource(R.string.admin_schedules_title),
                summary = stringResource(R.string.admin_schedules_summary, schedules.size),
                actionLabel = stringResource(R.string.admin_create_schedule),
                onAction = onCreateSchedule
            )
        }
        items(schedules, key = { "schedule_${it.id}" }) { schedule ->
            AdminScheduleCard(schedule = schedule)
        }

        item {
            AdminManagementHeader(
                title = stringResource(R.string.admin_bookings_title),
                summary = stringResource(R.string.admin_bookings_summary, bookings.size),
                actionLabel = null,
                onAction = null
            )
        }
        items(bookings, key = { "booking_${it.id}" }) { booking ->
            AdminBookingCard(booking = booking)
        }
    }
}

@Composable
private fun AdminManagementHeader(
    title: String,
    summary: String,
    actionLabel: String?,
    onAction: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!actionLabel.isNullOrBlank() && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun AdminScheduleCard(schedule: AdminScheduleItem) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(schedule.className, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                stringResource(R.string.admin_schedule_datetime_value, schedule.date, schedule.startTime, schedule.endTime),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.admin_schedule_room_monitor_value, schedule.roomName, schedule.monitorName),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusPill(
                    text = stringResource(R.string.admin_schedule_slots_value, schedule.reservedSlots, schedule.totalSlots),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun AdminBookingCard(booking: AdminBookingItem) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.userName, fontWeight = FontWeight.Bold)
                    Text(booking.userEmail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
                StatusPill(
                    text = booking.state.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    containerColor = bookingStateColor(booking.state).first,
                    contentColor = bookingStateColor(booking.state).second
                )
            }
            HorizontalDivider()
            Text(
                stringResource(R.string.admin_booking_class_value, booking.className),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.admin_booking_schedule_value, booking.date, booking.startTime),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.admin_booking_created_value, booking.reservationDate),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun CreateScheduleDialog(
    classes: List<AdminClassItem>,
    onDismiss: () -> Unit,
    onCreate: (Int, String, String, Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedClass by remember { mutableStateOf<AdminClassItem?>(classes.firstOrNull()) }
    var date by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("60") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(
                        selectedClass?.id ?: -1,
                        date,
                        startTime,
                        durationMinutes.toIntOrNull() ?: 0
                    )
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.admin_create_schedule)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    OutlinedTextField(
                        value = selectedClass?.name.orEmpty(),
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        readOnly = true,
                        label = { Text(stringResource(R.string.admin_schedule_class)) }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        classes.forEach { classItem ->
                            DropdownMenuItem(
                                text = { Text(classItem.name) },
                                onClick = {
                                    selectedClass = classItem
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.admin_schedule_date)) },
                    placeholder = { Text("2026-04-30") }
                )
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.admin_schedule_start_time)) },
                    placeholder = { Text("18:30") }
                )
                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.admin_schedule_duration)) },
                    placeholder = { Text("60") }
                )
                Text(
                    text = stringResource(R.string.admin_schedule_defaults_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    )
}

@Composable
private fun CreateClassDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, Uri?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onCreate(name, description, imageUri) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.admin_create_class)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.admin_class_name)) }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.admin_class_description)) }
                )
                TextButton(onClick = { imagePicker.launch("image/*") }) {
                    Text(
                        if (imageUri == null) stringResource(R.string.admin_pick_image)
                        else stringResource(R.string.admin_change_image)
                    )
                }
                imageUri?.let {
                    SubcomposeAsyncImage(
                        model = it,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(160.dp),
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun AdminClassCard(classItem: AdminClassItem) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                    if (classItem.imageUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = classItem.imageUrl,
                            contentDescription = classItem.name,
                            modifier = Modifier
                                .size(72.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp)),
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(classItem.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = classItem.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusPill(
                    text = stringResource(R.string.admin_class_schedules_value, classItem.schedulesCount),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                StatusPill(
                    text = stringResource(R.string.admin_class_reservations_value, classItem.reservationsCount),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun AdminUserRow(
    user: AdminUserItem,
    onToggleActive: (() -> Unit)?
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(user.fullName.ifBlank { "Usuario ${user.id}" }, fontWeight = FontWeight.Bold)
                    Text(user.email, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
                StatusPill(
                    text = if (user.active) stringResource(R.string.admin_status_active) else stringResource(R.string.admin_status_inactive),
                    containerColor = if (user.active) Color(0xFFDDF7E8) else Color(0xFFF4E1E1),
                    contentColor = if (user.active) Color(0xFF166534) else Color(0xFF991B1B)
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.admin_role_value, user.role.uppercase()),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    if (user.createdAt.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.admin_created_value, user.createdAt.take(10)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
                onToggleActive?.let {
                    Switch(checked = user.active, onCheckedChange = { it() })
                }
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun bookingStateColor(state: String): Pair<Color, Color> {
    return when (state.lowercase()) {
        "reservada" -> Color(0xFFE0F2FE) to Color(0xFF075985)
        "completada" -> Color(0xFFDDF7E8) to Color(0xFF166534)
        "cancelada" -> Color(0xFFFDE2E2) to Color(0xFF991B1B)
        else -> Color(0xFFE5E7EB) to Color(0xFF374151)
    }
}
