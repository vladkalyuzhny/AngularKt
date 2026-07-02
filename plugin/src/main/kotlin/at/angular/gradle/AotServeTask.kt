package at.angular.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.thread

/**
 * `aotServe` — one-command AOT dev loop: `ng serve` plus continuous Kotlin live-reload, no second terminal.
 *
 * The inversion that makes this work: the Angular dev server runs in the FOREGROUND and the Kotlin
 * watch (`gradlew <project>:aotSync -t`) runs as a BACKGROUND child. It has to be this way round —
 * `-t` only starts watching after a build finishes, and a foreground server never finishes, so the
 * server can't be the thing `-t` is attached to. The watch gets its own daemon; a `finally` (normal
 * exit / Ctrl+C) and a shutdown hook (SIGTERM) tear it down.
 */
abstract class AotServeTask : DefaultTask() {
    @get:Inject
    abstract val execOps: ExecOperations

    /** Workspace root the dev server runs in (`build/ng-aot`). */
    @get:Internal
    abstract val workspace: DirectoryProperty

    /** KGP-pinned Node `bin` dir, prepended to PATH so the Angular CLI's engine check passes. */
    @get:Internal
    abstract val nodeBinDir: Property<String>

    /** Fully-resolved dev-server argv, e.g. `[npx, ng, serve, --port, 4200]`. */
    @get:Internal
    abstract val serveCommand: ListProperty<String>

    /** Absolute path to the build's `gradlew` (or `gradlew.bat`). */
    @get:Internal
    abstract val gradlew: Property<String>

    /** Fully-qualified path of the sync task to watch, e.g. `:demo:aotSync`. */
    @get:Internal
    abstract val syncTaskPath: Property<String>

    /** Directory the background `gradlew` runs from (the build root). */
    @get:Internal
    abstract val rootDir: DirectoryProperty

    @TaskAction
    fun dev() {
        val nodePath = nodeBinDir.get() + File.pathSeparator + (System.getenv("PATH") ?: "")
        val watch = startWatch(nodePath)

        val teardown = Thread { watch.stopWithin(3) }
        Runtime.getRuntime().addShutdownHook(teardown)

        try {
            val code = execOps.exec {
                workingDir = workspace.get().asFile
                environment("PATH", nodePath)
                commandLine(nodeLauncherArgv(serveCommand.get(), nodeBinDir.get()))
                isIgnoreExitValue = true
            }.exitValue
            // 130/143 = Ctrl+C / SIGTERM: how you stop a dev server, not a failure.
            if (code != 0 && code != 130 && code != 143) {
                throw GradleException("ng serve exited with code $code")
            }
        } finally {
            runCatching { Runtime.getRuntime().removeShutdownHook(teardown) }
            watch.stopWithin(5)
        }
    }

    /**
     * Background: continuous Kotlin -> workspace sync, its output relayed into this task's log. A private
     * --project-cache-dir keeps this off the root's shared `.gradle/`, where two builds would serialize
     * on the file-hash journal and periodically deadlock ("Timeout waiting to lock ...").
     */
    private fun startWatch(nodePath: String): Process {
        val watch = ProcessBuilder(
            gradlew.get(), syncTaskPath.get(), "-t", "--console=plain",
            "--project-cache-dir=build/aot-watch-cache",
        ).apply {
            directory(rootDir.get().asFile)
            redirectErrorStream(true)
            environment()["PATH"] = nodePath // drop if aotSync never shells out to node
        }.start()

        // Relay via logger.lifecycle, not inheritIO: the child inherits the daemon's fds, which aren't
        // forwarded to the gradlew client terminal, so inherited output would be silent — a broken build
        // would look frozen instead of showing its compile error.
        thread(isDaemon = true) {
            runCatching {
                watch.inputStream.bufferedReader()
                    .forEachLine { logger.lifecycle("[aotSync] {}", it) }
            }
        }
        return watch
    }

    private fun Process.stopWithin(seconds: Long) {
        destroy()
        if (!waitFor(seconds, TimeUnit.SECONDS)) {
            destroyForcibly()
        }
    }
}