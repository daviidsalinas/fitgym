package com.example.fitgymkt.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.delay

/**
 * PantallaSplash
 *
 * Pantalla de carga inicial de FitGym.
 * Secuencia de animación:
 *   1. El logo (icono + nombre) aparece con fade-in + scale desde el centro.
 *   2. El tagline sube con fade-in con un ligero retraso.
 *   3. Los tres puntos de carga pulsan en loop mientras se inicializa la app.
 *   4. Tras [delayMs] ms (por defecto 2.5s), se llama a [alTerminar].
 *
 * @param alTerminar  Lambda que se ejecuta cuando termina el splash.
 *                    Navega a Login o a Inicio según el estado de sesión
 *                    (esa lógica la gestiona NavegacionPrincipal, no este composable).
 * @param delayMs     Duración total del splash en milisegundos. Por defecto 2500.
 */
@Composable
fun PantallaSplash(
    alTerminar: () -> Unit,
    delayMs: Long = 2500L
) {
    // ── Dispara la navegación una sola vez tras el delay ──────────────────────
    LaunchedEffect(Unit) {
        delay(delayMs)
        alTerminar()
    }

    // ── Animación del logo: scale + alpha ─────────────────────────────────────
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    // Controla la visibilidad del logo y tagline de forma escalonada
    var logoVisible by remember { mutableStateOf(false) }
    var taglineVisible by remember { mutableStateOf(false) }
    var dotsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        logoVisible = true
        delay(400)
        taglineVisible = true
        delay(300)
        dotsVisible = true
    }

    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = EaseOutCubic),
        label = "logoAlpha"
    )

    val logoScaleAnim by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "logoScaleAnim"
    )

    val taglineAlpha by animateFloatAsState(
        targetValue = if (taglineVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = EaseOutCubic),
        label = "taglineAlpha"
    )

    val taglineOffset by animateFloatAsState(
        targetValue = if (taglineVisible) 0f else 16f,
        animationSpec = tween(durationMillis = 500, easing = EaseOutCubic),
        label = "taglineOffset"
    )

    // ── Fondo: gradiente oscuro coherente con el tema de la app ───────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A),
                        Color(0xFF1A1A2E),
                        Color(0xFF0A0A0A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Círculo decorativo difuso de fondo (da profundidad)
        Box(
            modifier = Modifier
                .size(340.dp)
                .alpha(0.08f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF3B82F6), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(50)
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Icono + nombre ────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(logoAlpha)
                    .scale(logoScaleAnim)
            ) {
                // Icono con fondo redondeado, igual que en PantallaLogin
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(Color.White, shape = RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Logo FitGym",
                        tint = Color(0xFF0A0A0A),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Nombre de la app, mismo estilo que PantallaLogin
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 40.sp,
                                color = Color.White
                            )
                        ) { append("FIT") }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Light,
                                fontSize = 40.sp,
                                color = Color(0xFF3B82F6)
                            )
                        ) { append("GYM") }
                    }
                )
            }

            // ── Tagline ───────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tu Entrenamiento, Tu Ritmo",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .alpha(taglineAlpha)
                    .offset(y = taglineOffset.dp)
            )

            // ── Indicador de carga (tres puntos) ──────────────────────────────
            Spacer(modifier = Modifier.height(64.dp))

            if (dotsVisible) {
                LoadingDots()
            }
        }
    }
}

/**
 * Tres puntos que pulsan en cascada para indicar carga.
 * Usa InfiniteTransition para un loop suave y eficiente.
 */
@Composable
private fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    // Cada punto tiene un delay diferente para efecto cascada
    val scales = (0..2).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1200
                    0.4f at 0 using EaseInOut
                    1f at 200 using EaseInOut
                    0.4f at 600 using EaseInOut
                    0.4f at 1200 using EaseInOut
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(index * 200)
            ),
            label = "dotScale_$index"
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        scales.forEach { scale ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale.value)
                    .background(Color(0xFF3B82F6), RoundedCornerShape(50))
            )
        }
    }
}