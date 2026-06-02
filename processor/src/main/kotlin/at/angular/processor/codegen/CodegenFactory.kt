package at.angular.processor.codegen

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

/** Picks the [Codegen] for `angularKt.mode`, wiring in the target [AngularProfile]. */
class CodegenFactory(private val environment: SymbolProcessorEnvironment) {
    fun create(): Codegen {
        val profile = AngularProfile.parse(environment.options["angularKt.angularVersion"])
        if (profile.major < AngularProfile.SUPPORTED_FLOOR) {
            environment.logger.error(
                "AngularKt requires Angular ${AngularProfile.SUPPORTED_FLOOR}+ but " +
                    "angularKt.angularVersion=${profile.major}. Pre-${AngularProfile.SUPPORTED_FLOOR} " +
                    "majors lack signals/DestroyRef/standalone and are EOL.",
            )
        }
        return when (environment.options["angularKt.mode"] ?: "jit") {
            "aot" -> AotCodegen(
                codeGenerator = environment.codeGenerator,
                tsModule = environment.options["angularKt.tsModule"] ?: "kotlin",
                profile = profile,
                externalModules = parseExternalModules(environment.options["angularKt.externalModules"]),
                bootstrapComponentFqn = environment.options["angularKt.bootstrapComponent"]
                    ?.takeIf { it.isNotBlank() },
            )
            else -> JitCodegen(
                codeGenerator = environment.codeGenerator,
                profile = profile
            )
        }
    }

    /**
     * Decodes the `angularKt.externalModules` KSP option — `fqn=module;fqn=module;…` — that the
     * Gradle plugin builds from the consumer's `@file:JsModule` externals. Neither FQNs nor module
     * specifiers contain `;` or `=`, so the split is unambiguous.
     */
    private fun parseExternalModules(raw: String?): Map<String, String> =
        raw?.split(';')
            ?.filter { it.isNotBlank() }
            ?.associate { entry ->
                val (fqn, module) = entry.split('=', limit = 2)
                fqn to module
            }
            ?: emptyMap()
}
