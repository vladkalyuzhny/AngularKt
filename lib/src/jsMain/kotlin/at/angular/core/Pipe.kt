@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.io/api/core/Pipe
 */
external interface Pipe {
    val name: String
    val pure: Boolean?
}

external fun Pipe(options: Pipe): (JsClass<*>) -> JsClass<*>

/**
 * @see: https://angular.io/api/core/PipeTransform
 */
external interface PipeTransform {
    fun transform(value: Any?, vararg args: Any?): Any?
}