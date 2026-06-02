@file:OptIn(ExperimentalJsExport::class)

package app.examples.coroutines

import at.angular.core.Component
import at.angular.core.OnInit
import at.angular.flow.LifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A working stopwatch. A coroutine scoped to the component lifecycle ticks every
 * 100 ms; start/pause/reset just flip plain Kotlin state and change detection
 * repaints. The scope auto-cancels via Angular's DestroyRef on destroy — no manual
 * ngOnDestroy, no RxJS in app code.
 */
@JsExport
@Component(
    selector = "app-ticker",
    templateUrl = "./ticker.component.html",
    styleUrls = ["./ticker.component.css"]
)
class TickerComponent : OnInit {

    // Elapsed time in tenths of a second.
    var elapsed = 0
    var running = false
    private val lifecycle = LifecycleScope()

    val display: String
        get() {
            val totalSeconds = elapsed / 10
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val tenths = elapsed % 10
            return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}.$tenths"
        }

    override fun ngOnInit() {
        lifecycle.launch {
            while (true) {
                delay(100)
                if (running) elapsed++
            }
        }
    }

    fun toggle() {
        running = !running
    }

    fun reset() {
        running = false
        elapsed = 0
    }
}
