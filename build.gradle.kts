// Archivo de build a nivel de proyecto. Las opciones comunes a todos los
// modulos se declaran aqui; la configuracion propia de la app va en app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
