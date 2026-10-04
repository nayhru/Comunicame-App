package cl.mariovalle.comunicame.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.mariovalle.comunicame.ui.theme.AmbarAviso
import cl.mariovalle.comunicame.ui.theme.AmbarAvisoSuave
import cl.mariovalle.comunicame.ui.theme.CelesteOscuro
import cl.mariovalle.comunicame.ui.theme.CelesteSuave
import cl.mariovalle.comunicame.ui.theme.ComunicameTheme
import cl.mariovalle.comunicame.ui.theme.RojoError
import cl.mariovalle.comunicame.ui.theme.RojoErrorSuave
import cl.mariovalle.comunicame.ui.theme.VerdeExito
import cl.mariovalle.comunicame.ui.theme.VerdeExitoSuave

// Tipos de aviso que ve el usuario
enum class TipoMensaje(
    val icono: ImageVector,
    val colorTexto: Color,
    val colorFondo: Color,
    val prefijo: String
) {
    // Contrastes medidos contra la WCAG
    EXITO(Icons.Filled.CheckCircle, VerdeExito, VerdeExitoSuave, "Listo"),        // 10.07:1
    ERROR(Icons.Filled.Error, RojoError, RojoErrorSuave, "Error"),                //  8.84:1
    AVISO(Icons.Filled.Warning, AmbarAviso, AmbarAvisoSuave, "Atencion"),         // 10.35:1
    INFO(Icons.Filled.Info, CelesteOscuro, CelesteSuave, "Informacion")           // 10.80:1
}

// Banner de estado. Regla de la app: el estado va siempre por icono, color Y
// texto. Solo color deja fuera a quien es daltonico, y el sonido no sirve aca.
// liveRegion hace que TalkBack lo lea apenas aparece.
@Composable
fun MensajeEstado(
    texto: String,
    tipo: TipoMensaje,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(tipo.colorFondo, RoundedCornerShape(12.dp))
            .border(2.dp, tipo.colorTexto, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = tipo.icono,
            contentDescription = tipo.prefijo,
            tint = tipo.colorTexto,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = texto,
            color = tipo.colorTexto,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun MensajeEstadoPreview() {
    ComunicameTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MensajeEstado("Sesión iniciada correctamente", TipoMensaje.EXITO)
            MensajeEstado("Usuario o contraseña incorrectos", TipoMensaje.ERROR)
            MensajeEstado("La contrasena debe tener 8 caracteres", TipoMensaje.AVISO)
            MensajeEstado("Escribe un mensaje y presiona Reproducir", TipoMensaje.INFO)
        }
    }
}
