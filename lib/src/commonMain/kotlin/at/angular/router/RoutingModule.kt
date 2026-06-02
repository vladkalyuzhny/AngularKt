package at.angular.router

/**
 * Declares a set of routes
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RoutingModule(
    val routes: Array<Route> = [],
    /**
     * Hash-based location (`#/path` URLs) for the root router — compiles to
     * `provideRouter(routes, withHashLocation())`, the equivalent of `forRoot(routes, { useHash: true })`.
     */
    val useHash: Boolean = false,
    /**
     * Preload all lazy-loaded routes in the background for the root router — compiles to
     * `provideRouter(routes, withPreloading(PreloadAllModules))`, the equivalent of
     * `forRoot(routes, { preloadingStrategy: PreloadAllModules })`.
     */
    val preloadAllModules: Boolean = false,
)
