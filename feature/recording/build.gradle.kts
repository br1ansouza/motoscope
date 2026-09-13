plugins {
    id("motoscope.android.library")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android { namespace = "dev.br1ansouza.motoscope.feature.recording" }

dependencies {
    api(project(":core:recording"))
    implementation(libs.coroutines.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
