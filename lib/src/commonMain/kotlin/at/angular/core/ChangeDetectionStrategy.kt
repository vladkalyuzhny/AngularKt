package at.angular.core

/**
 * @see: https://angular.dev/api/core/ChangeDetectionStrategy
 */
enum class ChangeDetectionStrategy(val value: Int) {
    OnPush(0),
    Default(1),
}
