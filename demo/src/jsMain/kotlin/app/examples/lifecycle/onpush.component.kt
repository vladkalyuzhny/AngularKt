@file:OptIn(ExperimentalJsExport::class)

package app.examples.lifecycle

import at.angular.core.ChangeDetectionStrategy
import at.angular.core.Component
import at.angular.core.Input
import at.angular.core.OnChanges
import at.angular.core.SimpleChanges
import at.angular.core.ViewEncapsulation
import at.angular.core.get

/**
 * One child that exercises all three knobs at once:
 *  - `changeDetection = OnPush` — Angular checks this view only when an `@Input` reference changes
 *    (or an event fires inside it), not on every parent tick.
 *  - `encapsulation = None` — the component's styles are global, no per-view scoping.
 *  - `OnChanges` — `ngOnChanges` runs on every `@Input` change, the declarative alternative to a
 *    property setter (the "workaround" for reacting to an input). [SimpleChanges.get] reads the delta.
 */
@JsExport
@Component(
    selector = "app-onpush",
    templateUrl = "./onpush.component.html",
    styleUrls = ["./onpush.component.css"],
    changeDetection = ChangeDetectionStrategy.OnPush,
    encapsulation = ViewEncapsulation.None,
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
