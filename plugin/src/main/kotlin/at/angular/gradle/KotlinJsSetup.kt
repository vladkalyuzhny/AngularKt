package at.angular.gradle

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Wire the Kotlin/JS target and the KSP processor — the parts common to both modes.
 *
 * The JS target differs by mode: `aot` emits an importable library plus TypeScript defs (for the
 * generated `.ts` bridges), `jit` emits an executable webpack bundle. The KSP processor is the
 * framework's codegen and is always wired; mode + target version reach it in BOTH modes because
 * the capability profile (standalone gating etc.) is version-conditional regardless of jit/aot.
 */
internal fun Project.configureKotlinJsAndKsp(config: AngularKtConfig, extension: AngularKtExtension) {
    extensions.configure<KotlinMultiplatformExtension> {
        js {
            useEsModules()
            browser {
                // Dev-server port override (`-PangularKt.port`). Only the JIT executable serves via
                // webpack-dev-server; AOT serves through `ng serve`, whose port is wired in aotServe.
                if (!config.isAot) {
                    config.port?.let { port ->
                        commonWebpackConfig {
                            devServer = devServer?.copy(port = port)
                        }
                    }
                }
            }
            if (config.isAot) {
                generateTypeScriptDefinitions()
                binaries.library()
                // Kotlin/JS auto-invokes a zero-arg `fun main()` when the module loads — even for a
                // library. In AOT the generated main.ts imports the library to call the exported AOT
                // entry (`mainAot`), which would also fire the shared JIT `fun main()` on import. Suppress
                // that call so the JIT entry stays dead code (esbuild then tree-shakes it and its
                // `@angular/platform-browser-dynamic` dynamic import out of the AOT bundle).
                compilerOptions {
                    freeCompilerArgs.addAll("-main", "noCall")
                }
            } else {
                binaries.executable()
            }
        }
    }

    dependencies.add("kspJs", config.processorDep)

    extensions.configure<KspExtension> {
        arg("angularKt.mode", config.mode)
        arg("angularKt.angularVersion", config.angularVersion)
        // Tell the processor to emit .ts bridges that import the Kotlin library by its npm module name
        if (config.isAot) {
            arg("angularKt.tsModule", config.tsModule)
        }
    }

    // Third-party externals (Material, consumer Karakum modules): the processor can't read their
    // @file:JsModule off a klib, so resolve FQN → npm module here and forward it. Deferred to
    // afterEvaluate because the scan reads the consumer's project dependencies, which aren't
    // declared yet when this plugin applies.
    if (config.isAot) {
        afterEvaluate {
            val externalModules = collectNgExternalModules()
            extensions.configure<KspExtension> {
                if (externalModules.isNotEmpty()) {
                    arg(
                        "angularKt.externalModules",
                        externalModules.entries.joinToString(";") { (fqn, module) -> "$fqn=$module" },
                    )
                }
                // The standalone root for the generated main.ts (bootstrapApplication). Deferred to
                // afterEvaluate because the consumer sets it in the `angularKt { }` DSL block.
                extension.aotConfig.bootstrapComponent.orNull?.takeIf { it.isNotBlank() }?.let {
                    arg("angularKt.bootstrapComponent", it)
                }
            }
        }
    }
}
