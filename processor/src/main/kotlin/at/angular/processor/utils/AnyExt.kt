package at.angular.processor.utils

internal fun Any?.asStringList(): List<String> = when (this) {
    is List<*> -> map { it.toString() }
    is Array<*> -> map { it.toString() }
    is Iterable<*> -> map { it.toString() }
    else -> error("AngularKt: array helper expects a list, got: ${this?.let { it::class.simpleName } ?: "null"}")
}
