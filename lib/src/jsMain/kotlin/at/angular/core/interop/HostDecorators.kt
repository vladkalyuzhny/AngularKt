@file:JsModule("@angular/core")
@file:Suppress("FunctionName")

package at.angular.core.interop

/**
 * @see: https://angular.dev/api/core/HostBinding
 */
external fun HostBinding(hostPropertyName: String = definedExternally): dynamic

/**
 * @see: https://angular.dev/api/core/HostListener
 */
external fun HostListener(eventName: String, args: Array<String> = definedExternally): dynamic
