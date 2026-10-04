package cl.mariovalle.comunicame.data

import cl.mariovalle.comunicame.util.LARGO_MINIMO_CONTRASENA
import cl.mariovalle.comunicame.util.ResultadoValidacion
import cl.mariovalle.comunicame.util.esCorreoValido
import cl.mariovalle.comunicame.util.esTelefonoValido
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

    // Comunas agrupadas por region.
    //
    // Hasta la entrega anterior solo habia comunas de Santiago, lo que dejaba
    // fuera a cualquier persona del resto del pais. Agrupar por region permite
    // que el selector filtre primero y no muestre cuarenta opciones de golpe.
    //
    // La lista no pretende ser completa: trae las capitales regionales y las
    // ciudades mas pobladas, mas una opcion para quien no encuentre la suya.
    val comunasPorRegion: Map<String, List<String>> = linkedMapOf(
        "Arica y Parinacota" to listOf("Arica", "Putre"),
        "Tarapaca" to listOf("Iquique", "Alto Hospicio", "Pozo Almonte"),
        "Antofagasta" to listOf("Antofagasta", "Calama", "Tocopilla", "Mejillones"),
        "Atacama" to listOf("Copiapo", "Vallenar", "Chanaral", "Caldera"),
        "Coquimbo" to listOf("La Serena", "Coquimbo", "Ovalle", "Illapel"),
        "Valparaiso" to listOf(
            "Valparaiso", "Vina del Mar", "Quilpue", "Villa Alemana",
            "San Antonio", "Quillota", "Los Andes", "San Felipe"
        ),
        "Metropolitana" to listOf(
            "Santiago", "Maipu", "Puente Alto", "La Florida",
            "Nunoa", "Providencia", "Las Condes", "San Bernardo",
            "La Pintana", "El Bosque", "Penalolen", "Quilicura",
            "Recoleta", "Independencia", "La Cisterna", "Melipilla"
        ),
        "O'Higgins" to listOf("Rancagua", "San Fernando", "Rengo", "Santa Cruz"),
        "Maule" to listOf("Talca", "Curico", "Linares", "Constitucion"),
        "Nuble" to listOf("Chillan", "San Carlos", "Bulnes"),
        "Biobio" to listOf(
            "Concepcion", "Talcahuano", "Los Angeles",
            "Coronel", "San Pedro de la Paz", "Chiguayante"
        ),
        "La Araucania" to listOf("Temuco", "Padre Las Casas", "Villarrica", "Angol"),
        "Los Rios" to listOf("Valdivia", "La Union", "Panguipulli"),
        "Los Lagos" to listOf("Puerto Montt", "Osorno", "Castro", "Ancud"),
        "Aysen" to listOf("Coyhaique", "Puerto Aysen"),
        "Magallanes" to listOf("Punta Arenas", "Puerto Natales"),
        "Otra" to listOf("Otra comuna")
    )

    // Todas las comunas en una sola lista, para validar y para el buscador.
    val comunas: List<String> = comunasPorRegion.values.flatten()

    // Region a la que pertenece una comuna, o null si no esta en la lista.
    // La usa el selector para abrirse mostrando la region correcta.
    fun regionDe(comuna: String): String? =
        comunasPorRegion.entries.firstOrNull { (_, lista) -> comuna in lista }?.key

    // Filtra por texto escrito, sin distinguir mayusculas ni tildes del
    // teclado. Devuelve las comunas agrupadas tal como se muestran.
    fun buscarComunas(termino: String): Map<String, List<String>> {
        val limpio = termino.trim().lowercase()
        if (limpio.isEmpty()) return comunasPorRegion

        return comunasPorRegion
            .mapValues { (_, lista) -> lista.filter { it.lowercase().contains(limpio) } }
            .filterValues { it.isNotEmpty() }
    }

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

    // Valida el contacto de emergencia.
    //
    // El correo es opcional: lo esencial es el telefono, porque en una urgencia
    // nadie va a escribir un correo. Si se escribe, se comprueba que tenga
    // forma de correo.
    fun validarContactoEmergencia(
        nombre: String,
        numero: String,
        correo: String
    ): ResultadoValidacion = ResultadoValidacion.primeraFalla(
        {
            ResultadoValidacion.exigir(!nombre.estaVacio) {
                "Escribe el nombre de tu contacto"
            }
        },
        {
            ResultadoValidacion.exigir(!numero.estaVacio) {
                "Escribe el teléfono de tu contacto"
            }
        },
        {
            ResultadoValidacion.exigir(numero.esTelefonoValido(), critico = true) {
                "El teléfono debe tener 9 dígitos, por ejemplo 9 1234 5678"
            }
        },
        {
            // El correo solo se valida si lo escribieron
            ResultadoValidacion.exigir(correo.estaVacio || correo.esCorreoValido()) {
                "El correo no tiene un formato válido"
            }
        }
    )

    // Las frases se muestran en tarjetas; mas largas que esto no se alcanzan a
    // leer de una pasada, que es para lo que sirven.
    const val LARGO_MAXIMO_FRASE = 120
}
