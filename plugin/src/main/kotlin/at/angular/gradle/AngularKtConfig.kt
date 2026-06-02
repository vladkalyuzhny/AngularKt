package at.angular.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Resolved AngularKt build knobs, parsed once from project properties.
 */
class AngularKtConfig private constructor(
    val mode: String,
    val isAot: Boolean,
    val angularVersion: String,
    val angularNpmVersion: String,
    /**
     * zone.js npm version, coupled to the Angular major — `@angular/core`'s zone.js peer range steps
     * with each release, and the AOT workspace's npm install rejects a mismatched peer with ERESOLVE.
     */
    val zoneJsNpmVersion: String,
    val tsModule: String,
    /**
     * Dev-server port override (`-PangularKt.port`), applied to BOTH the JIT webpack-dev-server
     * (`jsBrowserDevelopmentRun`) and the AOT `ng serve` (`aotServe`). `null` keeps each mode's
     * native default (8080 for JIT, 4200 for AOT).
     */
    val port: Int?,
    internal val processorDep: Any,
) {
    companion object {
        // Supported floor is Angular 16 (signals/DestroyRef/standalone all exist there, so no
        // per-version source sets are needed). Mirror of AngularProfile.SUPPORTED_FLOOR.
        private const val SUPPORTED_FLOOR = 16

        fun from(project: Project): AngularKtConfig {
            val mode = resolveMode(project)
            val rawVersion = (project.findProperty("angularKt.angularVersion") ?: "22").toString().trim()
            val npmVersion = if (rawVersion.toIntOrNull() != null) "^$rawVersion.0.0" else rawVersion
            val major = Regex("""\d+""").find(rawVersion)?.value?.toInt()
            if (major != null && major < SUPPORTED_FLOOR) {
                throw GradleException(
                    "AngularKt requires Angular $SUPPORTED_FLOOR+ (angularKt.angularVersion=$rawVersion). " +
                        "Pre-$SUPPORTED_FLOOR majors are EOL and lack signals/DestroyRef/standalone."
                )
            }
            // zone.js tracks @angular/core's peer range, which steps with the major. 19+ (and a
            // non-numeric "latest"/"next", where major is null) track ~0.15.0; 21/22 also accept
            // 0.16 but 0.15 is within their union, so one default covers the whole modern range.
            val zoneJsNpmVersion = when (major) {
                16 -> "~0.13.0"
                17, 18 -> "~0.14.0"
                else -> "~0.15.0"
            }
            val processorDep: Any = project.findProject(":processor")
                ?: "io.github.vladkalyuzhny:angularkt-processor:${BuildInfo.VERSION}"
            val tsModule = "${project.rootProject.name}-${project.name}"
            val port = project.findProperty("angularKt.port")?.toString()?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let {
                    it.toIntOrNull() ?: throw GradleException(
                        "AngularKt: angularKt.port must be an integer port number, got '$it'"
                    )
                }
            return AngularKtConfig(
                mode = mode,
                isAot = mode == "aot",
                angularVersion = rawVersion,
                angularNpmVersion = npmVersion,
                zoneJsNpmVersion = zoneJsNpmVersion,
                tsModule = tsModule,
                port = port,
                processorDep = processorDep,
            )
        }

        // Mode follows the task you run: any requested `aot*`/`copyAot*` task selects aot,
        // everything else (and IDE sync / no task) defaults to jit. The AOT tasks are the
        // only ones named with "aot", so the task name alone disambiguates — no extra switch.
        private fun resolveMode(project: Project): String {
            val requestedAot = project.gradle.startParameter.taskNames
                .any { it.substringAfterLast(':').contains("aot", ignoreCase = true) }
            return if (requestedAot) "aot" else "jit"
        }
    }
}