package cl.mariovalle.comunicame.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun actualizar(usuario: Usuario, alGuardar: (Usuario) -> Unit = {}) {
        viewModelScope.launch {
            guardando = true
            error = null

            when (val resultado = repositorio.actualizarUsuario(usuario)) {
                is ResultadoOperacion.Exito -> {
                    aviso = "Perfil actualizado"
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
        error = null
        aviso = null
    }
}
