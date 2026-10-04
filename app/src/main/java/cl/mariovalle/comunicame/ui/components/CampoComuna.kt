package cl.mariovalle.comunicame.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

// Campo que muestra la comuna elegida y abre el selector al tocarlo.
//
// Se dibuja a mano en vez de usar un OutlinedTextField con readOnly: un campo
// de texto captura el toque aunque sea de solo lectura, de modo que el
// clickable encima nunca se dispara y el selector no abre.
//
// Al ser un boton de verdad, el lector de pantalla tambien lo anuncia como
// tal, en vez de ofrecer un cuadro de texto donde no se puede escribir.
@Composable
fun CampoComuna(
    comuna: String,
    alTocar: () -> Unit,
    region: String? = null,
    modifier: Modifier = Modifier
) {
    val sinElegir = comuna.isBlank()

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(
                    role = Role.Button,
                    onClick = alTocar
                )
                .semantics {
                    contentDescription = if (sinElegir) {
                        "Elegir comuna"
                    } else {
                        "Comuna: $comuna. Tocar para cambiarla"
                    }
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Comuna",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (sinElegir) "Toca para elegir" else comuna,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (sinElegir) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Confirma la region cuando hay comunas con nombres parecidos
        region?.let {
            Text(
                text = "Región: $it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}
