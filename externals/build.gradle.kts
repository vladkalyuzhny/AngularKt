plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

// Kotlin externals for third-party npm packages, generated from their TypeScript .d.ts by Karakum
// Pure `external` declarations — no runtime, no npm deps of its own. The consuming app provides
// the actual @angular/material at bundle time; here we only compile the type surface.
kotlin {
    js {
        useEsModules()
        browser()
        binaries.library()
    }
}