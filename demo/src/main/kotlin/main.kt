import app.AppModule
import at.angular.core.enableProdMode
import at.angular.platformBrowserDynamic.platformBrowserDynamic

fun main() {
    if (io.angular.sample.external.process.env.ENV == "production") {
        enableProdMode()
    }
    platformBrowserDynamic().bootstrapModule(module.initialize())
    if (environment.production as Boolean) {
        enableProdMode()
    }
    platformBrowserDynamic(undefined).bootstrapModule(AppModule::class, null).catch {
        console.error(it)
    }
}
