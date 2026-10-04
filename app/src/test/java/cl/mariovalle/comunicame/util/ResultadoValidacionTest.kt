package cl.mariovalle.comunicame.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de la clase sellada que representa el resultado de validar.
//
// Es la pieza de la que cuelgan todos los formularios: si primeraFalla
// devolviera el error equivocado, el usuario veria un mensaje que no
// corresponde al campo que escribio mal.
class ResultadoValidacionTest {

    @Test
    fun `Valido se reconoce como valido y no trae mensaje`() {
        val resultado: ResultadoValidacion = ResultadoValidacion.Valido

        assertTrue(resultado.esValido)
        assertNull(resultado.mensajeError)
    }

    @Test
    fun `Invalido no es valido y conserva su mensaje`() {
        val resultado: ResultadoValidacion =
            ResultadoValidacion.Invalido("Falta el correo")

        assertFalse(resultado.esValido)
        assertEquals("Falta el correo", resultado.mensajeError)
    }

    @Test
    fun `un error no critico lo es por omision`() {
        val resultado = ResultadoValidacion.Invalido("Revisa el campo")

        assertFalse(resultado.critico)
    }

    @Test
    fun `primeraFalla devuelve Valido cuando todas las reglas pasan`() {
        val resultado = ResultadoValidacion.primeraFalla(
            { ResultadoValidacion.Valido },
            { ResultadoValidacion.Valido }
        )

        assertTrue(resultado.esValido)
    }

    @Test
    fun `primeraFalla devuelve el primer error y no el ultimo`() {
        // Importa el orden: las pantallas muestran un solo mensaje a la vez y
        // debe ser el del primer campo que el usuario tiene que corregir.
        val resultado = ResultadoValidacion.primeraFalla(
            { ResultadoValidacion.Valido },
            { ResultadoValidacion.Invalido("Primer error") },
            { ResultadoValidacion.Invalido("Segundo error") }
        )

        assertEquals("Primer error", resultado.mensajeError)
    }

    @Test
    fun `primeraFalla sin reglas se considera valido`() {
        assertTrue(ResultadoValidacion.primeraFalla().esValido)
    }

    @Test
    fun `primeraFalla evalua todas las reglas aunque una ya haya fallado`() {
        // Deja constancia del comportamiento real: primeraFalla usa map antes
        // de firstOrNull, asi que ejecuta las reglas completas y recien
        // entonces elige la primera que fallo.
        //
        // No es un defecto: las reglas de esta aplicacion son comparaciones de
        // texto en memoria y ninguna cuesta nada. Queda escrito para que, si
        // alguna vez una regla consulta la red, se sepa que habria que cambiar
        // map por asSequence antes de agregarla.
        var evaluadas = 0

        val resultado = ResultadoValidacion.primeraFalla(
            { evaluadas++; ResultadoValidacion.Invalido("Falla temprana") },
            { evaluadas++; ResultadoValidacion.Valido }
        )

        assertEquals("Falla temprana", resultado.mensajeError)
        assertEquals(2, evaluadas)
    }

    @Test
    fun `exigir devuelve Valido cuando la condicion se cumple`() {
        val resultado = ResultadoValidacion.exigir(condicion = true) { "No deberia verse" }

        assertTrue(resultado.esValido)
    }

    @Test
    fun `exigir arma el mensaje solo cuando la condicion falla`() {
        var vecesConstruido = 0

        val valido = ResultadoValidacion.exigir(condicion = true) {
            vecesConstruido++
            "mensaje"
        }
        // La lambda no se llama si la condicion se cumple: ese es el punto de
        // recibir el mensaje como funcion y no como String ya armado.
        assertTrue(valido.esValido)
        assertEquals(0, vecesConstruido)

        val invalido = ResultadoValidacion.exigir(condicion = false) {
            vecesConstruido++
            "mensaje"
        }
        assertEquals("mensaje", invalido.mensajeError)
        assertEquals(1, vecesConstruido)
    }

    @Test
    fun `exigir propaga la marca de critico`() {
        val resultado = ResultadoValidacion.exigir(
            condicion = false,
            critico = true
        ) { "Error grave" }

        assertFalse(resultado.esValido)
        assertTrue((resultado as ResultadoValidacion.Invalido).critico)
    }
}
