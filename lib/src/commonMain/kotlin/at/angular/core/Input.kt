package at.angular.core

/**
 * @see: https://angular.dev/api/core/Input
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Input(
    val alias: String = ""
)
