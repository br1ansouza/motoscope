plugins {
    id("motoscope.android.library")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.room)
}

android { namespace = "dev.br1ansouza.motoscope.core.database" }
room { schemaDirectory("$projectDir/schemas") }

dependencies {
    implementation(project(":core:recording"))
    implementation(libs.room.runtime)
    implementation(libs.coroutines.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
