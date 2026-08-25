package com.example.comunicame.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.comunicame.data.RepositorioUsuarios
import com.example.comunicame.data.Usuario
import com.example.comunicame.ui.components.MensajeEstado
import com.example.comunicame.ui.components.PatronVibracion
import com.example.comunicame.ui.components.TipoMensaje
import com.example.comunicame.ui.components.vibrar
import com.example.comunicame.ui.theme.ComunicameTheme

// LOGIN. Pantalla de inicio, valida contra el arreglo de RepositorioUsuarios.
// Fondo blanco y el lila solo de acento.
// El resultado nunca se avisa con sonido: banner visual + vibracion.
@Composable
fun LoginScreen(
    onLoginExitoso: (Usuario) -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit
) {
    // Estado. remember guarda el valor entre recomposiciones y mutableStateOf
    // hace que Compose lo observe.
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var verContrasena by remember { mutableStateOf(false) }

    // null = no mostrar banner
    var mensaje by remember { mutableStateOf<Pair<String, TipoMensaje>?>(null) }

    val context = LocalContext.current

    // 12dp, apenas redondeado como los formularios de Google. Lo declaro una
    // vez para que todos los campos queden iguales.
    val formaCampo = RoundedCornerShape(12.dp)

    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    // La saco del onClick para que el boton se lea limpio
    fun intentarIngresar() {
        val ingresado = usuario.trim()

        if (ingresado.isEmpty() || contrasena.isEmpty()) {
            mensaje = "Completa tu usuario y tu contraseña" to TipoMensaje.AVISO
            vibrar(context, PatronVibracion.TOQUE)
            return
        }

        val encontrado = RepositorioUsuarios.validarCredenciales(ingresado, contrasena)

        if (encontrado == null) {
            mensaje = "Usuario o contraseña incorrectos" to TipoMensaje.ERROR
            vibrar(context, PatronVibracion.ERROR)
        } else {
            vibrar(context, PatronVibracion.EXITO)
            onLoginExitoso(encontrado)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Sin esto el teclado tapa los campos de abajo en pantallas chicas
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        // TODO: por ahora es un icono de stock de Material, despues le hago uno
        // propio bonito con la identidad de la app
        Icon(
            imageVector = Icons.Filled.RecordVoiceOver,
            contentDescription = null, // decorativo, el titulo de abajo ya lo dice
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Comunícame",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Tu voz, en tus palabras",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = {
                usuario = it
                mensaje = null // si esta corrigiendo, saco el error de antes
            },
            label = { Text("Usuario") },
            singleLine = true,
            shape = formaCampo,
            colors = coloresCampo,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = {
                contrasena = it
                mensaje = null
            },
            label = { Text("Contraseña") },
            singleLine = true,
            shape = formaCampo,
            colors = coloresCampo,
            // Tapa el texto salvo que pida verlo
            visualTransformation = if (verContrasena) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { verContrasena = !verContrasena }) {
                    Icon(
                        imageVector = if (verContrasena) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        // Cambia con el estado. Si el boton hace dos cosas no
                        // puede anunciarse igual siempre en el lector.
                        contentDescription = if (verContrasena) {
                            "Ocultar contraseña"
                        } else {
                            "Mostrar contraseña"
                        },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Va entre los campos y el boton, que es donde uno mira despues de
        // apretar
        mensaje?.let { (texto, tipo) ->
            Spacer(modifier = Modifier.height(20.dp))
            MensajeEstado(texto = texto, tipo = tipo)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { intentarIngresar() },
            shape = formaCampo,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Iniciar sesión",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onIrARecuperar) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Separador. Sin esto los dos botones se leen como si fueran lo mismo.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "o",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onIrARegistro,
            shape = formaCampo,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Crear una cuenta",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginScreenPreview() {
    ComunicameTheme {
        LoginScreen(onLoginExitoso = {}, onIrARegistro = {}, onIrARecuperar = {})
    }
}
