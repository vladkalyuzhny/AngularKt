package at.angular.lib

import at.angular.core.NgModule

abstract class NgModuleKt(
    override val providers: Array<out JsClass<Any>> = emptyArray(),
    override val declarations: Array<out JsClass<out Any>> = emptyArray(),
    override val imports: Array<out JsClass<*>> = emptyArray(),
    override val exports: Array<out JsClass<*>> = emptyArray(),
    override val entryComponents: Array<out JsClass<*>> = emptyArray(),
    override val bootstrap: Array<out JsClass<*>> = emptyArray()
) : NgModule {

    companion object {

        fun <T : Any> create(ngModule: NgModule): JsClass<out T> {
            return NgModule(
                NgModuleKt(
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
}