package at.angular.processor.literals

import at.angular.processor.utils.quoteWith

/** Escapes strings into Kotlin source literals — the JIT-mode output language. */
object KotlinLiterals {
    private val STRING_ESCAPES = mapOf(
        '\\' to "\\\\", '"' to "\\\"", '$' to "\\$",
        '\n' to "\\n", '\r' to "\\r", '\t' to "\\t",
    )
    /** Kotlin hard keywords — unusable as a bare package/class segment, so they need back-ticks. */
    private val HARD_KEYWORDS = setOf(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in",
        "interface", "is", "null", "object", "package", "return", "super", "this", "throw",
        "true", "try", "typealias", "typeof", "val", "var", "when", "while",
    )
    private val PLAIN_IDENTIFIER = Regex("[A-Za-z_][A-Za-z0-9_]*")

    fun stringLiteral(value: String): String = value.quoteWith('"', STRING_ESCAPES)

    fun stringArrayLiteral(values: List<String>): String =
        values.joinToString(", ", "arrayOf(", ")") { stringLiteral(it) }

    /**
     * Renders a fully-qualified name as a Kotlin reference, back-tick-escaping any dotted segment
     * that isn't a plain identifier (e.g. a package directory like `product-info`) or that collides
     * with a hard keyword. The FQN strings the model stores (and matches on) stay raw — only the
     * emitted Kotlin is escaped, so `app.router.product-info.X` becomes ``app.router.`product-info`.X``.
     */
    fun typeReference(fqn: String): String =
        fqn.split('.').joinToString(".") { segment ->
            if (segment.matches(PLAIN_IDENTIFIER) && segment !in HARD_KEYWORDS) segment else "`$segment`"
        }
}
