@file:OptIn(ExperimentalJsExport::class)

package app.examples.di

import at.angular.core.Injectable

/**
 * Phase 1 — `@Injectable` like Angular TS. The processor applies the decorator
 * at runtime (JIT); in AOT the `.ts` bridge carries `@Injectable` and `extends`
 * this class. `@JsExport` keeps the class + `greet()` reachable by name so the
 * bridge can import and the AOT-compiled template can call them.
 */
@JsExport
@Injectable(providedIn = "root")
class GreetingService {
    fun greet(): String = "Welcome back! 👋"
}
