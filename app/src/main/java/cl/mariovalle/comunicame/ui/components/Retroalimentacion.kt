package cl.mariovalle.comunicame.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

// El sonido no le sirve al usuario, asi que cada evento tiene su propio patron
// de vibracion. Con el uso uno aprende a distinguirlos sin mirar la pantalla.
enum class PatronVibracion(val tiempos: LongArray) {
    EXITO(longArrayOf(0, 120)),              // un pulso
    ERROR(longArrayOf(0, 200, 120, 200)),    // dos pulsos
    TOQUE(longArrayOf(0, 40))                // pulsito de confirmacion
}

// Esto es clave y me costo pillarlo: si vibro sin declarar para que es, con el
// telefono en silencio Android se come la vibracion. En una app normal da lo
// mismo, pero aca la vibracion ES el aviso: si se pierde, la usuaria no se
// entera de nada. Marcandola como accesibilidad el sistema no la silencia.
private val atributosAccesibilidad = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
    .build()

// La API de vibracion cambio dos veces desde minSdk 24, de ahi las ramas.
// Si el equipo no tiene vibrador no hace nada, por eso el aviso visual nunca
// puede depender de esto.
fun vibrar(context: Context, patron: PatronVibracion) {

    val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    if (vibrator == null || !vibrator.hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // El overload con VibrationAttributes recien existe en API 33 y el
        // minSdk es 24, asi que va con AudioAttributes.
        @Suppress("DEPRECATION")
        vibrator.vibrate(
            VibrationEffect.createWaveform(patron.tiempos, -1),
            atributosAccesibilidad
        )
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(patron.tiempos, -1, atributosAccesibilidad)
    }
}
