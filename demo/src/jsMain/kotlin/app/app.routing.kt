package app

import app.examples.routing.branch.BranchComponent
import app.examples.routing.branch.BranchRoutes
import app.examples.routing.lazy.LazyRoutes
import app.examples.routing.tree.TreeComponent
import app.explore.pages.AotPageComponent
import app.explore.pages.CoroutinesPageComponent
import app.explore.pages.CustomizePageComponent
import app.explore.pages.DiPageComponent
import app.explore.pages.DirectivePageComponent
import app.explore.pages.FormsPageComponent
import app.explore.pages.HttpPageComponent
import app.explore.pages.IoPageComponent
import app.explore.pages.JitPageComponent
import app.explore.pages.KoinPageComponent
import app.explore.pages.KtorPageComponent
import app.explore.pages.LifecyclePageComponent
import app.explore.pages.PipePageComponent
import app.explore.pages.RouterPageComponent
import app.explore.pages.SanitizerPageComponent
import app.explore.pages.SetupPageComponent
import app.explore.pages.SignalPageComponent
import app.explore.pages.ViewChildPageComponent
import at.angular.router.Route
import at.angular.router.RoutingModule

/**
 * The root routes — one declarative `Route(path, component)` per left-nav entry, so every section is
 * a real URL (`/setup`, `/signal`, `/coroutines`, …). The pages render inside the explorer shell's
 * `<router-outlet>`; the shell's nav uses `routerLink`/`routerLinkActive`, so the address bar tracks
 * the open tab and deep links / back-forward work. Visiting `/` redirects to the Setup page.
 *
 * The "Router" example owns nested children ([RouterChildRoutes]) under its own path, so its tabs
 * navigate real, deep URLs (`/router`, `/router/branch/leaf`, `/router/lazy`).
 */
@RoutingModule(
    routes = [
        Route(path = "", redirectTo = "setup", pathMatch = "full"),

        // Get started
        Route(path = "setup", component = SetupPageComponent::class),
        Route(path = "jit", component = JitPageComponent::class),
        Route(path = "aot", component = AotPageComponent::class),
        Route(path = "customize", component = CustomizePageComponent::class),

        // Examples
        Route(path = "di", component = DiPageComponent::class),
        Route(path = "koin", component = KoinPageComponent::class),
        Route(path = "router", component = RouterPageComponent::class, children = RouterChildRoutes::class),
        Route(path = "signal", component = SignalPageComponent::class),
        Route(path = "forms", component = FormsPageComponent::class),
        Route(path = "viewchild", component = ViewChildPageComponent::class),
        Route(path = "io", component = IoPageComponent::class),
        Route(path = "lifecycle", component = LifecyclePageComponent::class),
        Route(path = "directive", component = DirectivePageComponent::class),
        Route(path = "pipe", component = PipePageComponent::class),
        Route(path = "sanitizer", component = SanitizerPageComponent::class),
        Route(path = "http", component = HttpPageComponent::class),
        Route(path = "coroutines", component = CoroutinesPageComponent::class),
        Route(path = "ktor", component = KtorPageComponent::class),
    ]
)
class AppRoutingModule

/**
 * The nested routes shown inside the "Router" example's own outlet. Inlined into the `/router` route's
 * `children` by the processor. Like a tree: the root (`/router`) is the trunk, the `branch` shell
 * holds a `leaf` (`/router/branch/leaf`), and `lazy` is a standalone-lazy bundle.
 */
@RoutingModule(
    routes = [
        Route(path = "", component = TreeComponent::class),
        // Nested children: the `branch` shell holds a `leaf`. The processor inlines BranchRoutes
        // recursively into this route's `children`.
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
class RouterChildRoutes
