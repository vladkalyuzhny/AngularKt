package at.angular.utils

import kotlin.reflect.KClass

internal fun Array<KClass<*>>.toJsClasses(): Array<Any?> = Array(size) { this[it].js }