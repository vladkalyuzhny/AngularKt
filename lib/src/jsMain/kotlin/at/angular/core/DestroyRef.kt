@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.dev/api/core/DestroyRef
 */
abstract external class DestroyRef {
    abstract fun onDestroy(callback: () -> Unit): () -> Unit
}
