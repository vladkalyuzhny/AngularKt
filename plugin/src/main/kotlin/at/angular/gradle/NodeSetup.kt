package at.angular.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

internal fun Project.configureNodeAndYarn() {
    /**
     * - Angular 22 requires Node ^22.22.3 || ^24.15.0 || >=26, but the Kotlin Gradle plugin's default
     *   Node (24.10.0) satisfies none of those. Pin a compatible one so the JIT webpack/yarn step resolves.
     * - In KGP 2.3.x `NodeJsEnvSpec` is a PER-PROJECT extension (registered by `NodeJsPlugin`), not a
     *   single root one. Pinning only `rootProject` left `:demo`'s own spec at the default 24.10.0, so
     *   its webpack exec looked for a node the (root-pinned, 22.22.3) setup task never downloaded —
     *   green locally only because a stray 24.10.0 already sat in ~/.gradle/nodejs, ENOENT on clean CI.
     *   So pin the version on every project that gets a spec.
     */
    rootProject.allprojects {
        plugins.withType<NodeJsPlugin>().configureEach {
            extensions.configure<NodeJsEnvSpec> {
                version.set("22.22.3")
            }
        }
    }
    /**
     * - The `angularKt.mode=jit|aot` switch changes the Kotlin/JS npm dependency set
     *   (executable+webpack vs library+TS defs), which drifts the Kotlin yarn lock. Auto-replace
     *   instead of failing the build, so switching modes is painless.
     */
    rootProject.plugins.withType<YarnPlugin>().configureEach {
        rootProject.extensions.configure<YarnRootExtension> {
            yarnLockMismatchReport = YarnLockMismatchReport.WARNING
            reportNewYarnLock = false
            yarnLockAutoReplace = true
        }
    }
}
