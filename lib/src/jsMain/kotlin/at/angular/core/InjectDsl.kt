package at.angular.core

import at.angular.utils.jsObject

fun <T : Any> inject(token: JsClass<T>, optional: Boolean): T? =
    inject(token, jsObject<InjectOptions> { this.optional = optional })