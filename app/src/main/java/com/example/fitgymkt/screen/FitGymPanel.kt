package com.example.fitgymkt.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fitgymkt.R
import com.example.fitgymkt.ui.theme.ColoresFit

enum class FitGymDestination {
    Home,
    Classes,
    Analysis,
    Profile
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitGymTopBar(
    title: String? = null,
    subtitle: String? = null,
    unreadCount: Int = 0,
    onMenuClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    onNotificationsClick: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 6.dp, bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick ?: onMenuClick ?: {},
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        imageVector = if (onBackClick != null) Icons.Default.ArrowBack else Icons.Default.Menu,
                        contentDescription = if (onBackClick != null) stringResource(R.string.back_to_login) else stringResource(R.string.menu)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                ) {
                    Box {
                        Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.notifications))
                        if (unreadCount > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-6).dp),
                                containerColor = ColoresFit.Naranja
                            ) {
                                Text(unreadCount.coerceAtMost(99).toString(), color = Color.White)
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title ?: stringResource(R.string.app_name),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FitGymBottomBar(
    current: FitGymDestination,
    onHomeClick: () -> Unit,
    onClassesClick: () -> Unit,
    onAnalysisClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        FitGymNavItem(
            selected = current == FitGymDestination.Home,
            icon = Icons.Default.Home,
            label = stringResource(R.string.nav_home),
            onClick = onHomeClick
        )
        FitGymNavItem(
            selected = current == FitGymDestination.Classes,
            icon = Icons.Default.DateRange,
            label = stringResource(R.string.nav_classes),
            onClick = onClassesClick
        )
        FitGymNavItem(
            selected = current == FitGymDestination.Analysis,
            icon = Icons.Default.BarChart,
            label = stringResource(R.string.nav_analysis),
            onClick = onAnalysisClick
        )
        FitGymNavItem(
            selected = current == FitGymDestination.Profile,
            icon = Icons.Default.Person,
            label = stringResource(R.string.nav_profile),
            onClick = onProfileClick
        )
    }
}

@Composable
private fun RowScope.FitGymNavItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val contentColor = if (selected) ColoresFit.Naranja else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (selected) ColoresFit.NaranjaSuave else Color.Transparent,
                    RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, color = contentColor, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
fun FitGymPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    bordered: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (bordered) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
        content = content
    )
}

@Composable
fun FitGymDialogPanel(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FitGymPanel(
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp),
            bordered = true
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                content = content
            )
        }
    }
}

@Composable
fun FitGymHeroPanel(
    modifier: Modifier = Modifier,
    accent: Color = ColoresFit.Naranja,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = ColoresFit.NegroSuave),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            content = content
        )
    }
}

@Composable
fun FitGymSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = ColoresFit.GrisTexto,
                fontSize = 14.sp
            )
        }
    }
}
