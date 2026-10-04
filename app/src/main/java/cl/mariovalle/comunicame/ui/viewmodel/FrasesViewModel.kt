package cl.mariovalle.comunicame.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mariovalle.comunicame.data.FraseGuardada
import cl.mariovalle.comunicame.data.RepositorioComunicame
import cl.mariovalle.comunicame.data.ResultadoOperacion
import cl.mariovalle.comunicame.data.ValidadorFormularios
import cl.mariovalle.comunicame.data.proveedorDeRepositorio
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Las frases guardadas del usuario: listar, agregar, editar y eliminar.
//
// Es el CRUD completo de la aplicacion sobre datos que la persona crea. Antes
// vivian en una lista en memoria y se perdian al cerrar; para alguien que usa
// la aplicacion para comunicarse eso significaba volver a escribir cada dia lo
// mismo que ya habia escrito.
class FrasesViewModel(
    private val repositorio: RepositorioComunicame = proveedorDeRepositorio()
) : ViewModel() {

    var frases by mutableStateOf<List<FraseGuardada>>(emptyList())
        private set

    var cargando by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var aviso by mutableStateOf<String?>(null)
        private set

    // Igual que en el perfil: el aviso de exito se retira solo, el error no.
    private var borradoDelAviso: Job? = null

    // uid del usuario cuyas frases se estan mostrando. Se guarda para poder
    // recargar tras cada operacion sin que la pantalla lo repita.
    private var uid: String? = null

    fun cargar(uidUsuario: String) {
        uid = uidUsuario

        viewModelScope.launch {
            cargando = true
            error = null

            when (val resultado = repositorio.listarFrases(uidUsuario)) {
                is ResultadoOperacion.Exito -> frases = resultado.dato
                is ResultadoOperacion.Error -> error = resultado.mensaje
            }

            cargando = false
        }
    }

    fun agregar(texto: String, alGuardar: () -> Unit = {}) {
        val uidActual = uid ?: return

        val validacion = ValidadorFormularios.validarFrase(texto)
        if (!validacion.esValido) {
            error = validacion.mensajeError
            return
        }

        // Evita duplicados: guardar dos veces la misma frase solo alarga la
        // lista que la persona tiene que recorrer para encontrar la que busca.
        val repetida = frases.any { it.texto.equals(texto.trim(), ignoreCase = true) }
        if (repetida) {
            error = "Esa frase ya está en tus guardadas"
            return
        }

        viewModelScope.launch {
            cargando = true
            error = null

            when (val resultado = repositorio.agregarFrase(uidActual, texto)) {
                is ResultadoOperacion.Exito -> {
                    // Se agrega al principio sin volver a consultar: la lista
                    // va ordenada por fecha y esta es la mas reciente.
                    frases = listOf(resultado.dato) + frases
                    mostrarAviso("Frase guardada")
                    alGuardar()
                }
                is ResultadoOperacion.Error -> error = resultado.mensaje
            }

            cargando = false
        }
    }

    fun editar(fraseId: String, textoNuevo: String, alEditar: () -> Unit = {}) {
        val uidActual = uid ?: return

        val validacion = ValidadorFormularios.validarFrase(textoNuevo)
        if (!validacion.esValido) {
            error = validacion.mensajeError
            return
        }

        viewModelScope.launch {
            cargando = true
            error = null

            when (val resultado = repositorio.editarFrase(uidActual, fraseId, textoNuevo)) {
                is ResultadoOperacion.Exito -> {
                    frases = frases.map { si ->
                        if (si.id == fraseId) resultado.dato else si
                    }
                    mostrarAviso("Frase actualizada")
                    alEditar()
                }
                is ResultadoOperacion.Error -> error = resultado.mensaje
            }

            cargando = false
        }
    }

    fun eliminar(fraseId: String) {
        val uidActual = uid ?: return

        viewModelScope.launch {
            cargando = true
            error = null

            when (val resultado = repositorio.eliminarFrase(uidActual, fraseId)) {
                is ResultadoOperacion.Exito -> {
                    frases = frases.filterNot { it.id == fraseId }
                    mostrarAviso("Frase eliminada")
                }
                is ResultadoOperacion.Error -> error = resultado.mensaje
            }

            cargando = false
        }
    }

    fun limpiarMensajes() {
        borradoDelAviso?.cancel()
        error = null
        aviso = null
    }

    // Muestra la confirmacion y la retira sola. Dejarla fija haria que, al
    // rato, no se supiera si corresponde a la ultima accion o a una anterior.
    private fun mostrarAviso(texto: String) {
        borradoDelAviso?.cancel()
        aviso = texto

        borradoDelAviso = viewModelScope.launch {
            delay(DURACION_AVISO_MS)
            aviso = null
        }
    }

    companion object {
        private const val DURACION_AVISO_MS = 5_000L
    }
}
