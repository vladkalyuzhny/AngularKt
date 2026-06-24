@file:OptIn(ExperimentalJsExport::class)

import app.appProviders
import app.startAppKoin
import at.angular.core.EnvironmentProviders
import at.angular.platformBrowser.bootstrapApplication
import at.angular.utils.jsObject

/**
 * AOT entry — standalone `bootstrapApplication`, the modern Angular idiom and today's `ng new`
 * default. The processor-generated `main.ts` is a thin shim: it imports the AOT-compiled
 * [app.RootComponent] bridge and the root `@RoutingModule`'s providers (`importProvidersFrom`), then hands
 * both to this `main`. Templates are already compiled, so `@angular/compiler` stays out of the bundle.
 */
@JsExport
fun main(root: JsClass<*>, providers: Array<EnvironmentProviders>) {
    // Bring up the Koin container before Angular renders (see the "Koin DI" example).
    startAppKoin()
    bootstrapApplication(
        rootComponent = root,
        options = jsObject {
            this.providers = appProviders() + providers
        }
    ).catch { e -> console.error("Bootstrap failed:", e) }
}
