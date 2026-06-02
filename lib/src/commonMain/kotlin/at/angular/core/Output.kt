package at.angular.core

/**
 * @see: https://angular.dev/api/core/Output
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Output(
    val alias: String = ""
)
