@file:OptIn(ExperimentalJsExport::class)

package app

import at.angular.common.http.provideHttpClient
import at.angular.common.http.withFetch
import at.angular.core.EnvironmentProviders
import at.angular.platformBrowser.animations.provideAnimations

/**
 * The application's functional providers — the Kotlin equivalent of an Angular `app.config.ts`.
 * `@JsExport` so the processor-generated `main.ts` can import and spread them straight into
 * `bootstrapApplication(RootComponent, { providers: [...appProviders()] })`, plain Angular.
 */
@JsExport
fun appProviders(): Array<EnvironmentProviders> = arrayOf(
    provideAnimations(),
    provideHttpClient(withFetch()),
)
