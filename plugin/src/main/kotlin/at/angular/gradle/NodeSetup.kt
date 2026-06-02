package at.angular.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

/**
 * - Angular 22 requires Node ^22.22.3 || ^24.15.0 || >=26, but the Kotlin Gradle plugin ships an
 *   older Node (22.0.0). Pin a compatible one so the JIT webpack/yarn step resolves.
 * - The `angularKt.mode=jit|aot` switch changes the Kotlin/JS npm dependency set
 *   (executable+webpack vs library+TS defs), which drifts the Kotlin yarn lock. Auto-replace
 *   instead of failing the build, so switching modes is painless.
 */
internal fun Project.configureNodeAndYarn() {
    rootProject.plugins.withType<NodeJsRootPlugin>().configureEach {
        rootProject.extensions.configure<NodeJsEnvSpec> {
            version.set("22.22.3")
        }
    }
    rootProject.plugins.withType<YarnPlugin>().configureEach {
        rootProject.extensions.configure<YarnRootExtension> {
            yarnLockMismatchReport = YarnLockMismatchReport.WARNING
            reportNewYarnLock = false
            yarnLockAutoReplace = true
        }
    }
}
