@file:JsModule("@angular/core")
@file:Suppress("FunctionName")

package at.angular.core.interop

/**
 * @see: https://angular.dev/api/core/Directive
 */
external interface Directive {
    val selector: String?
}

external fun Directive(options: Directive): (JsClass<*>) -> JsClass<*>

/**
 * @see:https://angular.dev/api/core/Component
 */
external interface Component : Directive {
    val templateUrl: String?
    val template: String?
    val styleUrls: Array<String>?
    val styles: Array<String>?
}

external fun Component(options: Component): (JsClass<*>) -> JsClass<*>

/**
 * @see: https://angular.dev/api/core/Input
 */
external interface Input {
    val alias: String?
}

external fun Input(options: Input): (JsClass<*>) -> JsClass<*>

/**
 * @see: https://angular.dev/api/core/Output
 */
external interface Output {
    val alias: String?
}

external fun Output(options: Output): (JsClass<*>) -> JsClass<*>

/**
 * @see https://angular.dev/api/core/NgModule
 */
external interface NgModule {
    val providers: Array<out JsClass<*>>?
    val declarations: Array<out JsClass<*>>?
    val imports: Array<out JsClass<*>>?
    val exports: Array<out JsClass<*>>?
    val bootstrap: Array<out JsClass<*>>?
}

external fun NgModule(options: NgModule): (JsClass<*>) -> JsClass<*>

/**
 * @see: https://angular.dev/api/core/Pipe
 */
external interface Pipe {
    val name: String
    val pure: Boolean?
}

external fun Pipe(options: Pipe): (JsClass<*>) -> JsClass<*>

/**
 * @see: https://angular.dev/api/core/Injectable
 */
external interface Injectable {
    val providedIn: dynamic
}

external fun Injectable(options: Injectable): (JsClass<*>) -> JsClass<*>
