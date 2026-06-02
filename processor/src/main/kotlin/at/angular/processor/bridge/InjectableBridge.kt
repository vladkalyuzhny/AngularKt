package at.angular.processor.bridge

import at.angular.processor.bridge.references.NgReferences
import at.angular.processor.bridge.references.TsExpression
import at.angular.processor.bridge.references.TsRefResolver
import at.angular.processor.models.InjectableModel

class InjectableBridge(
    model: InjectableModel,
    resolver: TsRefResolver,
) : DecoratedBridge<InjectableModel>(model, resolver) {
    override val decorator get() = NgReferences.Injectable

    override fun metadata() = listOfNotNull(
        field("providedIn", model.providedIn?.let(TsExpression::String)),
    )
}
