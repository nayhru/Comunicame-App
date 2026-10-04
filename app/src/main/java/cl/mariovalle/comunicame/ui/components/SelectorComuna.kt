package cl.mariovalle.comunicame.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.mariovalle.comunicame.data.ValidadorFormularios

// Selector de comuna con buscador, agrupado por region.
//
// Con mas de setenta comunas, una lista plana obliga a desplazarse a ciegas.
// Agrupar por region da una referencia y el buscador resuelve el caso de quien
// ya sabe lo que busca y prefiere escribirlo.
//
// La lista va en LazyColumn con alto limitado: sin eso crece hasta empujar los
// botones del dialogo fuera de la pantalla, que es justo lo que pasaba antes.
@Composable
fun SelectorComuna(
    comunaElegida: String,
    alElegir: (String) -> Unit,
    alCerrar: () -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    val resultados = ValidadorFormularios.buscarComunas(busqueda)

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text("Elige tu comuna") },
        text = {
            Column {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    label = { Text("Buscar comuna") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.padding(top = 12.dp))

                if (resultados.isEmpty()) {
                    Text(
                        text = "No encontramos esa comuna. Prueba con otro nombre " +
                            "o elige \"Otra comuna\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        // Alto maximo para que el dialogo no tape sus propios
                        // botones en pantallas chicas.
                        modifier = Modifier.heightIn(max = 320.dp)
                    ) {
                        resultados.forEach { (region, comunas) ->
                            item(key = "region-$region") {
                                Text(
                                    text = region,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                                )
                                HorizontalDivider()
                            }

                            items(comunas, key = { "comuna-$it" }) { comuna ->
                                FilaComuna(
                                    nombre = comuna,
                                    seleccionada = comuna == comunaElegida,
                                    alTocar = {
                                        alElegir(comuna)
                                        alCerrar()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = alCerrar) { Text("Cancelar") }
        }
    )
}

@Composable
private fun FilaComuna(
    nombre: String,
    seleccionada: Boolean,
    alTocar: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { alTocar() }
            .padding(vertical = 2.dp)
    ) {
        RadioButton(selected = seleccionada, onClick = alTocar)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = nombre, style = MaterialTheme.typography.bodyLarge)
    }
}
