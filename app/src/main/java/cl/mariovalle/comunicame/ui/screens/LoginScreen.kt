package cl.mariovalle.comunicame.ui.screens

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.mariovalle.comunicame.data.Usuario
import cl.mariovalle.comunicame.ui.viewmodel.SesionViewModel
import cl.mariovalle.comunicame.ui.components.MensajeEstado
import cl.mariovalle.comunicame.ui.components.PatronVibracion
import cl.mariovalle.comunicame.ui.components.TipoMensaje
import cl.mariovalle.comunicame.ui.components.vibrar
import cl.mariovalle.comunicame.ui.theme.ComunicameTheme

// LOGIN. Pantalla de inicio, valida las credenciales contra Firebase Auth.
// Fondo blanco y el lila solo de acento.
// El resultado nunca se avisa con sonido: banner visual + vibracion.
@Composable
fun LoginScreen(
    onLoginExitoso: (Usuario) -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit,
    sesionViewModel: SesionViewModel = viewModel()
) {
    // Estado. remember guarda el valor entre recomposiciones y mutableStateOf
    // hace que Compose lo observe.
    // Firebase Auth identifica a la persona por su correo, no por el nombre
    // de usuario, asi que el campo cambio respecto de la entrega anterior.
    var correo by remember { mutableStateOf("") }
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

    // El error del ViewModel se traduce a banner y vibracion. LaunchedEffect
    // reacciona cuando el valor cambia, sin repetir el aviso en cada
    // recomposicion: sin esto el telefono vibraria al girar la pantalla.
    LaunchedEffect(sesionViewModel.error) {
        sesionViewModel.error?.let { textoError ->
            mensaje = textoError to TipoMensaje.ERROR
            vibrar(context, PatronVibracion.ERROR)
        }
    }

    // La saco del onClick para que el boton se lea limpio
    fun intentarIngresar() {
        mensaje = null
        sesionViewModel.limpiarMensajes()

        // El ViewModel valida el formulario y, si pasa, consulta a Firebase.
        // La respuesta tarda, asi que el resultado llega por el callback y por
        // el estado de error, no como valor de retorno.
        sesionViewModel.iniciarSesion(correo, contrasena) {
            vibrar(context, PatronVibracion.EXITO)
            sesionViewModel.usuario?.let(onLoginExitoso)
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
            value = correo,
            onValueChange = {
                correo = it
                mensaje = null // si esta corrigiendo, saco el error de antes
                sesionViewModel.limpiarMensajes()
            },
            label = { Text("Correo") },
            singleLine = true,
            shape = formaCampo,
            colors = coloresCampo,
            // Teclado con arroba a la vista, en vez del alfabetico normal
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = {
                contrasena = it
                mensaje = null
                sesionViewModel.limpiarMensajes()
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
            // Deshabilitado mientras espera a Firebase: sin esto, tocar dos
            // veces dispara dos inicios de sesion contra el servidor.
            enabled = !sesionViewModel.cargando,
            shape = formaCampo,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            if (sesionViewModel.cargando) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.labelLarge
                )
            }
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
