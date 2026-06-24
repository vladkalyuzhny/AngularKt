@file:OptIn(ExperimentalJsExport::class)

package app.examples.routing.leaf

import at.angular.core.Component

/** The leaf of the tree: a child route rendered inside the branch shell's nested outlet (`/branch/leaf`). */
@JsExport
@Component(
    selector = "app-leaf",
    templateUrl = "./leaf.component.html"
)
class LeafComponent
