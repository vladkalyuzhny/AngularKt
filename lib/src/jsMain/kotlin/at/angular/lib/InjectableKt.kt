package at.angular.lib

import at.angular.core.NgModule

object InjectableKt {

    fun <T : Any> create(ngModule: NgModule): JsClass<out T> {
        return NgModule(
            InjectableKt(
                providers = providers,
                declarations = declarations,
                imports = imports,
                exports = exports,
                entryComponents = entryComponents,
                bootstrap = bootstrap
            )
        ).Class(ClassDefinition(type))
    }
}