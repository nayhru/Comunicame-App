plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// El plugin de Google Services aborta la compilacion si no encuentra
// google-services.json, y ese archivo no se versiona porque lleva las claves
// del proyecto de Firebase. Aplicandolo solo cuando el archivo esta presente,
// el repositorio se puede clonar y compilar sin credenciales: la aplicacion
// arranca, las pantallas se ven y las pruebas unitarias corren igual.
val hayConfiguracionFirebase = file("google-services.json").exists()
if (hayConfiguracionFirebase) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "cl.mariovalle.comunicame"
    compileSdk = 36

    defaultConfig {
        applicationId = "cl.mariovalle.comunicame"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Se consulta desde el codigo para decidir si usar Firebase o el
        // repositorio en memoria de respaldo.
        buildConfigField("boolean", "FIREBASE_DISPONIBLE", hayConfiguracionFirebase.toString())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            // Robolectric levanta un Android simulado dentro de la JVM: sin esto
            // no encuentra los recursos y las pruebas de SharedPreferences fallan.
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // BOM: alinea automaticamente las versiones de todas las librerias Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Navigation Compose: NavController + NavHost
    implementation(libs.androidx.navigation.compose)

    // ViewModel dentro de Compose: sobrevive a los giros de pantalla
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Firebase. El BOM fija las versiones de auth y firestore de una vez.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    // Convierte las Task de Google Play Services en corrutinas (.await())
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
