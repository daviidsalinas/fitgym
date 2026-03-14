package com.example.fitgymkt.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.fitgymkt.R
import com.example.fitgymkt.repository.FitGymRepository
import com.example.fitgymkt.repository.RegisterResult
import com.example.fitgymkt.ui.theme.ColoresFit
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(ColoresFit.Blanco)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = ColoresFit.Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        Modifier.clickable { if (!cargando) alVolverAlLogin() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(" ${stringResource(R.string.back_to_login)}", fontSize = 13.sp, color = ColoresFit.GrisTexto)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.register_new_account),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        stringResource(R.string.register_subtitle),
                        color = ColoresFit.GrisTexto,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    val campos = listOf(
                        Triple(nombre, stringResource(R.string.full_name), Icons.Default.Person) to { s: String -> nombre = s },
                        Triple(correo, stringResource(R.string.email), Icons.Default.Email) to { s: String -> correo = s },
                        Triple(telefono, stringResource(R.string.phone), Icons.Default.Phone) to { s: String -> telefono = s }
                    )

                    campos.forEach { (datos, setter) ->
                        OutlinedTextField(
                            value = datos.first,
                            onValueChange = setter,
                            placeholder = { Text(datos.second) },
                            leadingIcon = { Icon(datos.third, contentDescription = null, tint = ColoresFit.AzulFit) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = ColoresFit.FondoCampo,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true,
                            enabled = !cargando
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        placeholder = { Text(stringResource(R.string.password)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ColoresFit.AzulFit) },
                        trailingIcon = {
                            IconButton(onClick = { mostrarPass = !mostrarPass }) {
                                Icon(
                                    imageVector = if (mostrarPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (mostrarPass) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                                )
                            }
                        },
                        visualTransformation = if (mostrarPass) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = ColoresFit.FondoCampo,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        enabled = !cargando
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = passConfirm,
                        onValueChange = { passConfirm = it },
                        placeholder = { Text(stringResource(R.string.confirm_password)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ColoresFit.AzulFit) },
                        trailingIcon = {
                            IconButton(onClick = { mostrarPassConfirm = !mostrarPassConfirm }) {
                                Icon(
                                    imageVector = if (mostrarPassConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (mostrarPassConfirm) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                                )
                            }
                        },
                        visualTransformation = if (mostrarPassConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = ColoresFit.FondoCampo,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        enabled = !cargando
                    )

                    Spacer(modifier = Modifier.height(24.dp))

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

                                    is RegisterResult.Error -> {
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColoresFit.Negro),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !cargando
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.create_account))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.register_terms),
                        fontSize = 11.sp,
                        color = ColoresFit.GrisTexto,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }


            }
        }
    }
}
