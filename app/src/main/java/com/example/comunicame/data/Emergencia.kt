package com.example.comunicame.data

// Ojo: una persona sorda no puede hablar por telefono. Estos numeros no son la
// via principal de auxilio, son informacion para que marque alguien que este
// al lado. Lo principal es la tarjeta que se muestra en pantalla.
data class ServicioEmergencia(
    val nombre: String,
    val numero: String,
    val detalle: String
)

val serviciosEmergencia = listOf(
    ServicioEmergencia("Ambulancia (SAMU)", "131", "Urgencias médicas"),
    ServicioEmergencia("Bomberos", "132", "Incendios y rescates"),
    ServicioEmergencia("Carabineros", "133", "Seguridad y delitos"),
    ServicioEmergencia("PDI", "134", "Investigaciones")
)
