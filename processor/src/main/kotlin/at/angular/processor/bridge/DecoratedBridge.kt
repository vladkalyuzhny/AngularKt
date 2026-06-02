package at.angular.processor.bridge

import at.angular.processor.bridge.references.NgReferences
import at.angular.processor.bridge.references.TsExpression
import at.angular.processor.bridge.references.TsRefResolver
import at.angular.processor.bridge.references.TsReference
import at.angular.processor.models.CtorParam
import at.angular.processor.models.NgDeclaration

/**
 * Template Method for the four declarables that compile to a decorated class. The shared shape —
 * `@Decorator({ … }) export class Name extends Impl { super(inject(...)) }` — is assembled once
 * here; a subclass supplies only the [decorator] and its [metadata]. Constructor DI is derived from
 * the declaration's params, so subclasses never touch it.
 */
abstract class DecoratedBridge<M : NgDeclaration>(
    protected val model: M,
    protected val resolver: TsRefResolver,
) : Bridge {
    protected abstract val decorator: TsReference

    /** Whether the bridge extends the Kotlin impl and forwards constructor DI (modules do not). */
    protected open val extendsImpl: Boolean = true

    /** The decorator's `{ … }` fields, in emission order; build them with [field]. */
    protected abstract fun metadata(): List<Pair<String, TsExpression>>

    final override fun createView(): BridgeView {
        val meta = metadata()
        val impl = if (extendsImpl) resolver.impl(model) else null
        val ctor = if (extendsImpl) ctorArgs() else null
        // Imports follow the order symbols are first needed while assembling the class: the metadata
        // values, then the decorator, then the extended impl, then the constructor.
        val symbols = buildList {
            meta.forEach { addAll(it.second.refs()) }
            add(decorator)
            impl?.let { add(it) }
            ctor?.forEach { addAll(it.refs()) }
        }
        return BridgeView.DecoratedClass(
            imports = importsFrom(symbols),
            decorator = decorator.name,
            name = model.simpleName,
            meta = meta.map { (key, value) -> MetaEntry(key, value.render()) },
            impl = impl?.name,
            hasCtor = ctor != null,
            ctorArgs = ctor?.joinToString(", ") { it.render() },
        )
    }

    /** `[inject(token, { …flags }), …]`, or null when the declaration has no constructor params. */
    private fun ctorArgs(): List<TsExpression>? = model.ctorParams.ifEmpty { null }?.map { param ->
        val token = resolver.ctorToken(param.injectTokenFqn ?: param.typeFqn, model.fqn)
        val flags = injectFlags(param)
        TsExpression.Call(
            TsExpression.Symbol(NgReferences.inject),
            buildList {
                add(TsExpression.Symbol(token))
                if (flags.isNotEmpty()) add(TsExpression.Object(flags))
            },
        )
    }

    private fun injectFlags(param: CtorParam): List<Pair<String, TsExpression>> = buildList {
        if (param.optional) add("optional" to bool(true))
        if (param.self) add("self" to bool(true))
        if (param.skipSelf) add("skipSelf" to bool(true))
        if (param.host) add("host" to bool(true))
    }

    /** A `key: value` field, or null when [value] is absent so the field is dropped from the object. */
    protected fun field(key: String, value: TsExpression?): Pair<String, TsExpression>? = value?.let { key to it }

    /** A bare `true`/`false`. */
    protected fun bool(value: Boolean): TsExpression = TsExpression.Raw(value.toString())
}
