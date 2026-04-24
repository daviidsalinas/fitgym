package com.example.fitgymkt.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.fitgymkt.R
import com.example.fitgymkt.repository.FitGymRepository
import com.example.fitgymkt.repository.LoginResult
import com.example.fitgymkt.ui.theme.ColoresFit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PantallaLogin(
    alIrARegistro: () -> Unit,
    alEntrarApp: (Int, String, String) -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val repository = remember(context) { FitGymRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(ColoresFit.Negro, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = stringResource(R.string.logo_fitgym),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.tagline),
                style = MaterialTheme.typography.bodySmall,
                color = ColoresFit.GrisTexto
            )

            Spacer(modifier = Modifier.height(28.dp))

            FitGymPanel(
                modifier = Modifier.fillMaxWidth(),
                bordered = true
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.login_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.login_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColoresFit.GrisTexto
                    )
                    Spacer(modifier = Modifier.height(22.dp))

                    FitGymLoginField(
                        value = correo,
                        onValueChange = { correo = it },
                        placeholder = stringResource(R.string.email),
                        leadingIcon = Icons.Default.Email,
                        enabled = !cargando
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    FitGymLoginField(
                        value = contrasena,
                        onValueChange = { contrasena = it },
                        placeholder = stringResource(R.string.password),
                        leadingIcon = Icons.Default.Lock,
                        enabled = !cargando,
                        visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                                Icon(
                                    imageVector = if (mostrarPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (mostrarPassword) {
                                        stringResource(R.string.hide_password)
                                    } else {
                                        stringResource(R.string.show_password)
                                    },
                                    tint = ColoresFit.GrisTexto
                                )
                            }
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { }) {
                            Text(
                                text = stringResource(R.string.forgot_password),
                                color = ColoresFit.GrisTexto
                            )
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                cargando = true
                                val result = withContext(Dispatchers.IO) {
                                    repository.login(correo, contrasena)
                                }
                                cargando = false

                                when (result) {
                                    is LoginResult.Success -> alEntrarApp(result.userId, result.userName, result.role)
                                    is LoginResult.Error -> snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColoresFit.Negro),
                        shape = RoundedCornerShape(20.dp),
                        enabled = !cargando
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.login_title),
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
            Row {
                Text(
                    text = "${stringResource(R.string.no_account)} ",
                    color = ColoresFit.GrisTexto,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.register_here),
                    color = ColoresFit.Naranja,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = !cargando) { alIrARegistro() }
                )
            }
        }
    }
}

@Composable
fun FitGymLoginField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        singleLine = true,
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(leadingIcon, contentDescription = null, tint = ColoresFit.AzulFit, modifier = Modifier.size(18.dp))
            }
        },
        trailingIcon = trailingIcon,
        placeholder = { Text(placeholder) },
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = ColoresFit.Negro,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = ColoresFit.GrisTexto,
            unfocusedPlaceholderColor = ColoresFit.GrisTexto
        ),
        enabled = enabled
    )
}
