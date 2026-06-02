@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see https://angular.dev/api/core/InjectOptions
 */
external interface InjectOptions {
    var optional: Boolean
    var self: Boolean
    var skipSelf: Boolean
    var host: Boolean
}

/**
 * @see https://angular.dev/api/core/inject
 */
external fun <T : Any> inject(token: JsClass<T>): T

external fun <T : Any> inject(token: JsClass<T>, options: InjectOptions): T?