plugins {
    id("motoscope.kotlin.library")
}

dependencies {
    implementation(libs.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
