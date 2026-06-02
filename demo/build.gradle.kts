import at.angular.gradle.AngularKtConfig

plugins {
    id("io.github.vladkalyuzhny.angularkt")
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

val angularConfig = AngularKtConfig.from(project)
val angularNpmVersion = angularConfig.angularNpmVersion

// Global build assets for the AOT build — the plugin adds them to the AOT workspace's angular.json.
// (The JIT build carries its own theme config in webpack.config.d/angular-jit.js.)
angularKt {
    aotConfig {
        // AOT bootstraps the standalone RootComponent via `bootstrapApplication`
        bootstrapComponent.set("app.RootComponent")
        // Angular Material's prebuilt theme — without it Material components and the app's own
        // `--mat-sys-*` styles render unthemed.
        styles.add("@angular/material/prebuilt-themes/indigo-pink.css")
        // highlight.js code theme for the "Explore" snippets — same token colors the JIT build
        // injects via webpack.config.d/angular-jit.js.
        styles.add("highlight.js/styles/github.css")
    }
}

kotlin {
    sourceSets {
        val jsMain by getting {
            // The two modes bootstrap differently, each with its own entry + root, kept in a
            // mode-specific source dir wired in only for that mode (jsMain holds the shared code)
            kotlin.srcDir(if (angularConfig.isAot) "src/jsAot/kotlin" else "src/jsJit/kotlin")
            dependencies {
                // AngularKt runtime.
                implementation(project(":lib"))
                // The @angular/* packages this app uses (version managed by the plugin).
                implementation(npm("@angular/animations", angularNpmVersion))
                implementation(npm("@angular/cdk", angularNpmVersion))
                implementation(npm("@angular/material", angularNpmVersion))
                implementation(npm("@angular/common", angularNpmVersion))
                implementation(npm("@angular/compiler", angularNpmVersion))
                implementation(npm("@angular/core", angularNpmVersion))
                implementation(npm("@angular/forms", angularNpmVersion))
                implementation(npm("@angular/platform-browser", angularNpmVersion))
                implementation(npm("@angular/platform-browser-dynamic", angularNpmVersion))
                implementation(npm("@angular/router", angularNpmVersion))
                implementation(npm("zone.js", angularConfig.zoneJsNpmVersion))
                implementation(npm("rxjs", "~7.8.0"))
                implementation(npm("tslib", "~2.7.0"))
                // Syntax highlighting for the "Explore" code snippets. The full build
                // auto-registers the kotlin/typescript grammars; theme CSS is wired per
                // mode (aotConfig above + webpack.config.d/angular-jit.js).
                implementation(npm("highlight.js", "^11.9.0"))
                // webpack loaders used by webpack.config.d/angular-jit.js to inject
                // the bundled Angular Material prebuilt theme (.css)
                implementation(devNpm("style-loader", "~4.0.0"))
                implementation(devNpm("css-loader", "~7.1.0"))
                // Generated Kotlin externals for @angular/material NgModule tokens (Karakum).
                implementation(project(":externals"))
                // Ktor as the HTTP client (alternative to Angular HttpClient).
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.js)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
