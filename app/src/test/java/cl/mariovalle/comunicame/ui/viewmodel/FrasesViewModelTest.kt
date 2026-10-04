package cl.mariovalle.comunicame.ui.viewmodel

import cl.mariovalle.comunicame.data.FraseGuardada
import cl.mariovalle.comunicame.data.RepositorioComunicame
import cl.mariovalle.comunicame.data.ResultadoOperacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

// Pruebas del ViewModel de frases con un repositorio simulado.
//
// Aqui no interesa si Firestore guarda bien (eso lo cubren las pruebas del
// repositorio), sino si el ViewModel decide bien: que no mande a guardar una
// frase vacia, que no repita una que ya existe, que refleje el error cuando la
// operacion falla.
//
// Mockito crea un doble del repositorio que responde lo que cada prueba
// necesite, incluido un error de red, que de otro modo habria que provocar
// desconectando el equipo.
@OptIn(ExperimentalCoroutinesApi::class)
class FrasesViewModelTest {

    private val despachador = StandardTestDispatcher()

    private lateinit var repositorio: RepositorioComunicame
    private lateinit var viewModel: FrasesViewModel

    private val uid = "uid-1"

    @Before
    fun preparar() {
        // viewModelScope usa el despachador principal, que en una prueba de
        // JVM no existe. Se reemplaza por uno de prueba que ademas permite
        // controlar cuando corren las corrutinas.
        Dispatchers.setMain(despachador)
        repositorio = mock()
        viewModel = FrasesViewModel(repositorio)
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cargar pide las frases del usuario y las deja disponibles`() = runTest {
        val guardadas = listOf(
            FraseGuardada(id = "f-1", texto = "Hola", creadaEn = 2),
            FraseGuardada(id = "f-2", texto = "Gracias", creadaEn = 1)
        )
        whenever(repositorio.listarFrases(uid)).thenReturn(ResultadoOperacion.Exito(guardadas))

        viewModel.cargar(uid)
        advanceUntilIdle()

        assertEquals(2, viewModel.frases.size)
        assertEquals("Hola", viewModel.frases.first().texto)
        assertFalse(viewModel.cargando)
    }

    @Test
    fun `si la carga falla se muestra el error y la lista queda vacia`() = runTest {
        whenever(repositorio.listarFrases(uid)).thenReturn(
            ResultadoOperacion.Error("No hay conexión a internet.", recuperable = true)
        )

        viewModel.cargar(uid)
        advanceUntilIdle()

        assertEquals("No hay conexión a internet.", viewModel.error)
        assertTrue(viewModel.frases.isEmpty())
    }

    @Test
    fun `no manda a guardar una frase vacia`() = runTest {
        whenever(repositorio.listarFrases(uid)).thenReturn(ResultadoOperacion.Exito(emptyList()))
        viewModel.cargar(uid)
        advanceUntilIdle()

        viewModel.agregar("   ")
        advanceUntilIdle()

        // Lo que se comprueba es que la peticion nunca sale: validar en el
        // dispositivo evita una ida a la red que ya se sabe que va a fallar.
        verify(repositorio, never()).agregarFrase(any(), any())
        assertEquals("Escribe una frase para guardarla", viewModel.error)
    }

    @Test
    fun `no manda a guardar una frase que ya existe`() = runTest {
        whenever(repositorio.listarFrases(uid)).thenReturn(
            ResultadoOperacion.Exito(listOf(FraseGuardada(id = "f-1", texto = "Hola")))
        )
        viewModel.cargar(uid)
        advanceUntilIdle()

        viewModel.agregar("hola")
        advanceUntilIdle()

        // Comparacion sin distinguir mayusculas: para quien busca una frase en
        // su lista, "Hola" y "hola" son la misma y solo alargan el recorrido.
        verify(repositorio, never()).agregarFrase(any(), any())
        assertEquals("Esa frase ya está en tus guardadas", viewModel.error)
    }

    @Test
    fun `agregar deja la frase nueva al principio de la lista`() = runTest {
        val anterior = FraseGuardada(id = "f-1", texto = "Gracias", creadaEn = 1)
        whenever(repositorio.listarFrases(uid)).thenReturn(
            ResultadoOperacion.Exito(listOf(anterior))
        )
        val nueva = FraseGuardada(id = "f-2", texto = "Necesito ayuda", creadaEn = 2)
        whenever(repositorio.agregarFrase(eq(uid), any())).thenReturn(
            ResultadoOperacion.Exito(nueva)
        )

        viewModel.cargar(uid)
        advanceUntilIdle()
        viewModel.agregar("Necesito ayuda")
        advanceUntilIdle()

        // Las ultimas primero: son las que la persona acaba de usar.
        assertEquals("Necesito ayuda", viewModel.frases.first().texto)
        assertEquals(2, viewModel.frases.size)
    }

    @Test
    fun `editar reemplaza solo la frase correspondiente`() = runTest {
        val primera = FraseGuardada(id = "f-1", texto = "Hola", creadaEn = 2)
        val segunda = FraseGuardada(id = "f-2", texto = "Gracias", creadaEn = 1)
        whenever(repositorio.listarFrases(uid)).thenReturn(
            ResultadoOperacion.Exito(listOf(primera, segunda))
        )
        whenever(repositorio.editarFrase(uid, "f-1", "Hola, buenos días")).thenReturn(
            ResultadoOperacion.Exito(primera.copy(texto = "Hola, buenos días"))
        )

        viewModel.cargar(uid)
        advanceUntilIdle()
        viewModel.editar("f-1", "Hola, buenos días")
        advanceUntilIdle()

        assertEquals("Hola, buenos días", viewModel.frases.first().texto)
        assertEquals("Gracias", viewModel.frases[1].texto)
    }

    @Test
    fun `eliminar quita la frase de la lista mostrada`() = runTest {
        val primera = FraseGuardada(id = "f-1", texto = "Hola", creadaEn = 2)
        val segunda = FraseGuardada(id = "f-2", texto = "Gracias", creadaEn = 1)
        whenever(repositorio.listarFrases(uid)).thenReturn(
            ResultadoOperacion.Exito(listOf(primera, segunda))
        )
        whenever(repositorio.eliminarFrase(uid, "f-1")).thenReturn(
            ResultadoOperacion.Exito(Unit)
        )

        viewModel.cargar(uid)
        advanceUntilIdle()
        viewModel.eliminar("f-1")
        advanceUntilIdle()

        assertEquals(1, viewModel.frases.size)
        assertEquals("Gracias", viewModel.frases.first().texto)
    }

    @Test
    fun `si eliminar falla la frase sigue en la lista`() = runTest {
        val frase = FraseGuardada(id = "f-1", texto = "Hola", creadaEn = 1)
        whenever(repositorio.listarFrases(uid)).thenReturn(
            ResultadoOperacion.Exito(listOf(frase))
        )
        whenever(repositorio.eliminarFrase(uid, "f-1")).thenReturn(
            ResultadoOperacion.Error("No hay conexión a internet.", recuperable = true)
        )

        viewModel.cargar(uid)
        advanceUntilIdle()
        viewModel.eliminar("f-1")
        advanceUntilIdle()

        // No se quita de la pantalla algo que el servidor no alcanzo a borrar:
        // la persona creeria que lo elimino y reaparecería al volver a entrar.
        assertEquals(1, viewModel.frases.size)
        assertEquals("No hay conexión a internet.", viewModel.error)
    }

    @Test
    fun `sin usuario cargado no se intenta guardar nada`() = runTest {
        // cargar() nunca se llamo, asi que el ViewModel no sabe de quien son
        // las frases. Mandar la peticion escribiria en una ruta vacia.
        viewModel.agregar("Hola")
        advanceUntilIdle()

        verify(repositorio, never()).agregarFrase(any(), any())
    }

    @Test
    fun `limpiarMensajes borra el error y el aviso`() = runTest {
        whenever(repositorio.listarFrases(uid)).thenReturn(
            ResultadoOperacion.Error("Falló")
        )

        viewModel.cargar(uid)
        advanceUntilIdle()
        assertEquals("Falló", viewModel.error)

        viewModel.limpiarMensajes()

        assertNull(viewModel.error)
        assertNull(viewModel.aviso)
    }
}
