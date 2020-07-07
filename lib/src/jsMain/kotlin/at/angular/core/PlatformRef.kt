@file:JsModule(at.angular.MODULE_CORE)

package at.angular.core

import kotlin.js.Promise

external fun enableProdMode()

external interface PlatformRef {
    fun <T : Any> bootstrapModule(moduleType: JsClass<out T>): Promise<dynamic>
}