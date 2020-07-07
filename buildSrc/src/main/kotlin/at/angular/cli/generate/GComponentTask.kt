package at.angular.cli.generate

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

// todo other options
@Suppress("UnstableApiUsage")
open class GComponentTask : GenerateTask() {

    //private val log = LoggerFactory.getLogger(this::class.java)!!

    @set:Option(
        option = "prefix", description = """
          The prefix to apply to the generated component selector.
          Aliases: -p
        """
    )
    @get:Input
    var prefix = ""

    @set:Option(
        option = "selector", description = """
          The HTML selector to use for this component.
        """
    )
    @get:Input
    var selector = ""

    @set:Option(
        option = "skipSelector", description = """
          Specifies if the component should have a selector or not.
          Default: false
        """
    )
    @get:Input
    var skipSelector = "false"

    @set:Option(
        option = "inlineTemplate", description = """
          When true, includes template inline in the component.kt file. By default, an external template file is created and referenced in the component.kt file.
          Default: false
          Aliases: -t
        """
    )
    @get:Input
    var inlineTemplate = "false"

    @set:Option(
        option = "inlineStyle", description = """
          When true, includes styles inline in the component.kt file. Only CSS styles can be included inline. By default, an external styles file is created and referenced in the component.kt file.
          Default: false
          Aliases: -s
        """
    )
    @get:Input
    var inlineStyle = "false"

    @set:Option(
        option = "style", description = """
          The file extension or preprocessor to use for style files.
          Default: css
        """
    )
    @get:Input
    var style = "css"

    @set:Option(
        option = "displayBlock", description = """
          Specifies if the style will contain :host { display: block; }.
          Default: false
          Aliases: -b
        """
    )
    @get:Input
    var displayBlock = "false"

    init {
        group = "node"
        description = "Attach Kotlin JAR files to node_modules"
    }

    @TaskAction
    override fun generate() {
        //log.info("Starting jar2npm")
        println("inlineStyle $inlineStyle")
    }
}