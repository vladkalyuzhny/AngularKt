package at.angular.runtime.specs

/** A `@HostListener` method bound to host [event] with the handler [args]. */
class HostListenerSpec(
    val method: String,
    val event: String,
    val args: Array<String> = emptyArray()
)