package at.angular.processor.bridge.references

import at.angular.processor.literals.TsLiterals

/** The `key: value, key: value` body of an object, without the surrounding braces (e.g. a route row). */
fun renderEntries(entries: List<Pair<String, TsExpression>>): String =
    entries.joinToString(", ") { (key, value) -> "$key: ${value.render()}" }

/**
 * A tiny immutable model of the TypeScript *values* a decorator carries — selector strings, the
 * `imports`/`declarations` arrays, `inject(...)` constructor args, query objects, and so on. The
 * file shape around them (`@Decorator`, `export class …`) lives in the `.hbs` templates; this only
 * models the bits that contain symbols.
 */
sealed interface TsExpression {

    /** The source text of this expression. */
    fun render(): kotlin.String = when (this) {
        is String -> TsLiterals.stringLiteral(value)
        is Template -> TsLiterals.templateLiteral(value)
        is Raw -> text
        is Symbol -> ref.name
        is Array -> items.joinToString(", ", "[", "]") { it.render() }
        is Object -> "{ ${renderEntries(entries)} }"
        is Call -> "${callee.render()}(${args.joinToString(", ") { it.render() }})"
        is New -> "new ${type.name}(${args.joinToString(", ") { it.render() }})"
        is LazyImport -> "() => import('./$name').then(m => m.$name)"
    }

    /** Every symbol this expression references, in source-appearance order. */
    fun refs(): List<TsReference> = when (this) {
        is Symbol -> listOf(ref)
        is New -> listOf(type) + args.flatMap { it.refs() }
        is Array -> items.flatMap { it.refs() }
        is Object -> entries.flatMap { it.second.refs() }
        is Call -> callee.refs() + args.flatMap { it.refs() }
        is String, is Template, is Raw, is LazyImport -> emptyList()
    }

    /** `'value'` — single-quoted string literal. */
    data class String(val value: kotlin.String) : TsExpression

    /** `` `value` `` — backtick template literal (Angular inline templates). */
    data class Template(val value: kotlin.String) : TsExpression

    /** Verbatim source — for non-symbol idents and keywords (`true`, `false`). */
    data class Raw(val text: kotlin.String) : TsExpression

    /** A reference to an imported symbol; renders as its name and pulls in its import. */
    data class Symbol(val ref: TsReference) : TsExpression

    /** `[a, b, …]`. */
    data class Array(val items: List<TsExpression>) : TsExpression

    /** `{ key: value, … }`; keys are written verbatim, so quote them in the key when needed. */
    data class Object(val entries: List<Pair<kotlin.String, TsExpression>>) : TsExpression

    /** `callee(arg, …)`. */
    data class Call(val callee: TsExpression, val args: List<TsExpression>) : TsExpression

    /** `new Type(arg, …)`. */
    data class New(val type: TsReference, val args: List<TsExpression>) : TsExpression

    /**
     * `() => import('./Name').then(m => m.Name)` — Angular's lazy `loadChildren`. Deliberately
     * carries no [refs] so it pulls in **no** static import; the dynamic `import()` is what lets
     * the Angular/esbuild toolchain split [name]'s bridge into its own chunk.
     */
    data class LazyImport(val name: kotlin.String) : TsExpression
}
