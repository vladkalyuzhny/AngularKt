@file:OptIn(ExperimentalJsExport::class)

package app.examples.standalone

import at.angular.core.Injectable

/**
 * Intentionally NOT `providedIn` and never listed in any module's providers — used
 * by [BadgeComponent] to prove the `@Optional` DI modifier resolves an absent
 * dependency to null instead of throwing NG0201.
 */
@JsExport
@Injectable
class ThemeService {
    val color: String = "cyan"
}
