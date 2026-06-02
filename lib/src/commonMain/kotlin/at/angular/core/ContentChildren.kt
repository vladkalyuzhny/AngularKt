package at.angular.core

/**
 * @see: https://angular.dev/api/core/ContentChildren
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ContentChildren(
    val selector: String
)
