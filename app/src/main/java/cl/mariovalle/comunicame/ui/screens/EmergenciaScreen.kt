package cl.mariovalle.comunicame.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.HearingDisabled
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.mariovalle.comunicame.data.RepositorioUsuarios
import cl.mariovalle.comunicame.data.ServicioEmergencia
import cl.mariovalle.comunicame.data.serviciosEmergencia
import cl.mariovalle.comunicame.ui.components.MensajeEstado
import cl.mariovalle.comunicame.ui.components.PatronVibracion
import cl.mariovalle.comunicame.ui.components.TipoMensaje
import cl.mariovalle.comunicame.ui.components.vibrar
import cl.mariovalle.comunicame.ui.theme.ComunicameTheme

// EMERGENCIA.
// La idea de fondo: una persona sorda no puede hablar por telefono. Asi que lo
// primero no es marcar, es la tarjeta grande que se le muestra al que este al
// lado. Los numeros van despues, para que marque esa persona.
@Composable
fun EmergenciaScreen(nombreUsuario: String) {
    val context = LocalContext.current
    val usuario = RepositorioUsuarios.buscarPorUsuario(nombreUsuario)

    var avisoMarcador by remember { mutableStateOf<String?>(null) }

    // Abre el marcador del sistema con el numero ya cargado.
    //
    // try/catch porque startActivity confia en que el equipo tenga una app de
    // telefono, y eso no siempre es cierto: una tablet sin modulo celular no
    // tiene marcador y lanza ActivityNotFoundException. En una pantalla de
    // emergencia un cierre inesperado es lo peor que puede pasar.
    fun abrirMarcador(servicio: ServicioEmergencia) {
        vibrar(context, PatronVibracion.TOQUE)
        try {
            // ACTION_DIAL abre el marcador con el numero cargado y NO requiere
            // el permiso CALL_PHONE: la llamada la confirma quien tenga el
            // telefono en la mano.
            val intento = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${servicio.numero}"))
            context.startActivity(intento)
            avisoMarcador = null
        } catch (e: ActivityNotFoundException) {
            // Sin app de telefono: al menos dejo el numero a la vista para que
            // alguien lo marque desde otro aparato.
            avisoMarcador = "Este dispositivo no tiene marcador. " +
                "Marca el ${servicio.numero} desde otro teléfono."
            vibrar(context, PatronVibracion.ERROR)
        } catch (e: SecurityException) {
            avisoMarcador = "El sistema bloqueó la apertura del marcador."
            vibrar(context, PatronVibracion.ERROR)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {

        Text(
            text = "Muestra esta pantalla",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Enséñale el teléfono a la persona que tengas cerca.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.HearingDisabled,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(52.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "NO ESCUCHO",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Necesito ayuda. Por favor escríbeme o llama a un servicio de emergencia.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Números de emergencia",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Toca uno para abrir el marcador del teléfono.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Solo aparece si el marcador fallo
        avisoMarcador?.let { aviso ->
            Spacer(modifier = Modifier.height(12.dp))
            MensajeEstado(texto = aviso, tipo = TipoMensaje.ERROR)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TABLA. Encabezado y una fila por servicio, cada fila abre el marcador.
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    CeldaEncabezado("Número", Modifier.width(84.dp))
                    CeldaEncabezado("Servicio", Modifier.weight(1f))
                }

                serviciosEmergencia.forEachIndexed { indice, servicio ->
                    if (indice > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { abrirMarcador(servicio) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = servicio.numero,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(84.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = servicio.nombre,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                // descripcionCorta es polimorfica: aunque
                                // recorro la lista como ServicioEmergencia,
                                // cada subclase responde lo suyo. El SAMU
                                // agrega "urgencia vital" y los policiales su
                                // jurisdiccion.
                                text = servicio.descripcionCorta(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.Call,
                            // etiquetaAccesible viene de la interfaz Contactable.
                            // El servicio medico la sobreescribe para avisarle
                            // a TalkBack que es urgencia vital.
                            contentDescription = servicio.etiquetaAccesible(),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Mis datos",
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
                FilaDato("Nombre", usuario?.nombre ?: "—")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                FilaDato("Comuna", usuario?.comuna ?: "—")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                FilaDato("Se comunica por", usuario?.preferencia?.etiqueta ?: "—")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                FilaDato("Correo", usuario?.correo ?: "—")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        MensajeEstado(
            texto = "Contacto de emergencia y ficha médica: esta funcionalidad está en " +
                "proceso. Queda documentada para futuras entregas.",
            tipo = TipoMensaje.INFO
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Celda del encabezado de la tabla
@Composable
private fun CeldaEncabezado(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun EmergenciaScreenPreview() {
    ComunicameTheme { EmergenciaScreen(nombreUsuario = "ana") }
}
