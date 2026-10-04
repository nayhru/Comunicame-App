package cl.mariovalle.comunicame.data

import androidx.compose.runtime.mutableStateListOf
import cl.mariovalle.comunicame.util.equivaleA
import cl.mariovalle.comunicame.util.estaVacio
import cl.mariovalle.comunicame.util.indiceDelPrimero
import cl.mariovalle.comunicame.util.limpio
import cl.mariovalle.comunicame.util.primerosQueCumplan

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

    // Cuantos resultados devuelve el buscador como maximo. Mas que esto ya no
    // se alcanza a leer de una pasada en el telefono.
    const val MAXIMO_RESULTADOS = 12

    // Busca en sugeridas y guardadas a la vez.
    //
    // filter + sortedBy + map encadenados: filtro las que contienen el texto,
    // las ordeno por largo (las cortas primero, son las mas usadas) y devuelvo
    // solo el texto. primerosQueCumplan corta apenas junta los que necesita,
    // en vez de recorrer todo y descartar despues.
    fun buscar(termino: String): List<String> {
        if (termino.estaVacio) return emptyList()
        val t = termino.limpio.lowercase()

        return (textosSugeridos() + guardadas)
            .sortedBy { it.length }
            .primerosQueCumplan(MAXIMO_RESULTADOS) { it.lowercase().contains(t) }
            .map { it.limpio }
    }

    // Posicion de una frase en las guardadas, o -1 si no esta.
    // La usa la pantalla para saber si una frase ya fue guardada.
    fun posicionEnGuardadas(frase: String): Int =
        guardadas.toList().indiceDelPrimero { it.equivaleA(frase) }

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
    //
    // Parte del Map que ya armo groupBy, cambia cada lista por su tamano con
    // mapValues y despues reemplaza la clave del enum por su etiqueta legible
    // con mapKeys. Las categorias sin frases se agregan aparte para que no
    // desaparezcan de la tabla.
    fun conteoPorCategoria(): Map<String, Int> {
        val conFrases: Map<String, Int> = porCategoria
            .mapValues { (_, lista) -> lista.size }
            .mapKeys { (cat, _) -> cat.etiqueta }

        val vacias: Map<String, Int> = CategoriaFrase.entries
            .filter { it !in porCategoria.keys }
            .associate { it.etiqueta to 0 }

        return conFrases + vacias
    }
}
