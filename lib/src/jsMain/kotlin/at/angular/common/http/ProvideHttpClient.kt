@file:JsModule("@angular/common/http")

package at.angular.common.http

import at.angular.core.EnvironmentProviders

/**
 * @see: https://angular.dev/api/common/http/provideHttpClient
 */
external fun provideHttpClient(vararg features: dynamic): EnvironmentProviders

/**
 * @see: https://angular.dev/api/common/http/withFetch
 */
external fun withFetch(): dynamic

/**
 * @see: https://angular.dev/api/common/http/withInterceptors
 */
external fun withInterceptors(interceptorFns: Array<dynamic>): dynamic
