package cl.mariovalle.comunicame.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta de la app. Lila y celeste apagados, pasteles solo de fondo y textos
// oscuros encima. Los contrastes estan medidos con la formula de la WCAG.

// Lila (primario). Saturacion baja a proposito: saturado vibra sobre blanco.
val LilaProfundo = Color(0xFF6E6194)   // blanco encima: 5.53:1
val LilaOscuro = Color(0xFF332B52)     // sobre lila suave: 10.80:1
val LilaSuave = Color(0xFFEBE8F3)
val LilaClaro = Color(0xFFC4BBDB)      // tema oscuro

// Celeste (secundario), tambien apagado para que no le pelee al lila
val CelesteProfundo = Color(0xFF4A7285) // blanco encima: 5.20:1
val CelesteOscuro = Color(0xFF223E4A)
val CelesteSuave = Color(0xFFE0EBF0)
val CelesteClaro = Color(0xFFA8C6D4)    // tema oscuro

// Neutros
val FondoLila = Color(0xFFF7F5FD)
val Superficie = Color(0xFFFFFFFF)
val TextoPrincipal = Color(0xFF1C1B22)  // 15.81:1
val TextoSecundario = Color(0xFF4A4458) // 9.30:1
val BordeSuave = Color(0xFF6F6B80)      // 5.13:1

// Estados. Se dejan verde/rojo/ambar aunque no calcen con la paleta: la gente
// ya los tiene aprendidos. Nunca van solos, siempre con icono y texto.
val VerdeExito = Color(0xFF0A3D28)      // 10.07:1
val VerdeExitoSuave = Color(0xFFD4EFE1)
val RojoError = Color(0xFF7A1710)       // 8.84:1
val RojoErrorSuave = Color(0xFFFBE4E2)
val AmbarAviso = Color(0xFF4A2F00)      // 10.35:1
val AmbarAvisoSuave = Color(0xFFFFE8C2)

// Tema oscuro
val FondoOscuro = Color(0xFF15141A)
val SuperficieOscura = Color(0xFF1F1D26)
val TextoClaro = Color(0xFFE6E3EC)
val LilaContenedorOscuro = Color(0xFF4A4269)
val BordeOscuro = Color(0xFF8A8698)
