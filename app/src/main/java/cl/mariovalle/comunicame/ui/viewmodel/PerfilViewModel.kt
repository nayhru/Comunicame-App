package cl.mariovalle.comunicame.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import cl.mariovalle.comunicame.data.RepositorioComunicame
import cl.mariovalle.comunicame.data.ResultadoOperacion
import cl.mariovalle.comunicame.data.Usuario
import cl.mariovalle.comunicame.data.proveedorDeRepositorio
import kotlinx.coroutines.launch

// Edicion y eliminacion de la cuenta.
//
// Son las operaciones de actualizar y borrar sobre los datos del usuario, las
// dos que faltaban para completar el CRUD: hasta la entrega anterior una
// cuenta se podia crear y consultar, pero no modificar ni dar de baja.
class PerfilViewModel(
    private val repositorio: RepositorioComunicame = proveedorDeRepositorio()
) : ViewModel() {

    var guardando by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var aviso by mutableStateOf<String?>(null)
        private set

    // Corrutina que borra el aviso pasados unos segundos. Se guarda para poder
    // cancelarla: si se guardan dos cambios seguidos, el temporizador del
    // primero no debe apagar el aviso del segundo apenas aparece.
    private var borradoDelAviso: Job? = null

    fun actualizar(usuario: Usuario, alGuardar: (Usuario) -> Unit = {}) {
        viewModelScope.launch {
            guardando = true
            error = null

            when (val resultado = repositorio.actualizarUsuario(usuario)) {
                is ResultadoOperacion.Exito -> {
                    mostrarAviso("Perfil actualizado")
                    alGuardar(resultado.dato)
                }
                is ResultadoOperacion.Error -> error = resultado.mensaje
            }

            guardando = false
        }
    }

    // Borra la cuenta y todo lo que cuelga de ella. No tiene vuelta atras, asi
    // que la pantalla pide confirmacion antes de llamar aqui.
    fun eliminarCuenta(uid: String, alEliminar: () -> Unit = {}) {
        viewModelScope.launch {
            guardando = true
            error = null

            when (val resultado = repositorio.eliminarCuenta(uid)) {
                is ResultadoOperacion.Exito -> alEliminar()
                is ResultadoOperacion.Error -> error = resultado.mensaje
            }

            guardando = false
        }
    }

    fun limpiarMensajes() {
        borradoDelAviso?.cancel()
        error = null
        aviso = null
    }

    // Muestra un aviso de exito y lo retira solo.
    //
    // Un banner de confirmacion que se queda indefinidamente deja de informar:
    // al rato ya no se sabe si corresponde a lo que uno acaba de hacer o a un
    // cambio de hace diez minutos. Los errores si permanecen, porque describen
    // algo que la persona todavia tiene que resolver.
    private fun mostrarAviso(texto: String) {
        borradoDelAviso?.cancel()
        aviso = texto

        borradoDelAviso = viewModelScope.launch {
            delay(DURACION_AVISO_MS)
            aviso = null
        }
    }

    companion object {
        // Suficiente para leerlo sin apuro. La pauta de accesibilidad de
        // Android sugiere no bajar de cinco segundos en mensajes que la
        // persona no pidio ver.
        private const val DURACION_AVISO_MS = 5_000L
    }
}
