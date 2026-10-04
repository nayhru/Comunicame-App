package cl.mariovalle.comunicame.data

import androidx.compose.runtime.mutableStateListOf
import cl.mariovalle.comunicame.util.LARGO_MINIMO_CONTRASENA
import cl.mariovalle.comunicame.util.ResultadoValidacion
import cl.mariovalle.comunicame.util.equivaleA
import cl.mariovalle.comunicame.util.esCorreoValido
import cl.mariovalle.comunicame.util.esUsuarioValido
import cl.mariovalle.comunicame.util.estaVacio
import cl.mariovalle.comunicame.util.limpio
import cl.mariovalle.comunicame.util.tieneLargoMinimo

// Arreglo de usuarios en memoria. Parte con los 5 que pide el enunciado y
// Registro le va agregando mas.
// Uso mutableStateListOf y no listOf para que Compose vea los cambios y
// recomponga solo lo que lee la lista.
object RepositorioUsuarios {

    // Documenta el requisito del enunciado: 5 usuarios precargados
    const val CANTIDAD_INICIAL = 5

    val usuarios = mutableStateListOf(
        Usuario(
            nombre = "Ana Torres",
            usuario = "ana",
            correo = "ana.torres@correo.cl",
            contrasena = "Ana12345",
            comuna = "Santiago",
            preferencia = PreferenciaComunicacion.LENGUA_DE_SENAS
        ),
        Usuario(
            nombre = "Carlos Perez",
            usuario = "carlos",
            correo = "carlos.perez@correo.cl",
            contrasena = "Carlos123",
            comuna = "Maipu",
            preferencia = PreferenciaComunicacion.TEXTO_ESCRITO
        ),
        Usuario(
            nombre = "Maria Soto",
            usuario = "maria",
            correo = "maria.soto@correo.cl",
            contrasena = "Maria2026",
            comuna = "Puente Alto",
            preferencia = PreferenciaComunicacion.LECTURA_LABIAL
        ),
        Usuario(
            nombre = "Luis Rojas",
            usuario = "luis",
            correo = "luis.rojas@correo.cl",
            contrasena = "Luis4567",
            comuna = "La Florida",
            preferencia = PreferenciaComunicacion.LENGUA_DE_SENAS
        ),
        Usuario(
            nombre = "Sofia Diaz",
            usuario = "sofia",
            correo = "sofia.diaz@correo.cl",
            contrasena = "Sofia789",
            comuna = "Nunoa",
            preferencia = PreferenciaComunicacion.TEXTO_ESCRITO
        )
    )

    // Opciones del combo box de Registro
    val comunas = listOf(
        "Santiago", "Maipu", "Puente Alto", "La Florida",
        "Nunoa", "Providencia", "Las Condes", "San Bernardo"
    )

    // Set con los correos ya tomados. Se recalcula porque la lista cambia;
    // un Set consulta pertenencia en O(1), una List tendria que recorrerla.
    private fun correosRegistrados(): Set<String> =
        usuarios.map { it.correo.lowercase() }.toSet()

    private fun usuariosRegistrados(): Set<String> =
        usuarios.map { it.usuario.lowercase() }.toSet()

    // Devuelve el usuario si coinciden usuario y clave, si no null
    fun validarCredenciales(usuario: String, contrasena: String): Usuario? =
        usuarios.find {
            it.usuario.equivaleA(usuario) && it.contrasena == contrasena
        }

    fun existeUsuario(usuario: String): Boolean =
        usuario.limpio.lowercase() in usuariosRegistrados()

    fun existeCorreo(correo: String): Boolean =
        correo.limpio.lowercase() in correosRegistrados()

    // Valida el Login antes de buscar en la lista.
    // primeraFalla corta en el primer error, asi el mensaje es el mas util.
    fun validarLogin(usuario: String, contrasena: String): ResultadoValidacion =
        ResultadoValidacion.primeraFalla(
            {
                ResultadoValidacion.exigir(!usuario.estaVacio && !contrasena.estaVacio) {
                    "Completa tu usuario y tu contraseña"
                }
            },
            {
                ResultadoValidacion.exigir(
                    validarCredenciales(usuario, contrasena) != null,
                    critico = true
                ) { "Usuario o contraseña incorrectos" }
            }
        )

    // Valida el Registro completo.
    // Cada regla es una lambda; se evaluan en orden hasta la primera que falle.
    fun validarRegistro(
        nombre: String,
        usuario: String,
        correo: String,
        contrasena: String,
        repetir: String,
        comuna: String,
        aceptaTerminos: Boolean
    ): ResultadoValidacion = ResultadoValidacion.primeraFalla(
        {
            val camposLlenos = listOf(nombre, usuario, correo, contrasena, repetir)
                .none { it.estaVacio }
            ResultadoValidacion.exigir(camposLlenos) { "Completa todos los campos" }
        },
        { ResultadoValidacion.exigir(!comuna.estaVacio) { "Selecciona tu comuna" } },
        {
            ResultadoValidacion.exigir(usuario.esUsuarioValido()) {
                "El usuario necesita 3 caracteres o más, sin espacios ni símbolos"
            }
        },
        {
            ResultadoValidacion.exigir(correo.esCorreoValido()) {
                "El correo no tiene un formato válido"
            }
        },
        {
            ResultadoValidacion.exigir(contrasena.tieneLargoMinimo()) {
                "La contraseña debe tener al menos $LARGO_MINIMO_CONTRASENA caracteres"
            }
        },
        {
            ResultadoValidacion.exigir(contrasena == repetir, critico = true) {
                "Las contraseñas no coinciden"
            }
        },
        {
            ResultadoValidacion.exigir(!existeUsuario(usuario), critico = true) {
                "Ese nombre de usuario ya está registrado"
            }
        },
        {
            ResultadoValidacion.exigir(!existeCorreo(correo), critico = true) {
                "Ese correo ya está registrado"
            }
        },
        {
            ResultadoValidacion.exigir(aceptaTerminos) {
                "Debes aceptar los términos para continuar"
            }
        }
    )

    // Valida el formulario de Recuperar contrasena
    fun validarRecuperacion(
        correo: String,
        nueva: String,
        repetir: String
    ): ResultadoValidacion = ResultadoValidacion.primeraFalla(
        {
            val llenos = listOf(correo, nueva, repetir).none { it.estaVacio }
            ResultadoValidacion.exigir(llenos) { "Completa todos los campos" }
        },
        {
            ResultadoValidacion.exigir(buscarPorCorreo(correo) != null, critico = true) {
                "No existe una cuenta con ese correo"
            }
        },
        {
            ResultadoValidacion.exigir(nueva.tieneLargoMinimo()) {
                "La contraseña debe tener al menos $LARGO_MINIMO_CONTRASENA caracteres"
            }
        },
        {
            ResultadoValidacion.exigir(nueva == repetir, critico = true) {
                "Las contraseñas no coinciden"
            }
        }
    )

    // false si el usuario o el correo ya existian
    fun registrar(nuevo: Usuario): Boolean {
        if (existeUsuario(nuevo.usuario) || existeCorreo(nuevo.correo)) return false
        usuarios.add(nuevo)
        return true
    }

    // Lo usa Recuperar contrasena
    fun buscarPorCorreo(correo: String): Usuario? =
        usuarios.find { it.correo.equivaleA(correo) }

    fun buscarPorUsuario(usuario: String): Usuario? =
        usuarios.find { it.usuario.equivaleA(usuario) }

    fun actualizarContrasena(correo: String, nueva: String): Boolean {
        val indice = usuarios.indexOfFirst { it.correo.equivaleA(correo) }
        if (indice == -1) return false
        usuarios[indice] = usuarios[indice].copy(contrasena = nueva)
        return true
    }

    // A proposito no hay funciones que expongan datos agregados de los usuarios
    // (cuantos hay, en que comunas viven, como se comunican). Aunque sean
    // estadisticas y no datos individuales, siguen siendo informacion de
    // terceros y ninguna pantalla de la app tiene por que mostrarla.
}
