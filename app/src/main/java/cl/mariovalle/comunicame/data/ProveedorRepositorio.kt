package cl.mariovalle.comunicame.data

import cl.mariovalle.comunicame.BuildConfig

// Entrega el repositorio que corresponde segun como se compilo el proyecto.
//
// Si google-services.json esta presente, la aplicacion habla con Firebase. Si
// no lo esta, usa una implementacion en memoria que permite abrir la
// aplicacion, recorrer las pantallas y ejecutar las pruebas sin credenciales.
//
// Esto existe porque el repositorio no versiona google-services.json: lleva
// las claves del proyecto. Sin este respaldo, quien clonara el codigo se
// encontraria con una aplicacion que compila pero se cae al primer toque.
//
// Se guarda una sola instancia: el repositorio en memoria pierde sus datos si
// se crea de nuevo, y Firebase no gana nada con instancias repetidas.
private val instancia: RepositorioComunicame by lazy {
    if (BuildConfig.FIREBASE_DISPONIBLE) {
        RepositorioFirebase()
    } else {
        RepositorioEnMemoria()
    }
}

fun proveedorDeRepositorio(): RepositorioComunicame = instancia
