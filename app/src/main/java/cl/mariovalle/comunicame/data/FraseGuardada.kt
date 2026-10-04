package cl.mariovalle.comunicame.data

// Una frase que el usuario guardo para tenerla a mano.
//
// Hasta la entrega anterior las frases vivian en una lista en memoria y se
// perdian al cerrar la aplicacion. Para alguien que usa la aplicacion como
// medio de comunicacion eso significaba volver a escribir cada vez lo mismo
// que ya habia escrito, asi que son el dato que tenia mas sentido persistir.
//
// El id lo genera Firestore al crear el documento. Va vacio mientras la frase
// todavia no se ha guardado.
data class FraseGuardada(
    val id: String = "",
    val texto: String = "",
    // Milisegundos desde 1970. Se guarda para poder mostrar las ultimas
    // primero, que es el orden en que sirven.
    val creadaEn: Long = 0L
) {

    fun aMapa(): Map<String, Any> = mapOf(
        CAMPO_TEXTO to texto,
        CAMPO_CREADA_EN to creadaEn
    )

    companion object {
        const val CAMPO_TEXTO = "texto"
        const val CAMPO_CREADA_EN = "creadaEn"

        fun desdeMapa(id: String, datos: Map<String, Any?>): FraseGuardada = FraseGuardada(
            id = id,
            texto = datos[CAMPO_TEXTO] as? String ?: "",
            // Firestore devuelve los numeros como Long, pero un documento
            // escrito a mano desde la consola puede traer un Int o un Double.
            // Number los cubre a todos.
            creadaEn = (datos[CAMPO_CREADA_EN] as? Number)?.toLong() ?: 0L
        )
    }
}
