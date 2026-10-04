package cl.mariovalle.comunicame.util

// Resultado de validar un formulario.
//
// Uso sealed class y no un Boolean porque un formulario invalido necesita decir
// POR QUE lo es. Con sealed el compilador sabe que solo existen estas dos
// opciones, asi que el when de las pantallas no necesita rama else: si mañana
// agrego un tercer caso, Kotlin me obliga a cubrirlo.
sealed class ResultadoValidacion {

    // object y no class: Valido no lleva datos, basta una unica instancia
    object Valido : ResultadoValidacion()

    // data class porque si lleva datos: el mensaje y su gravedad
    data class Invalido(
        val mensaje: String,
        val critico: Boolean = false
    ) : ResultadoValidacion()

    // Propiedades calculadas disponibles en ambos casos
    val esValido: Boolean
        get() = this is Valido

    val mensajeError: String?
        get() = (this as? Invalido)?.mensaje

    companion object {

        // Encadena validaciones y devuelve el primer error que encuentre.
        //
        // vararg + funciones de orden superior: cada regla es una funcion que
        // devuelve un resultado. firstOrNull recorre hasta el primer invalido y
        // corta ahi, asi no evaluo validaciones caras si ya fallo una barata.
        fun primeraFalla(vararg reglas: () -> ResultadoValidacion): ResultadoValidacion =
            reglas.map { it() }
                .firstOrNull { !it.esValido }
                ?: Valido

        // Azucar: construye el resultado a partir de una condicion
        inline fun exigir(
            condicion: Boolean,
            critico: Boolean = false,
            mensaje: () -> String
        ): ResultadoValidacion =
            if (condicion) Valido else Invalido(mensaje(), critico)
    }
}
