package cl.mariovalle.comunicame.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de las reglas de validacion de los formularios.
//
// Son JUnit puro, sin Robolectric: estas funciones son Kotlin y no tocan
// Android, asi que corren directo en la JVM y terminan en milisegundos.
//
// Se prueban sobre todo los limites y los casos que rompen, no los felices:
// que un correo bien escrito pase no dice mucho; que uno con espacios sea
// rechazado si.
class ValidacionesTest {

    // CORREO

    @Test
    fun `acepta un correo bien formado`() {
        assertTrue("ana@correo.cl".esCorreoValido())
        assertTrue("maria.ovalle@duocuc.cl".esCorreoValido())
    }

    @Test
    fun `rechaza correos sin arroba o sin punto despues de la arroba`() {
        assertFalse("anacorreo.cl".esCorreoValido())
        assertFalse("ana@correocl".esCorreoValido())
    }

    @Test
    fun `rechaza un correo que no tenga nada antes de la arroba`() {
        assertFalse("@correo.cl".esCorreoValido())
    }

    @Test
    fun `rechaza un correo que termine en punto`() {
        assertFalse("ana@correo.".esCorreoValido())
    }

    @Test
    fun `rechaza un correo con espacios intermedios`() {
        assertFalse("ana torres@correo.cl".esCorreoValido())
    }

    @Test
    fun `ignora los espacios de los extremos`() {
        // El usuario que pega su correo desde otro lado suele arrastrar
        // espacios. Eso no deberia costarle un error en pantalla.
        assertTrue("  ana@correo.cl  ".esCorreoValido())
    }

    // NOMBRE DE USUARIO

    @Test
    fun `acepta usuarios con letras, numeros, punto y guion bajo`() {
        assertTrue("ana".esUsuarioValido())
        assertTrue("ana.torres".esUsuarioValido())
        assertTrue("ana_torres2".esUsuarioValido())
    }

    @Test
    fun `rechaza usuarios de menos de tres caracteres`() {
        assertFalse("an".esUsuarioValido())
        assertTrue("ana".esUsuarioValido())
    }

    @Test
    fun `rechaza usuarios con espacios o simbolos`() {
        assertFalse("ana torres".esUsuarioValido())
        assertFalse("ana-torres".esUsuarioValido())
        assertFalse("ana@torres".esUsuarioValido())
    }

    // CONTRASENA

    @Test
    fun `exige el largo minimo definido por la aplicacion`() {
        assertFalse("corta".tieneLargoMinimo())
        assertTrue("suficientemente-larga".tieneLargoMinimo())
    }

    @Test
    fun `el largo minimo se cuenta justo en el limite`() {
        // Ocho caracteres exactos deben pasar: el minimo es inclusivo.
        assertEquals(8, LARGO_MINIMO_CONTRASENA)
        assertTrue("12345678".tieneLargoMinimo())
        assertFalse("1234567".tieneLargoMinimo())
    }

    @Test
    fun `la fuerza sube a medida que la contrasena cumple mas reglas`() {
        assertEquals(0, "".fuerzaContrasena())
        // Corta y solo letras minusculas: no cumple ninguna regla
        assertEquals(0, "abc".fuerzaContrasena())
        // Larga, pero sin numeros ni mayusculas ni simbolos
        assertEquals(1, "abcdefgh".fuerzaContrasena())
        // Larga y con numero
        assertEquals(2, "abcdefg1".fuerzaContrasena())
        // Larga, numero y mayuscula
        assertEquals(3, "Abcdefg1".fuerzaContrasena())
        // Las cuatro reglas
        assertEquals(4, "Abcdefg1!".fuerzaContrasena())
    }

    // TEXTO

    @Test
    fun `convierte a nombre propio y colapsa los espacios sobrantes`() {
        assertEquals("Ana Torres", "ana torres".aNombrePropio())
        assertEquals("Ana Torres", "  ANA   TORRES  ".aNombrePropio())
    }

    @Test
    fun `compara usuarios sin distinguir mayusculas ni espacios`() {
        // Evita que se registren dos cuentas que la persona leeria como iguales
        assertTrue("Ana".equivaleA(" ana "))
        assertFalse("Ana".equivaleA("Ana2"))
    }

    @Test
    fun `recorta el texto largo y deja el corto intacto`() {
        assertEquals("Hola", "Hola".recortadoA(10))
        assertEquals("Hola…", "Hola mundo".recortadoA(5))
    }

    @Test
    fun `cuenta las palabras sin dejarse enganar por los espacios dobles`() {
        assertEquals(2, "ana torres".cantidadPalabras)
        assertEquals(2, "  ana    torres  ".cantidadPalabras)
        assertEquals(0, "   ".cantidadPalabras)
    }

    @Test
    fun `un texto de solo espacios se considera vacio`() {
        assertTrue("     ".estaVacio)
        assertFalse(" a ".estaVacio)
    }
}
