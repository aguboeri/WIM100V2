// Plugins comunes a todos los módulos. Se declaran aquí y se aplican en app/.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
