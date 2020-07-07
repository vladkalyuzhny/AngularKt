package at.angular.cli.generate

import org.gradle.api.tasks.TaskAction

// todo other options
@Suppress("UnstableApiUsage")
open class GModuleTask : GenerateTask() {

    @TaskAction
    override fun generate() {
        //log.info("Starting jar2npm")
    }
}