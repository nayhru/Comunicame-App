package cl.mariovalle.comunicame.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Pruebas del CRUD completo sobre cuentas y frases.
//
// Se ejercita la implementacion en memoria, que cumple el mismo contrato que
// la de Firebase. Eso permite probar la logica de las cuatro operaciones sin
// red ni credenciales: una prueba que dependiera de Firestore tardaria
// segundos, necesitaria conexion y podria fallar por motivos ajenos al codigo.
//
// runTest viene de kotlinx-coroutines-test y permite llamar funciones suspend
// desde una prueba, adelantando el reloj virtual en vez de esperar de verdad.
class RepositorioEnMemoriaTest {

    private lateinit var repositorio: RepositorioEnMemoria

    private val anaDePrueba = Usuario(
        nombre = "Ana Torres",
        usuario = "ana",
        correo = "ana.torres@correo.cl",
        comuna = "Santiago",
        preferencia = PreferenciaComunicacion.LENGUA_DE_SENAS
    )

    @Before
    fun prepararRepositorio() {
        repositorio = RepositorioEnMemoria()
    }

    // REGISTRO E INICIO DE SESION

    @Test
    fun `registrar devuelve el usuario con el identificador asignado`() = runTest {
        val resultado = repositorio.registrar(anaDePrueba, "Ana12345")

        assertTrue(resultado.esExito)
        val registrada = resultado.datoONulo()!!
        assertTrue(registrada.uid.isNotBlank())
        assertEquals("ana", registrada.usuario)
    }

    @Test
    fun `no se puede registrar dos veces el mismo correo`() = runTest {
        repositorio.registrar(anaDePrueba, "Ana12345")

        val segundo = repositorio.registrar(
            anaDePrueba.copy(usuario = "ana2"),
            "Otra12345"
        )

        assertFalse(segundo.esExito)
        assertEquals("Ya existe una cuenta registrada con ese correo.", segundo.mensajeError)
    }

    @Test
    fun `el correo repetido se detecta aunque cambien las mayusculas`() = runTest {
        repositorio.registrar(anaDePrueba, "Ana12345")

        val segundo = repositorio.registrar(
            anaDePrueba.copy(correo = "ANA.TORRES@correo.cl"),
            "Otra12345"
        )

        assertFalse(segundo.esExito)
    }

    @Test
    fun `iniciar sesion con las credenciales correctas devuelve el usuario`() = runTest {
        repositorio.registrar(anaDePrueba, "Ana12345")
        repositorio.cerrarSesion()

        val resultado = repositorio.iniciarSesion("ana.torres@correo.cl", "Ana12345")

        assertTrue(resultado.esExito)
        assertEquals("Ana Torres", resultado.datoONulo()?.nombre)
    }

    @Test
    fun `iniciar sesion con la contrasena equivocada falla`() = runTest {
        repositorio.registrar(anaDePrueba, "Ana12345")

        val resultado = repositorio.iniciarSesion("ana.torres@correo.cl", "equivocada")

        assertFalse(resultado.esExito)
    }

    @Test
    fun `el mensaje de error no distingue correo inexistente de clave incorrecta`() = runTest {
        repositorio.registrar(anaDePrueba, "Ana12345")

        val claveMala = repositorio.iniciarSesion("ana.torres@correo.cl", "equivocada")
        val correoInexistente = repositorio.iniciarSesion("nadie@correo.cl", "Ana12345")

        // Si los mensajes fueran distintos, cualquiera podria averiguar que
        // correos tienen cuenta probando uno por uno.
        assertEquals(claveMala.mensajeError, correoInexistente.mensajeError)
    }

    @Test
    fun `cerrar sesion deja sin usuario conectado`() = runTest {
        repositorio.registrar(anaDePrueba, "Ana12345")
        assertTrue(repositorio.uidActual() != null)

        repositorio.cerrarSesion()

        assertNull(repositorio.uidActual())
    }

    // CRUD DE LA CUENTA

    @Test
    fun `obtener usuario devuelve los datos guardados`() = runTest {
        val uid = repositorio.registrar(anaDePrueba, "Ana12345").datoONulo()!!.uid

        val resultado = repositorio.obtenerUsuario(uid)

        assertTrue(resultado.esExito)
        assertEquals("Santiago", resultado.datoONulo()?.comuna)
    }

    @Test
    fun `obtener un usuario que no existe devuelve error`() = runTest {
        val resultado = repositorio.obtenerUsuario("uid-inventado")

        assertFalse(resultado.esExito)
    }

    @Test
    fun `actualizar guarda los cambios del perfil`() = runTest {
        val registrada = repositorio.registrar(anaDePrueba, "Ana12345").datoONulo()!!

        val editada = registrada.copy(
            nombre = "Ana Torres Rojas",
            comuna = "Providencia",
            vibracion = false
        )
        repositorio.actualizarUsuario(editada)

        val leida = repositorio.obtenerUsuario(registrada.uid).datoONulo()!!
        assertEquals("Ana Torres Rojas", leida.nombre)
        assertEquals("Providencia", leida.comuna)
        assertFalse(leida.vibracion)
    }

    @Test
    fun `eliminar la cuenta borra tambien sus frases`() = runTest {
        val uid = repositorio.registrar(anaDePrueba, "Ana12345").datoONulo()!!.uid
        repositorio.agregarFrase(uid, "Hola")
        repositorio.agregarFrase(uid, "Gracias")

        repositorio.eliminarCuenta(uid)

        // Lo que de verdad se comprueba: que no queden datos de la persona
        // dando vueltas despues de que pidio darse de baja.
        assertFalse(repositorio.obtenerUsuario(uid).esExito)
        assertTrue(repositorio.listarFrases(uid).datoONulo().isNullOrEmpty())
    }

    @Test
    fun `eliminar la cuenta cierra la sesion`() = runTest {
        val uid = repositorio.registrar(anaDePrueba, "Ana12345").datoONulo()!!.uid

        repositorio.eliminarCuenta(uid)

        assertNull(repositorio.uidActual())
    }

    // CRUD DE LAS FRASES

    @Test
    fun `una cuenta nueva no tiene frases guardadas`() = runTest {
        val uid = repositorio.registrar(anaDePrueba, "Ana12345").datoONulo()!!.uid

        val frases = repositorio.listarFrases(uid).datoONulo()!!

        assertTrue(frases.isEmpty())
    }

    @Test
    fun `agregar una frase la deja disponible en la lista`() = runTest {
        val uid = repositorio.registrar(anaDePrueba, "Ana12345").datoONulo()!!.uid

        repositorio.agregarFrase(uid, "Necesito ayuda")

        val frases = repositorio.listarFrases(uid).datoONulo()!!
        assertEquals(1, frases.size)
        assertEquals("Necesito ayuda", frases.first().texto)
    }

    @Test
    fun `agregar recorta los espacios de los extremos`() = runTest {
        val uid = repositorio.registrar(anaDePrueba, "Ana12345").datoONulo()!!.uid

        val frase = repositorio.agregarFrase(uid, "   Gracias   ").datoONulo()!!

        assertEquals("Gracias", frase.texto)
    }

    @Test
    fun `editar cambia el texto y conserva el identificador`() = runTest {
        val uid = repositorio.registrar(anaDePrueba, "Ana12345").datoONulo()!!.uid
        val original = repositorio.agregarFrase(uid, "Necesito ayuda").datoONulo()!!

        val editada = repositorio
            .editarFrase(uid, original.id, "Necesito ayuda, por favor")
            .datoONulo()!!

        assertEquals(original.id, editada.id)
        assertEquals("Necesito ayuda, por favor", editada.texto)
    }

    @Test
    fun `editar conserva la fecha de creacion para no mover la frase de lugar`() = runTest {
        val uid = repositorio.registrar(anaDePrueba, "Ana12345").datoONulo()!!.uid
        val original = repositorio.agregarFrase(uid, "Hola").datoONulo()!!

        val editada = repositorio.editarFrase(uid, original.id, "Hola, buenos días").datoONulo()!!

        assertEquals(original.creadaEn, editada.creadaEn)
    }

    @Test
    fun `editar una frase que no existe devuelve error`() = runTest {
        val uid = repositorio.registrar(anaDePrueba, "Ana12345").datoONulo()!!.uid

        val resultado = repositorio.editarFrase(uid, "frase-inventada", "Texto")

        assertFalse(resultado.esExito)
    }

    @Test
    fun `eliminar quita la frase de la lista`() = runTest {
        val uid = repositorio.registrar(anaDePrueba, "Ana12345").datoONulo()!!.uid
        val frase = repositorio.agregarFrase(uid, "Hola").datoONulo()!!
        repositorio.agregarFrase(uid, "Gracias")

        repositorio.eliminarFrase(uid, frase.id)

        val quedan = repositorio.listarFrases(uid).datoONulo()!!
        assertEquals(1, quedan.size)
        assertEquals("Gracias", quedan.first().texto)
    }

    @Test
    fun `eliminar una frase que no existe devuelve error`() = runTest {
        val uid = repositorio.registrar(anaDePrueba, "Ana12345").datoONulo()!!.uid

        val resultado = repositorio.eliminarFrase(uid, "frase-inventada")

        assertFalse(resultado.esExito)
    }

    @Test
    fun `las frases de una persona no aparecen en la lista de otra`() = runTest {
        val uidAna = repositorio.registrar(anaDePrueba, "Ana12345").datoONulo()!!.uid
        val uidLuis = repositorio.registrar(
            anaDePrueba.copy(usuario = "luis", correo = "luis@correo.cl"),
            "Luis12345"
        ).datoONulo()!!.uid

        repositorio.agregarFrase(uidAna, "Frase de Ana")

        // Es la misma separacion que imponen las reglas de seguridad de
        // Firestore. Se prueba aca porque una falla asi mostraria las frases
        // privadas de una persona a otra.
        assertNotEquals(uidAna, uidLuis)
        assertTrue(repositorio.listarFrases(uidLuis).datoONulo()!!.isEmpty())
        assertEquals(1, repositorio.listarFrases(uidAna).datoONulo()!!.size)
    }
}
