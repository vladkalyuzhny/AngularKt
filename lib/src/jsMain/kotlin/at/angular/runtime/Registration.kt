package at.angular.runtime

import at.angular.core.ChangeDetectionStrategy
import at.angular.core.ViewEncapsulation
import at.angular.core.interop.Component
import at.angular.core.interop.Directive
import at.angular.core.interop.Injectable
import at.angular.core.interop.NgModule
import at.angular.core.interop.Pipe
import at.angular.router.PreloadAllModules
import at.angular.router.RouterModule
import at.angular.runtime.specs.CtorParamSpec
import at.angular.runtime.specs.HostBindingSpec
import at.angular.runtime.specs.HostListenerSpec
import at.angular.runtime.specs.PropBindingSpec
import at.angular.runtime.specs.QuerySpec
import at.angular.utils.jsObject
import at.angular.utils.toJsClasses
import kotlin.reflect.KClass

/**
 * Runtime decorator registration — the hand-written counterpart to AngularKt's
 * generated JIT manifest. The KSP processor emits one `registerX(...)` call per
 * declarable (see `at.angular.generated.registerAngularKt`); every Kotlin/JS ↔
 * Angular metadata detail lives here, once, instead of being spelled out in the
 * generated output.
 */

/** Routes of each standalone-lazy `@RoutingModule` (a `loadChildren` Routes target), keyed by class. */
private val lazyRoutesRegistry: MutableMap<JsClass<*>, Array<dynamic>> = mutableMapOf()

@Suppress("unused")
fun registerComponent(
    cls: KClass<*>,
    selector: String? = null,
    template: String? = null,
    templateUrl: String? = null,
    styles: Array<String>? = null,
    styleUrls: Array<String>? = null,
    inputs: Array<PropBindingSpec> = emptyArray(),
    outputs: Array<PropBindingSpec> = emptyArray(),
    hostBindings: Array<HostBindingSpec> = emptyArray(),
    hostListeners: Array<HostListenerSpec> = emptyArray(),
    queries: Array<QuerySpec> = emptyArray(),
    standalone: Boolean = false,
    imports: Array<KClass<*>> = emptyArray(),
    encapsulation: ViewEncapsulation? = null,
    changeDetection: ChangeDetectionStrategy? = null,
    deps: Array<CtorParamSpec> = emptyArray(),
) {
    val options: dynamic = jsObject {
        selector?.let { this.selector = it }
        template?.let { this.template = it }
        templateUrl?.let { this.templateUrl = it }
        styles?.let { this.styles = it }
        styleUrls?.let { this.styleUrls = it }
        this.standalone = standalone
        if (standalone && imports.isNotEmpty()) this.imports = imports.toJsClasses()
        encapsulation?.let { this.encapsulation = it.value }
        changeDetection?.let { this.changeDetection = it.value }
    }
    Component(options.unsafeCast<Component>())(cls.js)
    applyInputs(cls, inputs)
    applyOutputs(cls, outputs)
    applyHostBindings(cls, hostBindings)
    applyHostListeners(cls, hostListeners)
    applyQueries(cls, queries)
    applyCtorParameters(cls, deps)
}

@Suppress("unused")
fun registerDirective(
    cls: KClass<*>,
    selector: String? = null,
    inputs: Array<PropBindingSpec> = emptyArray(),
    outputs: Array<PropBindingSpec> = emptyArray(),
    hostBindings: Array<HostBindingSpec> = emptyArray(),
    hostListeners: Array<HostListenerSpec> = emptyArray(),
    queries: Array<QuerySpec> = emptyArray(),
    standalone: Boolean = false,
    deps: Array<CtorParamSpec> = emptyArray(),
) {
    val options: dynamic = jsObject {
        selector?.let { this.selector = it }
        this.standalone = standalone
    }
    Directive(options.unsafeCast<Directive>())(cls.js)
    applyInputs(cls, inputs)
    applyOutputs(cls, outputs)
    applyHostBindings(cls, hostBindings)
    applyHostListeners(cls, hostListeners)
    applyQueries(cls, queries)
    applyCtorParameters(cls, deps)
}

@Suppress("unused")
fun registerPipe(
    cls: KClass<*>,
    name: String? = null,
    pure: Boolean? = null,
    standalone: Boolean = false,
    deps: Array<CtorParamSpec> = emptyArray(),
) {
    val options: dynamic = jsObject {
        name?.let { this.name = it }
        pure?.let { this.pure = it }
        this.standalone = standalone
    }
    Pipe(options.unsafeCast<Pipe>())(cls.js)
    applyCtorParameters(cls, deps)
}

@Suppress("unused")
fun registerInjectable(
    cls: KClass<*>,
    providedIn: String? = null,
    deps: Array<CtorParamSpec> = emptyArray(),
) {
    val options: dynamic = jsObject {
        providedIn?.let { this.providedIn = it }
    }
    Injectable(options.unsafeCast<Injectable>())(cls.js)
    applyCtorParameters(cls, deps)
}

@Suppress("unused")
fun registerModule(
    cls: KClass<*>,
    declarations: Array<KClass<*>> = emptyArray(),
    imports: Array<KClass<*>> = emptyArray(),
    exports: Array<KClass<*>> = emptyArray(),
    providers: Array<KClass<*>> = emptyArray(),
    bootstrap: Array<KClass<*>> = emptyArray(),
) {
    val options: dynamic = jsObject {
        if (declarations.isNotEmpty()) this.declarations = declarations.toJsClasses()
        if (imports.isNotEmpty()) this.imports = imports.toJsClasses()
        if (exports.isNotEmpty()) this.exports = exports.toJsClasses()
        if (providers.isNotEmpty()) this.providers = providers.toJsClasses()
        if (bootstrap.isNotEmpty()) this.bootstrap = bootstrap.toJsClasses()
    }
    NgModule(options.unsafeCast<NgModule>())(cls.js)
}

/**
 * A `@RoutingModule` compiled to a classic, importable router NgModule — the JIT counterpart of the
 * AOT bridge. The root set uses `RouterModule.forRoot(routes)` (with `{ useHash, preloadingStrategy }`
 * for the requested features), exactly as a hand-written `AppRoutingModule` would; a lazy feature
 * feature module's routes use `RouterModule.forChild(routes)`. The decorated class is then
 * imported by an `@NgModule` like any other — no runtime provider shim.
 */
@Suppress("unused")
fun registerRoutingModule(
    cls: KClass<*>,
    routes: Array<dynamic>,
    forChild: Boolean = false,
    useHash: Boolean = false,
    preloadAllModules: Boolean = false,
) {
    val routerImport = if (forChild) {
        RouterModule.forChild(routes)
    } else {
        val config: dynamic = jsObject {
            if (useHash) this.useHash = true
            if (preloadAllModules) this.preloadingStrategy = PreloadAllModules
        }
        RouterModule.forRoot(routes, config)
    }
    val options: dynamic = jsObject {
        imports = arrayOf(routerImport)
        exports = arrayOf<dynamic>(RouterModule::class.js)
    }
    NgModule(options.unsafeCast<NgModule>())(cls.js)
}

/**
 * Records the `Routes` of a standalone-lazy `@RoutingModule` (the modern `loadChildren: () => ROUTES`
 * target — a bare routes array, no `@NgModule`). [route]'s `loadChildrenRoutes` resolves to this array.
 */
@Suppress("unused")
fun registerLazyRoutes(cls: KClass<*>, routes: Array<dynamic>) {
    lazyRoutesRegistry[cls.js] = routes
}

/**
 * Builds one route descriptor for [registerRoutingModule]; [children] are nested descriptors.
 * Both lazy forms are the JIT counterpart of Angular's `loadChildren` — the single webpack bundle
 * already holds the target, so they resolve with no separate fetch (routing is lazy, code is not
 * split): [loadChildren] resolves to an eagerly-registered NgModule class (classic form);
 * [loadChildrenRoutes] resolves to a standalone-lazy `Routes` array (modern form, via [registerLazyRoutes]).
 */
@Suppress("unused")
fun route(
    path: String,
    component: KClass<*>? = null,
    redirectTo: String? = null,
    pathMatch: String? = null,
    children: Array<dynamic>? = null,
    loadChildren: KClass<*>? = null,
    loadChildrenRoutes: KClass<*>? = null,
): dynamic = jsObject {
    this.path = path
    component?.let { this.component = it.js }
    redirectTo?.let { this.redirectTo = it }
    pathMatch?.let { this.pathMatch = it }
    children?.let { this.children = it }
    loadChildren?.let { cls -> this.loadChildren = { js("Promise").resolve(cls.js) } }
    loadChildrenRoutes?.let { cls ->
        this.loadChildren = { js("Promise").resolve(lazyRoutesRegistry[cls.js]) }
    }
}
