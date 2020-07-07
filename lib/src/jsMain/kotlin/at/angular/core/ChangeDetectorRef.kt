@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.io/api/core/ChangeDetectorRef
 */
abstract external class ChangeDetectorRef {
    abstract fun markForCheck()
    abstract fun detach()
    abstract fun detectChanges()
    abstract fun checkNoChanges()
    abstract fun reattach()
}