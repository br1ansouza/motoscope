import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("motoscope.quality")
}

android {
    namespace = "dev.br1ansouza.motoscope"
    compileSdk = 35
    defaultConfig {
        applicationId = "dev.br1ansouza.motoscope"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
    lint {
        lintConfig = rootProject.file("lint.xml")
        warningsAsErrors = true
        checkDependencies = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        allWarningsAsErrors.set(true)
    }
}

hilt { enableAggregatingTask = true }

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:telemetry"))
    implementation(project(":core:recording"))
    implementation(project(":core:settings"))
    implementation(project(":core:vehicle"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:recording"))
    implementation(project(":simulator"))
    implementation(project(":core:database"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
