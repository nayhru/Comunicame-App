package cl.mariovalle.comunicame.ui.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Pruebas del reconocimiento de voz.
//
// Lo que importa aqui no es el reconocedor en si, que es del sistema, sino
// como la aplicacion interpreta lo que le devuelve: una respuesta inesperada
// no puede cerrar la aplicacion ni dejar el boton bloqueado en medio de una
// conversacion.
//
// Robolectric hace falta porque Intent y Activity son clases de Android.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ReconocimientoVozTest {

    private fun intentCon(vararg frases: String): Intent =
        Intent().putStringArrayListExtra(
            RecognizerIntent.EXTRA_RESULTS,
            ArrayList(frases.toList())
        )

    // INTERPRETACION DEL RESULTADO

    @Test
    fun `una frase reconocida se devuelve capitalizada`() {
        val resultado = interpretarEscucha(
            codigoResultado = Activity.RESULT_OK,
            datos = intentCon("su pedido está listo")
        ) {}

        assertTrue(resultado is ResultadoEscucha.Entendido)
        assertEquals(
            "Su pedido está listo",
            (resultado as ResultadoEscucha.Entendido).texto
        )
    }

    @Test
    fun `se toma la primera alternativa cuando el reconocedor devuelve varias`() {
        val resultado = interpretarEscucha(
            codigoResultado = Activity.RESULT_OK,
            datos = intentCon("buenos días", "buenos dias", "muy buenos días")
        ) {}

        assertEquals(
            "Buenos días",
            (resultado as ResultadoEscucha.Entendido).texto
        )
    }

    @Test
    fun `cancelar no se trata como error`() {
        // La persona cerro el dialogo sin hablar: no hay nada que explicarle.
        val resultado = interpretarEscucha(
            codigoResultado = Activity.RESULT_CANCELED,
            datos = null
        ) {}

        assertEquals(ResultadoEscucha.Cancelado, resultado)
    }

    @Test
    fun `una lista vacia se informa como que no se entendio`() {
        val resultado = interpretarEscucha(
            codigoResultado = Activity.RESULT_OK,
            datos = intentCon()
        ) {}

        assertTrue(resultado is ResultadoEscucha.Fallo)
    }

    @Test
    fun `un resultado sin datos se informa como que no se entendio`() {
        val resultado = interpretarEscucha(
            codigoResultado = Activity.RESULT_OK,
            datos = null
        ) {}

        assertTrue(resultado is ResultadoEscucha.Fallo)
    }

    @Test
    fun `una frase en blanco se informa como que no se entendio`() {
        val resultado = interpretarEscucha(
            codigoResultado = Activity.RESULT_OK,
            datos = intentCon("   ")
        ) {}

        assertTrue(resultado is ResultadoEscucha.Fallo)
    }

    @Test
    fun `el mensaje de fallo le dice a la persona que hacer`() {
        val resultado = interpretarEscucha(
            codigoResultado = Activity.RESULT_OK,
            datos = intentCon()
        ) {} as ResultadoEscucha.Fallo

        // No basta con avisar que fallo: hay que decir como reintentar.
        assertTrue(resultado.mensaje.contains("hable más cerca"))
    }

    // EL BLOQUE FINALLY

    @Test
    fun `el estado de escucha se apaga tras un reconocimiento correcto`() {
        var escuchando = true

        interpretarEscucha(
            codigoResultado = Activity.RESULT_OK,
            datos = intentCon("hola")
        ) { escuchando = false }

        assertEquals(false, escuchando)
    }

    @Test
    fun `el estado de escucha se apaga tambien al cancelar`() {
        var escuchando = true

        interpretarEscucha(
            codigoResultado = Activity.RESULT_CANCELED,
            datos = null
        ) { escuchando = false }

        assertEquals(false, escuchando)
    }

    @Test
    fun `el estado de escucha se apaga aunque el resultado venga mal formado`() {
        // Esto es lo que garantiza el finally. Sin el, un fallo al leer la
        // respuesta dejaria el boton deshabilitado y la persona sin poder
        // reintentar, justo en medio de una conversacion.
        var escuchando = true

        interpretarEscucha(
            codigoResultado = Activity.RESULT_OK,
            datos = Intent()
        ) { escuchando = false }

        assertEquals(false, escuchando)
    }

    // CONFIGURACION DEL INTENT

    @Test
    fun `el intent pide reconocimiento de voz en español`() {
        val intent = intentDeEscucha()

        assertEquals(RecognizerIntent.ACTION_RECOGNIZE_SPEECH, intent.action)
        assertEquals("es-CL", intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE))
    }

    @Test
    fun `el intent pide una sola alternativa`() {
        // A quien lee le sirve la mejor transcripcion, no una lista de
        // opciones parecidas que tenga que comparar.
        assertEquals(
            1,
            intentDeEscucha().getIntExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 0)
        )
    }

    @Test
    fun `el intent lleva una indicacion para quien va a hablar`() {
        val prompt = intentDeEscucha().getStringExtra(RecognizerIntent.EXTRA_PROMPT)

        assertTrue(!prompt.isNullOrBlank())
    }

    // MENSAJES DE ERROR

    @Test
    fun `sin reconocedor instalado se explica el motivo`() {
        val mensaje = mensajeDeFallo(ActivityNotFoundException())

        assertTrue(mensaje.contains("reconocimiento de voz"))
    }

    @Test
    fun `un bloqueo del sistema se explica como tal`() {
        val mensaje = mensajeDeFallo(SecurityException())

        assertTrue(mensaje.contains("micrófono"))
    }

    @Test
    fun `cualquier otra falla tiene un mensaje accionable`() {
        val mensaje = mensajeDeFallo(RuntimeException("algo raro"))

        assertTrue(mensaje.contains("Intenta nuevamente"))
    }

    // CAPITALIZACION

    @Test
    fun `capitaliza la primera letra sin tocar el resto`() {
        assertEquals("Hola", primeraEnMayuscula("hola"))
        assertEquals("¿Cuánto cuesta?", primeraEnMayuscula("¿cuánto cuesta?"))
    }

    @Test
    fun `una cadena vacia no rompe la capitalizacion`() {
        assertEquals("", primeraEnMayuscula(""))
    }

    // DISPONIBILIDAD

    @Test
    fun `la comprobacion de permiso responde sin lanzar excepcion`() {
        // En el entorno de prueba el permiso no esta concedido; lo que se
        // verifica es que la consulta no reviente.
        val contexto = ApplicationProvider.getApplicationContext<android.content.Context>()

        tienePermisoDeMicrofono(contexto)
    }
}
