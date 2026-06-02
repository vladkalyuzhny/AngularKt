package at.angular.runtime.specs

/**
 * One Angular view/content query. `applyQueries` matches on [kind] to pick the
 * matching `@angular/core` decorator (`ViewChild`/`ViewChildren`/`ContentChild`/
 * `ContentChildren`) and applies it to [property] on the class prototype.
 */
class QuerySpec(
    val kind: String,
    val property: String,
    val selector: String,
    val static: Boolean = false
)