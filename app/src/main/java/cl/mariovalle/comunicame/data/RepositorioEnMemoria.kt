package cl.mariovalle.comunicame.data

import cl.mariovalle.comunicame.util.equivaleA

// Repositorio que guarda todo en memoria, sin red.
//
// Cumple dos papeles. Permite abrir la aplicacion cuando se compilo sin
// google-services.json, y sirve de doble en las pruebas unitarias, que asi
// pueden ejercitar el CRUD completo sin emulador ni credenciales.
//
// Lo que no hace es persistir: al cerrar la aplicacion se pierde todo. Esa es
// justamente la limitacion que Firebase vino a resolver y la razon por la que
// no puede ser la implementacion principal.
class RepositorioEnMemoria(
    // Cuentas creadas, indexadas por uid. Se guarda la contrasena porque esta
    // implementacion tiene que poder validar un inicio de sesion; en Firebase
    // nunca llega al dispositivo.
    private val cuentas: MutableMap<String, CuentaEnMemoria> = mutableMapOf(),
    private val frases: MutableMap<String, MutableList<FraseGuardada>> = mutableMapOf()
) : RepositorioComunicame {

    data class CuentaEnMemoria(val usuario: Usuario, val contrasena: String)

    private var uidConectado: String? = null
    private var siguienteId = 1

    override suspend fun registrar(
        usuario: Usuario,
        contrasena: String
    ): ResultadoOperacion<Usuario> {
        val correoTomado = cuentas.values.any { it.usuario.correo.equivaleA(usuario.correo) }
        if (correoTomado) {
            return ResultadoOperacion.Error("Ya existe una cuenta registrada con ese correo.")
        }

        val uid = "uid-${siguienteId++}"
        val registrado = usuario.copy(uid = uid, correo = usuario.correo.trim())

        cuentas[uid] = CuentaEnMemoria(registrado, contrasena)
        frases[uid] = mutableListOf()
        uidConectado = uid

        return ResultadoOperacion.Exito(registrado)
    }

    override suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): ResultadoOperacion<Usuario> {
        val cuenta = cuentas.values.firstOrNull {
            it.usuario.correo.equivaleA(correo) && it.contrasena == contrasena
        } ?: return ResultadoOperacion.Error("El correo o la contraseña no son correctos.")

        uidConectado = cuenta.usuario.uid
        return ResultadoOperacion.Exito(cuenta.usuario)
    }

    override suspend fun enviarCorreoDeRecuperacion(
        correo: String
    ): ResultadoOperacion<Unit> = ResultadoOperacion.Exito(Unit)

    override fun cerrarSesion() {
        uidConectado = null
    }

    override fun uidActual(): String? = uidConectado

    override suspend fun obtenerUsuario(uid: String): ResultadoOperacion<Usuario> {
        val cuenta = cuentas[uid]
            ?: return ResultadoOperacion.Error("No encontramos los datos de tu cuenta.")
        return ResultadoOperacion.Exito(cuenta.usuario)
    }

    override suspend fun actualizarUsuario(usuario: Usuario): ResultadoOperacion<Usuario> {
        val cuenta = cuentas[usuario.uid]
            ?: return ResultadoOperacion.Error("No encontramos los datos de tu cuenta.")

        cuentas[usuario.uid] = cuenta.copy(usuario = usuario)
        return ResultadoOperacion.Exito(usuario)
    }

    override suspend fun eliminarCuenta(uid: String): ResultadoOperacion<Unit> {
        if (!cuentas.containsKey(uid)) {
            return ResultadoOperacion.Error("No encontramos los datos de tu cuenta.")
        }

        frases.remove(uid)
        cuentas.remove(uid)
        if (uidConectado == uid) uidConectado = null

        return ResultadoOperacion.Exito(Unit)
    }

    override suspend fun listarFrases(uid: String): ResultadoOperacion<List<FraseGuardada>> {
        val guardadas = frases[uid].orEmpty()
        // Las ultimas primero, igual que en Firestore
        return ResultadoOperacion.Exito(guardadas.sortedByDescending { it.creadaEn })
    }

    override suspend fun agregarFrase(
        uid: String,
        texto: String
    ): ResultadoOperacion<FraseGuardada> {
        if (!cuentas.containsKey(uid)) {
            return ResultadoOperacion.Error("No encontramos los datos de tu cuenta.")
        }

        val frase = FraseGuardada(
            id = "frase-${siguienteId++}",
            texto = texto.trim(),
            creadaEn = System.currentTimeMillis()
        )

        frases.getOrPut(uid) { mutableListOf() }.add(frase)
        return ResultadoOperacion.Exito(frase)
    }

    override suspend fun editarFrase(
        uid: String,
        fraseId: String,
        textoNuevo: String
    ): ResultadoOperacion<FraseGuardada> {
        val guardadas = frases[uid]
            ?: return ResultadoOperacion.Error("La frase ya no existe.")

        val indice = guardadas.indexOfFirst { it.id == fraseId }
        if (indice == -1) return ResultadoOperacion.Error("La frase ya no existe.")

        // Conserva creadaEn para que la frase no cambie de lugar en la lista
        val editada = guardadas[indice].copy(texto = textoNuevo.trim())
        guardadas[indice] = editada

        return ResultadoOperacion.Exito(editada)
    }

    override suspend fun eliminarFrase(
        uid: String,
        fraseId: String
    ): ResultadoOperacion<Unit> {
        val guardadas = frases[uid]
            ?: return ResultadoOperacion.Error("La frase ya no existe.")

        val quitada = guardadas.removeAll { it.id == fraseId }
        if (!quitada) return ResultadoOperacion.Error("La frase ya no existe.")

        return ResultadoOperacion.Exito(Unit)
    }
}
