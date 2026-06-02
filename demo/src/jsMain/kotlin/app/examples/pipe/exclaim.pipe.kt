@file:OptIn(ExperimentalJsExport::class)

package app.examples.pipe

import at.angular.core.Pipe

/**
 * Pipe `exclaim` — exercises @Pipe in both JIT and AOT. Used in a template as
 * `{{ value | exclaim }}`.
 *
 * Note: `transform` is a plain public method rather than an `override` of the
 * external `PipeTransform` — Kotlin/JS doesn't emit overrides of external
 * interface members into the .d.ts, so the AOT template type-check wouldn't see
 * `transform`. Angular only needs the method to exist at runtime; the @Pipe
 * decorator (on the generated bridge) is what registers it.
 */
@JsExport
@Pipe(name = "exclaim")
class ExclaimPipe {
    fun transform(value: String): String = "$value!!!"
}
