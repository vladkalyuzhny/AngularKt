package at.angular.lib

/**
 * @see: https://angular.io/api/core/Pipe
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Pipe(
    val name: String = "",
    val pure: Boolean = true
)
