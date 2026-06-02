@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.dev/api/core/EventEmitter
 */
external class EventEmitter<T> {
    fun emit(value: T)
    fun subscribe(next: (T) -> Unit): dynamic
}
