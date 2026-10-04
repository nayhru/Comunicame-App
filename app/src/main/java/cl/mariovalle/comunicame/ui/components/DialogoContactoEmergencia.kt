package cl.mariovalle.comunicame.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.mariovalle.comunicame.data.ContactoEmergencia
import cl.mariovalle.comunicame.data.Parentesco
import cl.mariovalle.comunicame.data.ValidadorFormularios
import cl.mariovalle.comunicame.util.ResultadoValidacion

// Formulario del contacto de emergencia.
//
// Pide nombre, telefono, parentesco y correo. El correo es opcional: en una
// urgencia nadie escribe un correo, el dato que resuelve es el telefono.
@Composable
fun DialogoContactoEmergencia(
    contacto: ContactoEmergencia,
    guardando: Boolean,
    alGuardar: (ContactoEmergencia) -> Unit,
    alCerrar: () -> Unit
) {
    var nombre by remember { mutableStateOf(contacto.nombre) }
    var numero by remember { mutableStateOf(contacto.numero) }
    var correo by remember { mutableStateOf(contacto.correo) }
    var parentesco by remember { mutableStateOf(contacto.parentesco) }

    var error by remember { mutableStateOf<String?>(null) }

    val forma = RoundedCornerShape(12.dp)

    AlertDialog(
        onDismissRequest = alCerrar,
        title = {
            Text(
                if (contacto.estaConfigurado) "Editar mi contacto"
                else "Agregar contacto de emergencia"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 420.dp)
            ) {
                Text(
                    text = "Esta persona aparecerá en la pantalla de emergencia " +
                        "para que alguien pueda avisarle.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; error = null },
                    label = { Text("Nombre") },
                    singleLine = true,
                    shape = forma,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = numero,
                    onValueChange = { numero = it; error = null },
                    label = { Text("Teléfono") },
                    placeholder = { Text("9 1234 5678") },
                    singleLine = true,
                    shape = forma,
                    // Teclado numerico: escribir un telefono con el alfabetico
                    // obliga a cambiar de capa en cada digito.
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it; error = null },
                    label = { Text("Correo (opcional)") },
                    singleLine = true,
                    shape = forma,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¿Quién es?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Parentesco.entries.forEach { opcion ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { parentesco = opcion }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = parentesco == opcion,
                            onClick = { parentesco = opcion }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = opcion.etiqueta)
                    }
                }

                error?.let { texto ->
                    Spacer(modifier = Modifier.height(12.dp))
                    MensajeEstado(texto = texto, tipo = TipoMensaje.ERROR)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !guardando,
                onClick = {
                    val validacion = ValidadorFormularios.validarContactoEmergencia(
                        nombre = nombre,
                        numero = numero,
                        correo = correo
                    )

                    when (validacion) {
                        is ResultadoValidacion.Invalido -> error = validacion.mensaje
                        is ResultadoValidacion.Valido -> alGuardar(
                            ContactoEmergencia(
                                nombre = nombre.trim(),
                                numero = numero.trim(),
                                correo = correo.trim(),
                                parentesco = parentesco
                            )
                        )
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = alCerrar) { Text("Cancelar") }
        }
    )
}
