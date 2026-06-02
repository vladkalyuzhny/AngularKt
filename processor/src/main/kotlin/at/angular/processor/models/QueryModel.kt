package at.angular.processor.models

/**
 * The four Angular query decorators, each carrying the names the codegens emit:
 * [jitKind] is the discriminator the JIT runtime matches on.
 * [aotClass] is the `@angular/core` class the AOT path instantiates (`new ViewChild(...)`).
 */
enum class QueryKind(val jitKind: String, val aotClass: String, val multiple: Boolean) {
    VIEW_CHILD("viewChild", "ViewChild", multiple = false),
    VIEW_CHILDREN("viewChildren", "ViewChildren", multiple = true),
    CONTENT_CHILD("contentChild", "ContentChild", multiple = false),
    CONTENT_CHILDREN("contentChildren", "ContentChildren", multiple = true),
}

/** A `@ViewChild`/`@ViewChildren`/`@ContentChild`/`@ContentChildren` on [property]. */
data class QueryModel(
    val property: String,
    val kind: QueryKind,
    val selector: String,
    val static: Boolean,
) {
    init {
        require(!static || !kind.multiple) {
            "`static` applies only to single-element queries; $kind is plural."
        }
    }
}