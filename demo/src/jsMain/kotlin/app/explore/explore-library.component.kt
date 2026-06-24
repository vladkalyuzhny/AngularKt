@file:OptIn(ExperimentalJsExport::class)

package app.explore

import at.angular.core.Component
import at.angular.core.ElementRef
import at.angular.core.OnInit

/**
 * "Explore library" — the app-shell below the toolbar: a Material `mat-sidenav` drawer (a persistent
 * side panel on desktop, a dismissable overlay on mobile — see [navMode]/[navOpened]) and a content
 * pane that is a single `<router-outlet>`. Each left-nav entry is a real route (`/setup`, `/signal`,
 * …); the nav uses `routerLink`/`routerLinkActive`, so the address bar tracks the open tab, the
 * highlight follows the URL, and deep links / back-forward just work — no manual index or URL parsing.
 *
 * The catalog ([GET_STARTED]/[EXAMPLES]) only feeds the nav here (label/icon/path); each page renders
 * itself (see `app.explore.pages`). `routerLink` is built from each entry's `id` (`'/' + f.id`).
 */
@JsExport
@Component(
    selector = "app-explore",
    templateUrl = "./explore-library.component.html",
    styleUrls = ["./explore-library.component.css"],
)
class ExploreLibraryComponent(private val el: ElementRef) : OnInit {

    /** Left-nav catalog: the "Get started" sections, then every feature example. */
    val getStarted: Array<Feature> = GET_STARTED
    val examples: Array<Feature> = EXAMPLES

    /** Responsive nav drawer: a persistent side panel on desktop, a dismissable overlay on mobile.
     *  Driven by a `matchMedia` breakpoint reached through the host element (no kotlinx.browser dep).
     *  [navMode] is one of MatDrawer's `"side"`/`"over"`; the template binds it via `$any(...)` since
     *  this Kotlin `String` widens to TS `string`, which AOT won't assign to the `MatDrawerMode` union. */
    var navMode = "side"
    var navOpened = true

    private val window get() = el.nativeElement.ownerDocument.defaultView

    override fun ngOnInit() {
        val mql = window.matchMedia(MOBILE_QUERY)
        applyLayout(mql.matches == true)
        // Re-evaluate when the viewport crosses the breakpoint (resize / rotate).
        mql.addEventListener("change", { e: dynamic -> applyLayout(e.matches == true) })
    }

    fun toggleNav() { navOpened = !navOpened }

    /** After picking a nav link on a narrow viewport, dismiss the overlay drawer (read the breakpoint
     *  fresh so this is correct even if a resize listener fired outside Angular's zone). */
    fun closeNavOnMobile() {
        if (window.matchMedia(MOBILE_QUERY).matches == true) navOpened = false
    }

    private fun applyLayout(mobile: Boolean) {
        navMode = if (mobile) "over" else "side"
        navOpened = !mobile
    }

    private companion object {
        const val MOBILE_QUERY = "(max-width: 900px)"
    }
}
