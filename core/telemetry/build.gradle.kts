plugins {
    id("motoscope.kotlin.library")
}

dependencies {
    api(project(":core:model"))
    implementation(libs.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
