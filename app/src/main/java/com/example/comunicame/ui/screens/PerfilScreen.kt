package com.example.comunicame.ui.screens

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
import com.example.comunicame.data.RepositorioFrases
import com.example.comunicame.data.RepositorioUsuarios
import com.example.comunicame.ui.theme.ComunicameTheme
import com.example.comunicame.util.contarQue
import com.example.comunicame.util.criterioDeLargo
import com.example.comunicame.util.resumen

// MI PERFIL. Los datos del registro y las preferencias que eligio.
// Abajo listo lo que falta por hacer, para que el alcance quede claro dentro de
// la misma app.
@Composable
fun PerfilScreen(nombreUsuario: String) {
    // Solo los datos de quien tiene la sesion abierta
    val usuario = RepositorioUsuarios.buscarPorUsuario(nombreUsuario)

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
                    text = usuario?.nombre ?: nombreUsuario,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "@${usuario?.usuario ?: nombreUsuario}",
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
                FilaPerfil("Correo", usuario?.correo ?: "—")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                FilaPerfil("Comuna", usuario?.comuna ?: "—")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                FilaPerfil("Se comunica por", usuario?.preferencia?.etiqueta ?: "—")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                FilaPerfil("Frases guardadas", "${RepositorioFrases.guardadas.size}")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                // resumen() es una extension de List<String>: junta las dos
                // primeras y cuenta el resto
                FilaPerfil("Mis frases", RepositorioFrases.guardadas.toList().resumen())
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                // contarQue recibe una lambda como criterio
                FilaPerfil(
                    "Frases cortas",
                    "${RepositorioFrases.guardadas.toList().contarQue(criterioDeLargo(25))}"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                FilaPreferencia("Alertas visuales en pantalla", usuario?.alertasVisuales ?: false)
                Spacer(modifier = Modifier.height(10.dp))
                FilaPreferencia("Vibración en cada aviso", usuario?.vibracion ?: false)
                Spacer(modifier = Modifier.height(10.dp))
                FilaPreferencia("Subtítulos siempre activos", usuario?.subtitulos ?: false)
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

        Spacer(modifier = Modifier.height(24.dp))
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
    ComunicameTheme { PerfilScreen(nombreUsuario = "ana") }
}
