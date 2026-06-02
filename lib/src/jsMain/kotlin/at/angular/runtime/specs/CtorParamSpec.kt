package at.angular.runtime.specs

import kotlin.reflect.KClass

/**
 * One constructor parameter, as Angular sees it for DI. `applyCtorParameters` turns
 * each of these into a `ctorParameters` entry (`{ type, decorators }`) so Angular JIT
 * can resolve the dependency — Kotlin/JS emits no constructor parameter types itself.
 *
 * [type] is the parameter's declared type and the DI token by default; [token]
 * (from `@Inject(Token::class)`) looks the dependency up by a different token instead.
 * The booleans map 1:1 to Angular's resolution modifiers `@Optional`, `@Self`,
 * `@SkipSelf`, and `@Host`.
 */
class CtorParamSpec(
    val type: KClass<*>,
    val token: KClass<*>? = null,
    val optional: Boolean = false,
    val self: Boolean = false,
    val skipSelf: Boolean = false,
    val host: Boolean = false,
)