package at.angular.processor.codegen

import at.angular.processor.bridge.bridgeFor
import at.angular.processor.bridge.references.TsRefResolver
import at.angular.processor.models.AngularModel
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import java.io.OutputStreamWriter

/**
 * AOT strategy: emits one thin TypeScript bridge per declarable, compiled by Angular's own
 * toolchain (so `@angular/compiler` stays out of the runtime). Each bridge carries the public
 * Angular decorator and `extends` the Kotlin-compiled class; constructor DI becomes
 * `super(inject(Dep), ...)`. A [bridgeFor] strategy builds each declarable's typed view; the
 * `aot` Handlebars templates lay out the file shape. This class only owns the KSP file plumbing.
 */
class AotCodegen(
    private val codeGenerator: CodeGenerator,
    /** npm module the bridges import the Kotlin classes from */
    private val tsModule: String,
    private val profile: AngularProfile,
    /** Third-party `@file:JsModule` externals (FQN → npm module) discovered by the Gradle plugin. */
    private val externalModules: Map<String, String> = emptyMap(),
    /**
     * FQN of the standalone root component to bootstrap via `bootstrapApplication`, from the
     * `angularKt { bootstrapComponent = … }` DSL. Null → the AOT build uses the classic `@NgModule`
     * (the one with a `bootstrap` array) instead.
     */
    private val bootstrapComponentFqn: String? = null,
) : Codegen {
    private val renderer = TemplateRenderer("aot")

    override fun generate(model: AngularModel) {
        val resolver = TsRefResolver(model.ownClasses, tsModule, externalModules)
        writeMainEntry(model)
        model.ngDeclarations.forEach { declaration ->
            val view = bridgeFor(declaration, resolver, profile).createView()
            write(declaration.simpleName, listOfNotNull(declaration.containingFile), aggregating = false) {
                renderer.render(view.templateName, view)
            }
        }
    }

    /**
     * Emits the AOT `main.ts` entry. If `angularKt.bootstrapComponent` names a component, generates a
     * standalone bootstrap that hands that root to the Kotlin `main` (where functional providers are
     * supplied); otherwise the legacy module bootstrap of the root `@NgModule` (the one with a
     * non-empty `bootstrap` array). Emits nothing if neither exists (e.g. a library).
     */
    private fun writeMainEntry(model: AngularModel) {
        val standaloneRoot = bootstrapComponentFqn?.let { fqn ->
            model.components.firstOrNull { it.fqn == fqn }
                ?: error("angularKt.bootstrapComponent = '$fqn' matches no @Component in the AOT sources.")
        }
        if (standaloneRoot != null) {
            // The root @RoutingModule's forRoot must be provided at the environment injector. Its
            // decorator lives on the generated TS bridge (the Kotlin class is bare in AOT), so the
            // entry imports the bridge and supplies importProvidersFrom(bridge) to bootstrap.
            val rootRouting = model.routingModules.firstOrNull { !it.forChild && !it.lazy }
            // main.ts aggregates over the bootstrap component AND the root @RoutingModule, so it must
            // declare BOTH (plus every routing file) as sources. Otherwise KSP never adds the routing
            // file to the incremental dirty set, and an edit to any unrelated file regenerates main.ts
            // from a model that no longer sees the @RoutingModule — silently dropping importProvidersFrom
            // and crashing at runtime with `NullInjectorError: No provider for ActivatedRoute`.
            val sources = (listOfNotNull(standaloneRoot.containingFile) +
                model.routingModules.mapNotNull { it.containingFile }).distinct()
            write("main", sources, aggregating = true) {
                renderer.render(
                    name = "main-standalone.ts",
                    model = mapOf(
                        "tsModule" to tsModule,
                        "rootComponent" to standaloneRoot.simpleName,
                        "routingModule" to (rootRouting?.simpleName ?: ""),
                    )
                )
            }
            return
        }

        val rootModule = model.modules.firstOrNull { it.bootstrap.isNotEmpty() } ?: return
        write("main", listOfNotNull(rootModule.containingFile), aggregating = true) {
            renderer.render(
                name = "main.ts",
                model = mapOf("tsModule" to tsModule, "moduleName" to rootModule.simpleName)
            )
        }
    }

    private fun write(path: String, sources: List<KSFile>, aggregating: Boolean, render: () -> String) {
        val output = codeGenerator.createNewFileByPath(
            dependencies = Dependencies(
                aggregating = aggregating,
                sources = sources.toTypedArray()
            ),
            path = path,
            extensionName = "ts",
        )
        OutputStreamWriter(output, Charsets.UTF_8).use { it.write(render()) }
    }
}
