package at.angular.processor.bridge

import at.angular.processor.bridge.references.NgReferences
import at.angular.processor.bridge.references.TsExpression
import at.angular.processor.bridge.references.TsRefResolver
import at.angular.processor.codegen.AngularProfile
import at.angular.processor.models.PipeModel

class PipeBridge(
    model: PipeModel,
    resolver: TsRefResolver,
    private val profile: AngularProfile,
) : DecoratedBridge<PipeModel>(model, resolver) {
    override val decorator get() = NgReferences.Pipe

    override fun metadata() = listOfNotNull(
        "name" to TsExpression.String(model.name ?: model.simpleName.replaceFirstChar { it.lowercase() }),
        field("standalone", if (profile.supportsStandalone) bool(model.standalone) else null),
        // Angular pipes are pure by default; only emit `pure: false` when impure.
        field("pure", if (model.pure == false) bool(false) else null),
    )
}
