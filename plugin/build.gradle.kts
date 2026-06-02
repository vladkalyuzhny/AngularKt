import java.util.Properties

plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.pluginPublish)
}

group = "io.github.vladkalyuzhny"
// Single source of truth: the main build's gradle.properties (this is a separate included build).
version = Properties().apply { file("../gradle.properties")
    .inputStream()
    .use { load(it) } }
    .getProperty("version")

// Emit the resolved project version as a Kotlin constant so the plugin code never hardcodes it.
val generateBuildInfo by tasks.registering {
    val version = project.version.toString()
    val outputDir = layout.buildDirectory.dir("generated/buildInfo/kotlin")
    inputs.property("version", version)
    outputs.dir(outputDir)
    doLast {
        outputDir.get().asFile.resolve("at/angular/gradle").apply {
            mkdirs()
            resolve("BuildInfo.kt").writeText(
                """
                package at.angular.gradle

                /** Generated from the Gradle project version — do not edit. */
                internal object BuildInfo {
                    const val VERSION: String = "$version"
                }
                """.trimIndent() + "\n",
            )
        }
    }
}

kotlin.sourceSets.named("main") {
    kotlin.srcDir(generateBuildInfo)
}

dependencies {
    implementation(libs.kotlin.multiplatform.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.kotlin.serialization.gradle.plugin)
    implementation(libs.kotlinx.serialization.json)
}

// kotlin-dsl compiles this plugin with Gradle's *embedded* Kotlin (older than the KGP/KSP we depend
// on), so reading their 2.3.x metadata trips the binary-version gate. We only call stable KGP/KSP
// configuration APIs, so skipping the metadata-version check is safe here.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}

// Published to the Gradle Plugin Portal
gradlePlugin {
    website = "https://github.com/vladkalyuzhny/AngularKt"
    vcsUrl = "https://github.com/vladkalyuzhny/AngularKt.git"
    plugins.configureEach {
        displayName = "AngularKt"
        description = "Write Angular apps in Kotlin: Kotlin/JS + KSP codegen, JIT/AOT builds and so on"
        tags = listOf("angular", "kotlin", "kotlin-js", "ksp", "frontend")
    }
}
