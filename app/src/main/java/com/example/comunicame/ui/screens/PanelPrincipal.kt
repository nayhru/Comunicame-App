package com.example.comunicame.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.comunicame.data.RepositorioUsuarios
import com.example.comunicame.navigation.Rutas

// Cada item de la barra inferior
private data class Seccion(
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector
)

private val secciones = listOf(
    Seccion(Rutas.COMUNICAR, "Comunicar", Icons.Filled.Forum),
    Seccion(Rutas.EMERGENCIA, "Emergencia", Icons.Filled.Emergency),
    Seccion(Rutas.PERFIL, "Mi perfil", Icons.Filled.Person)
)

// PANEL. Lo que ve el usuario ya logueado.
// Tiene su propio NavHost, aparte del de Login/Registro/Recuperar, asi la barra
// inferior cambia de seccion sin arrastrar las pantallas de acceso en la pila.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelPrincipal(
    nombreUsuario: String,
    onCerrarSesion: () -> Unit
) {
    val navInterno = rememberNavController()

    // Miro la entrada actual de la pila para saber que item marcar
    val entradaActual by navInterno.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    val usuario = RepositorioUsuarios.usuarios.find { it.usuario == nombreUsuario }
    val saludo = usuario?.nombre?.substringBefore(" ") ?: nombreUsuario

    val titulo = when (rutaActual) {
        Rutas.EMERGENCIA -> "Emergencia"
        Rutas.PERFIL -> "Mi perfil"
        else -> "Hola, $saludo"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                secciones.forEach { seccion ->
                    val seleccionada = rutaActual == seccion.ruta

                    NavigationBarItem(
                        selected = seleccionada,
                        onClick = {
                            navInterno.navigate(seccion.ruta) {
                                // Sin esto la pila crece cada vez que uno
                                // cambia de seccion
                                popUpTo(navInterno.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = seccion.icono,
                                contentDescription = null // la etiqueta ya lo dice
                            )
                        },
                        // Etiqueta siempre visible. Un icono solo obliga a adivinar.
                        label = { Text(seccion.etiqueta) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { relleno ->

        NavHost(
            navController = navInterno,
            startDestination = Rutas.COMUNICAR,
            modifier = Modifier.padding(relleno)
        ) {
            composable(Rutas.COMUNICAR) { ComunicarScreen() }
            composable(Rutas.EMERGENCIA) { EmergenciaScreen(nombreUsuario = nombreUsuario) }
            composable(Rutas.PERFIL) { PerfilScreen(nombreUsuario = nombreUsuario) }
        }
    }
}
