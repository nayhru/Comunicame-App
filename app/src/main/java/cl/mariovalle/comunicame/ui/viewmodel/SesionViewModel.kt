package cl.mariovalle.comunicame.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cl.mariovalle.comunicame.data.RepositorioComunicame
import cl.mariovalle.comunicame.data.ResultadoOperacion
import cl.mariovalle.comunicame.data.SesionLocal
import cl.mariovalle.comunicame.data.Usuario
import cl.mariovalle.comunicame.data.ValidadorFormularios
import cl.mariovalle.comunicame.data.PreferenciaComunicacion
import cl.mariovalle.comunicame.data.proveedorDeRepositorio
import kotlinx.coroutines.launch

// Estado y operaciones de la sesion: entrar, registrarse, recuperar y salir.
//
// Vive en un ViewModel y no dentro del Composable porque una peticion a la red
// tarda mas que un giro de pantalla: si el estado estuviera en un remember, al
// rotar el telefono en medio del login la respuesta llegaria a una pantalla que
// ya no existe y el usuario veria el formulario vacio otra vez.
class SesionViewModel(
    aplicacion: Application,
    private val repositorio: RepositorioComunicame = proveedorDeRepositorio()
) : AndroidViewModel(aplicacion) {

    private val sesionLocal = SesionLocal(aplicacion)

    // Usuario con la sesion iniciada, o null si no hay ninguna.
    var usuario by mutableStateOf<Usuario?>(null)
        private set

    var cargando by mutableStateOf(false)
        private set

    // Mensaje de error para mostrar en pantalla. Se limpia al reintentar.
    var error by mutableStateOf<String?>(null)
        private set

    // Aviso de exito, por ejemplo tras enviar el correo de recuperacion.
    var aviso by mutableStateOf<String?>(null)
        private set

    // true mientras se comprueba si habia una sesion guardada. Evita que la
    // aplicacion muestre el Login por un instante antes de saltar al panel.
    var comprobandoSesion by mutableStateOf(true)
        private set

    val haySesion: Boolean
        get() = usuario != null

    init {
        restaurarSesion()
    }

    // Al abrir la aplicacion, SharedPreferences dice de inmediato si habia
    // alguien dentro. Los datos completos se piden despues a Firestore.
    private fun restaurarSesion() {
        val uidGuardado = sesionLocal.uid ?: repositorio.uidActual()

        if (uidGuardado.isNullOrBlank()) {
            comprobandoSesion = false
            return
        }

        viewModelScope.launch {
            when (val resultado = repositorio.obtenerUsuario(uidGuardado)) {
                is ResultadoOperacion.Exito -> usuario = resultado.dato
                is ResultadoOperacion.Error -> {
                    // La sesion guardada ya no sirve: la cuenta pudo borrarse
                    // desde otro dispositivo. Se limpia para no dejar al
                    // usuario atrapado en una pantalla que no carga.
                    sesionLocal.cerrar()
                    repositorio.cerrarSesion()
                }
            }
            comprobandoSesion = false
        }
    }

    fun iniciarSesion(correo: String, contrasena: String, alEntrar: () -> Unit = {}) {
        val validacion = ValidadorFormularios.validarLogin(correo, contrasena)
        if (!validacion.esValido) {
            error = validacion.mensajeError
            return
        }

        viewModelScope.launch {
            cargando = true
            error = null

            when (val resultado = repositorio.iniciarSesion(correo, contrasena)) {
                is ResultadoOperacion.Exito -> {
                    guardarSesion(resultado.dato)
                    alEntrar()
                }
                is ResultadoOperacion.Error -> error = resultado.mensaje
            }

            cargando = false
        }
    }

    fun registrar(
        nombre: String,
        nombreUsuario: String,
        correo: String,
        contrasena: String,
        repetir: String,
        comuna: String,
        preferencia: PreferenciaComunicacion,
        aceptaTerminos: Boolean,
        alertasVisuales: Boolean = true,
        vibracion: Boolean = true,
        subtitulos: Boolean = true,
        alRegistrar: () -> Unit = {}
    ) {
        val validacion = ValidadorFormularios.validarRegistro(
            nombre = nombre,
            usuario = nombreUsuario,
            correo = correo,
            contrasena = contrasena,
            repetir = repetir,
            comuna = comuna,
            aceptaTerminos = aceptaTerminos
        )
        if (!validacion.esValido) {
            error = validacion.mensajeError
            return
        }

        viewModelScope.launch {
            cargando = true
            error = null

            val nuevo = Usuario(
                nombre = nombre.trim(),
                usuario = nombreUsuario.trim(),
                correo = correo.trim(),
                comuna = comuna,
                preferencia = preferencia,
                // Las eligio en el formulario; sin esto se perdian y la cuenta
                // quedaba con los valores por omision.
                alertasVisuales = alertasVisuales,
                vibracion = vibracion,
                subtitulos = subtitulos
            )

            when (val resultado = repositorio.registrar(nuevo, contrasena)) {
                is ResultadoOperacion.Exito -> {
                    guardarSesion(resultado.dato)
                    alRegistrar()
                }
                is ResultadoOperacion.Error -> error = resultado.mensaje
            }

            cargando = false
        }
    }

    fun recuperarContrasena(correo: String) {
        val validacion = ValidadorFormularios.validarRecuperacion(correo)
        if (!validacion.esValido) {
            error = validacion.mensajeError
            return
        }

        viewModelScope.launch {
            cargando = true
            error = null
            aviso = null

            when (val resultado = repositorio.enviarCorreoDeRecuperacion(correo)) {
                is ResultadoOperacion.Exito ->
                    // El mensaje no confirma si el correo existia. Decirlo
                    // permitiria averiguar quien tiene cuenta en la aplicacion.
                    aviso = "Si ese correo tiene una cuenta, te enviamos un " +
                        "enlace para crear una contraseña nueva. Revisa tu bandeja."
                is ResultadoOperacion.Error -> error = resultado.mensaje
            }

            cargando = false
        }
    }

    // Actualiza el usuario en memoria tras editar el perfil, para que las
    // pantallas que lo muestran reflejen el cambio sin volver a consultar.
    fun refrescarUsuario(actualizado: Usuario) {
        usuario = actualizado
        sesionLocal.actualizarNombreUsuario(actualizado.usuario)
    }

    fun cerrarSesion(alSalir: () -> Unit = {}) {
        repositorio.cerrarSesion()
        sesionLocal.cerrar()
        usuario = null
        error = null
        aviso = null
        alSalir()
    }

    fun limpiarMensajes() {
        error = null
        aviso = null
    }

    private fun guardarSesion(conectado: Usuario) {
        usuario = conectado
        sesionLocal.guardar(
            uid = conectado.uid,
            usuario = conectado.usuario,
            correo = conectado.correo
        )
    }

    companion object {

        // Fabrica propia.
        //
        // La fabrica por omision solo sabe construir un AndroidViewModel que
        // reciba unicamente el Application. Este ademas recibe el repositorio,
        // que es lo que permite pasarle un doble en las pruebas, asi que hay
        // que decirle como armarlo.
        fun fabrica(repositorio: RepositorioComunicame? = null): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: androidx.lifecycle.viewmodel.CreationExtras
                ): T {
                    val aplicacion = extras[
                        ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                    ] as Application

                    return SesionViewModel(
                        aplicacion,
                        repositorio ?: proveedorDeRepositorio()
                    ) as T
                }
            }
    }
}
