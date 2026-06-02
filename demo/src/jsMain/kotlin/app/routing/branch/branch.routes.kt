package app.routing.branch

import app.routing.leaf.LeafComponent
import at.angular.router.Route
import at.angular.router.RoutingModule

/**
 * The nested `children` of the `branch` route. Referenced from AppRoutingModule via
 * `Route(children = BranchRoutes::class)`; the processor inlines these into the parent route's
 * `children` array. Like a tree: the `branch` shell holds a `leaf`.
 * `/branch` redirects to `/branch/leaf`.
 */
@RoutingModule(
    routes = [
        Route(path = "", redirectTo = "leaf", pathMatch = "full"),
        Route(path = "leaf", component = LeafComponent::class),
    ]
)
class BranchRoutes
