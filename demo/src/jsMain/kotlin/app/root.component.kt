@file:OptIn(ExperimentalJsExport::class)

package app

import at.angular.core.Component

/**
 * The standalone root for the AOT build's `bootstrapApplication`. `AppComponent` (selector `app-root`)
 * is declared in [AppModule], so it isn't standalone and can't be handed to `bootstrapApplication`
 * directly; this shell imports the module (resolving `<app-root>` and everything AppModule exports) and
 * renders it, bridging the standalone bootstrap to the module-based app. `angularKt { bootstrapComponent }`
 * names it so the generated `main.ts` bootstraps it.
 */
@JsExport
@Component(
    selector = "root",
    standalone = true,
    imports = [AppModule::class],
    template = "<app-root></app-root>",
)
class RootComponent
