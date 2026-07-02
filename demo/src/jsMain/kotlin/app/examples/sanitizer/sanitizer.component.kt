@file:OptIn(ExperimentalJsExport::class)

package app.examples.sanitizer

import at.angular.core.Component
import at.angular.platformBrowser.DomSanitizer
import at.angular.platformBrowser.SafeHtml

/**
 * Renders an inline SVG glyph through `[innerHTML]`. Angular's sanitizer strips SVG, so binding the
 * raw string leaves a blank slot; `DomSanitizer.bypassSecurityTrustHtml` marks the same markup
 * trusted and it renders. The sanitizer is constructor-injected like any Angular service.
 */
@JsExport
@Component(
    selector = "app-sanitizer",
    templateUrl = "./sanitizer.component.html",
)
class SanitizerComponent(sanitizer: DomSanitizer) {

    /** The markup both boxes receive. */
    private val svg: String = """
        <svg viewBox="0 0 24 24" width="48" height="48" aria-hidden="true">
          <path fill="#7c4dff" d="M12 2 4 6v6c0 5 3.4 8.4 8 10 4.6-1.6 8-5 8-10V6l-8-4z"/>
        </svg>
    """.trimIndent()

    /** Raw string — Angular's sanitizer drops the `<svg>`, so nothing shows. */
    val raw: String = svg

    /** Trusted — survives sanitization and renders. */
    val safe: SafeHtml = sanitizer.bypassSecurityTrustHtml(svg)
}
