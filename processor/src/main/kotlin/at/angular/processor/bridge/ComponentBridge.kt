package at.angular.processor.bridge

import at.angular.processor.bridge.references.NgReferences
import at.angular.processor.bridge.references.TsExpression
import at.angular.processor.bridge.references.TsRefResolver
import at.angular.processor.codegen.AngularProfile
import at.angular.processor.literals.TsLiterals
import at.angular.processor.models.ComponentModel
import at.angular.processor.models.PropBindingModel

class ComponentBridge(
    model: ComponentModel,
    resolver: TsRefResolver,
    private val profile: AngularProfile,
) : DecoratedBridge<ComponentModel>(model, resolver) {
    override val decorator get() = NgReferences.decorator(model.isComponent)

    override fun metadata(): List<Pair<String, TsExpression>> {
        // A directive has no view — `view` is null for it, dropping the component-only fields.
        val view = model.takeIf { it.isComponent }
        return listOfNotNull(
            field("selector", model.selector?.let(TsExpression::String)),
            field("standalone", if (profile.supportsStandalone) bool(model.standalone) else null),
            field("imports", standaloneImports()),
            field("template", view?.template?.let(TsExpression::Template)),
            field("templateUrl", view?.templateUrl?.let(TsExpression::String)),
            field("styles", view?.let { stringArray(it.styles) }),
            field("styleUrls", view?.let { stringArray(it.styleUrls) }),
            field("inputs", stringArray(bindingNames(model.inputs))),
            field("outputs", stringArray(bindingNames(model.outputs))),
            // @Component-only enum knobs, emitted as the symbolic `ViewEncapsulation.None` /
            // `ChangeDetectionStrategy.OnPush` (importing just the enum); null view → directive → dropped.
            field("encapsulation", view?.encapsulation?.let { TsExpression.Symbol(NgReferences.encapsulation(it)) }),
            field("changeDetection", view?.changeDetection?.let { TsExpression.Symbol(NgReferences.changeDetection(it)) }),
            field("host", host()),
            field("queries", queries()),
        )
    }

    /** The standalone `imports: [...]`, or null when the component is module-declared. */
    private fun standaloneImports(): TsExpression? {
        if (!profile.supportsStandalone || !model.isComponent || !model.standalone || model.imports.isEmpty()) {
            return null
        }
        return TsExpression.Array(model.imports.map { TsExpression.Symbol(resolver.member(it)) })
    }

    /** `queries: { prop: new ViewChild('selector'[, { static: true }]), … }`, or null when none. */
    private fun queries(): TsExpression? {
        if (model.queries.isEmpty()) return null
        return TsExpression.Object(
            model.queries.map { query ->
                val args = buildList {
                    add(TsExpression.String(query.selector))
                    if (query.static) add(TsExpression.Object(listOf("static" to bool(true))))
                }
                query.property to TsExpression.New(NgReferences.queryClass(query.kind), args)
            },
        )
    }

    /** `host: { '[prop]': 'field', '(event)': 'handler()' }`, or null when nothing is bound. */
    private fun host(): TsExpression? {
        val entries = buildList {
            model.hostBindings.forEach { binding ->
                val target = binding.hostProperty ?: binding.property
                add(TsLiterals.stringLiteral("[$target]") to TsExpression.String(binding.property))
            }
            model.hostListeners.forEach { listener ->
                val handler = "${listener.method}(${listener.args.joinToString(", ")})"
                add(TsLiterals.stringLiteral("(${listener.event})") to TsExpression.String(handler))
            }
        }
        return if (entries.isEmpty()) null else TsExpression.Object(entries)
    }

    /** `['a', 'b', …]` of string literals, or null when empty so the field is omitted. */
    private fun stringArray(values: List<String>): TsExpression? =
        if (values.isEmpty()) null else TsExpression.Array(values.map(TsExpression::String))

    private fun bindingNames(bindings: List<PropBindingModel>): List<String> =
        bindings.map { if (it.alias != null) "${it.property}: ${it.alias}" else it.property }
}
