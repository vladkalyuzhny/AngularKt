package at.angular.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmDependency
import java.io.File

/**
 * Register the AOT pipeline tasks (aot mode only). The workspace is generated into `build/ng-aot`,
 * nothing checked in. Two halves: (1) Gradle scaffolds the workspace, builds the Kotlin/JS library
 * and copies in the generated `.ts` bridges + static resources, (2) the workspace npm-installs and
 * AOT-compiles (`aotBuild` / `aotServe`).
 */
internal fun Project.configureAotWorkspace(config: AngularKtConfig, ext: AngularKtExtension) {
    AotWorkspaceConfigurer(this, config, ext).configure()
}

/**
 * Holds the shared derived inputs the AOT tasks reference, and registers the tasks in cohesive
 * groups. Registration methods are `Project.()` extensions so the Gradle DSL stays clean.
 */
private class AotWorkspaceConfigurer(
    private val project: Project,
    private val config: AngularKtConfig,
    private val ext: AngularKtExtension,
) {
    // Resolve npx/npm/node against the KGP-pinned Node, not the system one, so the Angular CLI's
    // engine check passes regardless of the developer's local Node. `executable` is the pinned
    // `node`; its parent is the bin dir holding npm/npx. Lazy so it stays a real (String) task input.
    private val pinnedNodeBinDir = project.provider {
        val node = project.rootProject.extensions.getByType(NodeJsEnvSpec::class.java).executable.get()
        File(node).parentFile.absolutePath
    }

    // The compile-sync output: the module plus every transitive Kotlin runtime .mjs and the .d.ts.
    private val compileSyncKotlinDir =
        project.layout.buildDirectory.dir("compileSync/js/main/productionLibrary/kotlin")
    // Read from the producing tasks' real outputs, not a hardcoded KGP path — a renamed JS target
    // would otherwise silently point the copies at the wrong dir. Lazy because kspKotlinJs is
    // registered reactively (cf. tasks.matching below) and may not exist yet when this configures.
    private val kspBridgesDir = project.layout.dir(project.provider {
        project.tasks.named(KSP_TASK).get().outputs.files.files.firstOrNull { it.name == "resources" }
            ?: error("AngularKt: $KSP_TASK declares no 'resources' output directory.")
    })
    private val processedResourcesDir = project.layout.dir(project.provider {
        (project.tasks.named(PROCESS_RESOURCES_TASK).get() as Copy).destinationDir
    })

    // The workspace's runtime deps are exactly the build's own NORMAL-scope npm(...) declarations,
    // so derive them (name → version) instead of duplicating a list. Drop the JIT-only Angular
    // packages (they must stay out of the AOT bundle) and, via NORMAL scope, the devNpm loaders.
    // Dedupe by name; fail loud on a blank version rather than leaking null into package.json.
    private val jitOnlyNpmDeps = setOf("@angular/compiler", "@angular/platform-browser-dynamic")
    private val aotNpmDeps = project.provider {
        project.configurations.asSequence()
            .flatMap { it.dependencies.asSequence() }
            .filterIsInstance<NpmDependency>()
            .filter { it.scope == NpmDependency.Scope.NORMAL && it.name !in jitOnlyNpmDeps }
            .groupBy { it.name }
            .mapValues { (name, deps) ->
                deps.firstNotNullOfOrNull { it.version.takeIf(String::isNotBlank) }
                    ?: error("AngularKt: npm dependency '$name' has no version — pin it explicitly.")
            }
    }

    // Generated workspace root, and the single home of the "ng-aot" literal — every workspace path
    // derives from it, so a rename touches one line. Lazy Provider, no eager `.get().asFile`.
    private val ngAot = project.layout.buildDirectory.dir("ng-aot")
    // The workspace pulls the Kotlin library as a file: dep from build/aot-kotlin-lib, which
    // copyAotLibrary fills. Deliberately NOT jsBrowserProductionLibraryDistribution — that emits an
    // empty dir on incremental runs and breaks repeated aotServe/aotBuild.
    private val aotLibDir = project.layout.buildDirectory.dir("aot-kotlin-lib")
    private val libPathInWorkspace = ngAot.zip(aotLibDir) { ws, lib ->
        ws.asFile.toPath().relativize(lib.asFile.toPath()).toString()
    }
    private fun ngAotDir(sub: String) = ngAot.map { it.dir(sub) }
    private fun ngAotFile(sub: String) = ngAot.map { it.file(sub) }

    // npx/npm on Windows are `.cmd` shims; `node` is not. Resolve the suffix once.
    private val isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
    private fun exe(name: String) = if (isWindows) "$name.cmd" else name

    fun configure() = project.run {
        val copyAotLibrary = registerCopyAotLibrary()
        val aotInit = registerAotInit()
        val aotSync = registerWorkspaceSyncTasks(aotInit, copyAotLibrary)
        registerRunTasks(aotSync)
    }

    private fun Project.registerCopyAotLibrary(): TaskProvider<CopyAotLibraryTask> =
        tasks.register("copyAotLibrary", CopyAotLibraryTask::class.java) {
            group = "angularkt"
            description = "Sync the compiled Kotlin/JS library into build/aot-kotlin-lib (the workspace file: dep)."
            dependsOn(LIBRARY_COMPILE_SYNC_TASK)
            libKotlinDir.set(compileSyncKotlinDir)
            destination.set(aotLibDir)
            moduleName.set(config.tsModule)
        }

    // `ng new` once (pinned to the target Angular major), then patch it for AngularKt: point the build
    // at the generated main.ts, wire the Kotlin library as a file: dep, lift the demo bundle budgets.
    // Input-aware — re-scaffolds when any input below changes.
    private fun Project.registerAotInit(): TaskProvider<AotWorkspaceInitTask> =
        tasks.register("aotInit", AotWorkspaceInitTask::class.java) {
            group = "angularkt"
            description = "Generate + patch the AOT Angular workspace in build/ng-aot (runs `ng new`)."
            dependsOn(NODE_SETUP_TASK)
            angularNpmVersion.set(config.angularNpmVersion)
            tsModule.set(config.tsModule)
            ngDeps.set(aotNpmDeps)
            libRelativePath.set(libPathInWorkspace)
            nodeBinDir.set(pinnedNodeBinDir)
            // Global styles/scripts from the angularKt { } DSL (shared with the JIT build).
            globalStyles.set(ext.aotConfig.styles)
            globalScripts.set(ext.aotConfig.scripts)
            workspace.set(ngAot)
            scaffoldStamp.set(ngAotFile(".angularkt-scaffold"))
        }

    // The tasks that refresh the scaffolded workspace from Kotlin sources — bridges, mirrored
    // resources, the live-reload hash — and the `aotSync` umbrella that ties them together. Returns
    // aotSync: downstream tasks gate on it (and, transitively, on everything it depends on).
    private fun Project.registerWorkspaceSyncTasks(
        aotInit: TaskProvider<AotWorkspaceInitTask>,
        copyAotLibrary: TaskProvider<CopyAotLibraryTask>,
    ): TaskProvider<Task> {
        // dependsOn kspKotlinJs (the real producer) as well as the scaffold, so running this task
        // directly can't copy a stale/empty dir. finalizedBy (below) keeps a KSP run auto-refreshing.
        val copyAotBridges = tasks.register("copyAotBridges", CopyAotBridgesTask::class.java) {
            group = "angularkt"
            description = "Copy the KSP-generated Angular .ts bridges into build/ng-aot/src/generated."
            dependsOn(aotInit, KSP_TASK)
            bridgesDir.set(kspBridgesDir)
            destination.set(ngAotDir("src/generated"))
        }

        val copyAotAssets = tasks.register("copyAotAssets", CopyAotAssetsTask::class.java) {
            group = "angularkt"
            description = "Mirror the consumer's static resources (index.html, favicon, fonts) into build/ng-aot."
            dependsOn(aotInit, PROCESS_RESOURCES_TASK)
            processedResources.set(processedResourcesDir)
            workspace.set(ngAot)
        }

        // Content hash of the synced library, written into src as a watched asset. The library sits
        // behind the file: dep in node_modules, which ng serve ignores — without this a Kotlin-only
        // edit never reloads the dev server.
        val aotHashStamp = tasks.register("aotHashStamp", AotHashStampTask::class.java) {
            group = "angularkt"
            description = "Hash the compiled Kotlin library into a watched asset so `ng serve` reloads on Kotlin edits."
            dependsOn(aotInit, copyAotLibrary)
            libDir.set(aotLibDir)
            stampFile.set(ngAotFile("src/generated/build-stamp.txt"))
        }

        // A KSP run regenerates bridges / may reprocess resources — auto-refresh the workspace.
        tasks.matching { it.name == KSP_TASK }
            .configureEach { finalizedBy(copyAotBridges, copyAotAssets) }

        // One task to refresh the whole workspace (library + bridges + assets + hash). Run with -t
        // alongside aotServe for live reload, or in the manual two-terminal flow. No group on purpose:
        // it's the background watch target aotServe spawns, not a third public run mode.
        return tasks.register("aotSync") {
            description = "Refresh the AOT workspace from Kotlin sources (run with -t for live reload alongside aotServe)."
            dependsOn(copyAotLibrary, copyAotBridges, copyAotAssets, aotHashStamp)
        }
    }

    // npm install + the two public run modes. All three drive the pinned Node CLI against the
    // workspace; gating on aotSync pulls in every workspace-refresh task transitively.
    private fun Project.registerRunTasks(aotSync: TaskProvider<Task>) {
        // npm install as its own task, so package.json → node_modules gives up-to-date checking (skip
        // the reinstall when nothing changed) and a failed install reads as such, not a build failure.
        val aotNpmInstall = tasks.register("aotNpmInstall", AotNpmInstallTask::class.java) {
            group = "angularkt"
            description = "npm install the AOT workspace (resolves the local Kotlin library file: dep)."
            dependsOn(NODE_SETUP_TASK, aotSync)
            workspace.set(ngAot)
            nodeBinDir.set(pinnedNodeBinDir)
            command.set(listOf(exe("npm"), "install"))
            packageJson.set(ngAotFile("package.json"))
            nodeModules.set(ngAotDir("node_modules"))
        }

        tasks.register("aotBuild", NodeCliTask::class.java) {
            group = "angularkt"
            description = "Full AOT build: Kotlin library + bridges + optimized Angular production bundle (no @angular/compiler)."
            dependsOn(aotNpmInstall)
            workspace.set(ngAot)
            nodeBinDir.set(pinnedNodeBinDir)
            // Pin `production` explicitly for a deterministic release bundle across Angular versions.
            command.set(listOf(exe("npx"), "ng", "build", "--configuration", "production"))
        }

        // The one-command dev loop: foreground `ng serve` + a background `aotSync -t` it tears down on
        // exit (AotServeTask). aotSync is still available for the manual two-terminal flow.
        tasks.register("aotServe", AotServeTask::class.java) {
            group = "angularkt"
            description = "Serve the AOT app with Kotlin live reload (dev server + recompile-on-edit, one command)."
            dependsOn(aotNpmInstall)
            workspace.set(ngAot)
            nodeBinDir.set(pinnedNodeBinDir)
            // -PangularKt.port overrides ng serve's default 4200 (mirrors the JIT dev-server port).
            val portArgs = config.port?.let { listOf("--port", it.toString()) }.orEmpty()
            serveCommand.set(listOf(exe("npx"), "ng", "serve") + portArgs)
            gradlew.set(File(rootProject.rootDir, if (isWindows) "gradlew.bat" else "gradlew").absolutePath)
            // `path` here is the TASK path (:demo:aotServe); the watch wants the project's aotSync.
            syncTaskPath.set("${project.path.removeSuffix(":")}:aotSync")
            rootDir.set(rootProject.layout.projectDirectory)
        }
    }

    /** The KGP/KSP task names this file relies on — the external contract, in one place. */
    private companion object {
        const val KSP_TASK = "kspKotlinJs"
        const val PROCESS_RESOURCES_TASK = "jsProcessResources"
        const val LIBRARY_COMPILE_SYNC_TASK = "jsProductionLibraryCompileSync"
        const val NODE_SETUP_TASK = "kotlinNodeJsSetup"
    }
}