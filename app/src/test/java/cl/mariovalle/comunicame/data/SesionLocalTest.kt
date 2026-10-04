package cl.mariovalle.comunicame.data

import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Pruebas de la sesion local.
//
// SharedPreferences es parte de Android, no de Kotlin: en una prueba JUnit
// normal todos sus metodos devolverian null porque el framework no existe en
// la JVM. Robolectric levanta un Android simulado dentro de la misma JVM, asi
// que estas pruebas corren con ./gradlew test, sin emulador ni telefono.
//
// Eso importa en este proyecto: el equipo de pruebas es un Redmi con MIUI, que
// bloquea la inyeccion de eventos por adb. Toda prueba que dependiera del
// dispositivo tendria que hacerse a mano.
//
// El sdk se fija en 35 porque Robolectric todavia no publica la imagen del 36,
// que es la que apunta la aplicacion. SharedPreferences no cambio entre ambas
// versiones, asi que la prueba sigue siendo representativa; cuando Robolectric
// soporte el 36 basta con borrar esta anotacion.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SesionLocalTest {

    private lateinit var sesion: SesionLocal

    @Before
    fun prepararSesion() {
        sesion = SesionLocal(ApplicationProvider.getApplicationContext())
        // Robolectric reutiliza el mismo archivo entre pruebas de la clase.
        // Sin este limpiado, el resultado dependeria del orden de ejecucion.
        sesion.cerrar()
    }

    @After
    fun limpiar() {
        sesion.cerrar()
    }

    @Test
    fun `sin datos guardados no hay sesion activa`() {
        assertFalse(sesion.haySesionActiva)
        assertNull(sesion.uid)
        assertNull(sesion.nombreUsuario)
    }

    @Test
    fun `al guardar queda la sesion activa con sus datos`() {
        sesion.guardar(uid = "abc123", usuario = "ana", correo = "ana@correo.cl")

        assertTrue(sesion.haySesionActiva)
        assertEquals("abc123", sesion.uid)
        assertEquals("ana", sesion.nombreUsuario)
        assertEquals("ana@correo.cl", sesion.correo)
    }

    @Test
    fun `cerrar sesion borra todos los datos, no solo el uid`() {
        sesion.guardar(uid = "abc123", usuario = "ana", correo = "ana@correo.cl")

        sesion.cerrar()

        assertFalse(sesion.haySesionActiva)
        assertNull(sesion.uid)
        // Lo que de verdad se comprueba aca: que no quede ningun resto del
        // usuario anterior visible para quien use el telefono despues.
        assertNull(sesion.nombreUsuario)
        assertNull(sesion.correo)
    }

    @Test
    fun `actualizar el nombre no borra el resto de la sesion`() {
        sesion.guardar(uid = "abc123", usuario = "ana", correo = "ana@correo.cl")

        sesion.actualizarNombreUsuario("ana.torres")

        assertEquals("ana.torres", sesion.nombreUsuario)
        assertEquals("abc123", sesion.uid)
        assertEquals("ana@correo.cl", sesion.correo)
        assertTrue(sesion.haySesionActiva)
    }

    @Test
    fun `una sesion nueva lee lo que dejo la anterior`() {
        sesion.guardar(uid = "abc123", usuario = "ana", correo = "ana@correo.cl")

        // Otra instancia equivale a volver a abrir la aplicacion: si el dato
        // no estuviera en disco, esta lectura devolveria null.
        val sesionTrasReabrir = SesionLocal(ApplicationProvider.getApplicationContext())

        assertTrue(sesionTrasReabrir.haySesionActiva)
        assertEquals("ana", sesionTrasReabrir.nombreUsuario)
    }
}
