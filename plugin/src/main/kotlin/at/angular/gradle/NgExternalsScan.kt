package at.angular.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

private val JS_MODULE = Regex("""@file:JsModule\(\s*"([^"]+)"\s*\)""")
private val PACKAGE = Regex("""(?m)^\s*package\s+([A-Za-z0-9_.]+)""")
private val EXTERNAL_DECLARATION = Regex("""(?m)^\s*external\s+(?:class|object|interface)\s+([A-Za-z0-9_]+)""")

/**
 * Discovers third-party Angular externals (Material, any consumer-generated Karakum module) so the
 * AOT processor can import their NgModules from the right npm package.
 *
 * The processor can't read `@file:JsModule` off these — they reach it as compiled klib dependencies,
 * and KSP exposes no annotations for binary deps. So we resolve the mapping here, at the one point
 * that can see the dependencies' Kotlin sources: the consuming project's build. Each project
 * dependency's `@file:JsModule("…")` files are scanned into `FQN → npm module`, which the plugin
 * forwards to the processor via the `angularKt.externalModules` KSP option.
 *
 * AngularKt's own first-party `@angular` bindings are NOT produced here — the processor catalogs
 * those itself (NgReferences); this is purely the open-ended, third-party half.
 */
internal fun Project.collectNgExternalModules(): Map<String, String> {
    val result = LinkedHashMap<String, String>()
    dependencyProjects().forEach { dep ->
        val src = dep.projectDir.resolve("src")
        if (!src.isDirectory) return@forEach

        src.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { scanJsModuleFile(it.readText(), result) }
    }
    return result
}

/** Project dependencies across every configuration; extra (test) modules are harmless to scan. */
private fun Project.dependencyProjects(): Set<Project> =
    configurations.flatMap { it.dependencies.withType(ProjectDependency::class.java) }
        .map { it.dependencyProject }
        .toSet()
/**
 * A `@file:JsModule` annotation applies to every top-level declaration in the file, so each
 * `external class/object/interface` in such a file maps `package.Name → module`.
 */
private fun scanJsModuleFile(text: String, into: MutableMap<String, String>) {
    val module = JS_MODULE.find(text)?.groupValues?.get(1) ?: return
    val pkg = PACKAGE.find(text)?.groupValues?.get(1) ?: return
    EXTERNAL_DECLARATION.findAll(text).forEach { match ->
        into["$pkg.${match.groupValues[1]}"] = module
    }
}
