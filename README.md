# Comunícame

Aplicación móvil Android de accesibilidad para personas con **discapacidad sensorial auditiva**.
El usuario escribe lo que quiere decir y el teléfono lo reproduce en voz alta, permitiéndole
comunicarse con una persona oyente sin depender de que esta sepa lengua de señas.

Proyecto de la asignatura **Desarrollo de Aplicaciones Móviles (DSY2204)** — Evaluación
Final Transversal.

**Autora:** María Ovalle Suazo
**Repositorio:** https://github.com/nayhru/Comunicame-App

## Stack

| | |
|---|---|
| Lenguaje | Kotlin 2.1.10 |
| UI | Jetpack Compose + Material Design 3 |
| Navegación | Navigation Compose 2.8.5 |
| Backend | Firebase Authentication + Cloud Firestore |
| Sesión local | SharedPreferences |
| Pruebas | JUnit, Robolectric, Mockito |
| AGP / Gradle | 8.9.1 / 8.11.1 |
| compileSdk / targetSdk | 36 |
| minSdk | 24 (Android 7.0) |

## Pantallas

**Acceso**

| View | Qué hace |
|---|---|
| Login | Destino de inicio. Autentica contra Firebase con correo y contraseña. |
| Registro | Crea la cuenta en el proveedor de identidad y guarda sus datos. |
| Recuperar contraseña | Solicita el envío de un enlace para definir una contraseña nueva. |

**Panel del usuario autenticado** (barra de navegación inferior)

| Sección | Qué hace |
|---|---|
| Comunicar | Texto a voz y voz a texto, frases sugeridas y frases propias con crear, editar y eliminar. |
| Emergencia | Tarjeta para mostrar a un tercero, contacto de emergencia y servicios. |
| Mi perfil | Datos de la cuenta, edición, contacto de emergencia y baja de la cuenta. |

## Decisiones de accesibilidad

El usuario objetivo no percibe el canal auditivo, así que la app sigue estas reglas:

- **Ningún estado se comunica solo con sonido.** Todo evento se informa con un banner visual
  (icono + color + texto) y con vibración.
- **Ningún estado se comunica solo con color**, para no dejar fuera a quien es daltónico.
- **La vibración se declara como accesibilidad** (`USAGE_ASSISTANCE_ACCESSIBILITY`), no como
  notificación. Si no, el modo silencio del teléfono la suprime y el usuario se queda sin
  ningún aviso.
- **El permiso de micrófono se pide al usar voz a texto**, no al abrir la aplicación, para que
  el sistema lo muestre cuando ya se entiende para qué sirve.
- **La vibración de Emergencia ignora el ajuste del perfil.** Quien la desactivó lo hizo
  pensando en los avisos cotidianos, no en el momento en que está pidiendo ayuda.
- **Sin _dynamic color_**: la paleta es fija y sus pares texto/fondo superan la razón de
  contraste 4.5:1 de la WCAG.
- **Tipografía más grande** que la escala por defecto de Material 3.
- `contentDescription` en los iconos, `liveRegion` en los mensajes y `selectableGroup()` en
  los grupos de radio buttons, para que TalkBack los lea bien.
- En **Emergencia**, la vía principal no es llamar —una persona sorda no sostiene una llamada
  telefónica— sino una tarjeta de alta legibilidad que se muestra a quien esté al lado, y el
  contacto al que esa persona puede avisar.

## Estructura

```
app/src/main/java/cl/mariovalle/comunicame/
├── MainActivity.kt                  Activity única
├── data/
│   ├── Usuario.kt                   Modelo de cuenta y su conversión a documento
│   ├── FraseGuardada.kt             Frase propia de cada persona
│   ├── ContactoEmergencia.kt        A quién avisar en una urgencia
│   ├── RepositorioComunicame.kt     Interfaz de acceso a datos
│   ├── RepositorioFirebase.kt       Implementación contra Auth y Firestore
│   ├── RepositorioEnMemoria.kt      Respaldo sin red, también usado en pruebas
│   ├── ProveedorRepositorio.kt      Elige una u otra según la configuración
│   ├── SesionLocal.kt               Sesión en SharedPreferences
│   ├── PreferenciasAccesibilidad.kt Ajustes consultados en cada aviso
│   ├── ValidadorFormularios.kt      Reglas de validación y comunas por región
│   ├── RepositorioFrases.kt         Catálogo de frases sugeridas
│   └── Emergencia.kt                Servicios de emergencia
├── navigation/
│   ├── Rutas.kt                     Rutas centralizadas
│   └── NavegacionApp.kt             NavHost principal
├── ui/
│   ├── components/                  Banner de estado, vibración, selectores
│   ├── screens/                     Las siete pantallas
│   ├── viewmodel/                   Sesión, frases y perfil
│   └── theme/                       Paleta, tipografía y tema
└── util/                            Extensiones, clase sellada y colecciones
```

## Estructura de los datos

    usuarios/{uid}                       datos de la cuenta y contacto de emergencia
    usuarios/{uid}/frases/{fraseId}      frases guardadas por esa persona

El identificador del documento es el `uid` que entrega Firebase Auth. Las frases cuelgan del
usuario para que la regla de seguridad se escriba una vez y valga para toda la rama.

## Cómo ejecutar

1. Abrir la carpeta del proyecto en Android Studio.
2. Esperar la sincronización de Gradle.
3. Ejecutar sobre un emulador o dispositivo con Android 7.0 o superior.

El proyecto **compila sin las credenciales de Firebase**. Si falta `google-services.json`, la
aplicación usa un repositorio en memoria que permite recorrer las pantallas y ejecutar las
pruebas, pero no persiste nada. Para conectarlo de verdad, ver [firebase/README.md](firebase/README.md).

> La vibración y el motor de voz dependen del dispositivo, así que conviene probarlos en un
> equipo físico. En teléfonos Xiaomi/Redmi hay que activar **Instalar vía USB** en las opciones
> de desarrollador para poder instalar el APK.

## Pruebas

    ./gradlew testDebugUnitTest

Son 136 pruebas en nueve archivos y no requieren emulador ni dispositivo conectado. Cubren las
validaciones, las utilidades de colección, la conversión a documentos de Firestore, el CRUD
completo sobre el repositorio, las decisiones del ViewModel y la interpretación de lo que
devuelve el reconocedor de voz.

## Distribución

    ./gradlew assembleRelease

El procedimiento de firma y los pasos de publicación están en
[FIRMA_Y_PUBLICACION.md](FIRMA_Y_PUBLICACION.md).

## Alcance de esta entrega

La aplicación integra front end y back end. Los datos persisten en Cloud Firestore, el acceso
se gestiona con Firebase Authentication y la sesión se mantiene con SharedPreferences. Las
operaciones de registrar, consultar, modificar y eliminar funcionan sobre las cuentas y sobre
las frases guardadas.

La conversación funciona en los dos sentidos: texto a voz para que la persona se exprese, y voz
a texto para que lo que le digan aparezca escrito. El reconocimiento usa el del sistema, con
permiso de micrófono solicitado en el momento de usarlo y un tutorial accesible desde la misma
pantalla.

Quedan **en proceso, documentadas para futuras entregas**: el historial de conversaciones, la
ficha médica y los ajustes de tamaño de texto y contraste. Están declaradas dentro de la propia
app para que el alcance real quede visible para quien la usa.

## Credenciales

`google-services.json`, el almacén de claves de firma y `keystore.properties` no se versionan:
permiten operar sobre el proyecto de Firebase y firmar actualizaciones como si fueran
auténticas. Quien clone el repositorio usa los suyos.
