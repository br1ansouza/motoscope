plugins {
    id("motoscope.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.br1ansouza.motoscope.feature.dashboard"
    buildFeatures { compose = true }
}

dependencies {
    api(project(":core:telemetry"))
    implementation(project(":core:ui"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)
}
