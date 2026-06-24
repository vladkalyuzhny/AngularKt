package app.examples.koin

/**
 * A plain Kotlin service — no `@Injectable`, no Angular at all. Where the "Services & DI"
 * example lets Angular's own injector build [app.examples.di.GreetingService], here the
 * very same kind of class is wired by [appKoinModule] and pulled from Koin's container
 * instead (see [KoinDemoComponent]). It never touches `@angular/core`.
 */
class QuoteService {
    private val quotes = listOf(
        "Make it work, make it right, make it fast.",
        "Programs must be written for people to read.",
        "Simplicity is the soul of efficiency.",
        "Premature optimization is the root of all evil.",
        "Talk is cheap. Show me the code.",
    )
    private var cursor = 0

    /** Returns the next quote, cycling — deterministic, so no `Math.random()` in the demo. */
    fun next(): String = quotes[cursor++ % quotes.size]
}
