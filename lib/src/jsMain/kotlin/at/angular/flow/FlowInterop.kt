package at.angular.flow

import rxjs.Observable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first

/**
 * Bridges an RxJS [Observable] (what Angular's Router, Forms, and HttpClient hand
 * back) into a Kotlin [Flow], so app code can use coroutines/Flow operators instead
 * of RxJS. `next` values are emitted, a source `error` is rethrown into the flow, and
 * `complete` ends it. The subscription is disposed when the flow stops — whether the
 * collector is cancelled or the source terminates.
 */
fun <T> Observable<T>.asFlow(): Flow<T> = callbackFlow {
    val observer: dynamic = object {}
    observer.next = { value: T -> trySend(value); Unit }
    observer.error = { e: Throwable -> close(e); Unit }
    observer.complete = { close(); Unit }
    val subscription = subscribe(observer)
    awaitClose { subscription.unsubscribe() }
}

/**
 * Awaits the first emission of an [Observable] as a `suspend` call — the natural
 * bridge for an Angular HttpClient request, which emits once and completes. The
 * subscription is disposed as soon as the value arrives. A source error is rethrown
 * here, and a source that completes without emitting throws [NoSuchElementException].
 * Use from a [LifecycleScope] so an in-flight request is cancelled when the host is
 * destroyed.
 */
suspend fun <T> Observable<T>.await(): T = asFlow().first()
