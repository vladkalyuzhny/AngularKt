package at.angular.core

/**
 * @see: https://angular.dev/api/core/HostBinding
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class HostBinding(
    val hostPropertyName: String = ""
)
