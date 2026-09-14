import com.android.build.api.variant.HasUnitTestBuilder
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("motoscope.quality")
}

val keystoreProperties = rootProject.file("keystore.properties").takeIf { it.isFile }?.let {
    Properties().apply { it.inputStream().use(::load) }
}

android {
    namespace = "dev.br1ansouza.motoscope"
    compileSdk = 37
    defaultConfig {
        applicationId = "dev.br1ansouza.motoscope"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "0.2.0"
    }
    signingConfigs {
        keystoreProperties?.let { properties ->
            create("release") {
                storeFile = rootProject.file(properties.getProperty("storeFile"))
                storePassword = properties.getProperty("storePassword")
                keyAlias = properties.getProperty("keyAlias")
                keyPassword = properties.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.findByName("release")
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

androidComponents {
    beforeVariants(selector().withBuildType("release")) { variant ->
        (variant as? HasUnitTestBuilder)?.enableUnitTest = true
    }
}

hilt { enableAggregatingTask = true }

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:telemetry"))
    implementation(project(":core:recording"))
    implementation(project(":core:settings"))
    implementation(project(":core:vehicle"))
    implementation(project(":core:history"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:diagnostics"))
    implementation(project(":feature:history"))
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
