@file:JsModule("@angular/platform-browser")

package at.angular.platformBrowser

import at.angular.core.PlatformRef
import kotlin.js.Promise

/**
 * @see: https://angular.dev/api/platform-browser/platformBrowser
 */
external fun platformBrowser(extraProviders: dynamic = definedExternally): PlatformRef

/**
 * @see: https://angular.dev/api/platform-browser/bootstrapApplication
 */
external fun bootstrapApplication(rootComponent: dynamic, options: dynamic = definedExternally): Promise<dynamic>
