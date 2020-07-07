package at.angular.lib

import at.angular.core.NgModule
import at.angular.lib.extension.jsObject
import at.angular.platformBrowserDynamic.platformBrowserDynamic
import kotlin.js.Promise

internal external fun require(path: String): dynamic

object AngularKt {

    inline fun <reified T : Any> bootstrap(
        extraProviders: dynamic = null,
        compilerOptions: dynamic = null
    ): Promise<dynamic> {
        parseJson(T::class.js)
        return platformBrowserDynamic(extraProviders).bootstrapModule(T::class.js, compilerOptions)
    }

    fun parseJson(jsClass: JsClass<*>) {
        val serviceAccount = require("../../../resources/main/file.json")
        when ("") {
            at.angular.lib.NgModuleKt::class.js.name -> {
            }
            at.angular.lib.NgModuleKt::class.js.name -> {
            }
            at.angular.lib.NgModuleKt::class.js.name -> {
            }
            at.angular.lib.NgModuleKt::class.js.name -> {
            }
        }

        NgModule(jsObject<> {

        })(jsClass)
    }

    fun sadsdf() {
        Modules.add("" to jsObject {

        })
    }
}

fun sf() {
    AngularKt.bootstrap<Any>()
}