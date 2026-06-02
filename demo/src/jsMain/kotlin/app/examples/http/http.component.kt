@file:OptIn(ExperimentalJsExport::class)

package app.examples.http

import at.angular.core.Component
import at.angular.core.OnInit
import at.angular.flow.LifecycleScope
import kotlinx.coroutines.launch

/**
 * A "daily tip" card: each tap fetches a fresh item through Angular's HttpClient
 * (the opt-in alternative to Ktor). The Observable is awaited as a coroutine, so
 * the call site stays suspend-flavoured and change detection repaints when it
 * resolves — no manual nudge.
 */
@JsExport
@Component(
    selector = "app-http",
    templateUrl = "./http.component.html",
    styleUrls = ["./http.component.css"]
)
class HttpComponent(private val api: HttpTodoService) : OnInit {

    private val lifecycle = LifecycleScope()
    var tip = "…"
    var loading = false

    override fun ngOnInit() {
        another()
    }

    fun another() {
        if (loading) return
        loading = true
        val id = kotlin.random.Random.nextInt(1, 200)
        lifecycle.launch {
            val todo = api.fetch(id)
            tip = todo.title.replaceFirstChar { it.uppercase() }
            loading = false
        }
    }
}
