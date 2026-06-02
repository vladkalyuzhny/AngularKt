package at.angular.flow

import at.angular.core.Signal
import at.angular.core.WritableSignal
import at.angular.core.signal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Bridges a Kotlin [Flow] to an Angular signal (Flow → signal).
 *
 * Creates a real Angular signal — callable from a template as `{{ sig() }}` —
 * seeded with [initial], then pushes each emission into it via
 * [WritableSignal.set], so the template re-renders through Angular's
 * signal-aware change detection. The signal shows [initial] until the first
 * emission. The collector lives on [scope] (typically a [LifecycleScope]), so it
 * is cancelled with the component.
 *
 * Returns a read-only [Signal] — the value is owned by the [Flow], so callers
 * read it by *calling* it, `sig()` (or `{{ sig() }}` in the template), but cannot
 * `set`/`update` it. Mirrors Angular's own `toSignal()`, which also yields a
 * read-only signal rather than a [WritableSignal].
 */
fun <T> Flow<T>.asSignal(scope: CoroutineScope, initial: T): Signal<T> {
    val signal: WritableSignal<T> = signal(initial)
    scope.launch { collect { signal.set(it) } }
    return signal
}

/**
 * Bridges a Kotlin [StateFlow] to an Angular signal (StateFlow → signal).
 *
 * Convenience overload: delegates to the [Flow] overload, passing the flow's
 * current `value` as `initial`. Otherwise behaves identically.
 */
fun <T> StateFlow<T>.asSignal(scope: CoroutineScope): Signal<T> {
    return asSignal(scope, value)
}
