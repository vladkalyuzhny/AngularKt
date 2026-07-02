import at.angular.gradle.AngularKtConfig

plugins {
    id("io.github.vladkalyuzhny.angularkt")
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

val angularConfig = AngularKtConfig.from(project)
val angularNpmVersion = angularConfig.angularNpmVersion

// Global build assets for the AOT build — the plugin adds them to the AOT workspace's angular.json.
// (The JIT build carries its own theme config in webpack.config.d/app-jit.js.)
angularKt {
    aotConfig {
        // Names the standalone root @Component — this is what DETERMINES the generated AOT `main.ts`:
        //   set   → standalone `bootstrapApplication(RootComponent, [importProvidersFrom(routing)])`
        //   unset → classic module `bootstrapModule(<the @NgModule with bootstrap=[…]>)`
        // AOT-only: the AOT entry is generated, so the root's identity must live in this DSL knob where
        // KSP can read it.
        // The JIT entry (`main` in src/jsMain/kotlin/main.kt) is hand-written, so it needs no
        // equivalent — you pick the bootstrap style directly in that file.
        bootstrapComponent.set("app.RootComponent")
        // Angular Material's prebuilt theme — without it Material components and the app's own
        // `--mat-sys-*` styles render unthemed.
        styles.add("@angular/material/prebuilt-themes/indigo-pink.css")
        // highlight.js code theme for the "Explore" snippets — same token colors the JIT build
        // injects via webpack.config.d/app-jit.js.
        styles.add("highlight.js/styles/github.css")
    }
}

kotlin {
    sourceSets {
        val jsMain by getting {
            // Both bootstrap entries (JIT `main`, AOT `mainAot`) live in the shared source set — see
            // src/jsMain/kotlin/main.kt. The active build picks one: JIT (executable) auto-runs `main`;
            // AOT (library) has its generated `main.ts` call `mainAot`.
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
                // mode (aotConfig above + webpack.config.d/app-jit.js).
                implementation(npm("highlight.js", "^11.9.0"))
                // webpack loaders used by webpack.config.d/app-jit.js to inject
                // the bundled Angular Material prebuilt theme (.css)
                implementation(devNpm("style-loader", "~4.0.0"))
                implementation(devNpm("css-loader", "~7.1.0"))
                // Generated Kotlin externals for @angular/material NgModule tokens (Karakum).
                implementation(project(":externals"))
                // Ktor as the HTTP client (alternative to Angular HttpClient).
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.js)
                // Koin as a pure-Kotlin DI container (alternative to Angular's injector).
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
