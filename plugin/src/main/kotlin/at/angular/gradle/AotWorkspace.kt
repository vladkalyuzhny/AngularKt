package at.angular.gradle

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmDependency
import java.io.File

/**
 * Register the AOT pipeline tasks (aot mode only). The AOT Angular workspace is generated into
 * `build/ng-aot` — nothing is checked in. The pipeline is two halves: (1) Gradle scaffolds the
 * workspace, builds the Kotlin/JS library and copies the generated `.ts` bridges + static
 * resources in, (2) the Angular workspace npm-installs and AOT-compiles (`aotBuild`/`aotServe`)
 */
internal fun Project.configureAotWorkspace(config: AngularKtConfig, ext: AngularKtExtension) {
    val ngAot = layout.buildDirectory.dir("ng-aot").get().asFile

    // KSP and the resource-processing task own these dirs; derive their locations from the tasks'
    // real outputs instead of hardcoding the `js/jsMain` KGP layout — a renamed JS target (or a
    // changed KGP layout) would otherwise silently make the copies read the wrong/empty directory.
    // Same approach as `libRelativePathProvider` below. Read lazily through a Provider: `kspKotlinJs`
    // is registered reactively and may not exist yet when this configures (cf. the `tasks.matching`
    // below). KspAATask exposes classes/java/kotlin/resources outputs — the bridges land in
    // `resources` (KSP `createNewFileByPath`).
    val bridgesResourcesDir = layout.dir(provider {
        tasks.named("kspKotlinJs").get().outputs.files.files.firstOrNull { it.name == "resources" }
            ?: error("AngularKt: kspKotlinJs declares no 'resources' output directory.")
    })
    val processedDir = layout.dir(provider {
        tasks.named("jsProcessResources").get().outputs.files.singleFile
    })

    // npx/npm on Windows are `npm.cmd`/`npx.cmd`; `node` itself is not. Resolve once.
    val isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
    fun exe(name: String) = if (isWindows) "$name.cmd" else name

    // Run npx/npm/node from the KGP-pinned Node (configureNodeAndYarn pins 22.22.3) rather than
    // the system one, so the Angular CLI's Node engine check passes regardless of what Node the
    // developer has installed. NodeJsEnvSpec.executable is the pinned `node`; its parent is the bin
    // dir that also holds npm/npx. Resolve lazily through a Provider so `nodeBinDir` stays a real
    // task input (a serialized String) and the spec lookup happens at execution time, once configured.
    val nodeBinDirPath = provider {
        val node = rootProject.extensions.getByType(NodeJsEnvSpec::class.java).executable.get()
        File(node).parentFile.absolutePath
    }

    // The runtime deps the AOT workspace needs are exactly the ones the consuming build already
    // declares as `npm(...)` for the Kotlin/JS compilation — so derive them (name → version)
    // instead of duplicating a list. Take NORMAL-scope only (drops the `devNpm` JIT webpack
    // loaders) and drop @angular/compiler, which is JIT-only and must stay out of the AOT bundle.
    // This picks up @angular/* plus zone.js/rxjs/tslib. Dedupe by name (the same dep can appear in
    // several configurations) and fail loud on a missing version rather than letting null leak into
    // package.json. Read lazily through a Provider (execution-graph time).
    fun aotNpmDeps(): Map<String, String> =
        configurations.asSequence()
            .flatMap { it.dependencies.asSequence() }
            .filterIsInstance<NpmDependency>()
            .filter { it.scope == NpmDependency.Scope.NORMAL && it.name != "@angular/compiler" }
            .groupBy { it.name }
            .mapValues { (name, deps) ->
                deps.firstNotNullOfOrNull { it.version.takeIf(String::isNotBlank) }
                    ?: error("AngularKt: npm dependency '$name' has no version — pin it explicitly.")
            }

    // The Kotlin library is wired into the workspace as a `file:` dep pointing at `build/aot-kotlin-lib`,
    // which `copyAotLibrary` (below) fills from the stable library compile-sync output. We deliberately
    // avoid Kotlin's `jsBrowserProductionLibraryDistribution` here — it emits an empty dir on incremental
    // runs, which breaks `aotServe` and any repeated `aotBuild`. The path is fixed, so deriving it costs
    // no task wiring; the files are guaranteed present because `aotNpmInstall` dependsOn `copyAotLibrary`.
    val aotLibDir = layout.buildDirectory.dir("aot-kotlin-lib")
    val libRelativePathProvider = aotLibDir.map { dir ->
        ngAot.toPath().relativize(dir.asFile.toPath()).toString()
    }

    // The library compile-sync output holds the module plus every transitive Kotlin runtime `.mjs`
    // and the `.d.ts`. Its layout (`compileSync/js/main/productionLibrary/kotlin`) is stable across
    // KGP; `copyAotLibrary` dependsOn the producing task so the files exist before we read them.
    val libKotlinDirProvider = layout.buildDirectory.dir("compileSync/js/main/productionLibrary/kotlin")
    val copyAotLibrary = tasks.register("copyAotLibrary", CopyAotLibraryTask::class.java) {
        group = "angularkt"
        description = "Sync the compiled Kotlin/JS library into build/aot-kotlin-lib (the workspace file: dep)."
        dependsOn("jsProductionLibraryCompileSync")
        libKotlinDir.set(libKotlinDirProvider)
        destination.set(aotLibDir)
        moduleName.set(config.tsModule)
    }

    // `aotInit` runs `ng new` once (pinned to the target Angular major) and patches the result
    // for AngularKt: the build points at the generated main.ts, the Kotlin library is wired in as a
    // file: dependency, and bundle budgets are lifted for the demo. Typed + input-aware: it
    // re-scaffolds whenever any of the inputs below change (see AotWorkspaceInitTask).
    val aotInit = tasks.register("aotInit", AotWorkspaceInitTask::class.java) {
        group = "angularkt"
        description = "Generate + patch the AOT Angular workspace in build/ng-aot (runs `ng new`)."
        dependsOn("kotlinNodeJsSetup")
        angularNpmVersion.set(config.angularNpmVersion)
        tsModule.set(config.tsModule)
        ngDeps.set(provider { aotNpmDeps() })
        libRelativePath.set(libRelativePathProvider)
        nodeBinDir.set(nodeBinDirPath)
        // Global styles/scripts from the `angularKt { }` DSL (shared with the JIT build). Lazy
        // Providers — editing the DSL changes the input, re-scaffolding the workspace with the new list.
        globalStyles.set(ext.aotConfig.styles)
        globalScripts.set(ext.aotConfig.scripts)
        workspace.set(layout.buildDirectory.dir("ng-aot"))
    }

    // Copy the generated .ts bridges into the workspace where Angular AOT-compiles them. Depends
    // on the scaffold so the workspace (and src/generated) exists, and on `kspKotlinJs` — the actual
    // producer of `bridgesDir` — so `./gradlew copyAotBridges` direct (or any graph that doesn't
    // pull KSP transitively) can't copy a stale/empty resources dir. `finalizedBy` stays so a KSP
    // run auto-refreshes the workspace too.
    tasks.register("copyAotBridges", CopyAotBridgesTask::class.java) {
        group = "angularkt"
        description = "Copy the KSP-generated Angular .ts bridges into build/ng-aot/src/generated."
        dependsOn(aotInit, "kspKotlinJs")
        bridgesDir.set(bridgesResourcesDir)
        destination.set(layout.buildDirectory.dir("ng-aot/src/generated"))
    }

    tasks.register("copyAotAssets", CopyAotAssetsTask::class.java) {
        group = "angularkt"
        description = "Mirror the consumer's static resources (index.html, favicon, fonts) into build/ng-aot."
        dependsOn(aotInit, "jsProcessResources")
        processedResources.set(processedDir)
        workspace.set(layout.buildDirectory.dir("ng-aot"))
    }

    // A KSP run regenerates bridges and may reprocess resources — auto-refresh the workspace.
    tasks.matching { it.name == "kspKotlinJs" }
        .configureEach { finalizedBy("copyAotBridges", "copyAotAssets") }

    // Everything the workspace needs before `npm install`: the synced Kotlin library (the file: dep)
    // plus the freshly generated bridges + mirrored resources.
    val workspaceReady = arrayOf(
        "kotlinNodeJsSetup", copyAotLibrary, "copyAotBridges", "copyAotAssets",
    )

    // Split off `npm install` as its own task: declaring package.json as input and node_modules as
    // output gives up-to-date checking (skip the reinstall when nothing changed), avoids a shell,
    // and keeps a failed install from being mistaken for a build failure. It resolves the local
    // `file:` dep to the just-built library.
    val aotNpmInstall = tasks.register("aotNpmInstall", AotNpmInstallTask::class.java) {
        group = "angularkt"
        description = "npm install the AOT workspace (resolves the local Kotlin library file: dep)."
        dependsOn(*workspaceReady)
        workspace.set(layout.buildDirectory.dir("ng-aot"))
        nodeBinDir.set(nodeBinDirPath)
        command.set(listOf(exe("npm"), "install"))
        packageJson.set(layout.buildDirectory.file("ng-aot/package.json"))
        nodeModules.set(layout.buildDirectory.dir("ng-aot/node_modules"))
    }

    tasks.register("aotBuild", NodeCliTask::class.java) {
        group = "angularkt"
        description = "Full AOT build: Kotlin library + bridges + Angular AOT bundle (no @angular/compiler)."
        dependsOn(aotNpmInstall)
        workspace.set(layout.buildDirectory.dir("ng-aot"))
        nodeBinDir.set(nodeBinDirPath)
        command.set(listOf(exe("npx"), "ng", "build"))
    }

    tasks.register("aotServe", NodeCliTask::class.java) {
        group = "angularkt"
        description = "Serve the AOT demo with live reload (Angular dev server)."
        dependsOn(aotNpmInstall)
        workspace.set(layout.buildDirectory.dir("ng-aot"))
        nodeBinDir.set(nodeBinDirPath)
        // `-PangularKt.port` overrides ng serve's default 4200 (mirrors the JIT dev-server port).
        command.set(
            buildList {
                add(exe("npx")); add("ng"); add("serve")
                config.port?.let { add("--port"); add(it.toString()) }
            }
        )
    }
}
