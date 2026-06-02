package at.angular.processor.readers

import at.angular.core.Component
import at.angular.core.Directive
import at.angular.core.Input
import at.angular.core.Output
import at.angular.processor.models.ComponentModel
import at.angular.processor.utils.annotation
import at.angular.processor.utils.bindings
import at.angular.processor.utils.boolArg
import at.angular.processor.utils.classArrayArg
import at.angular.processor.utils.ctorParams
import at.angular.processor.utils.enumArg
import at.angular.processor.utils.fqn
import at.angular.processor.utils.hostBindings
import at.angular.processor.utils.hostListeners
import at.angular.processor.utils.queries
import at.angular.processor.utils.resolveStyles
import at.angular.processor.utils.resolveTemplate
import at.angular.processor.utils.stringArg
import at.angular.processor.utils.stringArrayArg
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal class ComponentReader {
    fun read(declaration: KSClassDeclaration, isComponent: Boolean): ComponentModel {
        val fqn = if (isComponent) Component::class.qualifiedName else Directive::class.qualifiedName
        val annotation = declaration.annotation(fqn)
        return ComponentModel(
            fqn = declaration.fqn(),
            simpleName = declaration.simpleName.asString(),
            containingFile = declaration.containingFile,
            ctorParams = declaration.ctorParams(),
            isComponent = isComponent,
            selector = annotation.stringArg("selector"),
            template = declaration.resolveTemplate(
                template = annotation.stringArg("template"),
                templateUrl = annotation.stringArg("templateUrl")
            ),
            templateUrl = null,
            styles = declaration.resolveStyles(
                styles = annotation.stringArrayArg("styles"),
                styleUrls = annotation.stringArrayArg("styleUrls")
            ),
            styleUrls = emptyList(),
            inputs = declaration.bindings(Input::class.qualifiedName),
            outputs = declaration.bindings(Output::class.qualifiedName),
            standalone = annotation.boolArg("standalone") ?: false,
            imports = if (isComponent) annotation.classArrayArg("imports") else emptyList(),
            // @Component-only knobs; omitted (null) when the Angular default applies, so the
            // generated metadata stays minimal — exactly like the standalone/styles fields.
            encapsulation = if (isComponent) annotation.enumArg("encapsulation")?.takeIf { it != "Emulated" } else null,
            changeDetection = if (isComponent) annotation.enumArg("changeDetection")?.takeIf { it != "Default" } else null,
            hostBindings = declaration.hostBindings(),
            hostListeners = declaration.hostListeners(),
            queries = declaration.queries(),
        )
    }
}
