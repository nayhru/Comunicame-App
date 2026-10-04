package cl.mariovalle.comunicame.data

import cl.mariovalle.comunicame.util.LARGO_MINIMO_CONTRASENA
import cl.mariovalle.comunicame.util.ResultadoValidacion
import cl.mariovalle.comunicame.util.esCorreoValido
import cl.mariovalle.comunicame.util.esUsuarioValido
import cl.mariovalle.comunicame.util.estaVacio
import cl.mariovalle.comunicame.util.tieneLargoMinimo

// Validaciones de los formularios antes de salir a la red.
//
// Hasta la entrega anterior estas reglas vivian junto al arreglo de usuarios en
// memoria y algunas lo consultaban para saber si un correo ya estaba tomado.
// Ahora esa pregunta la responde Firebase, que es el unico que conoce todas las
// cuentas: dos personas pueden registrarse al mismo tiempo desde dispositivos
// distintos y solo el servidor sabe quien llego primero.
//
// Lo que queda aca es lo que si se puede decidir sin consultar a nadie: que los
// campos esten completos, que el correo tenga forma de correo, que las dos
// contrasenas coincidan. Sirve para avisarle al usuario de inmediato, sin
// gastar una peticion ni hacerlo esperar.
object ValidadorFormularios {

    // Opciones del combo box de Registro
    val comunas = listOf(
        "Santiago", "Maipu", "Puente Alto", "La Florida",
        "Nunoa", "Providencia", "Las Condes", "San Bernardo"
    )

    // El login solo comprueba que haya algo escrito. Si las credenciales son
    // correctas lo dice Firebase, y a proposito con un mensaje unico para
    // correo inexistente y clave incorrecta: distinguirlos le permitiria a
    // cualquiera averiguar que correos tienen cuenta en la aplicacion.
    fun validarLogin(correo: String, contrasena: String): ResultadoValidacion =
        ResultadoValidacion.primeraFalla(
            {
                ResultadoValidacion.exigir(!correo.estaVacio && !contrasena.estaVacio) {
                    "Completa tu correo y tu contraseña"
                }
            },
            {
                ResultadoValidacion.exigir(correo.esCorreoValido()) {
                    "El correo no tiene un formato válido"
                }
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
            ResultadoValidacion.exigir(aceptaTerminos) {
                "Debes aceptar los términos para continuar"
            }
        }
    )

    // Recuperar contrasena ahora solo pide el correo: Firebase envia un enlace
    // y la persona elige la clave nueva en esa pagina. La aplicacion no vuelve
    // a tener la contrasena en ningun momento, que es justamente el punto.
    fun validarRecuperacion(correo: String): ResultadoValidacion =
        ResultadoValidacion.primeraFalla(
            {
                ResultadoValidacion.exigir(!correo.estaVacio) {
                    "Escribe tu correo"
                }
            },
            {
                ResultadoValidacion.exigir(correo.esCorreoValido()) {
                    "El correo no tiene un formato válido"
                }
            }
        )

    // Valida la frase antes de guardarla.
    fun validarFrase(texto: String): ResultadoValidacion =
        ResultadoValidacion.primeraFalla(
            {
                ResultadoValidacion.exigir(!texto.estaVacio) {
                    "Escribe una frase para guardarla"
                }
            },
            {
                ResultadoValidacion.exigir(texto.trim().length <= LARGO_MAXIMO_FRASE) {
                    "La frase no puede superar los $LARGO_MAXIMO_FRASE caracteres"
                }
            }
        )

    // Las frases se muestran en tarjetas; mas largas que esto no se alcanzan a
    // leer de una pasada, que es para lo que sirven.
    const val LARGO_MAXIMO_FRASE = 120
}
