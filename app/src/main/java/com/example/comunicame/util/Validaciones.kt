package com.example.comunicame.util

// Funciones y propiedades de extension para validar los formularios.
//
// Antes esta logica estaba repetida dentro de Login, Registro y Recuperar. Al
// sacarla a extensiones de String queda en un solo lugar y las pantallas se
// leen como lo que hacen: usuario.esCorreoValido() en vez de un if con dos
// contains encadenados.
//
// Una extension no modifica la clase String: solo permite llamarla con notacion
// de punto. Por eso puedo "agregarle" metodos a String sin heredar de ella.

// Largo minimo que exige la app para una contrasena
const val LARGO_MINIMO_CONTRASENA = 8

// ---------- Propiedades de extension ----------

// Propiedad de extension: se consulta como si fuera un atributo del String,
// sin parentesis. texto.limpio en vez de texto.limpio()
val String.limpio: String
    get() = trim()

val String.estaVacio: Boolean
    get() = limpio.isEmpty()

// Cuenta las palabras reales, ignorando espacios de mas
val String.cantidadPalabras: Int
    get() = limpio.split(" ").filter { it.isNotBlank() }.size

// ---------- Funciones de extension ----------

// Un correo valido necesita arroba, punto despues de la arroba y algo a cada lado
fun String.esCorreoValido(): Boolean {
    val t = limpio
    val posArroba = t.indexOf('@')
    val posPunto = t.lastIndexOf('.')
    return posArroba > 0 &&
        posPunto > posArroba + 1 &&
        posPunto < t.length - 1 &&
        !t.contains(' ')
}

fun String.tieneLargoMinimo(minimo: Int = LARGO_MINIMO_CONTRASENA): Boolean =
    limpio.length >= minimo

// El nombre de usuario solo admite letras, numeros, punto y guion bajo
fun String.esUsuarioValido(): Boolean {
    val t = limpio
    if (t.length < 3) return false
    // for + when: recorro caracter por caracter y clasifico cada uno
    for (c in t) {
        when {
            c.isLetterOrDigit() -> continue
            c == '.' || c == '_' -> continue
            else -> return false
        }
    }
    return true
}

// Capitaliza cada palabra: "ana torres" -> "Ana Torres"
fun String.aNombrePropio(): String =
    limpio.split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { palabra ->
            palabra.lowercase().replaceFirstChar { it.uppercase() }
        }

// Compara ignorando mayusculas y espacios sobrantes. La uso para no aceptar
// dos veces el mismo usuario escrito distinto.
fun String.equivaleA(otro: String): Boolean =
    limpio.equals(otro.limpio, ignoreCase = true)

// Recorta el texto para mostrarlo en espacios chicos
fun String.recortadoA(maximo: Int): String =
    if (limpio.length <= maximo) limpio else limpio.take(maximo).trimEnd() + "…"

// Fuerza de la contrasena de 0 a 4, para la barra visual del registro.
// Uso una lista de reglas y cuento cuantas se cumplen con count().
fun String.fuerzaContrasena(): Int {
    if (estaVacio) return 0
    val reglas: List<(String) -> Boolean> = listOf(
        { it.length >= LARGO_MINIMO_CONTRASENA },
        { it.any { c -> c.isDigit() } },
        { it.any { c -> c.isUpperCase() } },
        { it.any { c -> !it.isEmpty() && !c.isLetterOrDigit() } }
    )
    return reglas.count { regla -> regla(this) }
}
