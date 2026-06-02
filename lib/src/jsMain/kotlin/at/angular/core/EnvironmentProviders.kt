@file:JsModule("@angular/core")

package at.angular.core

/**
 * @see: https://angular.dev/api/core/EnvironmentProviders
 */
external interface EnvironmentProviders

/**
 * @see: https://angular.dev/api/core/importProvidersFrom
 */
external fun importProvidersFrom(vararg sources: dynamic): EnvironmentProviders
