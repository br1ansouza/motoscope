import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

detekt {
    toolVersion = catalog.findVersion("detekt").get().requiredVersion
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    source.setFrom(
        fileTree(projectDir) {
            include("**/*.kt", "**/*.kts")
            exclude("**/build/**", "**/.gradle/**", "**/.git/**")
            if (project == rootProject) {
                exclude("app/**", "core/**")
            }
        }
    )
}

ktlint {
    version.set(catalog.findVersion("ktlint").get().requiredVersion)
    filter {
        exclude { it.file.path.contains("/build/") }
        exclude { it.file.path.contains("/.gradle/") }
    }
}

if (project == rootProject) {
    ktlint {
        kotlinScriptAdditionalPaths {
            include(
                fileTree("buildSrc") {
                    include("**/*.kt", "**/*.kts")
                    exclude("**/build/**", "**/.gradle/**")
                }
            )
        }
    }
}
