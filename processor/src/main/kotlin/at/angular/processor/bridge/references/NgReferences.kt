package at.angular.processor.bridge.references

import at.angular.processor.models.QueryKind

/**
 * The single catalog of `@angular` symbols a bridge can reference.
 */
object NgReferences {
    /** The `inject` function a generated constructor calls; lower-cased to mirror its TS spelling. */
    val inject = core("inject")
    val NgModule = core("NgModule")
    val Pipe = core("Pipe")
    val Injectable = core("Injectable")
    val RouterModule = router("RouterModule")
    val Routes = router("Routes")
    val provideRouter = router("provideRouter")
    val withHashLocation = router("withHashLocation")
    val withPreloading = router("withPreloading")
    val PreloadAllModules = router("PreloadAllModules")

    /** `@Component` or `@Directive`, depending on whether the declarable has a view. */
    fun decorator(isComponent: Boolean): TsReference = core(if (isComponent) "Component" else "Directive")

    /** The `@angular/core` class the AOT path instantiates (`new ViewChild(...)`). */
    fun queryClass(kind: QueryKind): TsReference = core(kind.aotClass)

    /** `ViewEncapsulation.<entry>` — imports `ViewEncapsulation` from `@angular/core`. */
    fun encapsulation(entry: String): TsReference =
        TsReference.Member("@angular/core", "ViewEncapsulation", entry)

    /** `ChangeDetectionStrategy.<entry>` — imports `ChangeDetectionStrategy` from `@angular/core`. */
    fun changeDetection(entry: String): TsReference =
        TsReference.Member("@angular/core", "ChangeDetectionStrategy", entry)

    /**
     * The `@angular` import for a `:lib` external FQN, or null if the type isn't one of ours. The
     * imported name is the FQN's last segment — a `:lib` external keeps its TypeScript name.
     */
    fun externalFor(fqn: String): TsReference.External? =
        externalModules[fqn]?.let { module -> TsReference.External(module, fqn.substringAfterLast('.')) }

    private fun core(name: String) = TsReference.External("@angular/core", name)
    private fun router(name: String) = TsReference.External("@angular/router", name)

    /**
     * AngularKt's own `:lib` externals, mapped to the `@angular` module each symbol is imported
     * from. This catalog is intentionally limited to the framework's first-party bindings —
     * third-party externals (Angular Material, any consumer-generated Karakum module) are NOT
     * listed here; the Gradle plugin discovers those from their `@file:JsModule` annotations and
     * feeds them to [TsRefResolver]. See [TsRefResolver.externalModules].
     */
    private val externalModules: Map<String, String> = mapOf(
        "at.angular.core.EventEmitter" to "@angular/core",
        "at.angular.core.ElementRef" to "@angular/core",
        "at.angular.core.TemplateRef" to "@angular/core",
        "at.angular.core.ViewContainerRef" to "@angular/core",
        "at.angular.core.QueryList" to "@angular/core",
        "at.angular.common.CommonModule" to "@angular/common",
        "at.angular.common.http.HttpClient" to "@angular/common/http",
        "at.angular.common.http.HttpClientModule" to "@angular/common/http",
        "at.angular.router.Router" to "@angular/router",
        "at.angular.router.ActivatedRoute" to "@angular/router",
        "at.angular.router.RouterModule" to "@angular/router",
        "at.angular.forms.FormsModule" to "@angular/forms",
        "at.angular.forms.ReactiveFormsModule" to "@angular/forms",
        "at.angular.platformBrowser.BrowserModule" to "@angular/platform-browser",
        "at.angular.platformBrowser.DomSanitizer" to "@angular/platform-browser",
        "at.angular.platformBrowser.animations.BrowserAnimationsModule" to "@angular/platform-browser/animations",
    )
}
