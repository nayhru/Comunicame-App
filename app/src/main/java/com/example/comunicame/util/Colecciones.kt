package com.example.comunicame.util

// Utilidades de coleccion propias de la app.
//
// Todas son funciones de extension sobre List, o sea que se llaman con notacion
// de punto igual que las de la biblioteca estandar: frases.resumen(), sin tener
// que heredar de List ni envolverla en otra clase.

// Funcion de orden superior: recibe otra funcion como parametro.
//
// inline le dice al compilador que copie el cuerpo en el lugar de la llamada en
// vez de crear un objeto para la lambda. En un recorrido corto como este la
// diferencia es chica, pero es la razon por la que filter y map de Kotlin no
// pesan mas que un for escrito a mano.
inline fun <T> List<T>.contarQue(criterio: (T) -> Boolean): Int {
    var total = 0
    // for clasico: recorro la lista elemento por elemento
    for (elemento in this) {
        if (criterio(elemento)) total++
    }
    return total
}

// Devuelve los primeros n que cumplan, y corta apenas los junta.
//
// A diferencia de filter().take(n), esta version no recorre la lista completa:
// si encuentra los n que necesita en los primeros elementos, se sale con break.
inline fun <T> List<T>.primerosQueCumplan(
    cantidad: Int,
    criterio: (T) -> Boolean
): List<T> {
    val encontrados = mutableListOf<T>()
    for (elemento in this) {
        if (criterio(elemento)) {
            encontrados.add(elemento)
            // break: ya tengo los que queria, no sigo recorriendo
            if (encontrados.size == cantidad) break
        }
    }
    return encontrados
}

// Reparte la lista en dos segun el criterio. Devuelve un Pair con los que
// cumplen y los que no.
inline fun <T> List<T>.separarPor(criterio: (T) -> Boolean): Pair<List<T>, List<T>> {
    val siCumplen = mutableListOf<T>()
    val noCumplen = mutableListOf<T>()
    for (elemento in this) {
        if (criterio(elemento)) siCumplen.add(elemento) else noCumplen.add(elemento)
    }
    return siCumplen to noCumplen
}

// En vez de escribir un if adentro del filtro cada vez, armo el criterio una
// sola vez y lo reuso. La de afuera decide la logica, la de adentro la aplica.
fun criterioDeLargo(maximo: Int): (String) -> Boolean = { texto ->
    texto.limpio.length <= maximo
}

// Resume una lista de textos para mostrarla en una linea.
// Ejemplo: "Hola, Gracias y 3 más"
fun List<String>.resumen(mostrar: Int = 2): String {
    if (isEmpty()) return "ninguna"

    val visibles = take(mostrar).map { it.recortadoA(18) }
    val restantes = size - visibles.size

    // when con expresion: devuelve directo el string que corresponda
    return when {
        restantes <= 0 -> visibles.joinToString(", ")
        else -> visibles.joinToString(", ") + " y $restantes más"
    }
}

// Busca el indice del primer elemento que cumpla, o -1.
//
// Uso while en vez de for porque necesito manejar el indice a mano y salir
// apenas lo encuentro.
inline fun <T> List<T>.indiceDelPrimero(criterio: (T) -> Boolean): Int {
    var i = 0
    while (i < size) {
        if (criterio(this[i])) return i
        i++
    }
    return -1
}
