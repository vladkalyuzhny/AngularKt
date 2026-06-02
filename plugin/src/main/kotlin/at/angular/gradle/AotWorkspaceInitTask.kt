package at.angular.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

/**
 * Initializes the AOT Angular workspace: scaffolds it with `ng new`, then patches it.
 */
abstract class AotWorkspaceInitTask : DefaultTask() {
    @get:Inject
    abstract val execOps: ExecOperations

    /** Angular CLI version `ng new` is pinned to (e.g. `~22.0.0`). */
    @get:Input
    abstract val angularNpmVersion: Property<String>

    /** npm module name the generated `.ts` bridges import the Kotlin library by. */
    @get:Input
    abstract val tsModule: Property<String>

    /** name->version of the `@angular` (+ zone.js/rxjs/tslib) runtime deps to declare. */
    @get:Input
    abstract val ngDeps: MapProperty<String, String>

    /** Relative path (from the workspace root) to the built Kotlin library `file:` dep. */
    @get:Input
    abstract val libRelativePath: Property<String>

    /** Global stylesheets (from `angularkt.assets.json`) to add to angular.json `styles`. */
    @get:Input
    abstract val globalStyles: ListProperty<String>

    /** Global scripts (from `angularkt.assets.json`) to add to angular.json `scripts`. */
    @get:Input
    abstract val globalScripts: ListProperty<String>

    /** Absolute path to the KGP-pinned Node `bin` dir, prepended to PATH for the CLI. */
    @get:Input
    abstract val nodeBinDir: Property<String>

    @get:OutputDirectory
    abstract val workspace: DirectoryProperty

    @TaskAction
    fun initWorkspace() {
        val ngAot = workspace.get().asFile
        val nodePath = nodeBinDir.get() + File.pathSeparator + (System.getenv("PATH") ?: "")
        // Self-cleaning: an input changed (or this is a fresh run), so regenerate from scratch.
        ngAot.deleteRecursively()
        ngAot.parentFile.mkdirs()
        // Vanilla workspace via the pinned CLI (npx -y, no global install). --skip-install:
        // Gradle's aotNpmInstall owns node_modules; --skip-git: no nested repo inside build/;
        // --defaults: non-interactive. No arg contains a space, so whitespace is the argv separator.
        val name = ngAot.name
        // `--ssr` is an `ng new` flag only from Angular CLI 17; on 16 the CLI aborts with
        // "Unknown argument: ssr". Emit it only when the pinned CLI understands it — `--defaults`
        // already scaffolds a non-SSR app on every major, so 16 is correct without it. A blank
        // token here is dropped by the isNotBlank filter below.
        val major = Regex("""\d+""").find(angularNpmVersion.get())?.value?.toIntOrNull()
        val ssrFlag = if (major == null || major >= 17) "--ssr=false" else ""
        val ngNew = """
            npx -y -p @angular/cli@${angularNpmVersion.get()}
            ng new $name --directory $name
            --style=css --routing=false $ssrFlag --skip-install --skip-git --defaults
        """.trimIndent()
        val argv = nodeLauncherArgv(
            command = ngNew.split('\n', ' ').filter { it.isNotBlank() },
            nodeBinDir = nodeBinDir.get()
        )
        execOps.exec {
            workingDir = ngAot.parentFile
            environment("PATH", nodePath)
            environment("CI", "true")
            environment("NG_CLI_ANALYTICS", "false")
            commandLine(argv)
        }
        // Drop the default app - AngularKt supplies src/generated/* instead.
        listOf("src/app", "src/main.ts", "src/app.config.ts", "src/app.routes.ts")
            .forEach { File(ngAot, it).deleteRecursively() }
        // Patch angular.json / package.json / tsconfig* for the generated entry + lib.
        AotWorkspacePatcher.patch(
            ngAot = ngAot,
            tsModule = tsModule.get(),
            ngDeps = ngDeps.get(),
            libRelativePath = libRelativePath.get(),
            globalStyles = globalStyles.get(),
            globalScripts = globalScripts.get(),
        )
    }
}
