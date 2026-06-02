@file:OptIn(ExperimentalJsExport::class)

package app.explore

import at.angular.core.Component
import at.angular.core.ElementRef
import at.angular.core.OnInit

/**
 * "Explore library" — the whole below-toolbar experience, laid out as an app-shell with a Material
 * `mat-sidenav` drawer: a "Get started" section then the "Examples" group. The drawer is a persistent
 * side panel on desktop and a dismissable overlay on mobile (see [navMode]/[navOpened]). The content
 * pane shows either the setup steps, or (for an example) a description, a side-by-side TypeScript
 * (Angular) vs Kotlin (AngularKt) comparison, and a live demo that is a real AngularKt component
 * running in this very page. The example catalog lives in [buildExamples]; the template and styles
 * are in the sibling .html/.css files, with the footer pinned to the bottom of the scrolling pane.
 *
 * `active` indexes the whole nav: 0 is the setup section, 1..N map to `examples[active-1]`.
 */
@JsExport
@Component(
    selector = "app-explore",
    templateUrl = "./explore-library.component.html",
    styleUrls = ["./explore-library.component.css"],
)
class ExploreLibraryComponent(private val el: ElementRef) : OnInit {

    /** Left-nav catalog: the "Get started" sections, then every feature example. */
    val getStarted: Array<Feature> = buildGetStarted()
    val examples: Array<Feature> = buildExamples()

    /** Highlighted code for the bespoke "Get started" panels (Setup / JIT / AOT). */
    val setupGradle: String = setupGradleHtml()
    val setupComponent: String = setupComponentHtml()
    val configGradle: String = configGradleHtml()
    val jitEntry: String = jitEntryHtml()
    val aotEntry: String = aotEntryHtml()
    val jitRun: String = highlightGradleCmd("./gradlew :app:jsBrowserDevelopmentRun -t -PangularKt.port=8080")
    val aotServe: String = highlightGradleCmd("./gradlew :app:aotServe -PangularKt.port=4200")
    val aotBuild: String = highlightGradleCmd("./gradlew :app:aotBuild")
    val customizeAot: String = customizeAotHtml()
    val customizeJit: String = customizeJitHtml()

    var active = 0
    var reply = ""

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

    val gsCount: Int get() = getStarted.size
    val current: Feature get() = if (active < gsCount) getStarted[active] else examples[active - gsCount]

    /** True when an Examples item is selected (not a Get started section). */
    val isExample: Boolean get() = active >= gsCount

    /** Kotlin-native features (coroutines, Ktor) have no direct TypeScript/Angular equivalent —
     *  the TS column shows the closest workaround under a "not supported" warning. */
    val tsUnsupported: Boolean get() = current.id == "coroutines" || current.id == "ktor"

    fun select(i: Int) {
        active = i
        // On a narrow viewport the drawer is an overlay — dismiss it after choosing (read the
        // breakpoint fresh so this is correct even if a resize listener fired outside Angular's zone).
        if (window.matchMedia(MOBILE_QUERY).matches == true) navOpened = false
    }

    fun toggleNav() { navOpened = !navOpened }

    fun onNotify(message: String) { reply = message }

    private fun applyLayout(mobile: Boolean) {
        navMode = if (mobile) "over" else "side"
        navOpened = !mobile
    }

    private companion object {
        const val MOBILE_QUERY = "(max-width: 900px)"
    }
}
