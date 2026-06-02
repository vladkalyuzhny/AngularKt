package at.angular.core

/**
 * @see: https://angular.dev/api/core/Pipe
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Pipe(
    val name: String = "",
    val pure: Boolean = true,
    val standalone: Boolean = false
)
