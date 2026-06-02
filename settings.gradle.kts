pluginManagement {
    // Develop the AngularKt plugin alongside the build that consumes it: makes
    // `plugins { id("io.github.vladkalyuzhny.angularkt") }` resolve to the local plugin build.
    includeBuild("plugin")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "AngularKt"

include(":demo", ":lib", ":processor", ":externals")
