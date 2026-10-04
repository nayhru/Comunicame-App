package cl.mariovalle.comunicame.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.mariovalle.comunicame.data.RepositorioFrases
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.clickable
import cl.mariovalle.comunicame.data.Usuario
import cl.mariovalle.comunicame.ui.theme.ComunicameTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.mariovalle.comunicame.data.ValidadorFormularios
import cl.mariovalle.comunicame.ui.components.MensajeEstado
import cl.mariovalle.comunicame.ui.components.TipoMensaje
import cl.mariovalle.comunicame.ui.viewmodel.PerfilViewModel
import cl.mariovalle.comunicame.ui.viewmodel.SesionViewModel
import cl.mariovalle.comunicame.util.criterioDeLargo
import cl.mariovalle.comunicame.util.resumen
import cl.mariovalle.comunicame.util.separarPor

// MI PERFIL. Los datos del registro y las preferencias que eligio.
// Abajo listo lo que falta por hacer, para que el alcance quede claro dentro de
// la misma app.
@Composable
fun PerfilScreen(
    usuario: Usuario,
    sesionViewModel: SesionViewModel,
    perfilViewModel: PerfilViewModel = viewModel()
) {
    var editando by remember { mutableStateOf(false) }
    var confirmandoBorrado by remember { mutableStateOf(false) }

    // Copias editables. Parten del usuario actual y solo se escriben a la base
    // cuando la persona confirma.
    var nombre by remember(usuario) { mutableStateOf(usuario.nombre) }
    var comuna by remember(usuario) { mutableStateOf(usuario.comuna) }
    var alertasVisuales by remember(usuario) { mutableStateOf(usuario.alertasVisuales) }
    var vibracionActiva by remember(usuario) { mutableStateOf(usuario.vibracion) }
    var subtitulosActivos by remember(usuario) { mutableStateOf(usuario.subtitulos) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(58.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = usuario.nombre.ifBlank { usuario.usuario },
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "@${usuario.usuario}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Datos de la cuenta",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FilaPerfil("Correo", usuario.correo.ifBlank { "—" })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                FilaPerfil("Comuna", usuario.comuna.ifBlank { "—" })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                FilaPerfil("Se comunica por", usuario.preferencia.etiqueta)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                // separarPor divide la lista en dos de una sola pasada, segun
                // el criterio que le paso como lambda. Las frases cortas entran
                // completas en la tarjeta de la grilla; las largas se recortan.
                val (cortas, largas) = RepositorioFrases.guardadas
                    .toList()
                    .separarPor(criterioDeLargo(40))

                FilaPerfil(
                    "Mis frases",
                    if (cortas.isEmpty() && largas.isEmpty()) {
                        "Ninguna todavía"
                    } else {
                        "${cortas.size + largas.size} guardadas"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Las frases propias, listadas completas. Aca si tiene sentido verlas:
        // son de ella y es donde puede revisar que guardo.
        if (RepositorioFrases.guardadas.isNotEmpty()) {
            Text(
                text = "Mis frases guardadas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                // resumen() junta las dos primeras y cuenta el resto
                text = RepositorioFrases.guardadas.toList().resumen(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    RepositorioFrases.guardadas.forEachIndexed { indice, frase ->
                        if (indice > 0) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        Text(
                            text = frase,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Frases disponibles por categoria.
        //
        // Aca NO va nada de otros usuarios. Mostrar cuanta gente hay registrada
        // o en que comunas viven seria exponer datos de terceros en la pantalla
        // personal de alguien, y ademas contradice la condicion de aceptacion
        // del proyecto sobre exposicion de data sensible.
        // El Map de este bloque cuenta frases, que son contenido de la app.
        Text(
            text = "Frases disponibles",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Las que trae la app para usar en el día a día.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Map<String, Int> armado con groupBy sobre el catalogo de
                // frases. Se recorre desestructurando cada entrada en (clave, valor).
                val porCategoria = RepositorioFrases.conteoPorCategoria()

                porCategoria.entries.forEachIndexed { indice, (etiqueta, cantidad) ->
                    if (indice > 0) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    FilaPerfil(etiqueta, "$cantidad ${if (cantidad == 1) "frase" else "frases"}")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                val total = porCategoria.values.sum()
                FilaPerfil("Total sugeridas", "$total frases")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Preferencias de accesibilidad",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Las elegiste al crear tu cuenta.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FilaPreferencia("Alertas visuales en pantalla", usuario.alertasVisuales)
                Spacer(modifier = Modifier.height(10.dp))
                FilaPreferencia("Vibración en cada aviso", usuario.vibracion)
                Spacer(modifier = Modifier.height(10.dp))
                FilaPreferencia("Subtítulos siempre activos", usuario.subtitulos)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Funcionalidades en proceso",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Estas funcionalidades están en proceso. Quedan documentadas para " +
                "futuras entregas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        FilaPlanificada(
            icono = Icons.Filled.History,
            titulo = "Historial de conversaciones",
            detalle = "Guardar y revisar los mensajes reproducidos. En proceso."
        )
        Spacer(modifier = Modifier.height(8.dp))
        FilaPlanificada(
            icono = Icons.Filled.Settings,
            titulo = "Ajustes de accesibilidad editables",
            detalle = "Cambiar tamaño de texto, contraste e intensidad de la vibración. En proceso."
        )
        Spacer(modifier = Modifier.height(8.dp))
        FilaPlanificada(
            icono = Icons.Filled.AccountCircle,
            titulo = "Contacto de emergencia y ficha médica",
            detalle = "Datos de un familiar y condiciones de salud relevantes. En proceso."
        )

        Spacer(modifier = Modifier.height(28.dp))

        perfilViewModel.error?.let { textoError ->
            MensajeEstado(texto = textoError, tipo = TipoMensaje.ERROR)
            Spacer(modifier = Modifier.height(12.dp))
        }

        perfilViewModel.aviso?.let { textoAviso ->
            MensajeEstado(texto = textoAviso, tipo = TipoMensaje.EXITO)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                perfilViewModel.limpiarMensajes()
                editando = true
            },
            enabled = !perfilViewModel.guardando,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Editar mis datos", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                perfilViewModel.limpiarMensajes()
                confirmandoBorrado = true
            },
            enabled = !perfilViewModel.guardando,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Eliminar mi cuenta", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // DIALOGO DE EDICION
    if (editando) {
        AlertDialog(
            onDismissRequest = { editando = false },
            title = { Text("Editar mis datos") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Comuna",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Lista corta de comunas; con pocas opciones un desplegable
                    // esconde las alternativas sin necesidad.
                    ValidadorFormularios.comunas.forEach { opcion ->
                        FilaOpcionComuna(
                            texto = opcion,
                            seleccionada = comuna == opcion,
                            alElegir = { comuna = opcion }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FilaPreferenciaEditable(
                        texto = "Alertas visuales en pantalla",
                        marcado = alertasVisuales,
                        alCambiar = { alertasVisuales = it }
                    )
                    FilaPreferenciaEditable(
                        texto = "Vibración en cada aviso",
                        marcado = vibracionActiva,
                        alCambiar = { vibracionActiva = it }
                    )
                    FilaPreferenciaEditable(
                        texto = "Subtítulos siempre visibles",
                        marcado = subtitulosActivos,
                        alCambiar = { subtitulosActivos = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !perfilViewModel.guardando,
                    onClick = {
                        val editado = usuario.copy(
                            nombre = nombre.trim(),
                            comuna = comuna,
                            alertasVisuales = alertasVisuales,
                            vibracion = vibracionActiva,
                            subtitulos = subtitulosActivos
                        )
                        perfilViewModel.actualizar(editado) { guardado ->
                            // Avisa al ViewModel de sesion para que el resto de
                            // las pantallas muestre los datos nuevos.
                            sesionViewModel.refrescarUsuario(guardado)
                            editando = false
                        }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { editando = false }) { Text("Cancelar") }
            }
        )
    }

    // DIALOGO DE ELIMINACION
    if (confirmandoBorrado) {
        AlertDialog(
            onDismissRequest = { confirmandoBorrado = false },
            title = { Text("¿Eliminar tu cuenta?") },
            text = {
                Text(
                    "Se borrarán tus datos y todas las frases que guardaste. " +
                        "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !perfilViewModel.guardando,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        perfilViewModel.eliminarCuenta(usuario.uid) {
                            confirmandoBorrado = false
                            // Cierra la sesion local: la cuenta ya no existe.
                            sesionViewModel.cerrarSesion()
                        }
                    }
                ) { Text("Sí, eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoBorrado = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Una comuna de la lista del dialogo de edicion
@Composable
private fun FilaOpcionComuna(
    texto: String,
    seleccionada: Boolean,
    alElegir: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { alElegir() }
            .padding(vertical = 2.dp)
    ) {
        RadioButton(selected = seleccionada, onClick = alElegir)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = texto, style = MaterialTheme.typography.bodyMedium)
    }
}

// Una preferencia de accesibilidad que si se puede cambiar
@Composable
private fun FilaPreferenciaEditable(
    texto: String,
    marcado: Boolean,
    alCambiar: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { alCambiar(!marcado) }
            .padding(vertical = 2.dp)
    ) {
        Checkbox(checked = marcado, onCheckedChange = alCambiar)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FilaPerfil(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}

// Va con icono y texto ademas del color, asi se entiende sin distinguir tonos
@Composable
private fun FilaPreferencia(etiqueta: String, activa: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (activa) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            tint = if (activa) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (activa) "Activada" else "Desactivada",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilaPlanificada(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    detalle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun PerfilScreenPreview() {
    ComunicameTheme {
        PerfilScreen(
            sesionViewModel = viewModel(factory = SesionViewModel.fabrica()),
            usuario = Usuario(
                nombre = "Ana Torres",
                usuario = "ana",
                correo = "ana.torres@correo.cl",
                comuna = "Santiago"
            )
        )
    }
}
