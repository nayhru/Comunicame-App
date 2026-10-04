package cl.mariovalle.comunicame.data

// Como prefiere comunicarse el usuario. Se elige con RadioButton en Registro.
enum class PreferenciaComunicacion(val etiqueta: String) {
    LENGUA_DE_SENAS("Lengua de señas"),
    TEXTO_ESCRITO("Texto escrito"),
    LECTURA_LABIAL("Lectura labial");

    companion object {
        // Firestore devuelve el enum como texto. Si el valor guardado no
        // corresponde a ninguna constante (por ejemplo, un documento escrito
        // por una version anterior), se usa texto escrito en vez de reventar.
        fun desdeNombre(nombre: String?): PreferenciaComunicacion =
            entries.firstOrNull { it.name == nombre } ?: TEXTO_ESCRITO
    }
}

// Datos de la cuenta.
//
// La contrasena no esta aca y no se guarda en ninguna parte de la aplicacion.
// De eso se encarga Firebase Authentication, que almacena un hash en sus
// servidores y nunca lo devuelve. Hasta la entrega anterior el campo existia
// en texto plano porque no habia base de datos; ahora seria tanto innecesario
// como peligroso.
//
// El uid lo asigna Firebase Auth al crear la cuenta y es la llave del documento
// en Firestore. Va vacio mientras el usuario todavia no se registra.
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val usuario: String = "",
    val correo: String = "",
    val comuna: String = "",
    val preferencia: PreferenciaComunicacion = PreferenciaComunicacion.TEXTO_ESCRITO,
    val alertasVisuales: Boolean = true,
    val vibracion: Boolean = true,
    val subtitulos: Boolean = true,
    // A quien avisar en una emergencia. Va dentro del documento del usuario y
    // no en una coleccion aparte porque es uno solo y se necesita junto con el
    // resto del perfil: una consulta menos en el momento en que mas urge.
    val contactoEmergencia: ContactoEmergencia = ContactoEmergencia()
) {

    // Convierte el usuario al mapa que entiende Firestore.
    //
    // Se escribe a mano en vez de dejar que Firestore serialice el objeto
    // porque asi el enum queda guardado por su nombre y no por su posicion:
    // si mañana se agrega una preferencia en medio de la lista, los documentos
    // ya guardados siguen significando lo mismo.
    fun aMapa(): Map<String, Any> = mapOf(
        CAMPO_NOMBRE to nombre,
        CAMPO_USUARIO to usuario,
        CAMPO_CORREO to correo,
        CAMPO_COMUNA to comuna,
        CAMPO_PREFERENCIA to preferencia.name,
        CAMPO_ALERTAS to alertasVisuales,
        CAMPO_VIBRACION to vibracion,
        CAMPO_SUBTITULOS to subtitulos,
        CAMPO_CONTACTO to contactoEmergencia.aMapa()
    )

    companion object {
        const val CAMPO_NOMBRE = "nombre"
        const val CAMPO_USUARIO = "usuario"
        const val CAMPO_CORREO = "correo"
        const val CAMPO_COMUNA = "comuna"
        const val CAMPO_PREFERENCIA = "preferencia"
        const val CAMPO_ALERTAS = "alertasVisuales"
        const val CAMPO_VIBRACION = "vibracion"
        const val CAMPO_SUBTITULOS = "subtitulos"
        const val CAMPO_CONTACTO = "contactoEmergencia"

        // Reconstruye el usuario a partir de lo que vino de Firestore.
        //
        // Cada campo se lee con un valor por omision porque un documento puede
        // estar incompleto: alguien pudo editarlo desde la consola, o quedo a
        // medias si se corto la conexion durante el registro. Es preferible
        // mostrar un perfil con un dato en blanco que cerrar la aplicacion.
        fun desdeMapa(uid: String, datos: Map<String, Any?>): Usuario = Usuario(
            uid = uid,
            nombre = datos[CAMPO_NOMBRE] as? String ?: "",
            usuario = datos[CAMPO_USUARIO] as? String ?: "",
            correo = datos[CAMPO_CORREO] as? String ?: "",
            comuna = datos[CAMPO_COMUNA] as? String ?: "",
            preferencia = PreferenciaComunicacion.desdeNombre(
                datos[CAMPO_PREFERENCIA] as? String
            ),
            alertasVisuales = datos[CAMPO_ALERTAS] as? Boolean ?: true,
            vibracion = datos[CAMPO_VIBRACION] as? Boolean ?: true,
            subtitulos = datos[CAMPO_SUBTITULOS] as? Boolean ?: true,
            // Firestore devuelve los mapas anidados como Map<String, Any?>.
            // Si falta, queda un contacto vacio y la pantalla ofrece crearlo.
            contactoEmergencia = (datos[CAMPO_CONTACTO] as? Map<*, *>)
                ?.let { mapa ->
                    ContactoEmergencia.desdeMapa(
                        mapa.entries.associate { (clave, valor) ->
                            clave.toString() to valor
                        }
                    )
                }
                ?: ContactoEmergencia()
        )
    }
}
