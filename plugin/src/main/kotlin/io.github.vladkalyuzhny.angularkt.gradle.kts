import at.angular.gradle.AngularKtConfig
import at.angular.gradle.AngularKtExtension
import at.angular.gradle.configureAotWorkspace
import at.angular.gradle.configureKotlinJsAndKsp
import at.angular.gradle.configureNodeAndYarn

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.google.devtools.ksp")
}

// The consuming build resolves its own AngularKtConfig.from(project) for the knobs it needs
// (npm coordinate version, jit/aot flag) — no extra-properties bridge.
val config = AngularKtConfig.from(project)

// The `angularKt { }` DSL — the AOT bootstrap root plus global styles/scripts for the AOT build
// (added to angular.json). JIT carries its own webpack.config.d config and names its root in the
// hand-written entry
val angularKt = extensions.create("angularKt", AngularKtExtension::class.java)

configureNodeAndYarn()
configureKotlinJsAndKsp(config, angularKt)
if (config.isAot) {
    configureAotWorkspace(config, angularKt)
}