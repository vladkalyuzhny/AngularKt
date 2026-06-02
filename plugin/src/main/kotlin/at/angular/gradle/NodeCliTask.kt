package at.angular.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

/**
 * Rewrite a Node-launcher argv so an `npx`/`npm` invocation runs through the `node` binary directly
 * instead of letting the OS exec the launcher itself. In the KGP-pinned toolchain `npx`/`npm` are
 * Unix symlinks to `*-cli.js` shims (`#!/usr/bin/env node`); starting them relies on the shebang +
 * a PATH lookup of `node`, which on a cold Gradle run can fire while the toolchain is still being
 * provisioned and fails as "A problem occurred starting process 'command 'npx''". Resolving the
 * symlink to its real `*-cli.js` and calling `node <cli.js> …` removes that fragility. Anything that
 * is not a JS-shim symlink (a plain binary, or Windows' real `npm.cmd`/`npx.cmd` wrappers) passes
 * through unchanged.
 */
internal fun nodeLauncherArgv(command: List<String>, nodeBinDir: String): List<String> {
    val launcher = command.firstOrNull() ?: return command
    if (launcher.removeSuffix(".cmd") !in setOf("npx", "npm")) return command

    val launcherFile = File(nodeBinDir, launcher)
    val target = runCatching { launcherFile.canonicalFile }.getOrNull() ?: return command

    // Only a symlink pointing at a *-cli.js shim needs the node-direct treatment.
    if (target == launcherFile.absoluteFile || !target.name.endsWith(".js")) return command

    val isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
    val node = File(nodeBinDir, if (isWindows) "node.exe" else "node")
    return listOf(node.absolutePath, target.absolutePath) + command.drop(1)
}

/**
 * Run a Node CLI (npx/npm/ng) inside the AOT workspace
 */
abstract class NodeCliTask : DefaultTask() {
    @get:Inject
    abstract val execOps: ExecOperations

    /** Workspace root the CLI runs in (`build/ng-aot`). Tracked via task-specific I/O, not here. */
    @get:Internal
    abstract val workspace: DirectoryProperty

    /** Absolute path to the KGP-pinned Node `bin` dir, prepended to PATH so the engine check passes. */
    @get:Input
    abstract val nodeBinDir: Property<String>

    /** Fully-resolved argv, e.g. `[npx, ng, build]`. */
    @get:Input
    abstract val command: ListProperty<String>

    @TaskAction
    fun exec() {
        val nodePath = nodeBinDir.get() + File.pathSeparator + (System.getenv("PATH") ?: "")
        execOps.exec {
            workingDir = workspace.get().asFile
            environment("PATH", nodePath)
            commandLine(nodeLauncherArgv(command.get(), nodeBinDir.get()))
        }
    }
}

/**
 * `npm install` in the AOT workspace. Declaring `package.json` as input and `node_modules` as output
 * buys up-to-date checking — the reinstall is skipped when the manifest hasn't changed. The local
 * `file:` Kotlin library is resolved here; with `preserveSymlinks` a rebuilt library is picked up
 * through the symlink, so a manifest hash is the right granularity.
 */
abstract class AotNpmInstallTask : NodeCliTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val packageJson: RegularFileProperty

    @get:OutputDirectory
    abstract val nodeModules: DirectoryProperty
}
