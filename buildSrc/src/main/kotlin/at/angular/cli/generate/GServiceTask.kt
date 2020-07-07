package at.angular.cli.generate

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option
import java.io.File

/**
 * @see: https://angular.io/cli/generate#service
 */
// todo other options
@Suppress("UnstableApiUsage")
open class GServiceTask : GenerateTask() {

    @set:Option(
        option = "name", description = """
          The name of the service.
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

    init {
        description = "Creates a new, generic service definition in the given or default project."
    }

    override fun generate() {
        val code = readTemplate("name.service.txt")
            .replace("package app", "package $packageName")
            .replace("NameService", "${className}Service")
        File(getClassDir(flat.toBoolean()), "$fileName.service.kt").writeText(code)
    }
}