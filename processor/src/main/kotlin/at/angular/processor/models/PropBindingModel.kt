package at.angular.processor.models

/** An `@Input`/`@Output` property binding, with an optional template alias. */
data class PropBindingModel(val property: String, val alias: String?)
