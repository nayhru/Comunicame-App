package com.example.comunicame.data

import androidx.compose.runtime.mutableStateListOf

// Arreglo de usuarios en memoria. Parte con los 5 que pide el enunciado y
// Registro le va agregando mas.
// Uso mutableStateListOf y no listOf para que Compose vea los cambios y
// recomponga solo lo que lee la lista.
object RepositorioUsuarios {

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

    // Devuelve el usuario si coinciden usuario y clave, si no null
    fun validarCredenciales(usuario: String, contrasena: String): Usuario? =
        usuarios.find {
            it.usuario.equals(usuario.trim(), ignoreCase = true) &&
                it.contrasena == contrasena
        }

    fun existeUsuario(usuario: String): Boolean =
        usuarios.any { it.usuario.equals(usuario.trim(), ignoreCase = true) }

    fun existeCorreo(correo: String): Boolean =
        usuarios.any { it.correo.equals(correo.trim(), ignoreCase = true) }

    // false si el usuario o el correo ya existian
    fun registrar(nuevo: Usuario): Boolean {
        if (existeUsuario(nuevo.usuario) || existeCorreo(nuevo.correo)) return false
        usuarios.add(nuevo)
        return true
    }

    // Lo usa Recuperar contrasena
    fun buscarPorCorreo(correo: String): Usuario? =
        usuarios.find { it.correo.equals(correo.trim(), ignoreCase = true) }

    fun actualizarContrasena(correo: String, nueva: String): Boolean {
        val indice = usuarios.indexOfFirst { it.correo.equals(correo.trim(), ignoreCase = true) }
        if (indice == -1) return false
        usuarios[indice] = usuarios[indice].copy(contrasena = nueva)
        return true
    }
}
