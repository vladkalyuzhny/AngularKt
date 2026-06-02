package at.angular.router

import kotlin.reflect.KClass

class NoComponent

/**
 * @see: https://angular.dev/api/router/Route
 */
@Retention(AnnotationRetention.SOURCE)
annotation class Route(
    val path: String = "",
    val component: KClass<*> = NoComponent::class,
    val redirectTo: String = "",
    val pathMatch: String = "",
    /**
     * Nested routes. Kotlin forbids an annotation from holding an array of itself, so the children
     * can't be inlined — point this at another `@RoutingModule`-annotated class, whose `routes` the
     * processor inlines recursively into this route's `children: [...]`
     */
    val children: KClass<*> = NoComponent::class,
    /**
     * Lazy-loaded target — either a Kotlin `@NgModule` (the classic form; the module imports a plain
     * `@RoutingModule`, which the processor infers as `RouterModule.forChild` from that placement) or a
     * `@RoutingModule` directly (the modern standalone form, a bare `Routes` bundle). Compiles to a
     * `loadChildren: () => import(…)`, so the route's target is loaded on demand.
     */
    val loadChildren: KClass<*> = NoComponent::class,
)
