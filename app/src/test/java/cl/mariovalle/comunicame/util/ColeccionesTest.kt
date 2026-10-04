package cl.mariovalle.comunicame.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de las utilidades de coleccion propias de la aplicacion.
class ColeccionesTest {

    private val frases = listOf("Hola", "Gracias", "Necesito ayuda", "Si", "No")

    @Test
    fun `contarQue cuenta solo los que cumplen el criterio`() {
        assertEquals(2, frases.contarQue { it.length <= 2 })
        assertEquals(5, frases.contarQue { true })
        assertEquals(0, frases.contarQue { false })
    }

    @Test
    fun `contarQue sobre una lista vacia devuelve cero`() {
        assertEquals(0, emptyList<String>().contarQue { true })
    }

    @Test
    fun `primerosQueCumplan respeta la cantidad pedida`() {
        val cortas = frases.primerosQueCumplan(2) { it.length <= 7 }

        assertEquals(listOf("Hola", "Gracias"), cortas)
    }

    @Test
    fun `primerosQueCumplan deja de recorrer apenas junta los que necesita`() {
        // Esta es la razon de existir de la funcion frente a filter().take():
        // filter recorreria la lista completa aunque ya tuviera la respuesta.
        var revisados = 0

        frases.primerosQueCumplan(1) {
            revisados++
            true
        }

        assertEquals(1, revisados)
    }

    @Test
    fun `primerosQueCumplan devuelve lo que haya si no alcanzan`() {
        val resultado = frases.primerosQueCumplan(10) { it == "Hola" }

        assertEquals(listOf("Hola"), resultado)
    }

    @Test
    fun `separarPor reparte en los que cumplen y los que no`() {
        val (cortas, largas) = frases.separarPor { it.length <= 4 }

        assertEquals(listOf("Hola", "Si", "No"), cortas)
        assertEquals(listOf("Gracias", "Necesito ayuda"), largas)
    }

    @Test
    fun `separarPor conserva el orden original en ambos grupos`() {
        val (pares, impares) = listOf(1, 2, 3, 4, 5, 6).separarPor { it % 2 == 0 }

        assertEquals(listOf(2, 4, 6), pares)
        assertEquals(listOf(1, 3, 5), impares)
    }

    @Test
    fun `criterioDeLargo arma un criterio reutilizable`() {
        val cabeEnLaTarjeta = criterioDeLargo(maximo = 7)

        assertTrue(cabeEnLaTarjeta("Gracias"))
        assertEquals(false, cabeEnLaTarjeta("Necesito ayuda"))
    }

    @Test
    fun `criterioDeLargo no cuenta los espacios de los extremos`() {
        val criterio = criterioDeLargo(maximo = 4)

        assertTrue(criterio("  Si  "))
    }

    @Test
    fun `resumen muestra los primeros y cuenta el resto`() {
        assertEquals("Hola, Gracias y 3 más", frases.resumen())
    }

    @Test
    fun `resumen no agrega la cuenta si los muestra todos`() {
        assertEquals("Hola, Gracias", listOf("Hola", "Gracias").resumen())
    }

    @Test
    fun `resumen de una lista vacia avisa que no hay nada`() {
        assertEquals("ninguna", emptyList<String>().resumen())
    }

    @Test
    fun `resumen recorta las frases demasiado largas`() {
        val resultado = listOf("Una frase bastante larga para una tarjeta").resumen()

        assertTrue(resultado.endsWith("…"))
    }

    @Test
    fun `indiceDelPrimero ubica el elemento buscado`() {
        assertEquals(1, frases.indiceDelPrimero { it == "Gracias" })
        assertEquals(0, frases.indiceDelPrimero { it.startsWith("H") })
    }

    @Test
    fun `indiceDelPrimero devuelve menos uno cuando no encuentra`() {
        // Las pantallas usan este -1 para decidir si agregar o reemplazar.
        // Si devolviera 0 por error, sobreescribiria la primera frase guardada.
        assertEquals(-1, frases.indiceDelPrimero { it == "No existe" })
    }
}
