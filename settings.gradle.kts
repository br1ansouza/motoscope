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
    ":core:history",
    ":core:recording",
    ":core:settings",
    ":core:vehicle",
    ":core:ui",
    ":core:database",
    ":protocol:elm327",
    ":simulator",
    ":feature:dashboard",
    ":feature:diagnostics",
    ":feature:history",
    ":feature:recording"
)
