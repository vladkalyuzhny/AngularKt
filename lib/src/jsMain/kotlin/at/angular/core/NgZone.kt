@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.dev/api/core/NgZone
 */
@Suppress("SpellCheckingInspection")
external class NgZone(options: dynamic) {
    val isStable: Boolean
    val hasPendingMicrotasks: Boolean
    val hasPendingMacrotasks: Boolean
    fun <T> run(body: () -> T): T
    fun <T> runGuarded(body: () -> T): T
    fun <T> runOutsideAngular(body: () -> T): T

    companion object {
        fun isInAngularZone(): Boolean
        fun assertInAngularZone()
        fun assertNotInAngularZone()
    }
}