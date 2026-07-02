@file:OptIn(ExperimentalJsExport::class)

package app.examples.lifecycle

import at.angular.core.ChangeDetectionStrategy
import at.angular.core.Component
import at.angular.core.Input
import at.angular.core.OnChanges
import at.angular.core.SimpleChanges
import at.angular.core.get

/**
 * The child of the Lifecycle & CD demo. `changeDetection = OnPush` re-checks this view only when its
 * `@Input` reference changes; `ngOnChanges` then fires — a lifecycle hook that reacts to an input
 * change (the declarative alternative to a property setter). [SimpleChanges.get] reads the delta.
 */
@JsExport
@Component(
    selector = "app-onpush",
    templateUrl = "./onpush.component.html",
    styleUrls = ["./onpush.component.css"],
    changeDetection = ChangeDetectionStrategy.OnPush,
)
class OnPushComponent : OnChanges {

    @Input
    var count: Int = 0

    var changes = 0
    var lastDelta: String = "—"

    override fun ngOnChanges(changes: SimpleChanges) {
        this.changes++
        lastDelta = changes["count"]
            ?.let { "${it.previousValue} → ${it.currentValue}" }
            ?: "—"
    }
}
