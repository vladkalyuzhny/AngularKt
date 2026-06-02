package at.angular.processor.readers

import at.angular.core.Component
import at.angular.core.Directive
import at.angular.core.Injectable
import at.angular.core.NgModule
import at.angular.core.Pipe
import at.angular.processor.models.AngularModel
import at.angular.processor.models.RoutingModuleModel
import at.angular.processor.utils.annotation
import at.angular.processor.utils.childrenFqns
import at.angular.processor.utils.classArrayArg
import at.angular.processor.utils.classesWith
import at.angular.processor.utils.fqn
import at.angular.processor.utils.loadChildrenFqns
import at.angular.router.RoutingModule
import com.google.devtools.ksp.processing.Resolver

class AngularReader {
    private val componentReader = ComponentReader()
    private val pipeReader = PipeReader()
    private val injectableReader = InjectableReader()
    private val ngModuleReader = NgModuleReader()
    private val routingModuleReader = RoutingModuleReader()

    fun read(resolver: Resolver): AngularModel =
        AngularModel(
            injectables = resolver.classesWith(Injectable::class.qualifiedName)
                .map { injectableReader.read(it) },
            components = resolver.classesWith(Component::class.qualifiedName)
                .map { componentReader.read(it, isComponent = true) },
            directives = resolver.classesWith(Directive::class.qualifiedName)
                .map { componentReader.read(it, isComponent = false) },
            pipes = resolver.classesWith(Pipe::class.qualifiedName)
                .map { pipeReader.read(it) },
            routingModules = readEmittedRoutingModules(resolver),
            modules = resolver.classesWith(NgModule::class.qualifiedName)
                .map { ngModuleReader.read(it) },
        )

    /**
     * The `@RoutingModule`s that produce codegen output, with each one's role *derived from how it is
     * wired* (never a flag):
     * - referenced as a route's `children` → inlined into its parent (read recursively), no own output;
     * - reached via a route's `loadChildren` → a standalone-lazy `Routes` bundle (`lazy`);
     * - listed in a feature (non-bootstrap) `@NgModule`'s `imports` → `RouterModule.forChild` (`forChild`);
     * - none of the above → the root (`provideRouter`/`forRoot`).
     *
     * The root may itself be imported by the *bootstrap* `@NgModule` (JIT's `AppModuleClassic`), so only
     * non-bootstrap modules contribute `forChild` — that's what separates a feature module from the root.
     */
    private fun readEmittedRoutingModules(resolver: Resolver): List<RoutingModuleModel> {
        val routingClasses = resolver.classesWith(RoutingModule::class.qualifiedName)
        val routingFqns = routingClasses.mapTo(mutableSetOf()) { it.fqn() }
        val inlinedFqns = routingClasses.flatMapTo(mutableSetOf()) {
            it.annotation(RoutingModule::class.qualifiedName).childrenFqns()
        }
        val lazyRoutesFqns = routingClasses
            .flatMapTo(mutableSetOf()) { it.annotation(RoutingModule::class.qualifiedName).loadChildrenFqns() }
            .filterTo(mutableSetOf()) { it in routingFqns }
        // A @RoutingModule imported by a feature (non-bootstrap) @NgModule is a forChild feature module.
        val forChildFqns = resolver.classesWith(NgModule::class.qualifiedName)
            .filter { it.annotation(NgModule::class.qualifiedName).classArrayArg("bootstrap").isEmpty() }
            .flatMapTo(mutableSetOf()) { it.annotation(NgModule::class.qualifiedName).classArrayArg("imports") }
            .filterTo(mutableSetOf()) { it in routingFqns }
        return routingClasses
            .filter { it.fqn() !in inlinedFqns }
            .map {
                routingModuleReader.read(
                    declaration = it,
                    lazy = it.fqn() in lazyRoutesFqns,
                    forChild = it.fqn() in forChildFqns,
                )
            }
    }
}
