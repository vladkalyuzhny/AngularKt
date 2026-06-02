package at.angular.processor.codegen

/**
 * What a given Angular major version can do, as far as AngularKt codegen cares.
 *
 * The single source of truth for version-conditional emission: the codegen strategies
 * branch on these capability flags, never on raw version numbers scattered through the
 * emitters. Supporting a new Angular release means adjusting one tier in [of] — not
 * hunting `if (version >= N)` across the templates.
 *
 * Fed by the `angularKt.angularVersion` Gradle property; both jit and aot
 * codegens receive a profile from [CodegenFactory].
 */
data class AngularProfile(
    val major: Int,
    /**
     * The `standalone:` field exists on the @Component/@Directive/@Pipe decorators
     * (Angular 14+). On older versions the field is unknown and must not be emitted —
     * standalone components simply don't exist there.
     */
    val supportsStandalone: Boolean,
) {
    companion object {
        /** Latest stable Angular at time of writing (June 2026). Used when the option is unset. */
        const val DEFAULT_MAJOR = 22

        /**
         * Lowest Angular major AngularKt supports. Chosen at 16 because signals,
         * `DestroyRef`, and standalone all exist there — so the lib's entire `external`
         * surface is satisfiable on every supported version with NO per-version source
         * sets / runtime feature-detection. Older majors are EOL and out of scope.
         */
        const val SUPPORTED_FLOOR = 16

        fun of(major: Int): AngularProfile = AngularProfile(
            major = major,
            // Always true on supported versions (floor is 14+); kept as a capability
            // for codegen clarity and the (unsupported) sub-floor diagnostic path.
            supportsStandalone = major >= 14,
            // Future capabilities slot in here as flags, e.g. zoneless = major >= 18.
        )

        /**
         * Parses the `angularKt.angularVersion` option into a profile. Accepts a bare
         * major ("19"), a full version ("19.2.0"), or an npm range ("~19.0.0"); falls
         * back to [DEFAULT_MAJOR] when absent or unparseable.
         */
        fun parse(raw: String?): AngularProfile {
            val major = raw
                ?.trimStart('~', '^', '=', 'v', ' ')
                ?.substringBefore('.')
                ?.toIntOrNull()
                ?: DEFAULT_MAJOR
            return of(major)
        }
    }
}
