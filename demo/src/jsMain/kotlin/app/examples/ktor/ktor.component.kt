@file:OptIn(ExperimentalJsExport::class)

package app.examples.ktor

import at.angular.core.Component
import at.angular.core.OnInit
import at.angular.flow.LifecycleScope
import kotlinx.coroutines.launch

/**
 * One row in the to-do list. `@JsExport` keeps `title`/`done` un-mangled so the
 * template can read them (`{{t.title}}`, `[class.done]="t.done"`).
 */
@JsExport
class TaskItem(val title: String, var done: Boolean)

/**
 * A small to-do list. The initial items are fetched from the web with Ktor (a
 * `suspend` call + kotlinx.serialization), then the user can tick items off and
 * add their own — all live, with change detection repainting on each tap.
 */
@JsExport
@Component(
    selector = "app-ktor",
    templateUrl = "./ktor.component.html",
    styleUrls = ["./ktor.component.css"]
)
class KtorComponent(private val todos: KtorTodoService) : OnInit {

    private val lifecycle = LifecycleScope()

    var loading = true
    var items: Array<TaskItem> = arrayOf()

    override fun ngOnInit() {
        lifecycle.launch {
            val fetched = todos.fetchTodos(4)
            items = fetched
                .map { TaskItem(it.title.replaceFirstChar { c -> c.uppercase() }, it.completed) }
                .toTypedArray()
            loading = false
        }
    }

    fun toggle(item: TaskItem) {
        item.done = !item.done
    }

    fun add(title: String) {
        val text = title.trim()
        if (text.isEmpty()) return
        items += TaskItem(text, false)
    }
}
