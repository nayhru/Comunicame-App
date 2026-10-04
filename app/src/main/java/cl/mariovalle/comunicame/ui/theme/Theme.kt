package cl.mariovalle.comunicame.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EsquemaClaro = lightColorScheme(
    primary = LilaProfundo,
    onPrimary = Superficie,
    primaryContainer = LilaSuave,
    onPrimaryContainer = LilaOscuro,

    secondary = CelesteProfundo,
    onSecondary = Superficie,
    secondaryContainer = CelesteSuave,
    onSecondaryContainer = CelesteOscuro,

    // Fondo blanco: la app es minimalista y el lila queda solo de acento
    background = Superficie,
    onBackground = TextoPrincipal,
    surface = Superficie,
    onSurface = TextoPrincipal,
    surfaceVariant = LilaSuave,
    onSurfaceVariant = TextoSecundario,
    outline = BordeSuave,

    error = RojoError,
    onError = Superficie,
    errorContainer = RojoErrorSuave,
    onErrorContainer = RojoError
)

private val EsquemaOscuro = darkColorScheme(
    primary = LilaClaro,
    onPrimary = LilaOscuro,
    primaryContainer = LilaContenedorOscuro,
    onPrimaryContainer = LilaSuave,

    secondary = CelesteClaro,
    onSecondary = CelesteOscuro,
    secondaryContainer = CelesteProfundo,
    onSecondaryContainer = CelesteSuave,

    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro,
    surfaceVariant = SuperficieOscura,
    onSurfaceVariant = TextoClaro,
    outline = BordeOscuro,

    error = RojoErrorSuave,
    onError = RojoError,
    errorContainer = RojoError,
    onErrorContainer = RojoErrorSuave
)

// No uso dynamic color a proposito. Si Android saca los colores del fondo de
// pantalla del usuario el contraste se vuelve impredecible, y aca el contraste
// es requisito.
@Composable
fun ComunicameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            // Solo ajusto los iconos de la barra de estado.
            // statusBarColor quedo obsoleto en Android 15.
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Tipografia,
        content = content
    )
}
