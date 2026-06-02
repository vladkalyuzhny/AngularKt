@file:OptIn(ExperimentalJsExport::class)

package app.routing.branch

import at.angular.core.Component

/**
 * The `branch` route's component: a shell that renders nothing but a nested `<router-outlet>`, so
 * only its child — the `leaf` — shows a path. The processor inlines [BranchRoutes] into the branch
 * route's `children`; Angular instantiates this per activation.
 */
@JsExport
@Component(
    selector = "app-branch",
    templateUrl = "./branch.component.html"
)
class BranchComponent
