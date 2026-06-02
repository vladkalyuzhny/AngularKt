@file:OptIn(ExperimentalJsExport::class)

package app.examples.io

import at.angular.core.Component
import at.angular.core.EventEmitter
import at.angular.core.Input
import at.angular.core.Output

/**
 * Phase: @Input/@Output proof. A child component that receives `name` from the
 * parent and emits `notify` back up — exactly like Angular TS.
 */
@JsExport
@Component(
    selector = "app-child",
    templateUrl = "./child.component.html",
    styleUrls = ["./child.component.css"]
)
class ChildComponent {

    // Aliased input: the Kotlin property stays `name`, but the template binding the
    // parent uses is `childName` (proves @Input(alias) wiring end-to-end).
    @Input(alias = "childName")
    var name: String = ""

    @Output
    val notify: EventEmitter<String> = EventEmitter()

    var waves = 0

    fun ping() {
        waves++
        notify.emit(if (waves == 1) "Nice to meet you! 😊" else "Waving back again! 👋")
    }
}
