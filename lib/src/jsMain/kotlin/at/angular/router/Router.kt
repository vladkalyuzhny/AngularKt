@file:JsModule("@angular/router")

package at.angular.router

import at.angular.core.EnvironmentProviders
import rxjs.Observable
import kotlin.js.Promise

/**
 * @see: https://angular.dev/api/router/PreloadAllModules
 */
external val PreloadAllModules: dynamic

/**
 * @see: https://angular.dev/api/router/provideRouter
 */
external fun provideRouter(routes: Array<dynamic>, vararg features: dynamic): EnvironmentProviders

/**
 * @see: https://angular.dev/api/router/withHashLocation
 */
external fun withHashLocation(): dynamic

/**
 * @see: https://angular.dev/api/router/withPreloading
 */
external fun withPreloading(preloadingStrategy: dynamic): dynamic


/**
 * @see: https://angular.dev/api/router/RouterModule
 */
external class RouterModule {
    companion object {
        fun forRoot(routes: Array<dynamic>, config: dynamic = definedExternally): dynamic
        fun forChild(routes: Array<dynamic>): dynamic
    }
}

/**
 * @see: https://angular.dev/api/router/Router
 */
external class Router {
    fun navigate(commands: Array<dynamic>): Promise<Boolean>
    val events: Observable<dynamic>
}

/**
 * @see: https://angular.dev/api/router/ActivatedRoute
 */
external class ActivatedRoute {
    val params: Observable<dynamic>
    val paramMap: Observable<dynamic>
}
