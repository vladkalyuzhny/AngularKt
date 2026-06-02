@file:OptIn(ExperimentalJsExport::class)

package app.examples.signal

import at.angular.core.Component
import at.angular.core.computed
import at.angular.core.effect
import at.angular.core.invoke
import at.angular.flow.LifecycleScope
import at.angular.flow.asSignal
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A tap-to-count counter. The value is a Kotlin [MutableStateFlow] exposed to the
 * template as an Angular signal via `asSignal()`, so the template reads `count()`
 * (a real signal call). The +/- buttons drive the StateFlow and the view
 * re-renders through Angular's signal-aware change detection.
 */
@JsExport
@Component(
    selector = "app-signal",
    templateUrl = "./signal.component.html",
    styleUrls = ["./signal.component.css"]
)
class SignalComponent {

    private val lifecycle = LifecycleScope()
    private val state = MutableStateFlow(0)

    // `count` is a callable Angular signal the template invokes as `count()`.
    val count = state.asSignal(lifecycle)

    // Derived + memoized: `computed` recomputes only when `count()` actually changes,
    // unlike a plain getter that re-runs on every change-detection pass.
    val doubled = computed { count() * 2 }

    init {
        // Reactive side effect, re-runs on each `count()` change. Registered from the
        // constructor so it sits in Angular's injection context and is torn down with
        // the component.
        effect { println("[signal] count=${count()} doubled=${doubled()}") }
    }

    fun inc() {
        state.value += 1
    }

    fun dec() {
        state.value -= 1
    }
}
