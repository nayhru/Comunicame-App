package com.example.comunicame.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.comunicame.data.CategoriaFrase
import com.example.comunicame.data.RepositorioFrases
import com.example.comunicame.ui.components.MensajeEstado
import com.example.comunicame.ui.components.PatronVibracion
import com.example.comunicame.ui.components.TipoMensaje
import com.example.comunicame.ui.components.vibrar
import com.example.comunicame.ui.theme.ComunicameTheme
import com.example.comunicame.util.estaVacio
import com.example.comunicame.util.limpio
import java.util.Locale

private enum class ModoComunicacion(val etiqueta: String) {
    TEXTO_A_VOZ("Texto a voz"),
    VOZ_A_TEXTO("Voz a texto")
}

// COMUNICAR. Es el corazon de la app.
// Texto a voz esta listo. Voz a texto queda visible pero avisando que esta en
// proceso, asi el usuario no supone que existe y le falla.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunicarScreen() {
    val context = LocalContext.current

    var modo by remember { mutableStateOf(ModoComunicacion.TEXTO_A_VOZ) }
    var texto by remember { mutableStateOf("") }
    var busqueda by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<CategoriaFrase?>(null) }
    var mensaje by remember { mutableStateOf<Pair<String, TipoMensaje>?>(null) }
    var dialogoAbierto by remember { mutableStateOf(false) }
    var fraseNueva by remember { mutableStateOf("") }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsListo by remember { mutableStateOf(false) }

    // El onDispose libera el motor al salir. Si no, el TextToSpeech queda vivo
    // y filtra memoria.
    DisposableEffect(Unit) {
        val motor = TextToSpeech(context) { estado ->
            ttsListo = (estado == TextToSpeech.SUCCESS)
        }
        tts = motor
        onDispose {
            motor.stop()
            motor.shutdown()
        }
    }

    fun hablar() {
        val contenido = texto.limpio

        if (contenido.estaVacio) {
            mensaje = "Escribe un mensaje antes de reproducir" to TipoMensaje.AVISO
            vibrar(context, PatronVibracion.TOQUE)
            return
        }

        val motor = tts
        if (motor == null || !ttsListo) {
            mensaje = "El motor de voz no está disponible en este dispositivo" to TipoMensaje.ERROR
            vibrar(context, PatronVibracion.ERROR)
            return
        }

        // try/catch porque el motor de voz es hardware ajeno: puede no tener la
        // voz en espanol instalada, quedarse sin memoria o morirse a mitad de
        // la reproduccion. Si eso revienta sin capturar, se cae la app entera y
        // la usuaria pierde el mensaje que acababa de escribir.
        try {
            val idioma = motor.setLanguage(Locale.forLanguageTag("es-CL"))

            // setLanguage no lanza excepcion cuando falta el idioma: devuelve
            // un codigo. Hay que revisarlo a mano.
            if (idioma == TextToSpeech.LANG_MISSING_DATA ||
                idioma == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                mensaje = "Falta instalar la voz en español en este dispositivo" to
                    TipoMensaje.AVISO
                vibrar(context, PatronVibracion.ERROR)
                return
            }

            val resultado = motor.speak(
                contenido,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "mensaje_comunicame"
            )

            if (resultado == TextToSpeech.ERROR) {
                throw IllegalStateException("speak() devolvio ERROR")
            }

            // No escucha lo que acaba de sonar, hay que confirmarselo por otra via
            vibrar(context, PatronVibracion.EXITO)
            mensaje = "Mensaje reproducido en voz alta" to TipoMensaje.EXITO

        } catch (e: IllegalStateException) {
            // El motor quedo en un estado invalido
            mensaje = "El motor de voz no respondió. Intenta de nuevo." to TipoMensaje.ERROR
            vibrar(context, PatronVibracion.ERROR)

        } catch (e: Exception) {
            // Red de seguridad: cualquier otra falla del dispositivo.
            // Prefiero un mensaje visible antes que un cierre inesperado.
            mensaje = "No se pudo reproducir el mensaje en este dispositivo" to
                TipoMensaje.ERROR
            vibrar(context, PatronVibracion.ERROR)
        }
    }

    val formaCampo = RoundedCornerShape(12.dp)
    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {

        // Selector de modo
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ModoComunicacion.entries.forEachIndexed { indice, opcion ->
                SegmentedButton(
                    selected = modo == opcion,
                    onClick = { modo = opcion; mensaje = null },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = indice,
                        count = ModoComunicacion.entries.size
                    )
                ) {
                    Text(opcion.etiqueta)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (modo) {
            ModoComunicacion.TEXTO_A_VOZ -> {

                Text(
                    text = "Escribe lo que quieres decir y el teléfono lo dirá por ti.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it; mensaje = null },
                    label = { Text("Tu mensaje") },
                    minLines = 4,
                    shape = formaCampo,
                    colors = coloresCampo,
                    modifier = Modifier.fillMaxWidth()
                )

                mensaje?.let { (t, tipo) ->
                    Spacer(modifier = Modifier.height(14.dp))
                    MensajeEstado(texto = t, tipo = tipo)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { hablar() },
                    shape = formaCampo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Reproducir en voz alta", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { texto = ""; mensaje = null },
                    shape = formaCampo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Limpiar", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(28.dp))

                // GRILLA de frases sugeridas
                EncabezadoSeccion(
                    titulo = "Frases sugeridas",
                    detalle = "Tócala para cargarla en tu mensaje"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Buscador. Filtra sobre sugeridas y guardadas a la vez.
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    label = { Text("Buscar una frase") },
                    singleLine = true,
                    shape = formaCampo,
                    colors = coloresCampo,
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) {
                            IconButton(onClick = { busqueda = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Borrar la búsqueda"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Chips de categoria. null = todas.
                // El primer chip lo agrego aparte y el resto sale de recorrer
                // el enum con map, asi no repito la misma estructura 5 veces.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(
                        selected = categoria == null,
                        onClick = { categoria = null },
                        label = { Text("Todas") }
                    )
                    CategoriaFrase.entries.forEach { cat ->
                        FilterChip(
                            selected = categoria == cat,
                            onClick = { categoria = if (categoria == cat) null else cat },
                            label = { Text(cat.etiqueta) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Aca se combinan las dos cosas: si hay busqueda mando todo al
                // buscador; si no, filtro por la categoria elegida.
                val frasesVisibles: List<String> = if (busqueda.isNotBlank()) {
                    RepositorioFrases.buscar(busqueda)
                } else {
                    RepositorioFrases.sugeridasDe(categoria).map { it.texto }
                }

                if (frasesVisibles.isEmpty()) {
                    MensajeEstado(
                        texto = "No hay frases que coincidan con \"$busqueda\".",
                        tipo = TipoMensaje.INFO
                    )
                } else {
                    // Altura calculada: 2 columnas, 112dp por fila, 10dp entre filas.
                    // Con la grilla filtrada el alto ya no puede ser fijo.
                    val filas = (frasesVisibles.size + 1) / 2
                    val altoGrilla = (filas * 112 + (filas - 1).coerceAtLeast(0) * 10).dp

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        // Le apago el scroll propio: va dentro de un Column que
                        // ya scrollea y se pelean por el gesto.
                        userScrollEnabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(altoGrilla)
                    ) {
                        items(frasesVisibles) { frase ->
                            TarjetaFrase(
                                frase = frase,
                                alTocar = {
                                    texto = frase
                                    mensaje = null
                                    vibrar(context, PatronVibracion.TOQUE)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Las frases del usuario
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        EncabezadoSeccion(
                            titulo = "Mis frases",
                            detalle = "Las que tú guardas para tu día a día"
                        )
                    }
                    IconButton(onClick = { dialogoAbierto = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Guardar una frase nueva",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (RepositorioFrases.guardadas.isEmpty()) {
                    MensajeEstado(
                        texto = "Todavía no guardas frases. Usa el botón + para crear la primera.",
                        tipo = TipoMensaje.INFO
                    )
                } else {
                    RepositorioFrases.guardadas.forEach { frase ->
                        FilaFraseGuardada(
                            frase = frase,
                            alTocar = {
                                texto = frase
                                mensaje = null
                                vibrar(context, PatronVibracion.TOQUE)
                            },
                            alEliminar = {
                                RepositorioFrases.eliminar(frase)
                                vibrar(context, PatronVibracion.TOQUE)
                                mensaje = "Frase eliminada" to TipoMensaje.INFO
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            ModoComunicacion.VOZ_A_TEXTO -> {
                SeccionPlanificada(
                    icono = Icons.Filled.Mic,
                    titulo = "Voz a texto",
                    descripcion = "Permitirá que la persona con la que hablas use el micrófono " +
                        "y su voz aparezca escrita en tu pantalla, completando la conversación " +
                        "en los dos sentidos.",
                    entrega = "Esta funcionalidad está en proceso. Queda documentada para " +
                        "futuras entregas."
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Dialogo para guardar una frase nueva
    if (dialogoAbierto) {
        AlertDialog(
            onDismissRequest = { dialogoAbierto = false; fraseNueva = "" },
            title = { Text("Guardar una frase") },
            text = {
                OutlinedTextField(
                    value = fraseNueva,
                    onValueChange = { fraseNueva = it },
                    label = { Text("Escribe tu frase") },
                    shape = formaCampo,
                    colors = coloresCampo,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val guardada = RepositorioFrases.guardar(fraseNueva)
                        mensaje = if (guardada) {
                            vibrar(context, PatronVibracion.EXITO)
                            "Frase guardada" to TipoMensaje.EXITO
                        } else {
                            vibrar(context, PatronVibracion.ERROR)
                            "La frase está vacía o ya la tienes guardada" to TipoMensaje.AVISO
                        }
                        fraseNueva = ""
                        dialogoAbierto = false
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { dialogoAbierto = false; fraseNueva = "" }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun EncabezadoSeccion(titulo: String, detalle: String) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Text(
        text = detalle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// Tarjeta de la grilla
@Composable
private fun TarjetaFrase(frase: String, alTocar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            // Holgada: las frases largas ocupan 3 lineas y ademas el usuario
            // puede tener la letra del sistema mas grande
            .height(112.dp)
            .clickable { alTocar() }
    ) {
        Text(
            text = frase,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(12.dp)
        )
    }
}

// Frase del usuario, con el boton de borrar
@Composable
private fun FilaFraseGuardada(
    frase: String,
    alTocar: () -> Unit,
    alEliminar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { alTocar() }
                .padding(start = 14.dp, top = 6.dp, bottom = 6.dp, end = 4.dp)
        ) {
            Text(
                text = frase,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = alEliminar) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar la frase: $frase",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Tarjeta para lo que todavia no esta hecho. Prefiero mostrarlo a esconderlo:
// deja claro hasta donde llega esta entrega.
@Composable
private fun SeccionPlanificada(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    descripcion: String,
    entrega: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entrega,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun ComunicarScreenPreview() {
    ComunicameTheme { ComunicarScreen() }
}
