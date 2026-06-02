package at.angular.processor.bridge

import at.angular.processor.bridge.references.TsReference

/** One rendered `import { names } from 'module'` row (the `.hbs` reads [module] and [names]). */
data class Import(val module: String, val names: String)

/**
 * Groups referenced symbols into import rows by module — modules in first-reference order, distinct
 * specifiers within each in first-seen order. Pass the refs in the order they appear in the bridge,
 * and the import block falls out; nothing is registered by hand.
 */
fun importsFrom(refs: List<TsReference>): List<Import> =
    refs.groupBy { it.module }.map { (module, group) ->
        Import(module, group.map { it.importSpecifier }.distinct().joinToString(", "))
    }

/** One `key: value` entry inside a decorator's metadata object; [value] is pre-rendered TS. */
data class MetaEntry(val key: String, val value: String)

/** One `{ row }` entry in the `routes` array; [row] is the pre-rendered field list. */
data class Route(val row: String)

/**
 * The typed model a bridge hands its `.hbs` template. Each variant names the [templateName] that
 * draws it and exposes exactly the fields that template reads through their getters, so the
 * Kotlin↔template contract is checked by the compiler instead of by matching map keys. Values are
 * pre-rendered (see [at.angular.processor.bridge.references.TsExpression.render]) and the import
 * block is pre-derived (see [importsFrom]); the template only lays out the file shape.
 */
sealed interface BridgeView {
    val templateName: String

    /** The `@Decorator({ … }) export class Name [extends Impl] [{ ctor }]` shape. */
    data class DecoratedClass(
        val imports: List<Import>,
        val decorator: String,
        val name: String,
        val meta: List<MetaEntry>,
        val impl: String?,
        val hasCtor: Boolean,
        val ctorArgs: String?,
    ) : BridgeView {
        override val templateName: String get() = "decorated-class.ts"
    }

    /**
     * The root router (`provideRouter(routes)`), a lazy feature module's `RouterModule.forChild(routes)`
     * when [forChild], or a bare `export const Name: Routes = [...]` standalone-lazy bundle when [lazy].
     */
    data class RoutingModule(
        val imports: List<Import>,
        val name: String,
        val routes: List<Route>,
        val forChild: Boolean,
        val lazy: Boolean,
        val useHash: Boolean,
        val preloadAllModules: Boolean,
    ) : BridgeView {
        override val templateName: String get() = "routing-module.ts"
    }
}
