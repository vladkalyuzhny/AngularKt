@file:OptIn(ExperimentalJsExport::class)

package app.examples.lifecycle

import at.angular.core.Component

/**
 * The parent for the OnChanges / OnPush / ViewEncapsulation demo: it owns a counter and feeds it to
 * [OnPushComponent] as an `@Input`. Each bump changes the input, so the OnPush child is re-checked
 * and its `ngOnChanges` fires — proving the input plumbing drives the child's lifecycle.
 */
@JsExport
@Component(
    selector = "app-lifecycle",
    templateUrl = "./lifecycle.component.html",
)
class LifecycleComponent {
    var count = 0

    fun bump() {
        count++
    }
}
