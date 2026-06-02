package at.angular.utils

inline fun jsObject(init: dynamic.() -> Unit): dynamic {
    val obj = js("({})")
    init(obj)
    return obj
}

inline fun <T> jsObject(init: T.() -> Unit): T {
    val obj = js("({})") as T
    init(obj)
    return obj
}
