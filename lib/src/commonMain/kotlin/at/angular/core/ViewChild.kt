package at.angular.core

/**
 * @see: https://angular.dev/api/core/ViewChild
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ViewChild(
    val selector: String,
    val static: Boolean = false
)
