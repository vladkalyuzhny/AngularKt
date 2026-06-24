@file:OptIn(ExperimentalJsExport::class)

package app.explore.pages

import app.explore.Feature
import app.explore.aotEntryHtml
import app.explore.customizeAotHtml
import app.explore.customizeJitHtml
import app.explore.featureById
import app.explore.highlightGradleCmd
import app.explore.jitEntryHtml
import app.explore.jitWebpackHtml
import app.explore.setupComponentHtml
import app.explore.setupGradleHtml
import at.angular.core.Component

/**
 * The four routed "Get started" pages (`/setup`, `/jit`, `/aot`, `/customize`). Unlike the Examples
 * pages they have bespoke content rather than a code-comparison, so each carries its own template and
 * the highlighted snippet HTML it binds with `[innerHTML]`. `feature` supplies the heading/description.
 */

@JsExport
@Component(
    selector = "app-setup-page",
    templateUrl = "./setup.page.html",
    styleUrls = ["./getstarted.css", "../explore-content.css"],
)
class SetupPageComponent {
    val feature: Feature = featureById("setup")
    val setupGradle: String = setupGradleHtml()
    val setupComponent: String = setupComponentHtml()
}

@JsExport
@Component(
    selector = "app-jit-page",
    templateUrl = "./jit.page.html",
    styleUrls = ["./getstarted.css", "../explore-content.css"],
)
class JitPageComponent {
    val feature: Feature = featureById("jit")
    val jitEntry: String = jitEntryHtml()
    val jitWebpack: String = jitWebpackHtml()
    val jitRun: String = highlightGradleCmd("./gradlew :app:jsBrowserDevelopmentRun -t -PangularKt.port=8080")
}

@JsExport
@Component(
    selector = "app-aot-page",
    templateUrl = "./aot.page.html",
    styleUrls = ["./getstarted.css", "../explore-content.css"],
)
class AotPageComponent {
    val feature: Feature = featureById("aot")
    val aotEntry: String = aotEntryHtml()
    val aotServe: String = highlightGradleCmd("./gradlew :app:aotServe -PangularKt.port=4200  # serve + live reload")
    val aotBuild: String = highlightGradleCmd("./gradlew :app:aotBuild  # optimized production bundle")
}

@JsExport
@Component(
    selector = "app-customize-page",
    templateUrl = "./customize.page.html",
    styleUrls = ["./getstarted.css", "../explore-content.css"],
)
class CustomizePageComponent {
    val feature: Feature = featureById("customize")
    val customizeAot: String = customizeAotHtml()
    val customizeJit: String = customizeJitHtml()
}
