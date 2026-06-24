package app.examples.koin

import org.koin.dsl.module

/**
 * The Koin module that owns this example's graph — the Kotlin equivalent of an Angular
 * provider list, but framework-agnostic. `single { … }` registers [QuoteService] as a
 * lazily-built singleton. Started once at bootstrap (see `startAppKoin` in `koin.di.kt`).
 */
val appKoinModule = module {
    single { QuoteService() }
}
