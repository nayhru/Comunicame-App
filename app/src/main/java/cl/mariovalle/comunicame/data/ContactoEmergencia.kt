package cl.mariovalle.comunicame.data

// Parentesco de la persona de contacto. Se elige de una lista cerrada para que
// la pantalla de emergencia pueda mostrarlo sin depender de como lo escriba
// cada quien.
enum class Parentesco(val etiqueta: String) {
    MADRE("Madre"),
    PADRE("Padre"),
    HIJA_HIJO("Hija o hijo"),
    HERMANA_HERMANO("Hermana o hermano"),
    PAREJA("Pareja"),
    ABUELA_ABUELO("Abuela o abuelo"),
    TIA_TIO("Tía o tío"),
    AMIGA_AMIGO("Amiga o amigo"),
    CUIDADORA_CUIDADOR("Cuidadora o cuidador"),
    VECINA_VECINO("Vecina o vecino"),
    OTRO("Otro");

    companion object {
        fun desdeNombre(nombre: String?): Parentesco =
            entries.firstOrNull { it.name == nombre } ?: OTRO
    }
}

// Persona a la que avisar si quien usa la aplicacion necesita ayuda.
//
// Es el dato central de la pantalla de emergencia. Quien toma el telefono de
// una persona sorda en una situacion de apuro no necesita los datos de ella
// (los tiene delante), necesita saber a quien llamar.
//
// Implementa Contactable, la misma interfaz que los servicios de emergencia,
// porque la pantalla los trata igual: ambos son alguien a quien marcar.
data class ContactoEmergencia(
    override val nombre: String = "",
    override val numero: String = "",
    val correo: String = "",
    val parentesco: Parentesco = Parentesco.OTRO
) : Contactable {

    // Si no hay nombre ni numero, todavia no se ha configurado
    val estaConfigurado: Boolean
        get() = nombre.isNotBlank() && numero.isNotBlank()

    override fun descripcionCorta(): String = parentesco.etiqueta

    // Sobreescribe la de la interfaz para que el lector de pantalla diga de
    // quien se trata antes del numero, que es lo que importa en una urgencia.
    override fun etiquetaAccesible(): String =
        "Llamar a $nombre, ${parentesco.etiqueta}, al $numero"

    fun aMapa(): Map<String, Any> = mapOf(
        CAMPO_NOMBRE to nombre,
        CAMPO_NUMERO to numero,
        CAMPO_CORREO to correo,
        CAMPO_PARENTESCO to parentesco.name
    )

    companion object {
        const val CAMPO_NOMBRE = "nombre"
        const val CAMPO_NUMERO = "numero"
        const val CAMPO_CORREO = "correo"
        const val CAMPO_PARENTESCO = "parentesco"

        fun desdeMapa(datos: Map<String, Any?>): ContactoEmergencia = ContactoEmergencia(
            nombre = datos[CAMPO_NOMBRE] as? String ?: "",
            numero = datos[CAMPO_NUMERO] as? String ?: "",
            correo = datos[CAMPO_CORREO] as? String ?: "",
            parentesco = Parentesco.desdeNombre(datos[CAMPO_PARENTESCO] as? String)
        )
    }
}
