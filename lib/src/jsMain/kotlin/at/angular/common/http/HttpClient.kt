@file:JsModule("@angular/common/http")

package at.angular.common.http

import rxjs.Observable
import kotlin.js.definedExternally

/**
 * Responses are parsed JSON as plain JS objects (no kotlinx.serialization, unlike
 * Ktor) — type the result with an `external interface`, not a `@Serializable` class.
 *
 * @see https://angular.dev/api/common/http/HttpClient
 */
external class HttpClient {
    fun <T> get(url: String, options: dynamic = definedExternally): Observable<T>
    fun <T> post(url: String, body: dynamic = definedExternally, options: dynamic = definedExternally): Observable<T>
    fun <T> put(url: String, body: dynamic = definedExternally, options: dynamic = definedExternally): Observable<T>
    fun <T> delete(url: String, options: dynamic = definedExternally): Observable<T>
    fun <T> patch(url: String, body: dynamic = definedExternally, options: dynamic = definedExternally): Observable<T>
}

/**
 * @see https://angular.dev/api/common/http/HttpClientModule
 */
external class HttpClientModule
