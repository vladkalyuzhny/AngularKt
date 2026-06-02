package at.angular.processor.codegen

import at.angular.processor.models.AngularModel
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import java.io.OutputStreamWriter

/**
 * JIT strategy: emits a flat manifest of `registerX(...)` calls — one per declarable
 * — into `registerAngularKt()`. All the Kotlin/JS ↔ Angular plumbing lives in the
 * hand-written [at.angular.runtime] library; this only feeds the parsed model to the
 * template, which builds each call from it.
 */
class JitCodegen(
    private val codeGenerator: CodeGenerator,
    private val profile: AngularProfile,
) : Codegen {
    private val renderer = TemplateRenderer("jit")

    override fun generate(model: AngularModel) {
        val sourceFiles = model.ngDeclarations.mapNotNull { it.containingFile }
            .distinct()
            .toTypedArray()
        val output = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true, *sourceFiles),
            packageName = "at.angular.generated",
            fileName = "AngularKtRegistration",
        )
        val context = mapOf(
            "supportsStandalone" to profile.supportsStandalone,
            "injectables" to model.injectables,
            "components" to model.components,
            "directives" to model.directives,
            "pipes" to model.pipes,
            "routingModules" to model.routingModules,
            "modules" to model.modules,
        )
        OutputStreamWriter(output, Charsets.UTF_8).use {
            it.write(renderer.render("registration.kt", context))
        }
    }
}
