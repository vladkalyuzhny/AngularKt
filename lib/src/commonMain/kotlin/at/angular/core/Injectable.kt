package at.angular.core

/**
 * @see: https://angular.dev/api/core/Injectable
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Injectable(
    val providedIn: String = ""
)
