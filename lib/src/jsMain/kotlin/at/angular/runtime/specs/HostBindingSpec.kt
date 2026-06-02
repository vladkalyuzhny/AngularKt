package at.angular.runtime.specs

/** A `@HostBinding` member; [hostProperty] is the host target (null ⇒ member name). */
class HostBindingSpec(
    val property: String,
    val hostProperty: String? = null
)