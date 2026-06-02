import com.vanniktech.maven.publish.SonatypeHost
import java.util.Properties

plugins {
    kotlin("jvm")
    signing
    alias(libs.plugins.vanniktech.mavenPublish)
}

dependencies {
    implementation(libs.ksp.symbol.processing.api)
    // The annotations the processor matches on — resolves to :lib's jvm() variant
    // (commonMain only = just the annotations). Lets the readers match on the real
    // classes (Component::class.qualifiedName) instead of hardcoded FQN strings.
    implementation(project(":lib"))
    // Templating for the generated code. Handlebars carries the conditions and
    // escaping in the template itself (via helpers + if/each), so the codegen
    // file shape lives in resources, not in Kotlin string-building. See TemplateRenderer.
    implementation(libs.handlebars)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    coordinates(artifactId = "angularkt-processor")
    pom {
        name = "AngularKt KSP Processor"
        description = "KSP processor that generates Angular code wiring from AngularKt annotations."
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
                url = "https://vladkalyuzhnyu.com"
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
