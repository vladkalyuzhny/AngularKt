package at.angular.runtime.specs

/** An `@Input`/`@Output` member, with an optional template-facing alias. */
class PropBindingSpec(
    val property: String,
    val alias: String? = null
)