package at.angular.core

/**
 * Angular signals are plain callable JS functions, so the read compiles to a direct `signal()`
 * call through `dynamic`. A member `operator fun invoke()` on the external [Signal] would emit
 * `signal.invoke()`, a method the function value does not have; Kotlin/JS's `@nativeInvoke` does
 * this as a member but is deprecated in favor of exactly this extension.
 */
operator fun <T> Signal<T>.invoke(): T = asDynamic()()
