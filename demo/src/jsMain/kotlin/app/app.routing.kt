package app

import app.routing.branch.BranchComponent
import app.routing.branch.BranchRoutes
import app.routing.lazy.LazyRoutes
import app.routing.tree.TreeComponent
import at.angular.router.Route
import at.angular.router.RoutingModule

@RoutingModule(
    routes = [
        Route(path = "", component = TreeComponent::class),
        // Nested children, like a tree: the `branch` shell holds a `leaf`. The processor inlines
        // BranchRoutes recursively into this route's `children`.
        Route(
            path = "branch",
            component = BranchComponent::class,
            children = BranchRoutes::class,
        ),
        // The one lazy example: a modern standalone `Routes` bundle — no @NgModule, no forChild.
        // Compiles to `loadChildren: () => import('./LazyRoutes').then(m => m.LazyRoutes)`.
        Route(path = "lazy", loadChildren = LazyRoutes::class),
    ]
)
class AppRoutingModule
