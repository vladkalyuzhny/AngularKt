@file:OptIn(ExperimentalJsExport::class)

package app.examples.ktor

import at.angular.core.Injectable
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class Todo(
    val id: Int,
    val title: String,
    val completed: Boolean
)

/**
 * HTTP via Ktor instead of Angular's HttpClient — idiomatic Kotlin: a `suspend`
 * function, coroutines, and kotlinx.serialization for typed JSON. No RxJS.
 * Injected like any other Angular service. Pairs with `HttpTodoService`, which
 * fetches the same data through Angular's own HttpClient.
 */
@JsExport
@Injectable(providedIn = "root")
class KtorTodoService {

    private val client = HttpClient(Js)
    private val json = Json { ignoreUnknownKeys = true }

    // `internal` so @JsExport doesn't try to export a suspend fun (unsupported);
    // KtorComponent (same module) still calls it. The class itself is exported so
    // the AOT .ts bridge can extend it as a DI token.
    internal suspend fun fetchTodo(id: Int): Todo {
        val body = client.get("https://jsonplaceholder.typicode.com/todos/$id").bodyAsText()
        return json.decodeFromString(Todo.serializer(), body)
    }

    /** Fetches the first [limit] todos as a typed list (used by the to-do card). */
    internal suspend fun fetchTodos(limit: Int): List<Todo> {
        val body = client.get("https://jsonplaceholder.typicode.com/todos?_limit=$limit").bodyAsText()
        return json.decodeFromString(ListSerializer(Todo.serializer()), body)
    }
}
