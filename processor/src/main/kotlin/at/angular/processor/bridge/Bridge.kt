package at.angular.processor.bridge

import at.angular.processor.bridge.references.TsRefResolver
import at.angular.processor.codegen.AngularProfile
import at.angular.processor.models.ComponentModel
import at.angular.processor.models.InjectableModel
import at.angular.processor.models.NgDeclaration
import at.angular.processor.models.NgModuleModel
import at.angular.processor.models.PipeModel
import at.angular.processor.models.RoutingModuleModel

/**
 * Builds the [BridgeView] for one parsed declaration — the typed model its `.hbs` template draws.
 * Each implementation owns one declarable's metadata; the file shape lives in the template. Adding
 * a declarable means adding a `Bridge` and a line to [bridgeFor].
 */
interface Bridge {
    fun createView(): BridgeView
}

/** Selects the bridge for a parsed declaration. */
fun bridgeFor(
    declaration: NgDeclaration,
    resolver: TsRefResolver,
    profile: AngularProfile
): Bridge =
    when (declaration) {
        is ComponentModel -> ComponentBridge(declaration, resolver, profile)
        is PipeModel -> PipeBridge(declaration, resolver, profile)
        is InjectableModel -> InjectableBridge(declaration, resolver)
        is NgModuleModel -> NgModuleBridge(declaration, resolver)
        is RoutingModuleModel -> RoutingModuleBridge(declaration, resolver)
    }
