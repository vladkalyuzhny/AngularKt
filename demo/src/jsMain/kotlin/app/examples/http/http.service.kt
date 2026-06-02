@file:OptIn(ExperimentalJsExport::class)

package app.examples.http

import at.angular.common.http.HttpClient
import at.angular.core.Injectable
import at.angular.flow.await

/**
 * Plain-JS shape of the HttpClient JSON response. HttpClient parses JSON into a
 * raw JS object (no kotlinx.serialization), so the result is modelled with an
 * `external interface`, not the `@Serializable` `Todo` used by the Ktor path.
 */
external interface TodoResponse {
    val id: Int
    val title: String
    val completed: Boolean
}

/**
 * The same fetch as `KtorTodoService`, but through Angular's HttpClient instead of Ktor
 * — the opt-in legacy path (interceptors / HttpTestingController come for free).
 * HttpClient is constructor-injected; [at.angular.common.http.HttpClientModule] must be
 * imported in the @NgModule. `.await()` turns the one-shot Observable into a
 * suspend call so the call site stays coroutine-flavoured.
 */
@JsExport
@Injectable(providedIn = "root")
class HttpTodoService(private val http: HttpClient) {

    // `internal` for the same reason as KtorTodoService: @JsExport can't export a suspend fun.
    internal suspend fun fetch(id: Int): TodoResponse =
        http.get<TodoResponse>("https://jsonplaceholder.typicode.com/todos/$id").await()
}
