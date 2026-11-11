plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // KSP genera el código de Room en tiempo de compilación.
    alias(libs.plugins.ksp)
}

android {
    namespace = "cl.dymcyber.wim100"
    compileSdk = 37

    defaultConfig {
        applicationId = "cl.dymcyber.wim100"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization { enable = false }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures { compose = true }
}

dependencies {
    // Base de Android y ciclo de vida
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Interfaz: Jetpack Compose con Material 3
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    // MVVM: el estado vive en los ViewModel y la pantalla lo observa
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navegación entre pantallas
    implementation(libs.androidx.navigation.compose)

    // Persistencia local: Room sobre SQLite
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Recurso nativo: biometría (BiometricPrompt necesita FragmentActivity)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment.ktx)

    // Pruebas
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
