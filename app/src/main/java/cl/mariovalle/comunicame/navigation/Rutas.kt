package cl.mariovalle.comunicame.navigation

// Todas las rutas en un solo lugar. navigate() no valida el string, asi que si
// uno lo escribe mal la app compila igual y revienta al ejecutar. Con
// constantes eso no pasa.
object Rutas {

    // Grafo principal
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val RECUPERAR = "recuperar"

    // El panel ya no lleva el usuario en la ruta: lo toma del ViewModel de
    // sesion. Pasarlo por la URL obligaba a reconsultarlo en cada pantalla y
    // dejaba el nombre a la vista en la pila de navegacion.
    const val PANEL = "panel"

    // Grafo interno del panel, el de la barra inferior
    const val COMUNICAR = "comunicar"
    const val EMERGENCIA = "emergencia"
    const val PERFIL = "perfil"
}
