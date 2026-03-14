package com.example.fitgymkt.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitgymkt.repository.FitGymRepository
import com.example.fitgymkt.repository.LoginResult
import androidx.compose.ui.res.stringResource
import com.example.fitgymkt.R
import com.example.fitgymkt.ui.theme.ColoresFit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PantallaLogin(
    alIrARegistro: () -> Unit,
    alEntrarApp: (Int, String) -> Unit
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColoresFit.Blanco)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(ColoresFit.Negro, shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ){
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = stringResource(R.string.logo_fitgym),
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }


            Spacer(modifier = Modifier.height(12.dp))

            Text(text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp)) { append("FIT") }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Light, color = ColoresFit.AzulFit, fontSize = 28.sp)) { append("GYM") }
            })

            Text(text = stringResource(R.string.tagline), color = ColoresFit.GrisTexto, fontSize = 12.sp)


            Spacer(modifier = Modifier.height(40.dp))

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = ColoresFit.Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.login_title), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.login_subtitle), color = ColoresFit.GrisTexto, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        placeholder = { Text(stringResource(R.string.email)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ColoresFit.AzulFit) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = ColoresFit.FondoCampo,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = ColoresFit.AzulFit
                        ),
                        enabled = !cargando
                    )


                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = { contrasena = it },
                        placeholder = { Text(stringResource(R.string.password)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ColoresFit.AzulFit) },
                        trailingIcon = {
                            IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                                Icon(
                                    imageVector = if (mostrarPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (mostrarPassword) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                                )
                            }
                        },
                        visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = ColoresFit.FondoCampo,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = ColoresFit.AzulFit
                        ),
                        enabled = !cargando
                    )


                    TextButton(onClick = { }) {
                        Text(stringResource(R.string.forgot_password), color = ColoresFit.GrisTexto, fontSize = 12.sp)
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
                                    is LoginResult.Success -> alEntrarApp(result.userId, result.userName)
                                    is LoginResult.Error -> snackbarHostState.showSnackbar(result.message)
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
                            Text(stringResource(R.string.login_title), color = Color.White)
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Text("${stringResource(R.string.no_account)} ", color = ColoresFit.GrisTexto)
                Text(
                    stringResource(R.string.register_here),
                    fontWeight = FontWeight.Bold,
                    color = ColoresFit.Negro,
                    modifier = Modifier.clickable { if (!cargando) alIrARegistro() }
                )
            }
        }
    }
}