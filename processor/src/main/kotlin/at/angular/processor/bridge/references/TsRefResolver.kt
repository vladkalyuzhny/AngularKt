package at.angular.processor.bridge.references

import at.angular.processor.models.NgDeclaration

/**
 * Turns a project FQN into the [TsReference] a bridge should reference — deciding between one of our
 * own sibling bridges and an `@angular` framework type ([NgReferences.externalFor]).
 */
class TsRefResolver(
    private val ownClasses: Set<String>,
    /** npm module the bridges import the Kotlin classes from. */
    private val tsModule: String,
    /**
     * Third-party `@file:JsModule` externals (FQN → npm module), discovered by the Gradle plugin
     * from the consumer's dependencies and passed in via KSP options. KSP can't read `@JsModule`
     * off a binary klib dependency, so this map is how those imports cross the module boundary.
     * AngularKt's own `@angular` bindings live in [NgReferences]; this covers everything else.
     */
    private val externalModules: Map<String, String> = emptyMap(),
) {
    /** The Kotlin-compiled class a bridge `extends`, imported aliased from [tsModule]. */
    fun impl(declaration: NgDeclaration): TsReference = TsReference.Impl(tsModule, declaration.simpleName)

    /** A sibling bridge, imported from its `./Name` module — e.g. a route's target component. */
    fun local(fqn: String): TsReference = TsReference.Local(fqn.substringAfterLast('.'))

    /**
     * A constructor DI token: our own bridge wins over a framework token (they are disjoint in
     * practice), and an unmappable type is a hard error — DI must resolve to something.
     */
    fun ctorToken(fqn: String, ownerFqn: String): TsReference {
        if (fqn in ownClasses) return local(fqn)

        return external(fqn)
            ?: error("AngularKt AOT: no DI mapping for ctor param type '$fqn' on $ownerFqn")
    }

    /**
     * An `NgModule` member or standalone `imports` entry: a framework type wins, otherwise it is
     * treated as a bridge alongside (generated, or hand-authored like a routing module).
     */
    fun member(fqn: String): TsReference = external(fqn) ?: local(fqn)

    /** An external symbol's import: AngularKt's own framework catalog, then plugin-supplied externals. */
    private fun external(fqn: String): TsReference.External? {
        val catalog = NgReferences.externalFor(fqn)
        if (catalog != null) return catalog

        val module = externalModules[fqn]
        if (module != null) {
            return TsReference.External(module, fqn.substringAfterLast('.'))
        }

        return null
    }
}
