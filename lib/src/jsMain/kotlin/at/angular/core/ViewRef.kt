@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.dev/api/core/ViewRef
 */
abstract external class ViewRef : ChangeDetectorRef {
    abstract val destroyed: Boolean
    abstract fun destroy()
    abstract fun onDestroy(callback: () -> Unit): Any
}