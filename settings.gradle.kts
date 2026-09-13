pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MotoScope"
include(
    ":app",
    ":core:model",
    ":core:telemetry",
    ":core:recording",
    ":core:ui",
    ":core:database",
    ":protocol:elm327",
    ":simulator",
    ":feature:dashboard"
)
