package at.angular.core

/**
 * @see: https://angular.dev/api/core/ViewEncapsulation
 */
enum class ViewEncapsulation(val value: Int) {
    Emulated(0),
    None(2),
    ShadowDom(3),
}
