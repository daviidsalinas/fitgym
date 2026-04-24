package com.example.fitgymkt.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitgymkt.R
import com.example.fitgymkt.repository.FitGymRepository
import com.example.fitgymkt.repository.RegisterResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PantallaRegistro(
    alVolverAlLogin: () -> Unit,
    alRegistroCompletado: (Int, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passConfirm by remember { mutableStateOf("") }
    var mostrarPass by remember { mutableStateOf(false) }
    var mostrarPassConfirm by remember { mutableStateOf(false) }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .clickable(enabled = !cargando) { alVolverAlLogin() }
                    .padding(bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
                Text(
                    text = stringResource(R.string.back_to_login),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Text(
                text = stringResource(R.string.register_new_account),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = com.example.fitgymkt.ui.theme.ColoresFit.GrisTexto
            )

            Spacer(modifier = Modifier.height(20.dp))

            FitGymPanel(
                modifier = Modifier.fillMaxWidth(),
                bordered = true
            ) {
                Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp)) {
                    FitGymLoginField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        placeholder = stringResource(R.string.full_name),
                        leadingIcon = Icons.Default.Person,
                        enabled = !cargando
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    FitGymLoginField(
                        value = correo,
                        onValueChange = { correo = it },
                        placeholder = stringResource(R.string.email),
                        leadingIcon = Icons.Default.Email,
                        enabled = !cargando
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    FitGymLoginField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        placeholder = stringResource(R.string.phone),
                        leadingIcon = Icons.Default.Phone,
                        enabled = !cargando
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    FitGymLoginField(
                        value = pass,
                        onValueChange = { pass = it },
                        placeholder = stringResource(R.string.password),
                        leadingIcon = Icons.Default.Lock,
                        enabled = !cargando,
                        visualTransformation = if (mostrarPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { mostrarPass = !mostrarPass }) {
                                Icon(
                                    imageVector = if (mostrarPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    FitGymLoginField(
                        value = passConfirm,
                        onValueChange = { passConfirm = it },
                        placeholder = stringResource(R.string.confirm_password),
                        leadingIcon = Icons.Default.Lock,
                        enabled = !cargando,
                        visualTransformation = if (mostrarPassConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { mostrarPassConfirm = !mostrarPassConfirm }) {
                                Icon(
                                    imageVector = if (mostrarPassConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Button(
                        onClick = {
                            if (pass != passConfirm) {
                                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.passwords_do_not_match)) }
                                return@Button
                            }

                            scope.launch {
                                cargando = true
                                val result = withContext(Dispatchers.IO) {
                                    repository.register(
                                        nombreCompleto = nombre,
                                        email = correo,
                                        telefono = telefono,
                                        password = pass
                                    )
                                }
                                cargando = false
                                when (result) {
                                    is RegisterResult.Success -> {
                                        snackbarHostState.showSnackbar(context.getString(R.string.account_created))
                                        alRegistroCompletado(result.userId, result.userName)
                                    }

                                    is RegisterResult.Error -> snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.fitgymkt.ui.theme.ColoresFit.Negro),
                        shape = RoundedCornerShape(20.dp),
                        enabled = !cargando
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.create_account), color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.register_terms),
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.fitgymkt.ui.theme.ColoresFit.GrisTexto,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
