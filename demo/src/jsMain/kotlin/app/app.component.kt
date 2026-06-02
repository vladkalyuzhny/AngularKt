@file:OptIn(ExperimentalJsExport::class)

package app

import at.angular.core.Component
import at.angular.core.ElementRef
import at.angular.core.OnInit

/**
 * Root component — the app shell. Constructor-injects [ElementRef] (to reach the document for the
 * theme toggle), renders the Material toolbar with the header actions (light/dark toggle + GitHub
 * link), and hosts `ExploreLibraryComponent`, which is where every feature demo (and the single
 * router-outlet) lives.
 */
@JsExport
@Component(
    selector = "app-root",
    templateUrl = "./app.component.html",
    styleUrls = ["./app.component.css"]
)
class AppComponent(private val el: ElementRef) : OnInit {

    /** Whether the dark theme is active — drives the toolbar toggle icon. */
    var dark = false

    /** `<html>`'s document — reached through the host element (no kotlinx.browser dependency). */
    private val document get() = el.nativeElement.ownerDocument
    private val window get() = document.defaultView

    override fun ngOnInit() {
        // Restore the saved choice, or fall back to the OS preference on first visit.
        dark = when (window.localStorage.getItem(THEME_KEY)) {
            "dark" -> true
            "light" -> false
            else -> window.matchMedia("(prefers-color-scheme: dark)").matches == true
        }
        applyTheme()
        println("[AngularKt] ngOnInit, dark=$dark")
    }

    /** Flip light ⇄ dark, persist the choice, and reflect it onto `<body>`. */
    fun toggleTheme() {
        dark = !dark
        window.localStorage.setItem(THEME_KEY, if (dark) "dark" else "light")
        applyTheme()
    }

    private fun applyTheme() {
        // A single `dark` class on <body> swaps the CSS custom-property palette (see index.html).
        document.body.classList.toggle("dark", dark)
    }

    private companion object {
        const val THEME_KEY = "angularkt-theme"
    }
}
