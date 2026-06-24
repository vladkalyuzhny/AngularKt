@file:OptIn(ExperimentalJsExport::class)

package app.examples.koin

import at.angular.core.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The Koin counterpart to the "Services & DI" example. The component is constructed by
 * Angular (no DI parameters), so instead of receiving its dependency through the
 * constructor it implements Koin's [KoinComponent] and resolves [QuoteService] from the
 * global container with `by inject()`. The graph is wired in [appKoinModule] and started
 * at bootstrap — pure Kotlin DI living happily inside an Angular component.
 */
@JsExport
@Component(
    selector = "app-koin",
    templateUrl = "./koin.component.html",
    styleUrls = ["./koin.component.css"]
)
class KoinDemoComponent : KoinComponent {
    private val quotes: QuoteService by inject()

    var quote: String = quotes.next()

    fun next() {
        quote = quotes.next()
    }
}
