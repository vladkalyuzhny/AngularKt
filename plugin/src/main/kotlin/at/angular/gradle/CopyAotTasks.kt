package at.angular.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskAction
import java.security.MessageDigest

/**
 * Materialize the compiled Kotlin/JS library into a stable directory the AOT workspace links as a
 * `file:` dependency. We do NOT consume Kotlin's own `jsBrowserProductionLibraryDistribution`: that
 * task emits an empty directory on incremental runs (it only populates on the first run after a
 * clean), which silently breaks `aotServe`/repeated `aotBuild`. Instead we [Sync] from the library
 * compile-sync output — `compileSync/js/main/productionLibrary/kotlin`, which stays reliably
 * populated (the module `.mjs`/`.d.ts` plus every transitive Kotlin runtime `.mjs`) — and write the
 * one thing it lacks: a `package.json` pointing npm at the module entry. Angular's own deps are
 * resolved from the workspace root, so a minimal manifest is enough.
 */
abstract class CopyAotLibraryTask : Sync() {
    /** Library compile-sync output dir (`compileSync/js/main/productionLibrary/kotlin`). */
    @get:Internal
    abstract val libKotlinDir: DirectoryProperty

    /** Stable destination the workspace `file:` dep resolves to (`build/aot-kotlin-lib`). */
    @get:Internal
    abstract val destination: DirectoryProperty

    /** npm package name = the workspace tsModule; the entry files are `$moduleName.{mjs,d.ts}`. */
    @get:Input
    abstract val moduleName: Property<String>

    init {
        from(libKotlinDir)
        into(destination)
        // Sync prunes anything not in the source, so re-add package.json after each copy. On an
        // up-to-date (skipped) run the previous one survives, so the file: dep stays resolvable.
        doLast {
            val name = moduleName.get()
            val dir = destination.get().asFile
            // Kotlin's TS-definitions filename tracks the JS module kind: `.d.mts` for ES modules
            // (useEsModules — Kotlin 2.3+), `.d.ts` historically. The whole library dir is synced
            // here, so point "types" at whichever declaration file was actually emitted.
            val types = listOf("$name.d.mts", "$name.d.ts")
                .firstOrNull { dir.resolve(it).exists() }
                ?: "$name.d.ts"
            dir.resolve("package.json").writeJson(
                linkedMapOf(
                    "name" to name,
                    "version" to "0.1.0",
                    "main" to "$name.mjs",
                    "types" to types,
                )
            )
        }
    }
}

/**
 * Stamp a content hash of the compiled Kotlin library into a tiny file inside the workspace `src/`,
 * so editing Kotlin can live-reload the AOT dev server. The library itself lives behind the `file:`
 * dep under `node_modules` (via a symlink), which `ng serve`'s watcher ignores — so a Kotlin-only
 * change would never trigger a rebuild. This stamp file, listed as a watched build asset (see
 * [AotWorkspacePatcher]), changes whenever the library bytes change, which nudges `ng serve` to
 * rebuild and re-pull the fresh library through the symlink (`preserveSymlinks`). Its contents are
 * never read — only the change matters. Gradle's own up-to-date check (content-hashed [libDir])
 * keeps it from rewriting — and so from spuriously reloading — when the library is unchanged.
 */
abstract class AotHashStampTask : DefaultTask() {
    /** The synced library dir whose contents are hashed (`build/aot-kotlin-lib`). */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val libDir: DirectoryProperty

    /** Watched stamp file in the workspace src (`build/ng-aot/src/generated/build-stamp.txt`). */
    @get:OutputFile
    abstract val stampFile: RegularFileProperty

    @TaskAction
    fun stamp() {
        val dir = libDir.get().asFile
        val digest = MessageDigest.getInstance("SHA-256")
        // Hash path + bytes of every file, in a stable order, so the stamp tracks any content change.
        dir.walkTopDown().filter { it.isFile }.sortedBy { it.relativeTo(dir).invariantSeparatorsPath }
            .forEach { file ->
                digest.update(file.relativeTo(dir).invariantSeparatorsPath.toByteArray())
                digest.update(file.readBytes())
            }
        val hash = digest.digest().joinToString("") { "%02x".format(it) }
        val out = stampFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(hash)
    }
}

/**
 * Copy the KSP-generated Angular `.ts` bridges into the workspace where Angular AOT-compiles them.
 * A thin [Copy] subclass so the call site is pure wiring (set the two dirs) and the `*.ts` filter
 * lives with the task. [Copy] tracks the resolved sources/destination itself, so the convenience
 * properties are `@Internal` to avoid double-declaring inputs.
 */
abstract class CopyAotBridgesTask : Copy() {
    /** KSP resources output (`build/generated/ksp/js/jsMain/resources`). */
    @get:Internal
    abstract val bridgesDir: DirectoryProperty

    /** Workspace `src/generated`. */
    @get:Internal
    abstract val destination: DirectoryProperty

    init {
        from(bridgesDir) { include("*.ts") }
        into(destination)
    }
}

/**
 * Mirror the consumer's static resources into the workspace so AOT renders like JIT (favicon,
 * fonts/Material-Icons links, `<title>`, body styling). Source is the processed resources dir —
 * whatever the consumer's jsMain source set declares — so this stays generic. Two destinations:
 *   * everything except index.html  -> `public/`  (Angular serves it from the web root)
 *   * index.html                    -> `src/`     (Angular's index, bundles injected by it)
 * The JIT index.html hand-loads the webpack bundle (`<script src="demo.js">`); strip any local
 * `<script src>` on the way in — the Angular builder injects its own bundles, and a stale tag would
 * just 404.
 */
abstract class CopyAotAssetsTask : Copy() {
    /** Processed resources dir (`build/processedResources/js/main`). */
    @get:Internal
    abstract val processedResources: DirectoryProperty

    /** Workspace root (`build/ng-aot`). */
    @get:Internal
    abstract val workspace: DirectoryProperty

    init {
        into(workspace)
        from(processedResources) {
            exclude("index.html")
            into("public")
        }
        from(processedResources) {
            include("index.html")
            into("src")
            // Drop the JIT bundle <script src="demo.js"> (keep CDN/http ones); leaves a blank line.
            filter { line: String -> if (isLocalBundleScript(line)) "" else line }
        }
    }

    private companion object {
        /** A `<script>` whose src is a local `.js` bundle (not an absolute http(s) CDN URL). */
        fun isLocalBundleScript(line: String): Boolean {
            if ("<script" !in line || "src=" !in line) return false
            // The src value: text after `src=`, between its opening and matching closing quote.
            val afterEq = line.substringAfter("src=")
            val quote = afterEq.firstOrNull() ?: return false   // " or '
            val src = afterEq.drop(1).substringBefore(quote)
            return src.endsWith(".js") && !src.startsWith("http")
        }
    }
}
