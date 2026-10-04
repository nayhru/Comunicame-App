package cl.mariovalle.comunicame.data

// Contrato de acceso a datos de la aplicacion.
//
// Las pantallas hablan con esta interfaz y no con Firebase directamente. Eso
// permite dos cosas: probar la logica con una implementacion falsa, sin red ni
// credenciales, y tener un reemplazo en memoria para cuando el proyecto se
// compila sin google-services.json.
//
// Todas las operaciones son suspend porque consultan la red. Marcarlas asi
// obliga a llamarlas desde una corrutina y deja fuera, en tiempo de
// compilacion, la posibilidad de bloquear el hilo que dibuja la pantalla.
interface RepositorioComunicame {

    // AUTENTICACION

    // Crea la cuenta en el proveedor de identidad y guarda sus datos.
    // La contrasena viaja a Firebase y no se guarda en el dispositivo.
    suspend fun registrar(
        usuario: Usuario,
        contrasena: String
    ): ResultadoOperacion<Usuario>

    suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): ResultadoOperacion<Usuario>

    suspend fun enviarCorreoDeRecuperacion(correo: String): ResultadoOperacion<Unit>

    fun cerrarSesion()

    // Identificador de la sesion vigente, o null si no hay ninguna.
    fun uidActual(): String?

    // CRUD DE LA CUENTA

    suspend fun obtenerUsuario(uid: String): ResultadoOperacion<Usuario>

    suspend fun actualizarUsuario(usuario: Usuario): ResultadoOperacion<Usuario>

    // Borra la cuenta y todo lo que cuelga de ella.
    suspend fun eliminarCuenta(uid: String): ResultadoOperacion<Unit>

    // CRUD DE LAS FRASES GUARDADAS

    suspend fun listarFrases(uid: String): ResultadoOperacion<List<FraseGuardada>>

    suspend fun agregarFrase(uid: String, texto: String): ResultadoOperacion<FraseGuardada>

    suspend fun editarFrase(
        uid: String,
        fraseId: String,
        textoNuevo: String
    ): ResultadoOperacion<FraseGuardada>

    suspend fun eliminarFrase(uid: String, fraseId: String): ResultadoOperacion<Unit>
}
