@file:JsModule("@angular/core")

package at.angular.core

import kotlin.js.Promise

/**
 * @see: https://angular.dev/api/core/enableProdMode
 */
external fun enableProdMode()

/**
 * @see: https://angular.dev/api/core/PlatformRef
 */
external interface PlatformRef {
    fun <T : Any> bootstrapModule(
        moduleType: JsClass<out T>,
        compilerOptions: dynamic = definedExternally
    ): Promise<dynamic>
}
