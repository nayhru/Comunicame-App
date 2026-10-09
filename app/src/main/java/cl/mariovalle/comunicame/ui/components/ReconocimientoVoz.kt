package cl.mariovalle.comunicame.ui.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import java.util.Locale

// Resultado de intentar escuchar a la otra persona.
//
// Se modela como clase sellada y no como un String nullable porque la pantalla
// necesita distinguir los casos: no es lo mismo que el equipo no tenga
// reconocedor, que la persona cancele, o que haya hablado y no se le entienda.
// Cada uno se le explica distinto a quien usa la aplicacion.
sealed class ResultadoEscucha {

    data class Entendido(val texto: String) : ResultadoEscucha()

    // La persona cerro el dialogo sin hablar. No es un error que haya que
    // explicar: simplemente no se hace nada.
    object Cancelado : ResultadoEscucha()

    data class Fallo(val mensaje: String) : ResultadoEscucha()
}

// Prepara el intent del reconocedor de voz del sistema.
//
// Se usa el reconocedor del sistema y no uno propio porque es el mismo que la
// persona oyente ya conoce del teclado de Google: el dialogo le resulta
// familiar y no hay que explicarle nada.
fun intentDeEscucha(): Intent =
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        // Español de Chile cuando este disponible; si no, el reconocedor cae
        // solo en el idioma del sistema.
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CL")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es")
        putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Habla ahora. Lo que digas aparecerá escrito."
        )
        // Una sola alternativa: a quien lee le sirve la mejor, no una lista
        // de opciones parecidas que tenga que comparar.
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }

// Indica si el equipo tiene un reconocedor de voz instalado.
//
// No todos lo traen: un equipo sin servicios de Google puede no tenerlo, y en
// ese caso conviene avisar antes de mostrar un boton que no va a funcionar.
fun hayReconocedorDeVoz(context: Context): Boolean =
    SpeechRecognizer.isRecognitionAvailable(context)

fun tienePermisoDeMicrofono(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

// Traduce la respuesta del reconocedor a algo que la pantalla pueda mostrar.
//
// El try/catch/finally envuelve la lectura del resultado porque los datos
// vienen de otra aplicacion: el intent puede devolver una lista vacia, un
// extra con otro nombre o directamente nulo, segun el reconocedor instalado.
// El finally garantiza que el estado de escucha quede apagado pase lo que
// pase; sin el, un fallo inesperado dejaria el boton bloqueado y la persona
// sin poder reintentar, justo cuando esta en medio de una conversacion.
inline fun interpretarEscucha(
    codigoResultado: Int,
    datos: Intent?,
    alTerminar: () -> Unit
): ResultadoEscucha {
    try {
        if (codigoResultado != Activity.RESULT_OK) {
            return ResultadoEscucha.Cancelado
        }

        val reconocido = datos
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.trim()

        return when {
            reconocido.isNullOrBlank() -> ResultadoEscucha.Fallo(
                "No se entendió lo que se dijo. Pídele que hable más cerca del teléfono."
            )
            else -> ResultadoEscucha.Entendido(primeraEnMayuscula(reconocido))
        }
    } catch (e: Exception) {
        // El reconocedor es una aplicacion ajena al proyecto: si devuelve algo
        // inesperado, se informa en pantalla en lugar de cerrar la aplicacion.
        return ResultadoEscucha.Fallo(
            "Hubo un problema al leer lo que se dijo. Intenta nuevamente."
        )
    } finally {
        // Se ejecuta siempre: con exito, con error y tambien al cancelar.
        alTerminar()
    }
}

// El reconocedor devuelve el texto en minusculas. Se capitaliza para que se
// lea como una frase y no como una transcripcion.
//
// Se busca la primera letra y no el primer caracter: en español una pregunta
// empieza con "¿", que no tiene mayuscula, y capitalizar esa posicion dejaria
// la palabra siguiente en minuscula.
fun primeraEnMayuscula(texto: String): String {
    val posicion = texto.indexOfFirst { it.isLetter() }
    if (posicion == -1) return texto

    val letra = texto[posicion].titlecase(Locale("es", "CL"))
    return texto.substring(0, posicion) + letra + texto.substring(posicion + 1)
}

// Traduce al lenguaje de la persona el motivo por el que no se pudo escuchar.
fun mensajeDeFallo(e: Exception): String = when (e) {
    is ActivityNotFoundException ->
        "Este dispositivo no tiene instalado el reconocimiento de voz de Google."
    is SecurityException ->
        "El sistema bloqueó el acceso al micrófono."
    else ->
        "No se pudo iniciar la escucha. Intenta nuevamente."
}
