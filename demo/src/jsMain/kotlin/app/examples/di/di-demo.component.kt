@file:OptIn(ExperimentalJsExport::class)

package app.examples.di

import at.angular.core.Component

/**
 * Live demo for the "Services & DI" feature: [GreetingService] is constructor-injected
 * and its greeting is read once at construction, proving the DI graph end-to-end.
 */
@JsExport
@Component(
    selector = "app-di",
    templateUrl = "./di-demo.component.html",
    styleUrls = ["./di-demo.component.css"]
)
class DiDemoComponent(greeting: GreetingService) {
    val message: String = greeting.greet()
}
