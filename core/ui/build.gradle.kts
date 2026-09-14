plugins {
    id("motoscope.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.br1ansouza.motoscope.core.ui"
    buildFeatures { compose = true }
}

dependencies {
    api(project(":core:model"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
}
