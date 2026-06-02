package at.angular.core

import kotlin.reflect.KClass

/**
 * @see: https://angular.dev/api/core/Inject
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Inject(
    val token: KClass<*>
)
