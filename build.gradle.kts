plugins {
    id("motoscope.quality")
    alias(libs.plugins.room) apply false
}

tasks.register("quality") {
    group = "verification"
    description = "Compila debug/release e executa testes, lint, detekt e ktlint."
    dependsOn("detekt", "ktlintCheck")
    listOf(":app", ":core:ui", ":core:database").forEach { module ->
        dependsOn(
            "$module:assembleDebug",
            "$module:assembleRelease",
            "$module:testDebugUnitTest",
            "$module:testReleaseUnitTest",
            "$module:lint",
            "$module:detekt",
            "$module:ktlintCheck"
        )
    }
    listOf(":core:model", ":protocol:elm327").forEach { module ->
        dependsOn(
            "$module:assemble",
            "$module:test",
            "$module:detekt",
            "$module:ktlintCheck"
        )
    }
}
