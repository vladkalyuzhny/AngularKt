@file:OptIn(ExperimentalJsExport::class)

package app.routing.tree

import at.angular.core.Component

/** The root route (`path: ""`) — the trunk of the tree. Renders into the Router example's outlet. */
@JsExport
@Component(
    selector = "app-tree",
    templateUrl = "./tree.component.html"
)
class TreeComponent
