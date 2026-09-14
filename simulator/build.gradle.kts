plugins {
    id("motoscope.kotlin.library")
}

dependencies {
    api(project(":core:telemetry"))
    api(project(":protocol:elm327"))
    implementation(libs.coroutines.core)
}
