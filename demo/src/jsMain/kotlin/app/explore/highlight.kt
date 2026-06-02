package app.explore

/**
 * Syntax highlighting for the "Explore" code snippets, backed by highlight.js.
 *
 * Each function returns HTML with `<span class="hljs-…">` token wrappers; the colors come from
 * the highlight.js theme stylesheet, wired per build mode the same way as the Material theme —
 * JIT injects it from webpack.config.d/angular-jit.js, AOT adds it to angular.json via
 * `angularKt { aotConfig { styles.add(...) } }`. The result is bound with `[innerHTML]`, and
 * Angular's sanitizer keeps the `<span class>` markup.
 *
 * The full highlight.js build is imported, so the `kotlin` and `typescript` grammars are already
 * registered — no manual registerLanguage call needed.
 */

@JsModule("highlight.js")
private external val hljs: dynamic

internal fun highlightKotlin(code: String): String = highlight(code, "kotlin")

internal fun highlightTs(code: String): String = highlight(code, "typescript")

/**
 * The `./gradlew …` run commands shown in the setup instructions. highlight.js's `bash` grammar
 * leaves a bare `./gradlew :app:task` entirely un-tokenized, so this colors the meaningful parts
 * by hand — reusing the highlight.js theme classes so they match the rest of the snippets: the
 * launcher as `built_in`, each Gradle task path as `title`, any flags as `keyword`, and a trailing
 * `# …` comment (e.g. the dev-server URL) as `comment`.
 */
internal fun highlightGradleCmd(command: String): String {
    val escaped = command
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
    // Peel off a trailing `# …` comment (e.g. the dev-server URL) first, so the URL's `:` isn't
    // mistaken for a Gradle task path below; render it as one greyed comment span.
    val hash = escaped.indexOf('#')
    val cmd = (if (hash >= 0) escaped.substring(0, hash) else escaped).trimEnd()
    val comment = if (hash >= 0) escaped.substring(hash) else null
    val tokens = cmd.split(" ").joinToString(" ") { part ->
        when {
            part == "./gradlew" || part == "gradle" -> "<span class=\"hljs-built_in\">$part</span>"
            part.startsWith("-") -> "<span class=\"hljs-keyword\">$part</span>"
            ":" in part -> "<span class=\"hljs-title\">$part</span>"
            else -> part
        }
    }
    return if (comment != null) "$tokens  <span class=\"hljs-comment\">$comment</span>" else tokens
}

private fun highlight(code: String, language: String): String {
    val options = js("{}")
    options.language = language
    options.ignoreIllegals = true
    return hljs.highlight(code, options).value as String
}
