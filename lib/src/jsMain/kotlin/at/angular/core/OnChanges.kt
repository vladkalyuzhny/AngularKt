package at.angular.core

/**
 * @see: https://angular.dev/api/core/OnChanges
 */
external interface OnChanges {
    fun ngOnChanges(changes: SimpleChanges)
}

/**
 * @see: https://angular.dev/api/core/SimpleChanges
 */
external interface SimpleChanges

operator fun SimpleChanges.get(propName: String): SimpleChange? = asDynamic()[propName]

/**
 * @see: https://angular.dev/api/core/SimpleChange
 */
external interface SimpleChange {
    val previousValue: dynamic
    val currentValue: dynamic
    val firstChange: Boolean
    fun isFirstChange(): Boolean
}
