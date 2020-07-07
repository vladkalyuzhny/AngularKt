@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.io/api/core/Output
 */
external interface Output {
    val bindingPropertyName: String?
}

external fun Output(options: Output): (JsClass<*>) -> JsClass<*>