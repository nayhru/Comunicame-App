package cl.mariovalle.comunicame.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.mariovalle.comunicame.ui.screens.LoginScreen
import cl.mariovalle.comunicame.ui.screens.PanelPrincipal
import cl.mariovalle.comunicame.ui.screens.RecuperarScreen
import cl.mariovalle.comunicame.ui.screens.RegistroScreen
import cl.mariovalle.comunicame.ui.viewmodel.SesionViewModel

// Grafo de navegacion.
//
// Las pantallas no reciben el NavController, reciben lambdas (onIrA..., onVolver).
// Asi la navegacion queda toda aca y las views se pueden ver con @Preview sin
// tener que montar el grafo completo.
//
// El SesionViewModel se crea una sola vez en este nivel y se pasa a las
// pantallas que lo necesitan. Si cada una creara el suyo, el registro no
// dejaria la sesion iniciada para el panel.
@Composable
fun NavegacionApp(
    sesionViewModel: SesionViewModel = viewModel(factory = SesionViewModel.fabrica())
) {
    val navController = rememberNavController()

    // Mientras se comprueba si habia una sesion guardada no se dibuja nada
    // definitivo: si se mostrara el Login y resultara que si habia sesion, el
    // usuario veria la pantalla de acceso parpadear antes de entrar.
    if (sesionViewModel.comprobandoSesion) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Quien ya tenia sesion entra directo al panel. Es el efecto visible de
    // guardar la sesion en SharedPreferences: la aplicacion no vuelve a pedir
    // la contrasena cada vez que se abre.
    val destinoInicial = if (sesionViewModel.haySesion) Rutas.PANEL else Rutas.LOGIN

    NavHost(
        navController = navController,
        startDestination = destinoInicial
    ) {

        // LOGIN
        composable(Rutas.LOGIN) {
            LoginScreen(
                sesionViewModel = sesionViewModel,
                onLoginExitoso = {
                    navController.navigate(Rutas.PANEL) {
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
                sesionViewModel = sesionViewModel,
                onRegistroExitoso = {
                    // Al registrarse la sesion ya queda iniciada, asi que entra
                    // directo al panel en vez de volver a pedir las credenciales
                    // que acaba de escribir.
                    navController.navigate(Rutas.PANEL) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // RECUPERAR CONTRASENA
        composable(Rutas.RECUPERAR) {
            RecuperarScreen(
                sesionViewModel = sesionViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        // PANEL. Adentro tiene su propio NavHost con las 3 secciones.
        composable(Rutas.PANEL) {
            val usuario = sesionViewModel.usuario

            if (usuario == null) {
                // La sesion se cayo estando dentro: vuelve al Login en vez de
                // mostrar un panel vacio.
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            } else {
                PanelPrincipal(
                    usuario = usuario,
                    sesionViewModel = sesionViewModel,
                    onCerrarSesion = {
                        sesionViewModel.cerrarSesion {
                            navController.navigate(Rutas.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}
