@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.dev/api/core/TemplateRef
 */
external class TemplateRef<C> {
    val elementRef: dynamic
}

/**
 * @see: https://angular.dev/api/core/ViewContainerRef
 */
external class ViewContainerRef {
    fun createEmbeddedView(templateRef: TemplateRef<*>): dynamic
    fun clear()
}

/**
 * @see: https://angular.dev/api/core/QueryList
 */
external class QueryList<T> {
    val length: Int
    val first: T
    val last: T
    fun toArray(): Array<T>
    val changes: dynamic
}
