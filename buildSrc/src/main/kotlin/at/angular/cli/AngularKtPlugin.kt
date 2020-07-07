package at.angular.cli

import at.angular.cli.generate.*
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class AngularKtPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            // todo other cli tasks
            registerTasks<GenerateTask>("generate", "g")
            registerTask<GModuleTask>("module")
            registerTask<GServiceTask>("service")
            registerTask<GComponentTask>("component")
            registerTask<GDirectiveTask>("directive")
            registerTask<GPipeTask>("pipe")
        }
    }

    private inline fun <reified T : Task> Project.registerTasks(vararg names: String) {
        names.forEach {
            registerTask<T>(it)
        }
    }

    private inline fun <reified T : Task> Project.registerTask(name: String) {
        try {
            tasks.register(name, T::class.java)
        } catch (e: InvalidUserDataException) {
            e.printStackTrace()
        }
    }
}
