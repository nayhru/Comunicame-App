# Empaquetado, firma y publicación del APK

Documenta cómo se genera el APK de Comunicame, cómo se firma y qué pasos
seguirían para publicarlo en una plataforma de descarga.

## Por qué hay que firmar

Android no instala una aplicación sin firma. La firma es un certificado que
acredita quién construyó el APK, y el sistema la usa para una regla simple:
una actualización solo se instala encima de una versión anterior si viene
firmada con la misma clave.

Eso es lo que impide que alguien publique una versión modificada de Comunicame
que reemplace a la verdadera en el teléfono de quien la tiene instalada. Al
probar este proyecto apareció el mecanismo funcionando: el APK de release se
negó a instalarse sobre el de depuración con el mensaje
`INSTALL_FAILED_UPDATE_INCOMPATIBLE: signatures do not match`, porque el
primero está firmado con la clave de distribución y el segundo con la clave
automática de desarrollo.

## El almacén de claves

Se generó con `keytool`, la herramienta que viene con el JDK de Android Studio:

    keytool -genkeypair \
      -keystore comunicame-release.jks \
      -alias comunicame \
      -keyalg RSA -keysize 2048 \
      -validity 10950 \
      -dname "CN=Maria Ovalle Suazo, OU=DSY2204, O=Duoc UC, L=Santiago, C=CL"

| Parámetro | Valor | Por qué |
|---|---|---|
| Algoritmo | RSA 2048 bits | Es el mínimo que acepta Google Play |
| Vigencia | 10.950 días (30 años) | Play exige que el certificado siga vigente después de 2033 |
| Alias | `comunicame` | Nombre de la clave dentro del almacén |

El mismo almacén puede crearse desde Android Studio en
**Build > Generate Signed Bundle / APK**, que es el camino que muestra la guía
de la semana 8. Se usó `keytool` porque permite dejar el comando escrito y
repetible en esta documentación.

### Qué pasa si se pierde

No se puede recuperar. Ni Google ni nadie puede regenerarlo, porque la clave
privada solo existe en ese archivo. Perderlo significa no poder volver a
publicar actualizaciones de esta aplicación: habría que cambiar el
`applicationId` y los usuarios tendrían que instalarla de cero, perdiendo sus
datos locales.

Por eso el almacén y su contraseña se guardan fuera del proyecto y fuera del
repositorio.

## Cómo se guardan las credenciales

Las credenciales viven en `keystore.properties`, en la raíz del proyecto:

    storeFile=comunicame-release.jks
    storePassword=...
    keyAlias=comunicame
    keyPassword=...

Ese archivo y el propio `.jks` están en `.gitignore`. No viajan en el
repositorio ni en el ZIP de entrega, porque quien los tenga puede firmar
cualquier aplicación como si fuera esta.

`app/build.gradle.kts` los lee solo si el archivo existe:

    val archivoCredenciales = rootProject.file("keystore.properties")
    val hayFirma = archivoCredenciales.exists()

Si no está, el proyecto compila igual y el APK de release sale sin firmar. Esa
decisión permite que el código se pueda clonar y construir sin las
credenciales, que es lo que ocurre cuando alguien descarga el ZIP de la
entrega.

## Generar el APK firmado

    cd Comunicame
    ./gradlew assembleRelease

El resultado queda en `app/build/outputs/apk/release/app-release.apk`.

Desde Android Studio equivale a **Build > Generate Signed Bundle / APK >
APK > release**.

### Resultado obtenido

| | Depuración | Distribución |
|---|---|---|
| Tamaño | 21,8 MB | 15,1 MB |
| Firma | clave automática de desarrollo | certificado propio |
| `isMinifyEnabled` | — | `false` |

El APK de release pesa menos porque no incluye las herramientas de depuración
ni los recursos de las vistas previas de Compose.

Se dejó `isMinifyEnabled = false` a propósito: minificar renombra clases y
métodos, y el código de esta entrega se revisa como parte de la evaluación.

## Verificar la firma

Con `apksigner`, que viene en las build-tools del SDK:

    apksigner verify --verbose --print-certs app-release.apk

Salida para este APK:

    Verifies
    Verified using v2 scheme (APK Signature Scheme v2): true
    Number of signers: 1
    Signer #1 certificate DN: CN=Maria Ovalle Suazo, OU=DSY2204, O=Duoc UC, L=Santiago, C=CL
    Signer #1 key algorithm: RSA
    Signer #1 key size (bits): 2048
    Signer #1 certificate SHA-256 digest:
        bffdd5622f96a5bf691257a36ee97eb39842660c5e0fa1e5111da01d74270664

El esquema v1 aparece como `false` y está bien: v1 es el formato antiguo, que
solo hace falta para Android 6 o anterior. Esta aplicación declara
`minSdk = 24`, es decir Android 7, donde todos los equipos entienden v2.
Android Gradle Plugin lo omite por innecesario.

## Versionado

`app/build.gradle.kts` declara:

    versionCode = 1
    versionName = "1.0"

`versionCode` es un entero que solo usa el sistema: una actualización debe
traerlo más alto que la versión instalada o Android la rechaza. `versionName`
es el texto que ve la persona.

Para la próxima versión habría que subir ambos, por ejemplo a `2` y `"1.1"`.

## Publicación

El alcance de la asignatura es describir el proceso, no comprar una cuenta de
desarrollador, según lo indicado por el docente en el foro de la semana 8.

### Google Play

1. Crear una cuenta en Play Console, que tiene un costo único de 25 dólares.
2. Crear la ficha de la aplicación: nombre, descripción, categoría, capturas,
   ícono y clasificación de contenido.
3. Completar el cuestionario de privacidad, declarando qué datos recoge la
   aplicación. En el caso de Comunicame: correo electrónico, nombre, comuna y
   las frases que la persona guarda.
4. Subir el APK firmado, o preferentemente un Android App Bundle (`.aab`), que
   es el formato que Play exige para aplicaciones nuevas desde agosto de 2021 y
   que se genera con `./gradlew bundleRelease`.
5. Enviar a revisión y esperar la aprobación.

El límite de tamaño de Play es 200 MB para el bundle, muy por encima de los
15 MB de esta aplicación.

### Alternativas sin costo

Para distribuir la aplicación entre un grupo conocido o para probar antes de
una publicación definitiva:

- **Firebase App Distribution**: reparte el APK a una lista de personas
  invitadas por correo. Como el proyecto ya usa Firebase, no requiere crear
  nada nuevo.
- **Applivery** u otros administradores de dispositivos, que permiten instalar
  el APK de forma remota.
- **Upload APK** y servicios similares, que generan un enlace de descarga
  directa.

En todos estos casos quien instale la aplicación debe autorizar la instalación
desde orígenes desconocidos, porque el APK no viene de la tienda del sistema.

## Requisito de conexión

La aplicación necesita conexión a internet para iniciar sesión, registrarse y
sincronizar las frases, ya que los datos viven en Cloud Firestore.

La sesión en cambio se guarda en el dispositivo con SharedPreferences, de modo
que al abrir la aplicación no se vuelve a pedir la contraseña aunque no haya
red en ese momento.
