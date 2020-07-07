@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.io/api/core/Directive
 */
external interface Directive {
    val selector: String?
}

external fun Directive(options: Directive): (JsClass<*>) -> JsClass<*>

/**
 * @see: https://angular.io/api/core/OnInit
 */
external interface OnInit {
    fun ngOnInit()
}