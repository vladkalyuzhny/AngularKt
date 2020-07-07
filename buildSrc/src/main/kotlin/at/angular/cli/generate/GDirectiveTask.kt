package at.angular.cli.generate

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

// todo other options
@Suppress("UnstableApiUsage")
open class GDirectiveTask : GenerateTask() {

    @set:Option(
        option = "prefix", description = """
          A prefix to apply to generated selectors.
          Aliases: -p
        """
    )
    @get:Input
    var prefix = ""

    @set:Option(
        option = "selector", description = """
          The HTML selector to use for this directive.
        """
    )
    @get:Input
    var selector = ""

    @TaskAction
    override fun generate() {

    }
}