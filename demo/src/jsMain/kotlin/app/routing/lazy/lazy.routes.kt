package app.routing.lazy

import at.angular.router.Route
import at.angular.router.RoutingModule

/**
 * A standalone-lazy `Routes` bundle — no `@NgModule`, no `forChild`. The root points at it directly
 * with `Route(loadChildren = LazyRoutes::class)` (app.routing.kt). Because the `loadChildren` target
 * is a `@RoutingModule` (not an `@NgModule`), the processor emits a bare `Routes` array and the parent
 * route's `loadChildren` resolves to it — Angular's `() => ROUTES` idiom.
 */
@RoutingModule(
    routes = [
        Route(path = "", component = LazyComponent::class),
    ]
)
class LazyRoutes
