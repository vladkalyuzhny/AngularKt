package at.angular.cli.generate

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

/**
 * @see: https://angular.io/cli/generate#pipe
 */
// todo other options
@Suppress("UnstableApiUsage")
open class GPipeTask : GenerateTask() {

    @set:Option(
        option = "name", description = """
          The name of the pipe.
        """
    )
    @get:Input
    override var classPath = ""

    @set:Option(
        option = "flat", description = """
          When true (the default), creates files at the top level of the project.
          Default: true
        """
    )
    @get:Input
    var flat = "true"

    @TaskAction
    override fun generate() {
        val code = readTemplate("name.pipe.txt")
            .replace("package app", "package $packageName")
            .replace("NamePipe", "${className}Pipe")
        File(getClassDir(flat.toBoolean()), "$fileName.pipe.kt").writeText(code)
    }
}