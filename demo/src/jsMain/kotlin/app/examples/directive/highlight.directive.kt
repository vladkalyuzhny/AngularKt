@file:OptIn(ExperimentalJsExport::class)

package app.examples.directive

import at.angular.core.Directive
import at.angular.core.ElementRef
import at.angular.core.HostBinding
import at.angular.core.HostListener
import at.angular.core.OnInit

/**
 * Attribute directive `[appHighlight]` — exercises @Directive in both JIT and AOT.
 * Injects ElementRef (constructor DI through the bridge's super(inject(ElementRef)))
 * and tints its host element on init.
 *
 * Also exercises host bindings: `@HostBinding` reflects [hot] onto the host's
 * `is-hot` class, and the two `@HostListener`s flip it on hover.
 */
@JsExport
@Directive(selector = "[appHighlight]")
class HighlightDirective(private val el: ElementRef) : OnInit {

    @HostBinding("class.is-hot")
    var hot: Boolean = false

    override fun ngOnInit() {
        // Theme-aware CSS variables (defined per light/dark in index.html) so the
        // tint reads well on any background instead of a hard-coded color.
        el.nativeElement.style.backgroundColor = "var(--ak-highlight-bg)"
        el.nativeElement.setAttribute("data-highlighted", "true")
    }

    @HostListener("mouseenter")
    fun onEnter() {
        hot = true
        el.nativeElement.style.backgroundColor = "var(--ak-highlight-hot-bg)"
        el.nativeElement.setAttribute("data-hot", "true")
    }

    @HostListener("mouseleave")
    fun onLeave() {
        hot = false
        el.nativeElement.style.backgroundColor = "var(--ak-highlight-bg)"
        el.nativeElement.setAttribute("data-hot", "false")
    }
}
