package cl.mariovalle.comunicame.navigation

// Todas las rutas en un solo lugar. navigate() no valida el string, asi que si
// uno lo escribe mal la app compila igual y revienta al ejecutar. Con
// constantes eso no pasa.
object Rutas {

    // Grafo principal
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val RECUPERAR = "recuperar"

    // El panel recibe el usuario como argumento
    const val ARG_USUARIO = "usuario"
    const val PANEL = "panel/{$ARG_USUARIO}"

    fun panelDe(usuario: String) = "panel/$usuario"

    // Grafo interno del panel, el de la barra inferior
    const val COMUNICAR = "comunicar"
    const val EMERGENCIA = "emergencia"
    const val PERFIL = "perfil"
}
