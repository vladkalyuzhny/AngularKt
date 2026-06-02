@file:OptIn(ExperimentalJsExport::class)

package app.examples.viewchild

import at.angular.core.Component
import at.angular.core.ElementRef
import at.angular.core.ViewChild

/**
 * Live demo for the "ViewChild" feature: [box] is populated with the `#box` input's
 * ElementRef, and [read] reads its live DOM value on click.
 */
@JsExport
@Component(
    selector = "app-viewchild",
    templateUrl = "./viewchild-demo.component.html",
    styleUrls = ["./viewchild-demo.component.css"]
)
class ViewChildDemoComponent {

    @ViewChild("box")
    var box: ElementRef? = null

    var value: String = ""

    fun read() {
        value = (box?.nativeElement?.value as? String) ?: ""
    }
}
