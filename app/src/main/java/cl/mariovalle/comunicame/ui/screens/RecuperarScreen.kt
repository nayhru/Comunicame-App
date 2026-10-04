package cl.mariovalle.comunicame.ui.screens

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.mariovalle.comunicame.ui.components.MensajeEstado
import cl.mariovalle.comunicame.ui.components.PatronVibracion
import cl.mariovalle.comunicame.ui.components.TipoMensaje
import cl.mariovalle.comunicame.ui.components.vibrar
import cl.mariovalle.comunicame.ui.theme.ComunicameTheme
import cl.mariovalle.comunicame.ui.viewmodel.SesionViewModel

// RECUPERAR CONTRASENA.
//
// Ahora la pantalla solo pide el correo: Firebase envia un enlace y la persona
// define la clave nueva en esa pagina. La aplicacion ya no toca la contrasena
// en ningun momento, que es lo correcto y ademas lo unico posible, porque
// Firebase guarda un hash y no la devuelve nunca.
//
// Hasta la entrega anterior esta pantalla cambiaba la clave directamente en la
// lista en memoria, sin verificar que quien la pedia fuera el dueno del correo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarScreen(
    onVolver: () -> Unit,
    sesionViewModel: SesionViewModel = viewModel()
) {
    var correo by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf<Pair<String, TipoMensaje>?>(null) }

    val context = LocalContext.current
    val formaCampo = RoundedCornerShape(12.dp)
    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    LaunchedEffect(sesionViewModel.error) {
        sesionViewModel.error?.let { textoError ->
            mensaje = textoError to TipoMensaje.ERROR
            vibrar(context, PatronVibracion.ERROR)
        }
    }

    LaunchedEffect(sesionViewModel.aviso) {
        sesionViewModel.aviso?.let { textoAviso ->
            mensaje = textoAviso to TipoMensaje.EXITO
            vibrar(context, PatronVibracion.EXITO)
            correo = ""
        }
    }

    fun intentarEnviarEnlace() {
        mensaje = null
        sesionViewModel.limpiarMensajes()
        sesionViewModel.recuperarContrasena(correo)
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
                text = "Escribe el correo con el que te registraste. Te enviaremos un " +
                    "enlace para crear una contraseña nueva.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = {
                    correo = it
                    mensaje = null
                    sesionViewModel.limpiarMensajes()
                },
                label = { Text("Correo registrado") },
                singleLine = true,
                shape = formaCampo,
                colors = coloresCampo,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            mensaje?.let { (texto, tipo) ->
                Spacer(modifier = Modifier.height(20.dp))
                MensajeEstado(texto = texto, tipo = tipo)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { intentarEnviarEnlace() },
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
                    Text("Enviar enlace", style = MaterialTheme.typography.labelLarge)
                }
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
