# Configuracion de Firebase

El proyecto usa Firebase Authentication para el acceso y Cloud Firestore para
guardar los datos. Este directorio documenta esa configuracion; el codigo de la
aplicacion vive en `app/src/main/java`.

## Por que el repositorio no trae google-services.json

Ese archivo lleva las claves del proyecto de Firebase, asi que esta en
`.gitignore`. Sin el, la aplicacion igual compila y se ejecuta: el modulo `app`
aplica el plugin de Google Services solo cuando el archivo existe, y expone el
resultado en `BuildConfig.FIREBASE_DISPONIBLE`.

## Como dejar el proyecto operativo

1. Crear un proyecto en https://console.firebase.google.com
2. Agregar una aplicacion Android con el nombre de paquete
   `cl.mariovalle.comunicame`, que es el `applicationId` declarado en
   `app/build.gradle.kts`. Si no coincide exactamente, Firebase rechaza las
   peticiones.
3. Descargar `google-services.json` y dejarlo en `app/`.
4. En Authentication, habilitar el proveedor Correo electronico/contrasena.
5. En Firestore, crear la base de datos en modo de produccion y publicar las
   reglas de `firestore.rules`.

## Estructura de los datos

    usuarios/{uid}                       datos de la cuenta
    usuarios/{uid}/frases/{fraseId}      frases guardadas por esa persona

El identificador del documento es el `uid` que entrega Firebase Auth. Al colgar
las frases del usuario, la regla de seguridad se escribe una sola vez y vale
para toda la rama.

## Reglas de seguridad

`firestore.rules` es copia de lo publicado en la consola. La regla unica es que
cada persona accede solo a lo suyo, comparando `request.auth.uid` con el `uid`
de la ruta. Ese valor lo agrega Firebase al validar el token, no el cliente, de
modo que una aplicacion modificada no puede pedir los datos de otra cuenta.

La base se creo en modo de produccion y no en modo de prueba. El modo de prueba
abre la base a cualquiera durante treinta dias, y una aplicacion que guarda
datos de personas con discapacidad no deberia depender de que nadie encuentre
la direccion del proyecto antes de que venza el plazo.
