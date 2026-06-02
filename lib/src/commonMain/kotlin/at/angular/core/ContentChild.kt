package at.angular.core

/**
 * @see: https://angular.dev/api/core/ContentChild
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ContentChild(
    val selector: String,
    val static: Boolean = false
)
