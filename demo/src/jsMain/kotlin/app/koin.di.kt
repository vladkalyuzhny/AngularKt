package app

import app.examples.koin.appKoinModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatformTools

/**
 * Starts Koin's global container once, at application bootstrap — the idiomatic place, mirroring
 * a `startKoin { }` call in an Angular `main.ts`. Both entry points (AOT and JIT `main`) call this
 * before handing off to Angular, so the [app.examples.koin.KoinDemoComponent] always finds its
 * graph ready. Guarded against a double start (HMR can re-run `main` against a live container).
 */
fun startAppKoin() {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        startKoin { modules(appKoinModule) }
    } else {
        // A hot reload re-ran main over a live container; swap in the fresh module.
        stopKoin()
        startKoin { modules(appKoinModule) }
    }
}
