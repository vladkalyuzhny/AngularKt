@file:OptIn(ExperimentalJsExport::class)

package app.examples.standalone

import at.angular.common.CommonModule
import at.angular.core.Component
import at.angular.core.Optional

/**
 * Standalone component — it carries `standalone = true` and lists what it uses in
 * its own `imports` instead of being declared by an `@NgModule`. NgModules can
 * still import it (AppModule does), which is how it reaches the app.
 *
 * Doubles as the `@Optional` proof: [ThemeService] is provided nowhere, so the
 * optional constructor dependency is null and the badge renders "theme: none".
 */
@JsExport
@Component(
    selector = "app-badge",
    standalone = true,
    imports = [CommonModule::class],
    templateUrl = "./badge.component.html",
    styleUrls = ["./badge.component.css"]
)
class BadgeComponent(@Optional private val theme: ThemeService?) {
    val themeName: String get() = theme?.color ?: "none"
}
