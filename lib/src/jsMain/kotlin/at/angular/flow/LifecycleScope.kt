package at.angular.flow

import at.angular.core.ChangeDetectorRef
import at.angular.core.DestroyRef
import at.angular.core.ErrorHandler
import at.angular.core.inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A host-scoped [CoroutineScope]. Create it as a property of a component, directive,
 * pipe, or service; any `launch`/`collect` is cancelled automatically when that host is
 * destroyed — the Kotlin analog of Android's `viewModelScope`, with no manual
 * `ngOnDestroy` needed.
 *
 * Self-registers via Angular's [DestroyRef], so it must be constructed inside an
 * injection context (a constructor or field initializer of a DI-created class).
 *
 * In a view context (component/directive/pipe) coroutines run on a dispatcher that marks
 * the host for check after every resumption, so updates to component state (an `await`
 * returning, a Flow emission) repaint the view with no manual nudge. In a plain service
 * there is no view, so that step is skipped — see [ChangeDetectingDispatcher]. Uncaught
 * failures are routed through Angular's [ErrorHandler] when one is available.
 */
class LifecycleScope : CoroutineScope {
    // Optional: services have no view and therefore no ChangeDetectorRef.
    private val changeDetector = inject(ChangeDetectorRef::class.js, optional = true)
    private val errorHandler = inject(ErrorHandler::class.js, optional = true)

    private val dispatcher = ChangeDetectingDispatcher(changeDetector)

    override val coroutineContext = SupervisorJob() + dispatcher +
            (errorHandler?.let { handler -> CoroutineExceptionHandler { _, e -> handler.handleError(e) } }
                ?: EmptyCoroutineContext)

    init {
        inject(DestroyRef::class.js).onDestroy { dispose() }
    }

    /** Cancels the scope. Called automatically on host destroy; rarely needed manually. */
    fun dispose() {
        dispatcher.deactivate()
        cancel()
    }
}

/**
 * Dispatches continuations on [Dispatchers.Default], then marks the host for check.
 * Angular 21+ is zoneless by default and won't run change detection off a bare field
 * mutation — it needs an explicit notification. Doing it here, once, keeps app code free
 * of `markForCheck`/`NgZone.run`; under the zone-based CD of older versions it's a
 * harmless, cheap no-op. The notification is skipped when there is no view
 * ([changeDetector] is null) or once the host has been destroyed ([deactivate]).
 */
private class ChangeDetectingDispatcher(
    private val changeDetector: ChangeDetectorRef?,
) : CoroutineDispatcher() {
    private val delegate = Dispatchers.Default
    // JS is single-threaded, so a plain flag is enough — no atomicity concerns.
    private var isActive = true

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        delegate.dispatch(context, Runnable {
            block.run()
            if (isActive) changeDetector?.markForCheck()
        })
    }

    fun deactivate() {
        isActive = false
    }
}