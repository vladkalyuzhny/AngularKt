package at.angular.core

/**
 * @see: https://angular.dev/api/core/Directive
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Directive(
    val selector: String = "",
    val standalone: Boolean = false
)
