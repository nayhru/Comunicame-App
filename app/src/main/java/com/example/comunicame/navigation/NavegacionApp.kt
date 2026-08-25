package com.example.comunicame.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.comunicame.ui.screens.LoginScreen
import com.example.comunicame.ui.screens.PanelPrincipal
import com.example.comunicame.ui.screens.RecuperarScreen
import com.example.comunicame.ui.screens.RegistroScreen

// Grafo de navegacion. El destino de inicio es Login.
// Las pantallas no reciben el NavController, reciben lambdas (onIrA..., onVolver).
// Asi la navegacion queda toda aca y las views se pueden ver con @Preview sin
// tener que montar el grafo completo.
@Composable
fun NavegacionApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN
    ) {

        // LOGIN, destino de inicio
        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginExitoso = { usuario ->
                    navController.navigate(Rutas.panelDe(usuario.usuario)) {
                        // Saco el Login de la pila. Si no, apretando Atras el
                        // usuario ya logueado vuelve a la pantalla de acceso.
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                },
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) },
                onIrARecuperar = { navController.navigate(Rutas.RECUPERAR) }
            )
        }

        // REGISTRO
        composable(Rutas.REGISTRO) {
            RegistroScreen(
                onRegistroExitoso = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }

        // RECUPERAR CONTRASENA
        composable(Rutas.RECUPERAR) {
            RecuperarScreen(
                onVolver = { navController.popBackStack() }
            )
        }

        // PANEL. Adentro tiene su propio NavHost con las 3 secciones.
        composable(
            route = Rutas.PANEL,
            arguments = listOf(
                navArgument(Rutas.ARG_USUARIO) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Rescato el argumento que viene en la ruta
            val usuario = backStackEntry.arguments?.getString(Rutas.ARG_USUARIO).orEmpty()

            PanelPrincipal(
                nombreUsuario = usuario,
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
