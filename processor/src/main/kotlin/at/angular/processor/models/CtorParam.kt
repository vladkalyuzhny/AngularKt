package at.angular.processor.models

/**
 * One constructor parameter, as an Angular DI dependency. AOT turns it into an `inject(...)` call
 */
data class CtorParam(
    /** The parameter's declared type; the DI token unless [injectTokenFqn] overrides it. */
    val typeFqn: String,
    /** Explicit `@Inject(Token::class)`: look the dependency up by this token instead. */
    val injectTokenFqn: String? = null,
    /** Angular's `@Optional` resolution modifier. */
    val optional: Boolean = false,
    /** Angular's `@Self` resolution modifier. */
    val self: Boolean = false,
    /** Angular's `@SkipSelf` resolution modifier. */
    val skipSelf: Boolean = false,
    /** Angular's `@Host` resolution modifier. */
    val host: Boolean = false,
)
