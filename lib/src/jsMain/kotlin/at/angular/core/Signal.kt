@file:JsModule("@angular/core")

package at.angular.core

/**
 * An Angular signal: a callable reactive value, read as `signal()` from Kotlin (via the
 * [invoke] extension) or `{{ signal() }}` from a template.
 *
 * Kotlin call sites that read a signal must `import at.angular.core.invoke`. The read is an
 * extension rather than a member because an Angular signal is a bare JS function — a member
 * `invoke` would emit `signal.invoke()`, a method the function value does not have.
 *
 * @see: https://angular.dev/api/core/Signal
 */
external interface Signal<T>

/**
 * @see: https://angular.dev/api/core/WritableSignal
 */
external interface WritableSignal<T> : Signal<T> {
    fun set(value: T)
    fun update(updateFn: (T) -> T)
}

/**
 * @see: https://angular.dev/api/core/signal
 */
external fun <T> signal(initialValue: T): WritableSignal<T>

/**
 * @see: https://angular.dev/api/core/computed
 */
external fun <T> computed(computation: () -> T): Signal<T>
