package cl.mariovalle.comunicame.data

// Ojo: una persona sorda no puede hablar por telefono. Estos numeros no son la
// via principal de auxilio, son informacion para que marque alguien que este
// al lado. Lo principal es la tarjeta que se muestra en pantalla.

// Contrato que cumple cualquier cosa que se pueda contactar en una emergencia.
// Lo hago interface y no clase porque manana un contacto familiar tambien va a
// ser contactable, y no comparte nada mas con un servicio publico.
interface Contactable {
    val nombre: String
    val numero: String

    // La interfaz puede traer implementacion por defecto. Quien la implemente
    // solo sobreescribe si necesita algo distinto.
    fun etiquetaAccesible(): String = "Marcar $numero, $nombre"

    // Metodo abstracto: cada implementacion decide como describirse
    fun descripcionCorta(): String
}

// Clase base de los servicios. Va open porque por defecto en Kotlin las clases
// son cerradas y no se pueden heredar.
open class ServicioEmergencia(
    override val nombre: String,
    override val numero: String,
    val detalle: String,
    val prioridad: Int = 5
) : Contactable {

    // Constructor secundario: permite crear un servicio sin detalle ni
    // prioridad, dejando valores por defecto. Delega en el principal con this()
    constructor(nombre: String, numero: String) : this(
        nombre = nombre,
        numero = numero,
        detalle = "Servicio de emergencia",
        prioridad = 5
    )

    override fun descripcionCorta(): String = "$nombre ($numero)"

    // open para que las subclases puedan cambiarlo
    open fun esPrioritario(): Boolean = prioridad <= 2
}

// Subclase: un servicio medico agrega su especialidad.
class ServicioMedico(
    nombre: String,
    numero: String,
    detalle: String,
    val atiendeUrgenciaVital: Boolean
) : ServicioEmergencia(nombre, numero, detalle, prioridad = 1) {

    // Sobreescribo para sumar la especialidad a la descripcion de la clase base
    override fun descripcionCorta(): String =
        super.descripcionCorta() + if (atiendeUrgenciaVital) " · urgencia vital" else ""

    override fun esPrioritario(): Boolean = true

    override fun etiquetaAccesible(): String =
        "Marcar $numero, $nombre, servicio medico de urgencia"
}

// Subclase: servicio policial.
class ServicioPolicial(
    nombre: String,
    numero: String,
    detalle: String,
    val jurisdiccion: String
) : ServicioEmergencia(nombre, numero, detalle, prioridad = 2) {

    override fun descripcionCorta(): String =
        super.descripcionCorta() + " · $jurisdiccion"
}

// La lista mezcla servicios base y subclases: polimorfismo. Cada uno responde
// descripcionCorta() a su manera aunque los recorra como ServicioEmergencia.
val serviciosEmergencia: List<ServicioEmergencia> = listOf(
    ServicioMedico("Ambulancia (SAMU)", "131", "Urgencias médicas", atiendeUrgenciaVital = true),
    ServicioEmergencia("Bomberos", "132", "Incendios y rescates", prioridad = 2),
    ServicioPolicial("Carabineros", "133", "Seguridad y delitos", jurisdiccion = "nacional"),
    ServicioPolicial("PDI", "134", "Investigaciones", jurisdiccion = "investigativa")
)
