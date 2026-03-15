// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
}

tasks.register("generateTestData") {
    description = "Generates a test ZIP file with fake DiabData data"
    group = "diabdata"
    doLast {
        providers.exec {
            workingDir = rootDir
            commandLine("bash", "-c", "kotlin scripts/generateTestData.kts")
        }
    }
}