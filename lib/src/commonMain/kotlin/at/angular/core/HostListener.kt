package at.angular.core

/**
 * @see: https://angular.dev/api/core/HostListener
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class HostListener(
    val eventName: String,
    val args: Array<String> = []
)
