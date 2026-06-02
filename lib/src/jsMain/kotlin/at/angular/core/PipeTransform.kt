@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.dev/api/core/PipeTransform
 */
external interface PipeTransform {
    fun transform(value: Any?, vararg args: Any?): Any?
}
