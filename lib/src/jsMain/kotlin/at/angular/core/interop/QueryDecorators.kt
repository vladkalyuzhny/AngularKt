@file:JsModule("@angular/core")
@file:Suppress("FunctionName")

package at.angular.core.interop

/**
 * @see: https://angular.dev/api/core/ViewChild
 */
external fun ViewChild(selector: Any, opts: dynamic = definedExternally): dynamic

/**
 * @see: https://angular.dev/api/core/ViewChildren
 */
external fun ViewChildren(selector: Any, opts: dynamic = definedExternally): dynamic

/**
 * @see: https://angular.dev/api/core/ContentChild
 */
external fun ContentChild(selector: Any, opts: dynamic = definedExternally): dynamic

/**
 * @see: https://angular.dev/api/core/ContentChildren
 */
external fun ContentChildren(selector: Any, opts: dynamic = definedExternally): dynamic
