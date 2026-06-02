package at.angular.runtime

import at.angular.core.interop.ContentChild
import at.angular.core.interop.ContentChildren
import at.angular.core.interop.Host
import at.angular.core.interop.HostBinding
import at.angular.core.interop.HostListener
import at.angular.core.interop.Inject
import at.angular.core.interop.Input
import at.angular.core.interop.Optional
import at.angular.core.interop.Output
import at.angular.core.interop.Self
import at.angular.core.interop.SkipSelf
import at.angular.core.interop.ViewChild
import at.angular.core.interop.ViewChildren
import at.angular.runtime.specs.CtorParamSpec
import at.angular.runtime.specs.HostBindingSpec
import at.angular.runtime.specs.HostListenerSpec
import at.angular.runtime.specs.PropBindingSpec
import at.angular.runtime.specs.QuerySpec
import at.angular.utils.jsObject
import kotlin.reflect.KClass

/**
 * Shared decorator-application functions behind the `registerX(...)`
 *
 * Why the `dynamic`/`unsafeCast` dance: Angular's decorators are plain functions
 * that read an options object and stamp metadata onto the JS class. Kotlin/JS does
 * not emit constructor parameter types, so Angular JIT cannot resolve DI unless we
 * set `ctorParameters` ourselves
 */

internal fun applyInputs(cls: KClass<*>, specs: Array<PropBindingSpec>) =
    applyBindings(cls, specs) { Input(it) }

internal fun applyOutputs(cls: KClass<*>, specs: Array<PropBindingSpec>) =
    applyBindings(cls, specs) { Output(it) }

internal fun applyBindings(
    cls: KClass<*>,
    specs: Array<PropBindingSpec>,
    factory: (dynamic) -> dynamic
) {
    specs.forEach { spec ->
        // Angular's Input/Output factories take the alias positionally, not in an
        // options object; pass the alias string or `undefined`.
        val decorate: dynamic = factory(spec.alias?.asDynamic() ?: js("undefined"))
        decorate(cls.js.asDynamic().prototype, spec.property)
    }
}

internal fun applyHostBindings(cls: KClass<*>, specs: Array<HostBindingSpec>) {
    val prototype = cls.js.asDynamic().prototype
    specs.forEach { spec ->
        // A null host target lets Angular default to the member name.
        val decorate: dynamic = if (spec.hostProperty != null) {
            HostBinding(spec.hostProperty)
        } else {
            HostBinding()
        }
        decorate(prototype, spec.property)
    }
}

internal fun applyHostListeners(cls: KClass<*>, specs: Array<HostListenerSpec>) {
    val prototype = cls.js.asDynamic().prototype
    specs.forEach { spec ->
        val decorate: dynamic = HostListener(spec.event, spec.args)
        decorate(prototype, spec.method)
    }
}

internal fun applyQueries(cls: KClass<*>, specs: Array<QuerySpec>) {
    val prototype = cls.js.asDynamic().prototype
    specs.forEach { query ->
        val opts: dynamic = jsObject { if (query.static) static = true }
        val decorate: dynamic = when (query.kind) {
            "viewChild" -> ViewChild(query.selector, opts)
            "viewChildren" -> ViewChildren(query.selector, opts)
            "contentChild" -> ContentChild(query.selector, opts)
            else -> ContentChildren(query.selector, opts)
        }
        decorate(prototype, query.property)
    }
}

/**
 * Sets `Cls.ctorParameters = () => [{ type, decorators }, ...]` so Angular JIT can
 * resolve constructor dependencies — Kotlin/JS emits none. Modifier decorators
 * (`Optional`/`Self`/`SkipSelf`/`Host`/`Inject`) ride along in `decorators`, using
 * the genuine `@angular/core` factory functions as their `type`.
 */
internal fun applyCtorParameters(cls: KClass<*>, specs: Array<CtorParamSpec>) {
    if (specs.isEmpty()) return
    // Array<Any?> (not Array<dynamic> — dynamic can't be a reified type argument);
    // it is a plain JS array at runtime, which is all Angular reads.
    val params = Array<Any?>(specs.size) { i ->
        val spec = specs[i]
        val modifiers = ArrayList<Any?>()
        spec.token?.let { modifiers.add(decoratorRecord(Inject, arrayOf(it.js))) }
        if (spec.optional) modifiers.add(decoratorRecord(Optional))
        if (spec.self) modifiers.add(decoratorRecord(Self))
        if (spec.skipSelf) modifiers.add(decoratorRecord(SkipSelf))
        if (spec.host) modifiers.add(decoratorRecord(Host))
        jsObject {
            type = (spec.token ?: spec.type).js
            if (modifiers.isNotEmpty()) decorators = modifiers.toTypedArray()
        }
    }
    cls.js.asDynamic().ctorParameters = { params }
}

/** A `{ type: <factory>, args: [...] }` descriptor Angular instantiates into param metadata. */
private fun decoratorRecord(type: dynamic, args: Array<Any?> = emptyArray()): dynamic = jsObject {
    this.type = type
    this.args = args
}
