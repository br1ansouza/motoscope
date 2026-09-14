import com.android.build.api.variant.HasUnitTestBuilder
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("motoscope.quality")
}

android {
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        lintConfig = rootProject.file("lint.xml")
        warningsAsErrors = true
        checkDependencies = true
    }
    testOptions.unitTests.isIncludeAndroidResources = true
}

androidComponents {
    beforeVariants(selector().withBuildType("release")) { variant ->
        (variant as? HasUnitTestBuilder)?.enableUnitTest = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        allWarningsAsErrors.set(true)
    }
}
