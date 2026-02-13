package com.example.fitgymkt.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitgymkt.ui.theme.ColoresFit

@Composable
fun PantallaLogin(
    alIrARegistro: () -> Unit,
    alEntrarApp: () -> Unit // <--- Añadimos este parámetro para la navegación al inicio
) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColoresFit.Blanco)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(ColoresFit.Negro, shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            // He habilitado el icono usando FitnessCenter (puedes cambiarlo por tu recurso local)
            //Icon(
               // imageVector = Icons.Default.FitnessCenter,
               // contentDescription = "Logo FitGym",
             //   tint = Color.White,
             //   modifier = Modifier.size(40.dp)
          //  )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp)) { append("FIT") }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Light, color = ColoresFit.AzulFit, fontSize = 28.sp)) { append("GYM") }
        })

        Text(text = "Tu Entrenamiento, Tu Ritmo", color = ColoresFit.GrisTexto, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(40.dp))

        // Tarjeta de Login
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
                Text("Iniciar Sesión", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Accede a tu cuenta", color = ColoresFit.GrisTexto, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    placeholder = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ColoresFit.AzulFit) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = ColoresFit.FondoCampo,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = ColoresFit.AzulFit
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    placeholder = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ColoresFit.AzulFit) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = ColoresFit.FondoCampo,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = ColoresFit.AzulFit
                    )
                )

                TextButton(onClick = { /* Lógica futura */ }) {
                    Text("¿Olvidaste tu contraseña?", color = ColoresFit.GrisTexto, fontSize = 12.sp)
                }

                Button(
                    onClick = { alEntrarApp() }, // <--- LLAMADA A LA NAVEGACIÓN
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColoresFit.Negro),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Iniciar Sesión", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row {
            Text("¿No tienes una cuenta? ", color = ColoresFit.GrisTexto)
            Text(
                "Regístrate aquí",
                fontWeight = FontWeight.Bold,
                color = ColoresFit.Negro,
                modifier = Modifier.clickable { alIrARegistro() }
            )
        }
    }
}