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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitgymkt.ui.theme.ColoresFit

@Composable
fun PantallaRegistro(alVolverAlLogin: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passConfirm by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(ColoresFit.Blanco)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Podrías reutilizar la SeccionLogo aquí o poner el diseño manual
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = ColoresFit.Blanco),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Botón volver
                Row(
                    Modifier.clickable { alVolverAlLogin() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" Volver al inicio de sesión", fontSize = 13.sp, color = ColoresFit.GrisTexto)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Crear cuenta nueva", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text("Completa tus datos para comenzar", color = ColoresFit.GrisTexto, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(20.dp))

                // Campos de texto dinámicos
                val campos = listOf(
                    Triple(nombre, "Nombre completo", Icons.Default.Person) to { s: String -> nombre = s },
                    Triple(correo, "Email", Icons.Default.Email) to { s: String -> correo = s },
                    Triple(telefono, "Teléfono", Icons.Default.Phone) to { s: String -> telefono = s }
                )

                campos.forEach { (datos, setter) ->
                    OutlinedTextField(
                        value = datos.first,
                        onValueChange = setter,
                        placeholder = { Text(datos.second) },
                        leadingIcon = { Icon(datos.third, contentDescription = null, tint = ColoresFit.AzulFit) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = ColoresFit.FondoCampo, unfocusedBorderColor = Color.Transparent)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Campos de contraseña
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    placeholder = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ColoresFit.AzulFit) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = ColoresFit.FondoCampo, unfocusedBorderColor = Color.Transparent)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = passConfirm,
                    onValueChange = { passConfirm = it },
                    placeholder = { Text("Confirmar contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ColoresFit.AzulFit) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = ColoresFit.FondoCampo, unfocusedBorderColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { /* Lógica Registro */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColoresFit.Negro),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Crear Cuenta")
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Al registrarte, aceptas nuestros Términos y Privacidad",
                    fontSize = 11.sp,
                    color = ColoresFit.GrisTexto,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
