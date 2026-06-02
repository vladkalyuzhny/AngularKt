package at.angular.processor.models

import com.google.devtools.ksp.symbol.KSFile

data class RouteEntry(
    val path: String,
    val componentFqn: String?,
    val redirectTo: String?,
    val pathMatch: String?,
    val children: List<RouteEntry> = emptyList(),
    /** The lazy-loaded target (`loadChildren`), or null when this route loads eagerly. */
    val loadChildren: LazyRoutesTarget? = null,
)

/**
 * [isRoutesBundle] is true when [fqn] points at a `@RoutingModule` (a standalone-lazy `Routes` bundle —
 * `() => import(…).then(m => m.X)` where `X` is a `Routes` const), and false for a classic `@NgModule`
 * (the `RouterModule.forChild` lazy form). The two shapes resolve to different runtime values, so codegen
 * emits different `route()` args for each.
 */
data class LazyRoutesTarget(
    val fqn: String,
    val isRoutesBundle: Boolean,
)

/**
 * `@RoutingModule` — a route set. Emitted as the root (AOT: `provideRouter(routes)`; JIT:
 * `RouterModule.forRoot`), a lazy feature module's `RouterModule.forChild` ([forChild]), or a
 * standalone-lazy `Routes` bundle ([lazy]) when reached via another route's `loadChildren`.
 */
data class RoutingModuleModel(
    override val fqn: String,
    override val simpleName: String,
    override val containingFile: KSFile?,
    val routes: List<RouteEntry>,
    val forChild: Boolean = false,
    /**
     * This `@RoutingModule` is a standalone-lazy `Routes` bundle (a `loadChildren` target): emit
     * `export const Name: Routes = [...]` (AOT) / `registerLazyRoutes(...)` (JIT), not a root/forChild.
     */
    val lazy: Boolean = false,
    val useHash: Boolean = false,
    val preloadAllModules: Boolean = false,
) : NgDeclaration {
    override val ctorParams: List<CtorParam> get() = emptyList()
}
