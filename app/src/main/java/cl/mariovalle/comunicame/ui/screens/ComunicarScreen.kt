package cl.mariovalle.comunicame.ui.screens

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
import androidx.compose.material.icons.filled.Edit
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
import cl.mariovalle.comunicame.data.CategoriaFrase
import cl.mariovalle.comunicame.data.FraseGuardada
import cl.mariovalle.comunicame.data.RepositorioFrases
import cl.mariovalle.comunicame.ui.viewmodel.FrasesViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.RecordVoiceOver
import cl.mariovalle.comunicame.ui.components.MensajeEstado
import cl.mariovalle.comunicame.ui.components.ResultadoEscucha
import cl.mariovalle.comunicame.ui.components.hayReconocedorDeVoz
import cl.mariovalle.comunicame.ui.components.intentDeEscucha
import cl.mariovalle.comunicame.ui.components.interpretarEscucha
import cl.mariovalle.comunicame.ui.components.mensajeDeFallo
import cl.mariovalle.comunicame.ui.components.tienePermisoDeMicrofono
import cl.mariovalle.comunicame.ui.components.PatronVibracion
import cl.mariovalle.comunicame.ui.components.TipoMensaje
import cl.mariovalle.comunicame.ui.components.vibrar
import cl.mariovalle.comunicame.ui.theme.ComunicameTheme
import cl.mariovalle.comunicame.util.contarQue
import cl.mariovalle.comunicame.util.criterioDeLargo
import cl.mariovalle.comunicame.util.estaVacio
import cl.mariovalle.comunicame.util.limpio
import java.util.Locale

private enum class ModoComunicacion(val etiqueta: String) {
    TEXTO_A_VOZ("Texto a voz"),
    VOZ_A_TEXTO("Voz a texto")
}

// COMUNICAR. Es el corazon de la app.
//
// Los dos sentidos de la conversacion estan cubiertos. Texto a voz: la persona
// escribe y el telefono lo dice. Voz a texto: la otra persona habla y su voz
// aparece escrita en la pantalla.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunicarScreen(
    uid: String,
    frasesViewModel: FrasesViewModel = viewModel()
) {
    // Carga las frases del usuario al entrar. La clave del LaunchedEffect es
    // el uid: si cambia la cuenta, vuelve a consultar; mientras sea la misma,
    // no repite la peticion en cada recomposicion.
    LaunchedEffect(uid) {
        frasesViewModel.cargar(uid)
    }

    // Frase que se esta editando, o null si no hay ninguna en edicion
    var fraseEnEdicion by remember { mutableStateOf<FraseGuardada?>(null) }

    val context = LocalContext.current

    var modo by remember { mutableStateOf(ModoComunicacion.TEXTO_A_VOZ) }
    var texto by remember { mutableStateOf("") }
    var busqueda by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<CategoriaFrase?>(null) }
    var mensaje by remember { mutableStateOf<Pair<String, TipoMensaje>?>(null) }
    var dialogoAbierto by remember { mutableStateOf(false) }

    // VOZ A TEXTO
    var escuchando by remember { mutableStateOf(false) }
    var loQueDijo by remember { mutableStateOf("") }
    var tutorialAbierto by remember { mutableStateOf(false) }

    // El reconocedor puede no estar instalado: en ese caso conviene decirlo
    // antes de ofrecer un boton que no va a funcionar.
    val hayReconocedor = remember { hayReconocedorDeVoz(context) }

    // Recibe el resultado del dialogo de voz del sistema.
    //
    // interpretarEscucha envuelve la lectura en try/catch/finally: los datos
    // vienen de otra aplicacion y el finally garantiza que el estado de
    // escucha se apague aunque algo falle, para que el boton no quede
    // bloqueado en medio de una conversacion.
    val lanzadorDeEscucha = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        when (val escuchado = interpretarEscucha(
            codigoResultado = resultado.resultCode,
            datos = resultado.data,
            alTerminar = { escuchando = false }
        )) {
            is ResultadoEscucha.Entendido -> {
                loQueDijo = escuchado.texto
                mensaje = null
                vibrar(context, PatronVibracion.EXITO)
            }
            is ResultadoEscucha.Fallo -> {
                mensaje = escuchado.mensaje to TipoMensaje.AVISO
                vibrar(context, PatronVibracion.ERROR)
            }
            // Cancelar es una decision de la persona, no un error que avisar.
            ResultadoEscucha.Cancelado -> Unit
        }
    }

    // Abre el dialogo de voz del sistema.
    fun escuchar() {
        mensaje = null
        try {
            escuchando = true
            lanzadorDeEscucha.launch(intentDeEscucha())
        } catch (e: ActivityNotFoundException) {
            escuchando = false
            mensaje = mensajeDeFallo(e) to TipoMensaje.ERROR
            vibrar(context, PatronVibracion.ERROR)
        } catch (e: SecurityException) {
            escuchando = false
            mensaje = mensajeDeFallo(e) to TipoMensaje.ERROR
            vibrar(context, PatronVibracion.ERROR)
        }
    }

    // El permiso se pide en el momento de usar la funcion y no al abrir la
    // aplicacion: asi el sistema lo muestra cuando se entiende para que sirve.
    val pedirMicrofono = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            escuchar()
        } else {
            mensaje = ("Sin acceso al micrófono no se puede escuchar. " +
                "Puedes activarlo desde los ajustes del teléfono.") to TipoMensaje.AVISO
            vibrar(context, PatronVibracion.ERROR)
        }
    }

    fun iniciarEscucha() {
        if (tienePermisoDeMicrofono(context)) {
            escuchar()
        } else {
            pedirMicrofono.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    // Los mensajes del ViewModel se muestran en el mismo banner que el resto
    // de la pantalla, para que el usuario tenga un solo lugar donde mirar.
    LaunchedEffect(frasesViewModel.error, frasesViewModel.aviso) {
        val error = frasesViewModel.error
        val aviso = frasesViewModel.aviso

        mensaje = when {
            error != null -> error to TipoMensaje.ERROR
            aviso != null -> aviso to TipoMensaje.EXITO
            // Cuando el ViewModel retira su aviso, el banner tiene que irse
            // con el. Si solo se copiara el valor cuando existe, la
            // confirmacion quedaria fija en pantalla despues de que el
            // ViewModel ya la dio por terminada.
            else -> null
        }
    }

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
                        // contarQue recibe el criterio como lambda: cuento las
                        // que entran completas en una tarjeta de la grilla.
                        val textosGuardados = frasesViewModel.frases.map { it.texto }
                        // contarQue recibe el criterio como lambda: cuento las
                        // que entran completas en una tarjeta de la grilla.
                        val cortas = textosGuardados.contarQue(criterioDeLargo(40))

                        EncabezadoSeccion(
                            titulo = "Mis frases",
                            detalle = when (textosGuardados.size) {
                                0 -> "Las que tú guardas para tu día a día"
                                cortas -> "Todas caben completas en la grilla"
                                else -> "$cortas de ${textosGuardados.size} " +
                                    "caben completas en la grilla"
                            }
                        )
                    }
                    IconButton(onClick = {
                        fraseEnEdicion = null
                        fraseNueva = ""
                        dialogoAbierto = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Guardar una frase nueva",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (frasesViewModel.cargando && frasesViewModel.frases.isEmpty()) {
                    MensajeEstado(
                        texto = "Cargando tus frases guardadas...",
                        tipo = TipoMensaje.INFO
                    )
                } else if (frasesViewModel.frases.isEmpty()) {
                    MensajeEstado(
                        texto = "Todavía no guardas frases. Usa el botón + para crear la primera.",
                        tipo = TipoMensaje.INFO
                    )
                } else {
                    frasesViewModel.frases.forEach { frase ->
                        FilaFraseGuardada(
                            frase = frase.texto,
                            alTocar = {
                                texto = frase.texto
                                mensaje = null
                                vibrar(context, PatronVibracion.TOQUE)
                            },
                            alEditar = {
                                fraseEnEdicion = frase
                                fraseNueva = frase.texto
                                dialogoAbierto = true
                            },
                            alEliminar = {
                                frasesViewModel.eliminar(frase.id)
                                vibrar(context, PatronVibracion.TOQUE)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            ModoComunicacion.VOZ_A_TEXTO -> {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Muéstrale el teléfono a quien te habla y toca Escuchar.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { tutorialAbierto = true }) {
                        Icon(
                            imageVector = Icons.Filled.HelpOutline,
                            contentDescription = "Cómo usar voz a texto",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!hayReconocedor) {
                    // Se avisa antes de ofrecer un boton que no va a funcionar.
                    MensajeEstado(
                        texto = "Este dispositivo no tiene instalado el reconocimiento de voz " +
                            "de Google, así que esta función no está disponible aquí.",
                        tipo = TipoMensaje.AVISO
                    )
                } else {
                    Button(
                        onClick = { iniciarEscucha() },
                        enabled = !escuchando,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            // Alto generoso: se toca mientras se sostiene el
                            // telefono en alto frente a otra persona.
                            .height(96.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (escuchando) "Escuchando..." else "ESCUCHAR",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Lo que te dijeron",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Este texto es para que tú lo leas. El teléfono no lo dice en " +
                        "voz alta.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // El texto reconocido queda editable: el reconocedor se
                // equivoca con los nombres propios y con los numeros, y
                // corregir una palabra es mas rapido que repetir la frase.
                // Fondo distinto al del campo de "Tu mensaje": a simple vista
                // debe quedar claro que este texto es de la otra persona y
                // que no es lo que el telefono va a decir.
                OutlinedTextField(
                    value = loQueDijo,
                    onValueChange = { loQueDijo = it },
                    placeholder = {
                        Text("Aquí aparecerá escrito lo que diga la otra persona")
                    },
                    label = { Text("Dicho por la otra persona") },
                    shape = formaCampo,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary,
                        cursorColor = MaterialTheme.colorScheme.secondary,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Puedes corregir cualquier palabra tocando el texto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Spacer(modifier = Modifier.height(16.dp))

                // El nombre dice a donde lleva y no que hace con el texto de
                // arriba: la version anterior decia solo "Responder", debajo
                // del campo con lo que dijo la otra persona, y se entendia
                // que iba a reproducir ese texto en voz alta.
                OutlinedButton(
                    onClick = {
                        modo = ModoComunicacion.TEXTO_A_VOZ
                        mensaje = ("Escribe tu respuesta y toca Reproducir en voz alta") to
                            TipoMensaje.INFO
                        vibrar(context, PatronVibracion.TOQUE)
                    },
                    shape = formaCampo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.RecordVoiceOver,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ir a Texto a voz para contestar")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        loQueDijo = ""
                        mensaje = null
                        vibrar(context, PatronVibracion.TOQUE)
                    },
                    enabled = loQueDijo.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Borrar lo escuchado")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // TUTORIAL de voz a texto.
    //
    // Los requerimientos piden un area de ayuda. Se resuelve aqui, junto a la
    // funcion que explica, y no en una pantalla aparte: quien no entiende como
    // usarla la busca en el momento, no en otro menu.
    if (tutorialAbierto) {
        AlertDialog(
            onDismissRequest = { tutorialAbierto = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Hearing,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Cómo usar Voz a texto") },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .heightIn(max = 420.dp)
                ) {
                    PasoTutorial(
                        numero = "1",
                        texto = "Toca el botón ESCUCHAR y muéstrale la pantalla a la " +
                            "persona con la que quieres hablar."
                    )
                    PasoTutorial(
                        numero = "2",
                        texto = "La primera vez, el teléfono pedirá permiso para usar el " +
                            "micrófono. Toca Permitir: sin eso no puede oír a la otra persona."
                    )
                    PasoTutorial(
                        numero = "3",
                        texto = "Se abrirá el cuadro de voz de Google. Pídele que hable " +
                            "cerca del teléfono, en frases cortas."
                    )
                    PasoTutorial(
                        numero = "4",
                        texto = "Lo que diga aparecerá escrito en tu pantalla. Puedes " +
                            "corregir cualquier palabra tocando el texto."
                    )
                    PasoTutorial(
                        numero = "5",
                        texto = "Toca Responder para pasar a Texto a voz y contestar."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    MensajeEstado(
                        texto = "Si dijiste que no al permiso y te arrepentiste, puedes " +
                            "activarlo en Ajustes del teléfono, Aplicaciones, Comunícame, " +
                            "Permisos, Micrófono.",
                        tipo = TipoMensaje.INFO
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { tutorialAbierto = false }) { Text("Entendido") }
            }
        )
    }

    // Dialogo para guardar una frase nueva
    if (dialogoAbierto) {
        AlertDialog(
            onDismissRequest = {
                dialogoAbierto = false
                fraseNueva = ""
                fraseEnEdicion = null
            },
            // El mismo dialogo sirve para crear y para editar; el titulo dice
            // cual de las dos cosas esta pasando.
            title = {
                Text(
                    if (fraseEnEdicion == null) "Guardar una frase"
                    else "Editar la frase"
                )
            },
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
                    enabled = !frasesViewModel.cargando,
                    onClick = {
                        val enEdicion = fraseEnEdicion
                        val alTerminar = {
                            vibrar(context, PatronVibracion.EXITO)
                            fraseNueva = ""
                            fraseEnEdicion = null
                            dialogoAbierto = false
                        }

                        if (enEdicion == null) {
                            frasesViewModel.agregar(fraseNueva) { alTerminar() }
                        } else {
                            frasesViewModel.editar(enEdicion.id, fraseNueva) { alTerminar() }
                        }
                    }
                ) {
                    Text(if (fraseEnEdicion == null) "Guardar" else "Actualizar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    dialogoAbierto = false
                    fraseNueva = ""
                    fraseEnEdicion = null
                }) {
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
    alEditar: () -> Unit,
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
            IconButton(onClick = alEditar) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    // La descripcion incluye la frase: con el lector de
                    // pantalla, "Editar" a secas no dice cual de todas.
                    contentDescription = "Editar la frase: $frase",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
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
    ComunicameTheme { ComunicarScreen(uid = "vista-previa") }
}


// Un paso numerado del tutorial
@Composable
private fun PasoTutorial(numero: String, texto: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = numero,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(26.dp)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}
