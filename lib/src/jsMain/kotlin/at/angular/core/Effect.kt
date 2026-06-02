@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.dev/api/core/EffectRef
 */
external interface EffectRef {
    fun destroy()
}

/**
 * @see: https://angular.dev/api/core/effect
 */
external fun effect(effectFn: () -> Unit): EffectRef
