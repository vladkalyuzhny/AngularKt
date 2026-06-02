package at.angular.processor.codegen

import at.angular.processor.literals.KotlinLiterals
import at.angular.processor.models.RouteEntry
import at.angular.processor.utils.asStringList
import com.github.jknack.handlebars.EscapingStrategy
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Template
import java.util.concurrent.ConcurrentHashMap

/**
 * Renders the codegen templates under a classpath [resourceRoot] (`aot` or `jit`).
 * Unlike a logic-less engine, Handlebars carries the conditions (`{{#if}}`/`{{#each}}`)
 * and the escaping (via the codegen helpers) in the template itself, so the codegen
 * strategies hand over the model almost as-is and the file shape lives in the
 * `.hbs` resources rather than in Kotlin string-building.
 *
 * Models are read directly through their getters (data-class properties). HTML
 * entity-escaping is disabled — we emit source code, not markup — so the `kt`
 * helpers control quoting. Inserted values are never re-parsed, so an Angular
 * `{{binding}}` carried in a template string passes through verbatim.
 *
 * Templates are read as resource strings and compiled *inline*: Handlebars' own
 * classpath loader opens the resource through a `jar:` URL, whose handle the Gradle
 * KSP worker daemon caches and then reads stale after the processor jar is rebuilt
 * (`ZipException: invalid LOC header`). Compiled templates are cached per name.
 */
class TemplateRenderer(private val resourceRoot: String) {
    private val handlebars = Handlebars()
        .with(EscapingStrategy.NOOP)
        // Strip standalone block tags (`{{#if}}`/`{{#each}}` alone on a line) and
        // their trailing newline, so a skipped/iterated section leaves no blank line.
        .prettyPrint(true)
        .registerHelper("kt", Helper<Any?> { v, _ -> KotlinLiterals.stringLiteral(v.toString()) })
        // Renders a fully-qualified name as a Kotlin reference, back-tick-escaping segments that
        // aren't plain identifiers (e.g. a `product-info` package directory).
        .registerHelper("ref", Helper<Any?> { v, _ -> KotlinLiterals.typeReference(v.toString()) })
        .registerHelper("ktArray",
            Helper<Any?> { v, _ -> KotlinLiterals.stringArrayLiteral(v.asStringList()) })
        // Block helper: renders the body when the value is non-null (distinct from
        // {{#if}}, which also treats the Boolean `false` as falsy).
        .registerHelper("present",
            Helper<Any?> { v, opts -> if (v != null) opts.fn() else opts.inverse() })
        // Renders one `route(...)` builder call, recursing into nested children. Recursion
        // lives here rather than in the template because Handlebars has no clean self-recursion.
        .registerHelper("routeCall", Helper<RouteEntry> { route, _ -> renderRouteCall(route) })

    private val compiled = ConcurrentHashMap<String, Template>()

    /** @param name resource name under [resourceRoot], e.g. `"component.ts"` (`.hbs` implied). */
    fun render(name: String, model: Any): String {
        val template = compiled.getOrPut(name) { handlebars.compileInline(readTemplate(name)) }
        return template.apply(model)
    }

    private fun readTemplate(name: String): String {
        val path = "/$resourceRoot/$name.hbs"
        val stream = javaClass.getResourceAsStream(path)
            ?: error("AngularKt: missing codegen template resource '$path'")
        return stream.use { it.readBytes().toString(Charsets.UTF_8) }
    }

    /** `route("path", component = Fqn::class, …, children = arrayOf(route(…), …))` — present fields only. */
    private fun renderRouteCall(route: RouteEntry): String = buildString {
        append("route(").append(KotlinLiterals.stringLiteral(route.path))
        route.componentFqn?.let { append(", component = ").append(KotlinLiterals.typeReference(it)).append("::class") }
        route.redirectTo?.let { append(", redirectTo = ").append(KotlinLiterals.stringLiteral(it)) }
        route.pathMatch?.let { append(", pathMatch = ").append(KotlinLiterals.stringLiteral(it)) }
        route.loadChildren?.let { target ->
            // A @RoutingModule target resolves to a lazy Routes array (registerLazyRoutes); an @NgModule
            // target resolves to the module class. Different runtime shapes → different route() args.
            val arg = if (target.isRoutesBundle) "loadChildrenRoutes" else "loadChildren"
            append(", $arg = ").append(KotlinLiterals.typeReference(target.fqn)).append("::class")
        }
        if (route.children.isNotEmpty()) {
            append(", children = arrayOf(")
            append(route.children.joinToString(", ", transform = ::renderRouteCall))
            append(")")
        }
        append(")")
    }
}