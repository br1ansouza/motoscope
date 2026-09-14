plugins {
    id("motoscope.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.br1ansouza.motoscope.feature.history"
    buildFeatures { compose = true }
}

dependencies {
    api(project(":core:history"))
    implementation(project(":core:ui"))
    implementation(libs.coroutines.core)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)
}
