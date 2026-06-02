package at.angular.core

/**
 * @see: https://angular.dev/api/core/ViewChildren
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ViewChildren(
    val selector: String
)
