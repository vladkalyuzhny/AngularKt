package at.angular.processor.models

/**
 * Everything parsed from the [com.google.devtools.ksp.processing.Resolver],
 * grouped by kind. Codegen emits declarables first, then routing modules, then
 * the regular modules that import them.
 */
class AngularModel(
    val injectables: List<InjectableModel>,
    val components: List<ComponentModel>,
    val directives: List<ComponentModel>,
    val pipes: List<PipeModel>,
    val routingModules: List<RoutingModuleModel>,
    val modules: List<NgModuleModel>,
) {
    val ngDeclarations: List<NgDeclaration>
        get() = injectables + components + directives + pipes + routingModules + modules

    val ownClasses: Set<String> by lazy { ngDeclarations.map { it.fqn }.toSet() }
}
