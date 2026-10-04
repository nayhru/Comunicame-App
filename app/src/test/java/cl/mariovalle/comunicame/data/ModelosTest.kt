package cl.mariovalle.comunicame.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de la conversion entre los modelos y los mapas de Firestore.
//
// Es la frontera donde los datos dejan de ser objetos de Kotlin y pasan a ser
// documentos. Si la conversion pierde un campo o lo interpreta mal, el error
// no se ve al guardar sino la proxima vez que la persona abre la aplicacion,
// que es el peor momento para descubrirlo.
class ModelosTest {

    private val ana = Usuario(
        uid = "uid-1",
        nombre = "Ana Torres",
        usuario = "ana",
        correo = "ana.torres@correo.cl",
        comuna = "Santiago",
        preferencia = PreferenciaComunicacion.LENGUA_DE_SENAS,
        alertasVisuales = true,
        vibracion = false,
        subtitulos = true
    )

    @Test
    fun `el usuario sobrevive a la ida y vuelta por el mapa`() {
        val recuperado = Usuario.desdeMapa(ana.uid, ana.aMapa())

        assertEquals(ana, recuperado)
    }

    @Test
    fun `la preferencia se guarda por su nombre y no por su posicion`() {
        val mapa = ana.aMapa()

        // Si se guardara el ordinal, agregar una preferencia en medio del enum
        // cambiaria el significado de todos los documentos ya escritos.
        assertEquals("LENGUA_DE_SENAS", mapa[Usuario.CAMPO_PREFERENCIA])
    }

    @Test
    fun `el mapa del usuario no incluye la contrasena`() {
        val mapa = ana.aMapa()

        // La contrasena no existe en el modelo: de ella se encarga Firebase
        // Auth. Esta prueba deja constancia de que no vuelva a colarse.
        assertFalse(mapa.keys.any { it.contains("contrasena", ignoreCase = true) })
        assertFalse(mapa.keys.any { it.contains("password", ignoreCase = true) })
    }

    @Test
    fun `el mapa del usuario no incluye el identificador`() {
        // El uid es el nombre del documento, no un campo adentro. Guardarlo
        // dos veces abriria la puerta a que dejen de coincidir.
        assertFalse(ana.aMapa().containsKey("uid"))
    }

    @Test
    fun `un documento incompleto no rompe la lectura`() {
        // Un documento puede quedar a medias si se corta la conexion durante
        // el registro, o si alguien lo edita desde la consola de Firebase.
        val incompleto = mapOf<String, Any?>(Usuario.CAMPO_NOMBRE to "Solo el nombre")

        val usuario = Usuario.desdeMapa("uid-2", incompleto)

        assertEquals("Solo el nombre", usuario.nombre)
        assertEquals("", usuario.correo)
        // Los valores por omision mantienen la accesibilidad activada: ante la
        // duda, conviene que las ayudas esten puestas y no al reves.
        assertTrue(usuario.alertasVisuales)
        assertTrue(usuario.vibracion)
    }

    @Test
    fun `una preferencia desconocida cae en texto escrito`() {
        val datos = mapOf<String, Any?>(Usuario.CAMPO_PREFERENCIA to "LENGUA_INVENTADA")

        val usuario = Usuario.desdeMapa("uid-3", datos)

        assertEquals(PreferenciaComunicacion.TEXTO_ESCRITO, usuario.preferencia)
    }

    @Test
    fun `una preferencia ausente cae en texto escrito`() {
        val usuario = Usuario.desdeMapa("uid-4", emptyMap())

        assertEquals(PreferenciaComunicacion.TEXTO_ESCRITO, usuario.preferencia)
    }

    @Test
    fun `un campo con el tipo equivocado no rompe la lectura`() {
        // Si alguien edita el documento desde la consola puede dejar un numero
        // donde iba un texto. El casteo seguro devuelve el valor por omision.
        val datos = mapOf<String, Any?>(Usuario.CAMPO_NOMBRE to 42)

        val usuario = Usuario.desdeMapa("uid-5", datos)

        assertEquals("", usuario.nombre)
    }

    // FRASES

    @Test
    fun `la frase sobrevive a la ida y vuelta por el mapa`() {
        val frase = FraseGuardada(id = "f-1", texto = "Necesito ayuda", creadaEn = 1_700_000_000_000)

        val recuperada = FraseGuardada.desdeMapa(frase.id, frase.aMapa())

        assertEquals(frase, recuperada)
    }

    @Test
    fun `la fecha se lee aunque Firestore la devuelva como entero`() {
        // Firestore guarda los numeros como Long, pero un documento escrito a
        // mano puede traer un Int. Number cubre los dos casos.
        val comoEntero = mapOf<String, Any?>(
            FraseGuardada.CAMPO_TEXTO to "Hola",
            FraseGuardada.CAMPO_CREADA_EN to 1_700_000_000
        )

        val frase = FraseGuardada.desdeMapa("f-2", comoEntero)

        assertEquals(1_700_000_000L, frase.creadaEn)
    }

    @Test
    fun `una frase sin fecha queda en cero y no rompe el orden`() {
        val frase = FraseGuardada.desdeMapa("f-3", mapOf(FraseGuardada.CAMPO_TEXTO to "Hola"))

        assertEquals(0L, frase.creadaEn)
        assertEquals("Hola", frase.texto)
    }

    @Test
    fun `el mapa de la frase no incluye el identificador`() {
        val frase = FraseGuardada(id = "f-4", texto = "Hola", creadaEn = 1L)

        assertFalse(frase.aMapa().containsKey("id"))
    }
}
