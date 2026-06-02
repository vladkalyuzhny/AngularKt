package at.angular.processor.utils

internal fun String.quoteWith(quote: Char, escapes: Map<Char, String>): String =
    buildString(length + 2) {
        append(quote)
        for (char in this@quoteWith) append(escapes[char] ?: char.toString())
        append(quote)
    }
