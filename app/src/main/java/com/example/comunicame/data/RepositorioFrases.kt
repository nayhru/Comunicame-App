package com.example.comunicame.data

import androidx.compose.runtime.mutableStateListOf

// Dos listas de frases: las sugeridas son fijas, las guardadas las crea el
// usuario. Las dos viven en memoria, esta entrega no persiste nada.
object RepositorioFrases {

    val sugeridas = listOf(
        "Hola, soy sorda. ¿Me puedes escribir?",
        "Necesito ayuda, por favor",
        "Gracias, muy amable",
        "¿Cuánto cuesta?",
        "¿Dónde está el baño?",
        "Habla más lento, por favor",
        "Un momento, estoy escribiendo",
        "Llama a un familiar mío"
    )

    // Las del usuario. Dejo dos de ejemplo para que no arranque vacio.
    val guardadas = mutableStateListOf(
        "Vengo a retirar un pedido",
        "¿Me puedes anotar la dirección?"
    )

    // false si venia vacia o repetida
    fun guardar(frase: String): Boolean {
        val limpia = frase.trim()
        if (limpia.isEmpty()) return false
        if (guardadas.any { it.equals(limpia, ignoreCase = true) }) return false
        guardadas.add(limpia)
        return true
    }

    // Borra una frase del usuario
    fun eliminar(frase: String) {
        guardadas.remove(frase)
    }
}
