import com.vanniktech.maven.publish.SonatypeHost
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    signing
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    js {
        useEsModules()
        browser()
        binaries.library()
    }
    // Carries no sources of its own (jvmMain is empty); compiles commonMain only,
    // i.e. just the annotations. Exists so :processor (a plain JVM module) can
    // depend on them.
    jvm()
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    coordinates(artifactId = "angularkt")
    pom {
        name = "AngularKt"
        description = "Write Angular applications in Kotlin - runtime bindings and annotations."
        url = "https://github.com/vladkalyuzhny/AngularKt"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "vladkalyuzhny"
                name = "Vlad Kalyuzhnyu"
                url = "https://github.com/vladkalyuzhny"
            }
        }
        scm {
            url = "https://github.com/vladkalyuzhny/AngularKt"
            connection = "scm:git:git://github.com/vladkalyuzhny/AngularKt.git"
            developerConnection = "scm:git:ssh://git@github.com/vladkalyuzhny/AngularKt.git"
        }
    }
}

run {
    val secrets = Properties()
    rootProject.file("secrets.properties")
        .takeIf { it.exists() }
        ?.inputStream()
        ?.use { secrets.load(it) }
    val signingKey = secrets.getProperty("signingInMemoryKey")
        ?: providers.gradleProperty("signingInMemoryKey").orNull
    val signingPass = secrets.getProperty("signingInMemoryKeyPassword")
        ?: providers.gradleProperty("signingInMemoryKeyPassword").orNull
    if (!signingKey.isNullOrBlank()) {
        configure<SigningExtension> { useInMemoryPgpKeys(signingKey, signingPass) }
        afterEvaluate {
            configure<SigningExtension> { sign(extensions.getByType<PublishingExtension>().publications) }
        }
    }
}
