package at.angular.processor.readers

import at.angular.core.NgModule
import at.angular.processor.models.NgModuleModel
import at.angular.processor.utils.annotation
import at.angular.processor.utils.classArrayArg
import at.angular.processor.utils.fqn
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal class NgModuleReader {
    fun read(declaration: KSClassDeclaration): NgModuleModel {
        val annotation = declaration.annotation(NgModule::class.qualifiedName)
        return NgModuleModel(
            fqn = declaration.fqn(),
            simpleName = declaration.simpleName.asString(),
            containingFile = declaration.containingFile,
            declarations = annotation.classArrayArg("declarations"),
            imports = annotation.classArrayArg("imports"),
            exports = annotation.classArrayArg("exports"),
            providers = annotation.classArrayArg("providers"),
            bootstrap = annotation.classArrayArg("bootstrap"),
        )
    }
}
