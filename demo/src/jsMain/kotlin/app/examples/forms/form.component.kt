@file:OptIn(ExperimentalJsExport::class)

package app.examples.forms

import at.angular.core.Component
import at.angular.core.OnInit
import at.angular.flow.LifecycleScope
import at.angular.flow.asFlow
import at.angular.forms.FormControl
import kotlinx.coroutines.launch

/**
 * A live greeter. A reactive [FormControl]'s `valueChanges` (an RxJS Observable)
 * is consumed as a Kotlin Flow, so the greeting + character count update on every
 * keystroke — form reactivity expressed as coroutines, not RxJS, in app code.
 */
@JsExport
@Component(
    selector = "app-form",
    templateUrl = "./form.component.html",
    styleUrls = ["./form.component.css"]
)
class FormComponent : OnInit {

    val nameControl = FormControl("")
    var message = "Start typing above…"
    var count = 0
    private val lifecycle = LifecycleScope()

    override fun ngOnInit() {
        lifecycle.launch {
            nameControl.valueChanges.asFlow().collect { value ->
                val text = "$value"
                count = text.length
                message = if (text.isBlank()) "Start typing above…" else "Hello, $text! 👋"
            }
        }
    }

    fun clear() {
        nameControl.setValue("")
    }
}
