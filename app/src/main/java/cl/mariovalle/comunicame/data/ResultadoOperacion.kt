package cl.mariovalle.comunicame.data

// Resultado de una operacion contra Firebase.
//
// Las operaciones de red fallan por motivos muy distintos y el usuario necesita
// saber cual: no es lo mismo "tu contrasena esta mala" que "no hay internet".
// El primero se corrige escribiendo de nuevo, el segundo no.
//
// Es una clase sellada por la misma razon que ResultadoValidacion: el when de
// las pantallas no necesita rama else y el compilador avisa si se agrega un
// caso nuevo sin cubrirlo.
sealed class ResultadoOperacion<out T> {

    data class Exito<T>(val dato: T) : ResultadoOperacion<T>()

    // mensaje es lo que se le muestra al usuario, ya traducido a algo que
    // pueda entender y accionar. recuperable distingue los errores que vale
    // la pena reintentar (se cayo la conexion) de los que no (la clave es
    // incorrecta), para que la pantalla decida si ofrecer un boton de
    // reintentar o pedir que corrija lo que escribio.
    data class Error(
        val mensaje: String,
        val recuperable: Boolean = false
    ) : ResultadoOperacion<Nothing>()

    val esExito: Boolean
        get() = this is Exito

    // Devuelve el dato o null, para los casos en que la pantalla solo quiere
    // el valor y ya mostro el error por otro lado.
    fun datoONulo(): T? = (this as? Exito)?.dato

    val mensajeError: String?
        get() = (this as? Error)?.mensaje
}
