package cl.mariovalle.comunicame.data

import android.content.Context
import android.content.SharedPreferences

// Sesion del usuario guardada en el dispositivo.
//
// Firebase Auth ya recuerda quien inicio sesion, pero esa respuesta llega
// despues de consultar a la red. SharedPreferences responde al instante y sin
// conexion, asi que la app sabe a que pantalla ir apenas abre, sin mostrar el
// Login un segundo para despues saltar al Panel.
//
// Guarda lo minimo para dibujar la pantalla: identificador y nombre visible.
// La contrasena nunca se guarda aca: SharedPreferences es un XML en claro
// dentro del telefono, cualquiera con acceso al equipo lo puede leer.
class SesionLocal(context: Context) {

    // applicationContext y no el de la Activity: si guardara el de la Activity
    // la mantendria viva despues de cerrada y eso es una fuga de memoria.
    private val preferencias: SharedPreferences =
        context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    // Identificador que entrega Firebase Auth. Es la llave de los datos del
    // usuario en Firestore.
    val uid: String?
        get() = preferencias.getString(CLAVE_UID, null)

    val nombreUsuario: String?
        get() = preferencias.getString(CLAVE_USUARIO, null)

    val correo: String?
        get() = preferencias.getString(CLAVE_CORREO, null)

    // Propiedad de solo lectura que resume el estado. La pantalla de arranque
    // pregunta esto y nada mas.
    val haySesionActiva: Boolean
        get() = !uid.isNullOrBlank()

    // Deja registrada la sesion recien iniciada.
    //
    // apply() y no commit(): apply escribe en segundo plano y devuelve de
    // inmediato. commit bloquea el hilo principal hasta que el disco responde,
    // y justo despues del login la app esta navegando.
    fun guardar(uid: String, usuario: String, correo: String) {
        preferencias.edit()
            .putString(CLAVE_UID, uid)
            .putString(CLAVE_USUARIO, usuario)
            .putString(CLAVE_CORREO, correo)
            .apply()
    }

    // Actualiza solo el nombre visible, sin tocar el resto de la sesion.
    // Se usa cuando el usuario edita su perfil estando dentro.
    fun actualizarNombreUsuario(usuario: String) {
        preferencias.edit().putString(CLAVE_USUARIO, usuario).apply()
    }

    // Borra todo al cerrar sesion.
    //
    // clear() y no quitar clave por clave: si mas adelante se agrega un dato
    // a la sesion y alguien olvida sumarlo aca, quedaria vivo despues de
    // cerrar sesion. clear() no deja ese cabo suelto.
    fun cerrar() {
        preferencias.edit().clear().apply()
    }

    companion object {
        // El nombre del archivo XML dentro de /data/data/<paquete>/shared_prefs
        private const val ARCHIVO = "sesion_comunicame"

        private const val CLAVE_UID = "uid"
        private const val CLAVE_USUARIO = "usuario"
        private const val CLAVE_CORREO = "correo"
    }
}
