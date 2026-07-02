@file:OptIn(ExperimentalJsExport::class)

package app.explore

import at.angular.core.Component
import at.angular.core.Input

/**
 * Presentational frame shared by every Examples page: it renders the heading, the description, the
 * TypeScript-vs-Kotlin code comparison from the bound [feature], and a live-demo card whose body is
 * projected in via `<ng-content>`. Each Examples route is its own tiny component that drops its live
 * demo inside `<app-example>` — keeping the per-page boilerplate to a single line.
 */
@JsExport
@Component(
    selector = "app-example",
    templateUrl = "./example-panel.component.html",
    styleUrls = ["./example-panel.component.css", "./explore-content.css"],
)
class ExamplePanelComponent {
    @Input
    var feature: Feature = EXAMPLES[0]

    /** Kotlin-native features (Koin, coroutines, Ktor) have no direct TypeScript/Angular equivalent —
     *  the TS column shows the closest workaround under a "not supported" warning. */
    val tsUnsupported: Boolean get() =
        feature.id == "koin" || feature.id == "coroutines" || feature.id == "ktor"
}
