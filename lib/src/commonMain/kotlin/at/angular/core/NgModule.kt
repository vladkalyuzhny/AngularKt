package at.angular.core

import kotlin.reflect.KClass

/**
 * @see: https://angular.dev/api/core/NgModule
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class NgModule(
    val declarations: Array<KClass<*>> = [],
    val imports: Array<KClass<*>> = [],
    val exports: Array<KClass<*>> = [],
    val providers: Array<KClass<*>> = [],
    val bootstrap: Array<KClass<*>> = []
)
