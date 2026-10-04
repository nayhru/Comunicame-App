package cl.mariovalle.comunicame.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.io.IOException

// Implementacion del repositorio contra Firebase Authentication y Firestore.
//
// Firebase entrega sus operaciones como Task, que trabaja con callbacks.
// await() las convierte en corrutinas, asi que el codigo se lee de arriba
// abajo en vez de anidarse en addOnSuccessListener.
class RepositorioFirebase(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RepositorioComunicame {

    // AUTENTICACION

    override suspend fun registrar(
        usuario: Usuario,
        contrasena: String
    ): ResultadoOperacion<Usuario> = ejecutar {
        // Primero la cuenta: si el correo ya existe, conviene saberlo antes de
        // escribir nada en la base.
        val credencial = auth
            .createUserWithEmailAndPassword(usuario.correo.trim(), contrasena)
            .await()

        val uid = credencial.user?.uid
            ?: return@ejecutar ResultadoOperacion.Error(
                "No se pudo crear la cuenta. Intenta nuevamente."
            )

        val registrado = usuario.copy(uid = uid, correo = usuario.correo.trim())

        try {
            documentoUsuario(uid).set(registrado.aMapa()).await()
        } catch (e: Exception) {
            // La cuenta quedo creada pero sus datos no. Si no se deshace, el
            // correo queda ocupado y la persona no puede volver a registrarse
            // ni entrar a un perfil que no existe.
            runCatching { credencial.user?.delete()?.await() }
            throw e
        }

        ResultadoOperacion.Exito(registrado)
    }

    override suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): ResultadoOperacion<Usuario> = ejecutar {
        val credencial = auth
            .signInWithEmailAndPassword(correo.trim(), contrasena)
            .await()

        val uid = credencial.user?.uid
            ?: return@ejecutar ResultadoOperacion.Error(
                "No se pudo iniciar sesión. Intenta nuevamente."
            )

        obtenerUsuario(uid)
    }

    override suspend fun enviarCorreoDeRecuperacion(
        correo: String
    ): ResultadoOperacion<Unit> = ejecutar {
        auth.sendPasswordResetEmail(correo.trim()).await()
        ResultadoOperacion.Exito(Unit)
    }

    override fun cerrarSesion() {
        auth.signOut()
    }

    override fun uidActual(): String? = auth.currentUser?.uid

    // CRUD DE LA CUENTA

    override suspend fun obtenerUsuario(uid: String): ResultadoOperacion<Usuario> = ejecutar {
        val documento = documentoUsuario(uid).get().await()

        val datos = documento.data
        if (!documento.exists() || datos == null) {
            return@ejecutar ResultadoOperacion.Error(
                "No encontramos los datos de tu cuenta."
            )
        }

        ResultadoOperacion.Exito(Usuario.desdeMapa(uid, datos))
    }

    override suspend fun actualizarUsuario(
        usuario: Usuario
    ): ResultadoOperacion<Usuario> = ejecutar {
        // set reemplaza el documento completo. Como el mapa lleva todos los
        // campos del perfil, no quedan datos antiguos mezclados con nuevos.
        documentoUsuario(usuario.uid).set(usuario.aMapa()).await()
        ResultadoOperacion.Exito(usuario)
    }

    override suspend fun eliminarCuenta(uid: String): ResultadoOperacion<Unit> = ejecutar {
        // Las frases primero: una vez borrada la cuenta, las reglas de
        // seguridad impiden tocarlas y quedarian ocupando espacio sin que
        // nadie pueda leerlas ni eliminarlas.
        val frases = coleccionFrases(uid).get().await()
        for (documento in frases.documents) {
            documento.reference.delete().await()
        }

        documentoUsuario(uid).delete().await()
        auth.currentUser?.delete()?.await()

        ResultadoOperacion.Exito(Unit)
    }

    // CRUD DE LAS FRASES GUARDADAS

    override suspend fun listarFrases(
        uid: String
    ): ResultadoOperacion<List<FraseGuardada>> = ejecutar {
        val consulta = coleccionFrases(uid)
            // Las ultimas primero: son las que la persona acaba de usar.
            .orderBy(FraseGuardada.CAMPO_CREADA_EN, Query.Direction.DESCENDING)
            .get()
            .await()

        val frases = consulta.documents.mapNotNull { documento ->
            documento.data?.let { FraseGuardada.desdeMapa(documento.id, it) }
        }

        ResultadoOperacion.Exito(frases)
    }

    override suspend fun agregarFrase(
        uid: String,
        texto: String
    ): ResultadoOperacion<FraseGuardada> = ejecutar {
        val frase = FraseGuardada(
            texto = texto.trim(),
            creadaEn = System.currentTimeMillis()
        )

        val documento = coleccionFrases(uid).add(frase.aMapa()).await()

        ResultadoOperacion.Exito(frase.copy(id = documento.id))
    }

    override suspend fun editarFrase(
        uid: String,
        fraseId: String,
        textoNuevo: String
    ): ResultadoOperacion<FraseGuardada> = ejecutar {
        val limpio = textoNuevo.trim()

        // update y no set: cambia el texto sin tocar la fecha de creacion, asi
        // la frase conserva su lugar en la lista.
        coleccionFrases(uid)
            .document(fraseId)
            .update(FraseGuardada.CAMPO_TEXTO, limpio)
            .await()

        val documento = coleccionFrases(uid).document(fraseId).get().await()
        val datos = documento.data
            ?: return@ejecutar ResultadoOperacion.Error("La frase ya no existe.")

        ResultadoOperacion.Exito(FraseGuardada.desdeMapa(fraseId, datos))
    }

    override suspend fun eliminarFrase(
        uid: String,
        fraseId: String
    ): ResultadoOperacion<Unit> = ejecutar {
        coleccionFrases(uid).document(fraseId).delete().await()
        ResultadoOperacion.Exito(Unit)
    }

    // AYUDANTES

    private fun documentoUsuario(uid: String) =
        firestore.collection(COLECCION_USUARIOS).document(uid)

    private fun coleccionFrases(uid: String) =
        documentoUsuario(uid).collection(COLECCION_FRASES)

    // Envuelve cada operacion para traducir las excepciones de Firebase a
    // mensajes que el usuario pueda entender.
    //
    // Sin esto, un fallo de red llegaria a la pantalla como el texto en ingles
    // de la libreria, o peor, cerraria la aplicacion. En una aplicacion de
    // accesibilidad el mensaje importa tanto como la funcion: quien la usa
    // para comunicarse necesita saber si debe reintentar o corregir algo.
    private inline fun <T> ejecutar(
        bloque: () -> ResultadoOperacion<T>
    ): ResultadoOperacion<T> = try {
        bloque()
    } catch (e: FirebaseAuthWeakPasswordException) {
        ResultadoOperacion.Error("La contraseña es demasiado débil. Usa al menos 8 caracteres.")
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        ResultadoOperacion.Error("El correo o la contraseña no son correctos.")
    } catch (e: FirebaseAuthInvalidUserException) {
        ResultadoOperacion.Error("No existe una cuenta con ese correo.")
    } catch (e: FirebaseAuthUserCollisionException) {
        ResultadoOperacion.Error("Ya existe una cuenta registrada con ese correo.")
    } catch (e: IOException) {
        // Sin conexion. Es recuperable: los mismos datos van a funcionar
        // cuando vuelva la red, asi que la pantalla puede ofrecer reintentar.
        ResultadoOperacion.Error(
            "No hay conexión a internet. Revisa tu red e intenta nuevamente.",
            recuperable = true
        )
    } catch (e: Exception) {
        ResultadoOperacion.Error(
            "No pudimos completar la operación. Intenta nuevamente.",
            recuperable = true
        )
    }

    companion object {
        const val COLECCION_USUARIOS = "usuarios"
        const val COLECCION_FRASES = "frases"
    }
}
