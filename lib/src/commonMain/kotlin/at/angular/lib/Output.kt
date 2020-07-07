package at.angular.lib

/**
 * @see: https://angular.io/api/core/Output
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Output(
    val bindingPropertyName: String = ""
)