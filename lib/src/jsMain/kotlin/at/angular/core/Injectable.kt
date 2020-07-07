@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.io/api/core/Injectable
 */
external interface Injectable {
    val providedIn: dynamic
}

external fun Injectable(options: Injectable): (JsClass<*>) -> JsClass<*>