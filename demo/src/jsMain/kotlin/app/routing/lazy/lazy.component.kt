@file:OptIn(ExperimentalJsExport::class)

package app.routing.lazy

import at.angular.core.Component
import at.angular.core.OnInit

/**
 * A lazily-loaded **standalone** route component — the modern lazy form, no `@NgModule`. It carries
 * `standalone = true`; the root reaches it through a standalone-lazy `Routes` bundle ([LazyRoutes])
 * via `Route(loadChildren = …)`, which compiles to `loadChildren: () => import(…)` (no NgModule,
 * no `forChild`) and the router mounts it lazily. The `ngOnInit` log makes the moment of (lazy)
 * instantiation observable.
 */
@JsExport
@Component(
    selector = "app-lazy",
    standalone = true,
    template = "<p>Lazy route works — loaded with no NgModule.</p>",
)
class LazyComponent : OnInit {
    override fun ngOnInit() {
        println("[AngularKt] LazyComponent loaded (standalone-lazy Routes)")
    }
}
