package com.example.comunicame.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.comunicame.data.RepositorioUsuarios
import com.example.comunicame.ui.components.MensajeEstado
import com.example.comunicame.ui.components.PatronVibracion
import com.example.comunicame.ui.components.TipoMensaje
import com.example.comunicame.ui.components.vibrar
import com.example.comunicame.ui.theme.ComunicameTheme
import com.example.comunicame.util.ResultadoValidacion

// RECUPERAR CONTRASENA. Valida el correo contra el arreglo y cambia la clave.
// No manda correo real ni genera token porque esta entrega no tiene backend.
// Esta declarado en las restricciones, no es algo a medio hacer.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarScreen(
    onVolver: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var repetir by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<Pair<String, TipoMensaje>?>(null) }

    val context = LocalContext.current
    val formaCampo = RoundedCornerShape(12.dp)
    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    fun intentarActualizar() {
        when (val resultado = RepositorioUsuarios.validarRecuperacion(correo, nueva, repetir)) {

            is ResultadoValidacion.Invalido -> {
                val tipo = if (resultado.critico) TipoMensaje.ERROR else TipoMensaje.AVISO
                val patron = if (resultado.critico) {
                    PatronVibracion.ERROR
                } else {
                    PatronVibracion.TOQUE
                }
                mensaje = resultado.mensaje to tipo
                vibrar(context, patron)
            }

            is ResultadoValidacion.Valido -> {
                RepositorioUsuarios.actualizarContrasena(correo, nueva)
                vibrar(context, PatronVibracion.EXITO)
                mensaje = "Contraseña actualizada. Ya puedes iniciar sesión" to TipoMensaje.EXITO
                nueva = ""
                repetir = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al inicio de sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { relleno ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp)
        ) {

            Text(
                text = "Ingresa el correo con el que te registraste y define una contraseña nueva.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it; mensaje = null },
                label = { Text("Correo registrado") },
                singleLine = true,
                shape = formaCampo,
                colors = coloresCampo,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nueva,
                onValueChange = { nueva = it; mensaje = null },
                label = { Text("Nueva contraseña") },
                singleLine = true,
                shape = formaCampo,
                colors = coloresCampo,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                supportingText = { Text("Mínimo 8 caracteres") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = repetir,
                onValueChange = { repetir = it; mensaje = null },
                label = { Text("Repetir nueva contraseña") },
                singleLine = true,
                shape = formaCampo,
                colors = coloresCampo,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            mensaje?.let { (texto, tipo) ->
                Spacer(modifier = Modifier.height(20.dp))
                MensajeEstado(texto = texto, tipo = tipo)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { intentarActualizar() },
                shape = formaCampo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Actualizar contraseña", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onVolver,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Volver al inicio de sesión",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RecuperarScreenPreview() {
    ComunicameTheme {
        RecuperarScreen(onVolver = {})
    }
}
