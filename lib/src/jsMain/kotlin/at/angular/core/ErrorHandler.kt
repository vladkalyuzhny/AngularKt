@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see https://angular.dev/api/core/ErrorHandler
 */
abstract external class ErrorHandler {
    fun handleError(error: Any)
}