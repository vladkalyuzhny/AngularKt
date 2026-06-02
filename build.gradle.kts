plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
}

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

tasks.register("publishToMavenLocal") {
    group = "publishing"
    description = "Publishes :lib, :processor and the Gradle plugin (sources included) to ~/.m2."
    dependsOn(
        ":lib:publishToMavenLocal",
        ":processor:publishToMavenLocal",
        gradle.includedBuild("plugin").task(":publishToMavenLocal"),
    )
}
