package com.example.comunicame.data

import androidx.compose.runtime.mutableStateListOf
import com.example.comunicame.util.equivaleA
import com.example.comunicame.util.estaVacio
import com.example.comunicame.util.limpio

// Categorias para agrupar las frases sugeridas
enum class CategoriaFrase(val etiqueta: String) {
    SALUDO("Saludos"),
    AYUDA("Pedir ayuda"),
    COMPRAS("Compras"),
    ORIENTACION("Orientación")
}

data class Frase(
    val texto: String,
    val categoria: CategoriaFrase
)

// Frases del panel de comunicacion.
//
// Las sugeridas son fijas y vienen categorizadas. Las guardadas las crea la
// usuaria. Todo vive en memoria: esta entrega no persiste nada.
object RepositorioFrases {

    // by lazy: el catalogo no se arma hasta la primera vez que alguien lo pide.
    // Al arrancar la app en el Login nadie lo necesita, asi que no se construye.
    val sugeridas: List<Frase> by lazy {
        listOf(
            Frase("Hola, soy sorda. ¿Me puedes escribir?", CategoriaFrase.SALUDO),
            Frase("Buenos días", CategoriaFrase.SALUDO),
            Frase("Necesito ayuda, por favor", CategoriaFrase.AYUDA),
            Frase("Llama a un familiar mío", CategoriaFrase.AYUDA),
            Frase("Habla más lento, por favor", CategoriaFrase.AYUDA),
            Frase("¿Cuánto cuesta?", CategoriaFrase.COMPRAS),
            Frase("Vengo a retirar un pedido", CategoriaFrase.COMPRAS),
            Frase("¿Dónde está el baño?", CategoriaFrase.ORIENTACION),
            Frase("¿Me puedes anotar la dirección?", CategoriaFrase.ORIENTACION),
            Frase("Un momento, estoy escribiendo", CategoriaFrase.SALUDO)
        )
    }

    // Map generado con groupBy: indice de categoria -> frases de esa categoria.
    // Buscar por categoria queda en O(1) en vez de recorrer la lista entera.
    val porCategoria: Map<CategoriaFrase, List<Frase>> by lazy {
        sugeridas.groupBy { it.categoria }
    }

    // Set con los textos ya usados, en minuscula. Un Set no admite duplicados y
    // consulta en O(1), asi evito que se guarde dos veces la misma frase.
    private val textosSugeridos: Set<String> by lazy {
        sugeridas.map { it.texto.lowercase() }.toSet()
    }

    // Las que crea la usuaria
    val guardadas = mutableStateListOf(
        "Vengo a retirar un pedido en caja",
        "Mi cita médica es a las tres"
    )

    // Devuelve solo los textos de las sugeridas, para la grilla
    fun textosSugeridos(): List<String> = sugeridas.map { it.texto }

    // Filtra las sugeridas por categoria. Si no se pasa ninguna, las devuelve
    // todas. Uso el Map indexado en vez de recorrer.
    fun sugeridasDe(categoria: CategoriaFrase?): List<Frase> =
        categoria?.let { porCategoria[it].orEmpty() } ?: sugeridas

    // Busca en sugeridas y guardadas a la vez.
    //
    // filter + map + sortedBy encadenados: filtro las que contienen el texto,
    // las ordeno por largo (las cortas primero, son las mas usadas) y devuelvo
    // solo el texto.
    fun buscar(termino: String): List<String> {
        if (termino.estaVacio) return emptyList()
        val t = termino.limpio.lowercase()
        return (textosSugeridos() + guardadas)
            .filter { it.lowercase().contains(t) }
            .sortedBy { it.length }
            .map { it.limpio }
    }

    // false si venia vacia o ya existia en cualquiera de las dos listas
    fun guardar(frase: String): Boolean {
        val limpia = frase.limpio
        if (limpia.estaVacio) return false
        if (limpia.lowercase() in textosSugeridos) return false
        if (guardadas.any { it.equivaleA(limpia) }) return false
        guardadas.add(limpia)
        return true
    }

    // Borra una frase de la usuaria
    fun eliminar(frase: String) {
        guardadas.remove(frase)
    }

    // Estadistica para Mi perfil: cuantas frases hay por categoria.
    // associateWith arma un Map recorriendo las categorias una sola vez.
    fun conteoPorCategoria(): Map<String, Int> =
        CategoriaFrase.entries.associateWith { cat ->
            porCategoria[cat]?.size ?: 0
        }.mapKeys { (cat, _) -> cat.etiqueta }
}
