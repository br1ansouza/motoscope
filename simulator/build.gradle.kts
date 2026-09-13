plugins {
    id("motoscope.kotlin.library")
}

dependencies {
    api(project(":core:telemetry"))
    implementation(libs.coroutines.core)
}
