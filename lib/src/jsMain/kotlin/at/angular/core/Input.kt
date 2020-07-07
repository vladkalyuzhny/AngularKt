@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.io/api/core/Input
 */
external interface Input {
    val bindingPropertyName: String?
}

external fun Input(options: Input): (JsClass<*>) -> JsClass<*>