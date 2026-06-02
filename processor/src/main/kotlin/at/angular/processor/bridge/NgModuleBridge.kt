package at.angular.processor.bridge

import at.angular.processor.bridge.references.NgReferences
import at.angular.processor.bridge.references.TsExpression
import at.angular.processor.bridge.references.TsRefResolver
import at.angular.processor.models.NgModuleModel

class NgModuleBridge(
    model: NgModuleModel,
    resolver: TsRefResolver,
) : DecoratedBridge<NgModuleModel>(model, resolver) {
    override val decorator get() = NgReferences.NgModule
    override val extendsImpl get() = false

    override fun metadata() = listOfNotNull(
        field("declarations", symbolArray(model.declarations)),
        field("imports", symbolArray(model.imports)),
        field("exports", symbolArray(model.exports)),
        field("providers", symbolArray(model.providers)),
        field("bootstrap", symbolArray(model.bootstrap)),
    )

    /** A `[A, B, …]` array of the resolved member symbols, or null when the list is empty. */
    private fun symbolArray(fqns: List<String>): TsExpression? {
        if (fqns.isEmpty()) return null

        return TsExpression.Array(fqns.map { TsExpression.Symbol(resolver.member(it)) })
    }
}
