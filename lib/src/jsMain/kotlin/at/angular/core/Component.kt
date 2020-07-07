@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.io/api/core/Component
 */
external interface Component : Directive {
    val templateUrl: String?
    val template: String?
    val styleUrls: Array<String>?
    val styles: Array<String>?
    // todo other fields
}

external fun Component(options: Component): (JsClass<*>) -> JsClass<*>