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
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import cl.mariovalle.comunicame.data.PreferenciaComunicacion
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ArrowDropDown
import cl.mariovalle.comunicame.ui.components.CampoComuna
import cl.mariovalle.comunicame.ui.components.SelectorComuna
import cl.mariovalle.comunicame.data.ValidadorFormularios
import cl.mariovalle.comunicame.ui.viewmodel.SesionViewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.mariovalle.comunicame.data.Usuario
import cl.mariovalle.comunicame.ui.components.MensajeEstado
import cl.mariovalle.comunicame.ui.components.PatronVibracion
import cl.mariovalle.comunicame.ui.components.TipoMensaje
import cl.mariovalle.comunicame.ui.components.vibrar
import cl.mariovalle.comunicame.ui.theme.ComunicameTheme
import cl.mariovalle.comunicame.util.aNombrePropio
import cl.mariovalle.comunicame.util.fuerzaContrasena
import cl.mariovalle.comunicame.util.limpio

// REGISTRO. Da de alta un usuario y lo mete al mismo arreglo que despues
// consulta el Login.
// Aca estan casi todos los componentes que pide la pauta: inputs, combo box,
// radio buttons y check list.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    sesionViewModel: SesionViewModel = viewModel(factory = SesionViewModel.fabrica()),
    onVolver: () -> Unit
) {
    // Estado del formulario
    var nombre by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var repetir by remember { mutableStateOf("") }

    // Un estado por campo: son dos contrasenas distintas y la usuaria puede
    // querer revisar solo una de las dos
    var verContrasena by remember { mutableStateOf(false) }
    var verRepetir by remember { mutableStateOf(false) }

    var comuna by remember { mutableStateOf("") }
    var eligiendoComuna by remember { mutableStateOf(false) }

    // Radios, una sola opcion
    var preferencia by remember { mutableStateOf(PreferenciaComunicacion.LENGUA_DE_SENAS) }

    // Checks, se pueden marcar varios
    var alertasVisuales by remember { mutableStateOf(true) }
    var vibracion by remember { mutableStateOf(true) }
    var subtitulos by remember { mutableStateOf(true) }

    var aceptaTerminos by remember { mutableStateOf(false) }

    var mensaje by remember { mutableStateOf<Pair<String, TipoMensaje>?>(null) }

    val context = LocalContext.current
    val formaCampo = RoundedCornerShape(12.dp)
    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    // Valida en orden, de lo mas barato a lo mas caro
    LaunchedEffect(sesionViewModel.error) {
        sesionViewModel.error?.let { textoError ->
            mensaje = textoError to TipoMensaje.ERROR
            vibrar(context, PatronVibracion.ERROR)
        }
    }

    fun intentarRegistrar() {
        mensaje = null
        sesionViewModel.limpiarMensajes()

        // Las validaciones de formato se resuelven aqui mismo, sin gastar una
        // peticion. Si el correo ya esta tomado lo dice Firebase, que es el
        // unico que conoce todas las cuentas.
        sesionViewModel.registrar(
            // aNombrePropio arregla "ANA torres" -> "Ana Torres"
            nombre = nombre.aNombrePropio(),
            nombreUsuario = usuario.limpio.lowercase(),
            correo = correo.limpio.lowercase(),
            contrasena = contrasena,
            repetir = repetir,
            comuna = comuna,
            preferencia = preferencia,
            aceptaTerminos = aceptaTerminos,
            alertasVisuales = alertasVisuales,
            vibracion = vibracion,
            subtitulos = subtitulos
        ) {
            vibrar(context, PatronVibracion.EXITO)
            mensaje = "Cuenta creada. Ya puedes iniciar sesión" to TipoMensaje.EXITO
            onRegistroExitoso()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
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
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {

            Text(
                text = "Tus datos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; mensaje = null },
                label = { Text("Nombre completo") },
                singleLine = true,
                shape = formaCampo,
                colors = coloresCampo,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it; mensaje = null },
                label = { Text("Nombre de usuario") },
                singleLine = true,
                shape = formaCampo,
                colors = coloresCampo,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it; mensaje = null },
                label = { Text("Correo electrónico") },
                singleLine = true,
                shape = formaCampo,
                colors = coloresCampo,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Selector de comuna con buscador, agrupado por region. El mismo
            // que usa Mi perfil: con mas de setenta comunas, un desplegable
            // plano obliga a recorrer la lista a ciegas.
            CampoComuna(
                comuna = comuna,
                region = ValidadorFormularios.regionDe(comuna),
                alTocar = {
                    eligiendoComuna = true
                    mensaje = null
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it; mensaje = null },
                label = { Text("Contraseña") },
                singleLine = true,
                shape = formaCampo,
                colors = coloresCampo,
                // Tapa el texto salvo que pida verlo, igual que en el Login
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
                            // Cambia con el estado. Si el boton hace dos cosas
                            // no puede anunciarse igual siempre en el lector.
                            contentDescription = if (verContrasena) {
                                "Ocultar contraseña"
                            } else {
                                "Mostrar contraseña"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                supportingText = {
                    // fuerzaContrasena cuenta cuantas reglas cumple (0 a 4).
                    // El when traduce ese numero a un texto util en vez de
                    // repetir siempre "minimo 8 caracteres".
                    val fuerza = contrasena.fuerzaContrasena()
                    val (texto, color) = when (fuerza) {
                        0 -> "Mínimo 8 caracteres" to MaterialTheme.colorScheme.onSurfaceVariant
                        1 -> "Muy débil" to MaterialTheme.colorScheme.error
                        2 -> "Débil: agrega números o mayúsculas" to MaterialTheme.colorScheme.error
                        3 -> "Aceptable" to MaterialTheme.colorScheme.onSurfaceVariant
                        else -> "Segura" to MaterialTheme.colorScheme.primary
                    }
                    Text(text = texto, color = color)
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = repetir,
                onValueChange = { repetir = it; mensaje = null },
                label = { Text("Repetir contraseña") },
                singleLine = true,
                shape = formaCampo,
                colors = coloresCampo,
                // Estado propio: se puede mirar una y dejar la otra tapada
                visualTransformation = if (verRepetir) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { verRepetir = !verRepetir }) {
                        Icon(
                            imageVector = if (verRepetir) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (verRepetir) {
                                "Ocultar la contraseña repetida"
                            } else {
                                "Mostrar la contraseña repetida"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))

            // RADIO BUTTONS
            Text(
                text = "¿Cómo prefieres comunicarte?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Elige una opción",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            // selectableGroup hace que TalkBack los lea como "opcion 2 de 3"
            // en vez de leerlos sueltos
            Column(modifier = Modifier.selectableGroup()) {
                PreferenciaComunicacion.entries.forEach { opcion ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = (preferencia == opcion),
                            onClick = { preferencia = opcion }
                        )
                        Text(
                            text = opcion.etiqueta,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CHECK LIST
            Text(
                text = "Preferencias de accesibilidad",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Puedes elegir varias",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            FilaCheck(
                texto = "Alertas visuales en pantalla",
                marcado = alertasVisuales,
                alCambiar = { alertasVisuales = it }
            )
            FilaCheck(
                texto = "Vibración en cada aviso",
                marcado = vibracion,
                alCambiar = { vibracion = it }
            )
            FilaCheck(
                texto = "Subtítulos siempre activos",
                marcado = subtitulos,
                alCambiar = { subtitulos = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            FilaCheck(
                texto = "Acepto los términos y condiciones",
                marcado = aceptaTerminos,
                alCambiar = { aceptaTerminos = it; mensaje = null }
            )

            mensaje?.let { (texto, tipo) ->
                Spacer(modifier = Modifier.height(16.dp))
                MensajeEstado(texto = texto, tipo = tipo)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { intentarRegistrar() },
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
                    Text("Registrarme", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onVolver,
                shape = formaCampo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Volver", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // SELECTOR DE COMUNA
    if (eligiendoComuna) {
        SelectorComuna(
            comunaElegida = comuna,
            alElegir = { comuna = it },
            alCerrar = { eligiendoComuna = false }
        )
    }
}

// La saco aparte para no repetir lo mismo cuatro veces
@Composable
private fun FilaCheck(
    texto: String,
    marcado: Boolean,
    alCambiar: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = marcado,
            onCheckedChange = alCambiar
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun RegistroScreenPreview() {
    ComunicameTheme {
        RegistroScreen(onRegistroExitoso = {}, onVolver = {})
    }
}
