package at.angular.core

import kotlin.reflect.KClass

/**
 * @see: https://angular.dev/api/core/Component
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Component(
    val selector: String = "",
    val templateUrl: String = "",
    val template: String = "",
    val styleUrls: Array<String> = [],
    val styles: Array<String> = [],
    val standalone: Boolean = false,
    val imports: Array<KClass<*>> = [],
    val encapsulation: ViewEncapsulation = ViewEncapsulation.Emulated,
    val changeDetection: ChangeDetectionStrategy = ChangeDetectionStrategy.Default,
)
