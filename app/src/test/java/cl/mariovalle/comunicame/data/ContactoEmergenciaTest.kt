package cl.mariovalle.comunicame.data

import cl.mariovalle.comunicame.util.esTelefonoValido
import cl.mariovalle.comunicame.util.soloDigitos
import cl.mariovalle.comunicame.util.telefonoLegible
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas del contacto de emergencia y de la validacion de telefonos.
//
// Es la pantalla que se usa en el peor momento posible, asi que los casos
// raros importan mas que de costumbre: un numero mal guardado se descubre
// cuando ya no hay tiempo de corregirlo.
class ContactoEmergenciaTest {

    private val mamaDePrueba = ContactoEmergencia(
        nombre = "Rosa Suazo",
        numero = "912345678",
        correo = "rosa@correo.cl",
        parentesco = Parentesco.MADRE
    )

    // TELEFONOS

    @Test
    fun `acepta un movil chileno de nueve digitos`() {
        assertTrue("912345678".esTelefonoValido())
    }

    @Test
    fun `acepta el numero con codigo de pais`() {
        assertTrue("56912345678".esTelefonoValido())
        assertTrue("+56 9 1234 5678".esTelefonoValido())
    }

    @Test
    fun `acepta el numero escrito con espacios y guiones`() {
        // La gente anota los telefonos como se le ocurre. Rechazar un numero
        // valido por el formato es peor que aceptarlo y normalizarlo.
        assertTrue("9 1234 5678".esTelefonoValido())
        assertTrue("9-1234-5678".esTelefonoValido())
        assertTrue("(9) 1234 5678".esTelefonoValido())
    }

    @Test
    fun `rechaza numeros demasiado cortos o largos`() {
        assertFalse("12345".esTelefonoValido())
        assertFalse("9123456789012".esTelefonoValido())
    }

    @Test
    fun `rechaza un campo vacio`() {
        assertFalse("".esTelefonoValido())
        assertFalse("   ".esTelefonoValido())
    }

    @Test
    fun `rechaza once digitos que no empiecen en cincuenta y seis`() {
        assertFalse("99912345678".esTelefonoValido())
    }

    @Test
    fun `soloDigitos quita todo lo que no sea numero`() {
        assertEquals("56912345678", "+56 9 1234 5678".soloDigitos)
        assertEquals("912345678", "(9) 1234-5678".soloDigitos)
    }

    @Test
    fun `telefonoLegible formatea el numero para mostrarlo`() {
        assertEquals("+56 9 1234 5678", "912345678".telefonoLegible())
        assertEquals("+56 9 1234 5678", "56912345678".telefonoLegible())
        assertEquals("+56 9 1234 5678", "+56 9 1234 5678".telefonoLegible())
    }

    @Test
    fun `telefonoLegible deja intacto lo que no puede formatear`() {
        // Si el numero no tiene el largo esperado se muestra tal cual, en vez
        // de inventar un formato que lo deje irreconocible.
        assertEquals("12345", "12345".telefonoLegible())
    }

    // CONTACTO

    @Test
    fun `un contacto sin datos no esta configurado`() {
        assertFalse(ContactoEmergencia().estaConfigurado)
    }

    @Test
    fun `un contacto sin telefono no esta configurado`() {
        assertFalse(ContactoEmergencia(nombre = "Rosa").estaConfigurado)
    }

    @Test
    fun `un contacto sin nombre no esta configurado`() {
        assertFalse(ContactoEmergencia(numero = "912345678").estaConfigurado)
    }

    @Test
    fun `con nombre y telefono ya esta configurado aunque falte el correo`() {
        // El correo es opcional a proposito: en una urgencia nadie escribe un
        // correo, el dato que resuelve es el telefono.
        val sinCorreo = ContactoEmergencia(nombre = "Rosa", numero = "912345678")

        assertTrue(sinCorreo.estaConfigurado)
    }

    @Test
    fun `la etiqueta accesible dice a quien se llama antes del numero`() {
        // Con el lector de pantalla, oir primero nueve digitos y despues el
        // nombre obliga a escuchar el anuncio entero para saber si es el
        // contacto correcto.
        val etiqueta = mamaDePrueba.etiquetaAccesible()

        assertTrue(etiqueta.startsWith("Llamar a Rosa Suazo"))
        assertTrue(etiqueta.contains("Madre"))
        assertTrue(etiqueta.contains("912345678"))
    }

    @Test
    fun `la descripcion corta es el parentesco`() {
        assertEquals("Madre", mamaDePrueba.descripcionCorta())
    }

    @Test
    fun `el contacto es Contactable igual que un servicio de emergencia`() {
        // La pantalla los recorre con el mismo tipo: ambos son alguien a quien
        // marcar. Si dejara de cumplir la interfaz, el codigo no compilaria.
        val contactable: Contactable = mamaDePrueba

        assertEquals("Rosa Suazo", contactable.nombre)
        assertEquals("912345678", contactable.numero)
    }

    // CONVERSION A FIRESTORE

    @Test
    fun `el contacto sobrevive a la ida y vuelta por el mapa`() {
        val recuperado = ContactoEmergencia.desdeMapa(mamaDePrueba.aMapa())

        assertEquals(mamaDePrueba, recuperado)
    }

    @Test
    fun `el parentesco se guarda por su nombre`() {
        assertEquals("MADRE", mamaDePrueba.aMapa()[ContactoEmergencia.CAMPO_PARENTESCO])
    }

    @Test
    fun `un parentesco desconocido cae en otro`() {
        val datos = mapOf<String, Any?>(ContactoEmergencia.CAMPO_PARENTESCO to "PRIMO_SEGUNDO")

        assertEquals(Parentesco.OTRO, ContactoEmergencia.desdeMapa(datos).parentesco)
    }

    @Test
    fun `un mapa vacio devuelve un contacto sin configurar`() {
        val contacto = ContactoEmergencia.desdeMapa(emptyMap())

        assertFalse(contacto.estaConfigurado)
    }

    // EL CONTACTO DENTRO DEL USUARIO

    @Test
    fun `el usuario guarda y recupera su contacto de emergencia`() {
        val ana = Usuario(
            uid = "uid-1",
            nombre = "Ana Torres",
            usuario = "ana",
            correo = "ana@correo.cl",
            comuna = "Santiago",
            contactoEmergencia = mamaDePrueba
        )

        val recuperado = Usuario.desdeMapa(ana.uid, ana.aMapa())

        assertEquals(mamaDePrueba, recuperado.contactoEmergencia)
    }

    @Test
    fun `un usuario sin contacto guardado queda con uno vacio`() {
        val datos = mapOf<String, Any?>(Usuario.CAMPO_NOMBRE to "Ana")

        val usuario = Usuario.desdeMapa("uid-2", datos)

        assertFalse(usuario.contactoEmergencia.estaConfigurado)
    }

    // VALIDACION DEL FORMULARIO

    @Test
    fun `el formulario exige nombre`() {
        val resultado = ValidadorFormularios.validarContactoEmergencia(
            nombre = "", numero = "912345678", correo = ""
        )

        assertFalse(resultado.esValido)
    }

    @Test
    fun `el formulario exige telefono`() {
        val resultado = ValidadorFormularios.validarContactoEmergencia(
            nombre = "Rosa", numero = "", correo = ""
        )

        assertFalse(resultado.esValido)
    }

    @Test
    fun `el formulario rechaza un telefono mal escrito`() {
        val resultado = ValidadorFormularios.validarContactoEmergencia(
            nombre = "Rosa", numero = "123", correo = ""
        )

        assertFalse(resultado.esValido)
    }

    @Test
    fun `el formulario acepta el contacto sin correo`() {
        val resultado = ValidadorFormularios.validarContactoEmergencia(
            nombre = "Rosa", numero = "912345678", correo = ""
        )

        assertTrue(resultado.esValido)
    }

    @Test
    fun `el formulario valida el correo solo si se escribio`() {
        val malEscrito = ValidadorFormularios.validarContactoEmergencia(
            nombre = "Rosa", numero = "912345678", correo = "esto-no-es-un-correo"
        )
        assertFalse(malEscrito.esValido)

        val bienEscrito = ValidadorFormularios.validarContactoEmergencia(
            nombre = "Rosa", numero = "912345678", correo = "rosa@correo.cl"
        )
        assertTrue(bienEscrito.esValido)
    }
}
