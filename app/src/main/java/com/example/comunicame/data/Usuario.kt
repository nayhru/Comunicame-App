package com.example.comunicame.data

// Como prefiere comunicarse el usuario. Se elige con RadioButton en Registro.
enum class PreferenciaComunicacion(val etiqueta: String) {
    LENGUA_DE_SENAS("Lengua de señas"),
    TEXTO_ESCRITO("Texto escrito"),
    LECTURA_LABIAL("Lectura labial")
}

// La contrasena va en texto plano porque esta entrega no lleva base de datos.
// Esta declarado en las restricciones del proyecto.
data class Usuario(
    val nombre: String,
    val usuario: String,
    val correo: String,
    val contrasena: String,
    val comuna: String,
    val preferencia: PreferenciaComunicacion,
    val alertasVisuales: Boolean = true,
    val vibracion: Boolean = true,
    val subtitulos: Boolean = true
)
