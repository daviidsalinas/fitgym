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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.fitgymkt.model.ui.AdminMonitorItem
import com.example.fitgymkt.model.ui.AdminRoomItem
import com.example.fitgymkt.model.ui.AdminScheduleItem
import com.example.fitgymkt.model.ui.AdminUserItem
import com.example.fitgymkt.repository.ActionResult
import com.example.fitgymkt.repository.FitGymRepository
import com.example.fitgymkt.ui.theme.ColoresFit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.Search
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private enum class AdminSection(
    val titleRes: Int,
    val icon: ImageVector
) {
    DASHBOARD(R.string.admin_dashboard_title, Icons.Default.Dashboard),
    USERS(R.string.admin_users_title, Icons.Default.Groups),
    MANAGEMENT(R.string.admin_management_title, Icons.Default.CalendarMonth)
}

private enum class AdminScheduleFilter {
    ALL,
    FUTURE
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
    var showCreateClass by remember { mutableStateOf(false) }
    var showCreateUser by remember { mutableStateOf(false) }
    var classForEdit by remember { mutableStateOf<AdminClassItem?>(null) }
    var classForDelete by remember { mutableStateOf<AdminClassItem?>(null) }
    var classForSchedule by remember { mutableStateOf<AdminClassItem?>(null) }
    var scheduleForManagement by remember { mutableStateOf<AdminScheduleItem?>(null) }
    var userForNotification by remember { mutableStateOf<AdminUserItem?>(null) }
    var userForStatusChange by remember { mutableStateOf<AdminUserItem?>(null) }

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
    val rooms by produceState<List<AdminRoomItem>>(initialValue = emptyList(), refreshKey) {
        value = withContext(Dispatchers.IO) { repository.getAdminRooms() }
    }
    val monitors by produceState<List<AdminMonitorItem>>(initialValue = emptyList(), refreshKey) {
        value = withContext(Dispatchers.IO) { repository.getAdminMonitors() }
    }
    val bookings by produceState<List<AdminBookingItem>>(initialValue = emptyList(), refreshKey) {
        value = withContext(Dispatchers.IO) { repository.getAdminBookings() }
    }

    Scaffold(
        snackbarHost = { FitGymSnackbarHost(snackbarHostState) },
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
                    onCreateUser = { showCreateUser = true },
                    onToggleActive = { user -> userForStatusChange = user },
                    onCreateNotification = { user -> userForNotification = user }
                )
                AdminSection.MANAGEMENT -> AdminManagementContent(
                    classes = classes,
                    schedules = schedules,
                    onCreateClass = { showCreateClass = true },
                    onEditClass = { classForEdit = it },
                    onDeleteClass = { classForDelete = it },
                    onCreateSchedule = { classForSchedule = it },
                    onScheduleClick = { scheduleForManagement = it }
                )
            }
        }

        if (showCreateClass) {
            CreateClassDialog(
                onDismiss = { showCreateClass = false },
                onSave = { name, description, imageUri ->
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            repository.createAdminClass(name, description, imageUri)
                        }
                        when (result) {
                            is ActionResult.Success -> {
                                snackbarHostState.showSnackbar(result.message)
                                showCreateClass = false
                                refreshKey++
                            }
                            is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            )
        }

        if (showCreateUser) {
            CreateUserDialog(
                onDismiss = { showCreateUser = false },
                onCreate = { form ->
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            repository.createAdminUser(
                                nombre = form.nombre,
                                apellidos = form.apellidos,
                                email = form.email,
                                password = form.password,
                                role = form.role,
                                active = form.active,
                                profilePhoto = "",
                                phone = form.phone,
                                age = form.age,
                                weightKg = form.weightKg,
                                heightCm = form.heightCm,
                                darkMode = false,
                                notificationsEnabled = form.notificationsEnabled,
                                language = form.language,
                                monitorSpecialty = form.monitorSpecialty,
                                monitorPhone = form.monitorPhone
                            )
                        }
                        when (result) {
                            is ActionResult.Success -> {
                                snackbarHostState.showSnackbar(result.message)
                                showCreateUser = false
                                refreshKey++
                            }
                            is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            )
        }

        userForStatusChange?.let { user ->
            AlertDialog(
                onDismissRequest = { userForStatusChange = null },
                title = {
                    Text(
                        if (user.active) stringResource(R.string.admin_deactivate_user)
                        else stringResource(R.string.admin_activate_user)
                    )
                },
                text = {
                    Text(
                        if (user.active) stringResource(R.string.admin_deactivate_user_warning, user.fullName.ifBlank { user.email })
                        else stringResource(R.string.admin_activate_user_warning, user.fullName.ifBlank { user.email })
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    repository.updateUserActiveStatus(user.id, !user.active)
                                }
                                when (result) {
                                    is ActionResult.Success -> {
                                        snackbarHostState.showSnackbar(result.message)
                                        userForStatusChange = null
                                        refreshKey++
                                    }
                                    is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        }
                    ) {
                        Text(
                            if (user.active) stringResource(R.string.admin_deactivate_user)
                            else stringResource(R.string.admin_activate_user),
                            color = if (user.active) ColoresFit.Rojo else MaterialTheme.colorScheme.primary
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userForStatusChange = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        classForEdit?.let { targetClass ->
            CreateClassDialog(
                classItem = targetClass,
                onDismiss = { classForEdit = null },
                onSave = { name, description, imageUri ->
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            repository.updateAdminClass(targetClass.id, name, description, imageUri)
                        }
                        when (result) {
                            is ActionResult.Success -> {
                                snackbarHostState.showSnackbar(result.message)
                                classForEdit = null
                                refreshKey++
                            }
                            is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            )
        }

        classForDelete?.let { targetClass ->
            AlertDialog(
                onDismissRequest = { classForDelete = null },
                title = { Text(stringResource(R.string.admin_delete_class)) },
                text = {
                    Text(
                        stringResource(
                            R.string.admin_delete_class_warning,
                            targetClass.name,
                            targetClass.schedulesCount,
                            targetClass.reservationsCount
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    repository.deleteAdminClass(targetClass.id)
                                }
                                when (result) {
                                    is ActionResult.Success -> {
                                        snackbarHostState.showSnackbar(result.message)
                                        classForDelete = null
                                        refreshKey++
                                    }
                                    is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        }
                    ) {
                        Text(stringResource(R.string.delete), color = ColoresFit.Rojo)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { classForDelete = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        classForSchedule?.let { targetClass ->
            CreateScheduleDialog(
                classItem = targetClass,
                rooms = rooms,
                monitors = monitors,
                onDismiss = { classForSchedule = null },
                onCreate = { classId, date, startTime, durationMinutes, monitorId, roomId, totalSlots ->
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            repository.createAdminSchedule(classId, date, startTime, durationMinutes, monitorId, roomId, totalSlots)
                        }
                        when (result) {
                            is ActionResult.Success -> {
                                snackbarHostState.showSnackbar(result.message)
                                classForSchedule = null
                                refreshKey++
                            }
                            is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            )
        }

        scheduleForManagement?.let { schedule ->
            ManageScheduleDialog(
                schedule = schedule,
                rooms = rooms,
                monitors = monitors,
                onDismiss = { scheduleForManagement = null },
                onUpdate = { date, startTime, durationMinutes, totalSlots, monitorId, roomId ->
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            repository.updateAdminSchedule(schedule.id, date, startTime, durationMinutes, totalSlots, monitorId, roomId)
                        }
                        when (result) {
                            is ActionResult.Success -> {
                                snackbarHostState.showSnackbar(result.message)
                                scheduleForManagement = null
                                refreshKey++
                            }
                            is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                        }
                    }
                },
                onDelete = {
                    scope.launch {
                        val result = withContext(Dispatchers.IO) { repository.deleteAdminSchedule(schedule.id) }
                        when (result) {
                            is ActionResult.Success -> {
                                snackbarHostState.showSnackbar(result.message)
                                scheduleForManagement = null
                                refreshKey++
                            }
                            is ActionResult.Error -> snackbarHostState.showSnackbar(result.message)
                        }
                    }
                }
            )
        }

        userForNotification?.let { user ->
            CreateNotificationDialog(
                user = user,
                onDismiss = { userForNotification = null },
                onCreate = { title, message ->
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            repository.createManualNotification(user.id, title, message)
                        }
                        when (result) {
                            is ActionResult.Success -> {
                                snackbarHostState.showSnackbar(result.message)
                                userForNotification = null
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

        AdminDashboardBlock(
            title = stringResource(R.string.admin_dashboard_notifications),
            subtitle = stringResource(R.string.admin_dashboard_notifications_subtitle)
        ) {
            dashboardData.notifications.forEach { notification ->
                AdminDashboardNotificationRow(notification)
            }
        }

        AdminDashboardBlock(
            title = stringResource(R.string.admin_recent_users_title),
            subtitle = stringResource(R.string.admin_recent_users_subtitle, dashboardData.todayReservations)
        ) {
            dashboardData.recentUsers.forEach { user ->
                AdminUserRow(user = user, onToggleActive = null)
            }
        }
    }
}

@Composable
private fun AdminDashboardBlock(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

@Composable
private fun AdminDashboardNotificationRow(notification: com.example.fitgymkt.model.ui.AdminDashboardNotification) {
    val color = when (notification.type) {
        "booking" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "class" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "subscription" -> Color(0xFFFDE2E2) to Color(0xFF991B1B)
        else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(color.first, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = color.second, modifier = Modifier.size(19.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(notification.title, fontWeight = FontWeight.Bold)
            Text(notification.message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
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
    onCreateUser: () -> Unit,
    onToggleActive: (AdminUserItem) -> Unit,
    onCreateNotification: (AdminUserItem) -> Unit
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
                Button(
                    onClick = onCreateUser,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.admin_create_user))
                }
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
            AdminUserRow(
                user = user,
                onToggleActive = { onToggleActive(user) },
                onCreateNotification = { onCreateNotification(user) }
            )
        }
    }
}

@Composable
private fun AdminManagementContent(
    classes: List<AdminClassItem>,
    schedules: List<AdminScheduleItem>,
    onCreateClass: () -> Unit,
    onEditClass: (AdminClassItem) -> Unit,
    onDeleteClass: (AdminClassItem) -> Unit,
    onCreateSchedule: (AdminClassItem) -> Unit,
    onScheduleClick: (AdminScheduleItem) -> Unit
) {
    val listState = rememberLazyListState()
    var classQuery by remember { mutableStateOf("") }
    var scheduleFilter by remember { mutableStateOf(AdminScheduleFilter.FUTURE) }
    val today = remember { adminTodayDate() }
    val filteredClasses = remember(classes, classQuery) {
        val normalized = classQuery.trim()
        if (normalized.isBlank()) classes
        else classes.filter { it.name.contains(normalized, ignoreCase = true) }
    }
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
                    text = stringResource(R.string.admin_management_classes_schedule_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onCreateClass,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.admin_create_class))
                }
                OutlinedTextField(
                    value = classQuery,
                    onValueChange = { classQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text(stringResource(R.string.admin_filter_classes_placeholder)) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { scheduleFilter = AdminScheduleFilter.ALL },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (scheduleFilter == AdminScheduleFilter.ALL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (scheduleFilter == AdminScheduleFilter.ALL) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(stringResource(R.string.admin_schedule_filter_all))
                    }
                    Button(
                        onClick = { scheduleFilter = AdminScheduleFilter.FUTURE },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (scheduleFilter == AdminScheduleFilter.FUTURE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (scheduleFilter == AdminScheduleFilter.FUTURE) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(stringResource(R.string.admin_schedule_filter_future))
                    }
                }
            }
        }
        items(filteredClasses, key = { "class_${it.id}" }) { classItem ->
            val classSchedules = schedules.filter {
                it.className == classItem.name && (scheduleFilter == AdminScheduleFilter.ALL || it.date >= today)
            }
            AdminClassScheduleCard(
                classItem = classItem,
                schedules = classSchedules,
                onEditClass = { onEditClass(classItem) },
                onDeleteClass = { onDeleteClass(classItem) },
                onCreateSchedule = { onCreateSchedule(classItem) },
                onScheduleClick = onScheduleClick
            )
        }
    }
}

@Composable
private fun AdminClassScheduleCard(
    classItem: AdminClassItem,
    schedules: List<AdminScheduleItem>,
    onEditClass: () -> Unit,
    onDeleteClass: () -> Unit,
    onCreateSchedule: () -> Unit,
    onScheduleClick: (AdminScheduleItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                    if (classItem.imageUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = classItem.imageUrl,
                            contentDescription = classItem.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(classItem.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(
                        classItem.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.admin_view_schedules, schedules.size),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            if (expanded && schedules.isEmpty()) {
                Text(stringResource(R.string.admin_no_schedules_for_class), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (expanded) {
                schedules.forEach { schedule ->
                    AdminScheduleCompactRow(schedule, onClick = { onScheduleClick(schedule) })
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TextButton(onClick = onEditClass, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.edit))
                }
                TextButton(onClick = onDeleteClass, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ColoresFit.Rojo, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.delete), color = ColoresFit.Rojo)
                }
                TextButton(onClick = onCreateSchedule, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.admin_create_schedule_short))
                }
            }
        }
    }
}

private fun adminMinimumScheduleDate(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -3) }
    return formatter.format(calendar.time)
}

private fun adminTodayDate(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)

private fun adminDefaultStartTime(): String {
    val calendar = Calendar.getInstance().apply {
        add(Calendar.HOUR_OF_DAY, 1)
        set(Calendar.MINUTE, 0)
    }
    return SimpleDateFormat("HH:mm", Locale.US).format(calendar.time)
}

@Composable
private fun AdminScheduleCompactRow(schedule: AdminScheduleItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(19.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                stringResource(R.string.admin_schedule_datetime_value, schedule.date, schedule.startTime, schedule.endTime),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(R.string.admin_schedule_room_monitor_value, schedule.roomName, schedule.monitorName),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        StatusPill(
            text = stringResource(R.string.admin_schedule_slots_value, schedule.reservedSlots, schedule.totalSlots),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), modifier = Modifier.padding(start = 8.dp).size(18.dp))
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
private fun AdminPickerField(
    label: String,
    value: String,
    options: List<Pair<Int, String>>,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.no_items_yet)) },
                    onClick = { expanded = false }
                )
            } else {
                options.forEach { (id, text) ->
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = {
                            onSelected(id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ManageScheduleDialog(
    schedule: AdminScheduleItem,
    rooms: List<AdminRoomItem>,
    monitors: List<AdminMonitorItem>,
    onDismiss: () -> Unit,
    onUpdate: (String, String, Int, Int, Int, Int) -> Unit,
    onDelete: () -> Unit
) {
    var date by remember(schedule.id) { mutableStateOf(schedule.date) }
    var startTime by remember(schedule.id) { mutableStateOf(schedule.startTime.take(5)) }
    var durationMinutes by remember(schedule.id) { mutableStateOf(adminScheduleDurationMinutes(schedule.startTime, schedule.endTime).toString()) }
    var totalSlots by remember(schedule.id) { mutableStateOf(schedule.totalSlots.toString()) }
    var selectedRoomId by remember(schedule.id) { mutableStateOf(schedule.roomId) }
    var selectedMonitorId by remember(schedule.id) { mutableStateOf(schedule.monitorId) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.admin_delete_schedule)) },
            text = { Text(stringResource(R.string.admin_delete_schedule_warning, schedule.className, schedule.date, schedule.startTime)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    }
                ) {
                    Text(stringResource(R.string.delete), color = ColoresFit.Rojo)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    FitGymDialogPanel(onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.admin_manage_schedule), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text(schedule.className, color = MaterialTheme.colorScheme.onSurfaceVariant)

            AdminPickerField(
                label = stringResource(R.string.admin_schedule_room),
                value = rooms.firstOrNull { it.id == selectedRoomId }?.let { "${it.name} (${it.capacity})" }
                    ?: stringResource(R.string.admin_select_room),
                options = rooms.map { it.id to "${it.name} (${it.capacity})" },
                onSelected = { selectedRoomId = it }
            )
            AdminPickerField(
                label = stringResource(R.string.admin_schedule_monitor),
                value = monitors.firstOrNull { it.id == selectedMonitorId }?.let {
                    listOf(it.fullName, it.specialty).filter(String::isNotBlank).joinToString(" · ")
                } ?: stringResource(R.string.admin_select_monitor),
                options = monitors.map {
                    it.id to listOf(it.fullName, it.specialty).filter(String::isNotBlank).joinToString(" · ")
                },
                onSelected = { selectedMonitorId = it }
            )
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.admin_schedule_date)) }
            )
            OutlinedTextField(
                value = startTime,
                onValueChange = { startTime = it.take(5) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.admin_schedule_start_time)) }
            )
            OutlinedTextField(
                value = durationMinutes,
                onValueChange = { durationMinutes = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.admin_schedule_duration)) }
            )
            OutlinedTextField(
                value = totalSlots,
                onValueChange = { totalSlots = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.admin_schedule_total_slots)) }
            )
            Text(
                text = stringResource(R.string.admin_schedule_reserved_slots_hint, schedule.reservedSlots),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        onUpdate(
                            date,
                            startTime,
                            durationMinutes.toIntOrNull() ?: 0,
                            totalSlots.toIntOrNull() ?: 0,
                            selectedMonitorId,
                            selectedRoomId
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text(stringResource(R.string.save))
                }
            }

            TextButton(onClick = { showDeleteConfirmation = true }, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = ColoresFit.Rojo, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.admin_delete_schedule), color = ColoresFit.Rojo)
            }
        }
    }
}

@Composable
private fun CreateScheduleDialog(
    classItem: AdminClassItem,
    rooms: List<AdminRoomItem>,
    monitors: List<AdminMonitorItem>,
    onDismiss: () -> Unit,
    onCreate: (Int, String, String, Int, Int, Int, Int) -> Unit
) {
    var date by remember { mutableStateOf(adminTodayDate()) }
    var startTime by remember { mutableStateOf(adminDefaultStartTime()) }
    var durationMinutes by remember { mutableStateOf("60") }
    var selectedRoomId by remember(rooms) { mutableStateOf(rooms.firstOrNull()?.id ?: 0) }
    var selectedMonitorId by remember(monitors) { mutableStateOf(monitors.firstOrNull()?.id ?: 0) }
    var totalSlots by remember(rooms) { mutableStateOf((rooms.firstOrNull()?.capacity ?: 20).toString()) }

    FitGymDialogPanel(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.admin_create_schedule), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = classItem.name,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    label = { Text(stringResource(R.string.admin_schedule_class)) }
                )
                AdminPickerField(
                    label = stringResource(R.string.admin_schedule_room),
                    value = rooms.firstOrNull { it.id == selectedRoomId }?.let { "${it.name} (${it.capacity})" }
                        ?: stringResource(R.string.admin_select_room),
                    options = rooms.map { it.id to "${it.name} (${it.capacity})" },
                    onSelected = { roomId ->
                        selectedRoomId = roomId
                        rooms.firstOrNull { it.id == roomId }?.let { totalSlots = it.capacity.toString() }
                    }
                )
                AdminPickerField(
                    label = stringResource(R.string.admin_schedule_monitor),
                    value = monitors.firstOrNull { it.id == selectedMonitorId }?.let {
                        listOf(it.fullName, it.specialty).filter(String::isNotBlank).joinToString(" · ")
                    } ?: stringResource(R.string.admin_select_monitor),
                    options = monitors.map {
                        it.id to listOf(it.fullName, it.specialty).filter(String::isNotBlank).joinToString(" · ")
                    },
                    onSelected = { selectedMonitorId = it }
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.admin_schedule_date)) },
                    placeholder = { Text(adminTodayDate()) }
                )
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.admin_schedule_start_time)) },
                    placeholder = { Text(adminDefaultStartTime()) }
                )
                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.admin_schedule_duration)) },
                    placeholder = { Text("60") }
                )
                OutlinedTextField(
                    value = totalSlots,
                    onValueChange = { totalSlots = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.admin_schedule_total_slots)) }
                )
                Text(
                    text = stringResource(R.string.admin_schedule_defaults_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            onCreate(
                                classItem.id,
                                date,
                                startTime,
                                durationMinutes.toIntOrNull() ?: 0,
                                selectedMonitorId,
                                selectedRoomId,
                                totalSlots.toIntOrNull() ?: 0
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
    }
}

@Composable
private fun CreateClassDialog(
    classItem: AdminClassItem? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, Uri?) -> Unit
) {
    var name by remember(classItem?.id) { mutableStateOf(classItem?.name.orEmpty()) }
    var description by remember(classItem?.id) { mutableStateOf(classItem?.description.orEmpty()) }
    var imageUri by remember(classItem?.id) { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    FitGymDialogPanel(onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(if (classItem == null) R.string.admin_create_class else R.string.admin_edit_class),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(stringResource(R.string.admin_class_dialog_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }

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
                minLines = 3,
                label = { Text(stringResource(R.string.admin_class_description)) }
            )
            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    if (imageUri == null && classItem?.imageUrl.isNullOrBlank()) stringResource(R.string.admin_pick_image)
                    else stringResource(R.string.admin_change_image)
                )
            }
            val preview = imageUri ?: classItem?.imageUrl?.takeIf { it.isNotBlank() }?.let(Uri::parse)
            preview?.let {
                SubcomposeAsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(160.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp)),
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) { Text(stringResource(R.string.cancel)) }
                Button(
                    onClick = { onSave(name, description, imageUri) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                ) { Text(stringResource(R.string.save)) }
            }
        }
    }
}

private fun adminScheduleDurationMinutes(startTime: String, endTime: String): Int {
    val parser = SimpleDateFormat("HH:mm", Locale.US)
    val start = parser.parse(startTime.take(5)) ?: return 60
    val end = parser.parse(endTime.take(5)) ?: return 60
    val diff = end.time - start.time
    val minutes = (diff / 60000).toInt()
    return if (minutes > 0) minutes else minutes + (24 * 60)
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
    onToggleActive: (() -> Unit)?,
    onCreateNotification: (() -> Unit)? = null
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

            if (onCreateNotification != null || onToggleActive != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    onCreateNotification?.let {
                        Button(
                            onClick = it,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.admin_send_user_notification))
                        }
                    }
                    onToggleActive?.let {
                        Button(
                            onClick = it,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.active) Color(0xFFFDE2E2) else Color(0xFFDDF7E8),
                                contentColor = if (user.active) Color(0xFF991B1B) else Color(0xFF166534)
                            )
                        ) {
                            Text(
                                if (user.active) stringResource(R.string.admin_deactivate_user)
                                else stringResource(R.string.admin_activate_user)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class AdminUserForm(
    val nombre: String,
    val apellidos: String,
    val email: String,
    val password: String,
    val role: String,
    val active: Boolean,
    val phone: String,
    val age: Int,
    val weightKg: Double,
    val heightCm: Double,
    val notificationsEnabled: Boolean,
    val language: String,
    val monitorSpecialty: String,
    val monitorPhone: String
)

@Composable
private fun CreateUserDialog(
    onDismiss: () -> Unit,
    onCreate: (AdminUserForm) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("cliente") }
    var active by remember { mutableStateOf(true) }
    var phone by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf("") }
    var heightCm by remember { mutableStateOf("") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var language by remember { mutableStateOf("ES") }
    var monitorSpecialty by remember { mutableStateOf("") }
    var monitorPhone by remember { mutableStateOf("") }
    val roles = listOf("cliente", "admin", "monitor")
    val languages = listOf("ES", "EN", "DE", "PT")

    FitGymDialogPanel(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.admin_create_user), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.first_name)) })
            OutlinedTextField(value = apellidos, onValueChange = { apellidos = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.last_name)) })
            OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.email)) })
            OutlinedTextField(value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.password)) })
            AdminPickerField(
                label = stringResource(R.string.admin_role),
                value = role.uppercase(),
                options = roles.mapIndexed { index, value -> index to value.uppercase() },
                onSelected = { role = roles[it] }
            )
            OutlinedTextField(value = phone, onValueChange = { phone = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.phone)) })

            if (role == "cliente") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = age, onValueChange = { age = it.filter(Char::isDigit) }, modifier = Modifier.weight(1f), singleLine = true, label = { Text(stringResource(R.string.profile_age)) })
                    OutlinedTextField(value = weightKg, onValueChange = { weightKg = it.filter { c -> c.isDigit() || c == '.' } }, modifier = Modifier.weight(1f), singleLine = true, label = { Text(stringResource(R.string.profile_weight)) })
                    OutlinedTextField(value = heightCm, onValueChange = { heightCm = it.filter { c -> c.isDigit() || c == '.' } }, modifier = Modifier.weight(1f), singleLine = true, label = { Text(stringResource(R.string.profile_height)) })
                }
            }

            if (role == "monitor") {
                OutlinedTextField(value = monitorSpecialty, onValueChange = { monitorSpecialty = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.admin_monitor_specialty)) })
                OutlinedTextField(value = monitorPhone, onValueChange = { monitorPhone = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.admin_monitor_phone)) })
            }

            AdminPickerField(
                label = stringResource(R.string.language),
                value = language,
                options = languages.mapIndexed { index, value -> index to value },
                onSelected = { language = languages[it] }
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.admin_user_active))
                Switch(checked = active, onCheckedChange = { active = it })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.notifications))
                Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) { Text(stringResource(R.string.cancel)) }
                Button(
                    onClick = {
                        onCreate(
                            AdminUserForm(
                                nombre = nombre,
                                apellidos = apellidos,
                                email = email,
                                password = password,
                                role = role,
                                active = active,
                                phone = phone,
                                age = age.toIntOrNull() ?: 0,
                                weightKg = weightKg.toDoubleOrNull() ?: 0.0,
                                heightCm = heightCm.toDoubleOrNull() ?: 0.0,
                                notificationsEnabled = notificationsEnabled,
                                language = language,
                                monitorSpecialty = monitorSpecialty,
                                monitorPhone = monitorPhone
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                ) { Text(stringResource(R.string.save)) }
            }
        }
    }
}

@Composable
private fun CreateNotificationDialog(
    user: AdminUserItem,
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var title by remember(user.id) { mutableStateOf("") }
    var message by remember(user.id) { mutableStateOf("") }

    FitGymDialogPanel(onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.admin_create_notification),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = user.fullName.ifBlank { user.email },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.admin_notification_title)) }
            )
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text(stringResource(R.string.admin_notification_message)) }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = { onCreate(title, message) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.admin_send_notification))
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
