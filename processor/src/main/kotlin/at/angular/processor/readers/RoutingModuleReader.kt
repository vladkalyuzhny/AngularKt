package at.angular.processor.readers

import at.angular.processor.models.RoutingModuleModel
import at.angular.processor.utils.annotation
import at.angular.processor.utils.boolArg
import at.angular.processor.utils.fqn
import at.angular.processor.utils.readRoutes
import at.angular.router.RoutingModule
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal class RoutingModuleReader {
    fun read(
        declaration: KSClassDeclaration,
        lazy: Boolean = false,
        forChild: Boolean = false,
    ): RoutingModuleModel {
        val annotation = declaration.annotation(RoutingModule::class.qualifiedName)
        return RoutingModuleModel(
            fqn = declaration.fqn(),
            simpleName = declaration.simpleName.asString(),
            containingFile = declaration.containingFile,
            routes = annotation.readRoutes(),
            forChild = forChild,
            lazy = lazy,
            useHash = annotation.boolArg("useHash") ?: false,
            preloadAllModules = annotation.boolArg("preloadAllModules") ?: false,
        )
    }
}