package cl.mariovalle.comunicame.data

import android.content.Context
import android.content.SharedPreferences

// Ajustes de accesibilidad que el usuario elige en su perfil.
//
// Se guardan en Firestore junto al resto de la cuenta, pero tambien aqui, en
// el dispositivo. La razon es que hay que consultarlos en cada vibracion y en
// cada aviso de pantalla: ir a la red para eso seria absurdo, y en una
// pantalla de emergencia significaria esperar por algo que ya se sabe.
//
// SharedPreferences responde al instante y sin conexion. Firestore manda
// cuando hay discrepancia, porque es donde quedan al cambiar de dispositivo.
class PreferenciasAccesibilidad(context: Context) {

    private val preferencias: SharedPreferences =
        context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    // Los tres parten activados. Ante la duda conviene que las ayudas esten
    // puestas: alguien que las necesita y no las tiene se encuentra con una
    // aplicacion que no le sirve, mientras que al reves solo sobra.
    val alertasVisuales: Boolean
        get() = preferencias.getBoolean(CLAVE_ALERTAS, true)

    val vibracion: Boolean
        get() = preferencias.getBoolean(CLAVE_VIBRACION, true)

    val subtitulos: Boolean
        get() = preferencias.getBoolean(CLAVE_SUBTITULOS, true)

    // Copia al dispositivo lo que vino de la cuenta. Se llama al iniciar
    // sesion y cada vez que el usuario edita su perfil.
    fun sincronizarDesde(usuario: Usuario) {
        preferencias.edit()
            .putBoolean(CLAVE_ALERTAS, usuario.alertasVisuales)
            .putBoolean(CLAVE_VIBRACION, usuario.vibracion)
            .putBoolean(CLAVE_SUBTITULOS, usuario.subtitulos)
            .apply()
    }

    fun limpiar() {
        preferencias.edit().clear().apply()
    }

    companion object {
        private const val ARCHIVO = "accesibilidad_comunicame"

        private const val CLAVE_ALERTAS = "alertasVisuales"
        private const val CLAVE_VIBRACION = "vibracion"
        private const val CLAVE_SUBTITULOS = "subtitulos"
    }
}
