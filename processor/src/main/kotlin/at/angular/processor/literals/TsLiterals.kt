package at.angular.processor.literals

import at.angular.processor.utils.quoteWith

/** Escapes strings into TypeScript source — the AOT-mode output language. */
object TsLiterals {
    private val STRING_ESCAPES = mapOf(
        '\\' to "\\\\", '\'' to "\\'",
        '\n' to "\\n", '\r' to "\\r", '\t' to "\\t",
    )
    private val TEMPLATE_ESCAPES = linkedMapOf(
        "\\" to "\\\\",
        "`" to "\\`",
        "\${" to "\\\${",
    )

    fun stringLiteral(value: String): String = value.quoteWith('\'', STRING_ESCAPES)

    /** A `[a, b, …]` array literal of already-rendered TypeScript expressions (unquoted). */
    fun arrayLiteral(items: List<String>): String = items.joinToString(", ", "[", "]")

    /** A `['a', 'b', …]` array of string literals. */
    fun stringArrayLiteral(values: List<String>): String = arrayLiteral(values.map { stringLiteral(it) })

    /** A `{ key: value, … }` object literal of already-rendered entries. */
    fun objectLiteral(entries: List<Pair<String, String>>): String =
        entries.joinToString(", ", "{ ", " }") { (key, value) -> "$key: $value" }

    /**
     * Emits an Angular template as a backtick literal, escaping `\`, backtick and `${`
     */
    fun templateLiteral(value: String): String {
        var result = value
        for ((from, to) in TEMPLATE_ESCAPES) {
            result = result.replace(from, to)
        }
        return "`$result`"
    }
}
