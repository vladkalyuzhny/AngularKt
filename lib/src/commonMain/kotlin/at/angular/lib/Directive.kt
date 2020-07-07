package at.angular.lib

/**
 * @see: https://angular.io/api/core/Directive
 */
// todo possible fields
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Directive(
    val selector: String = ""
)
