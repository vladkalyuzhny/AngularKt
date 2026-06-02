package at.angular.processor.readers

import at.angular.core.Pipe
import at.angular.processor.models.PipeModel
import at.angular.processor.utils.annotation
import at.angular.processor.utils.boolArg
import at.angular.processor.utils.ctorParams
import at.angular.processor.utils.fqn
import at.angular.processor.utils.stringArg
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal class PipeReader {
    fun read(declaration: KSClassDeclaration): PipeModel {
        val annotation = declaration.annotation(Pipe::class.qualifiedName)
        return PipeModel(
            fqn = declaration.fqn(),
            simpleName = declaration.simpleName.asString(),
            containingFile = declaration.containingFile,
            ctorParams = declaration.ctorParams(),
            name = annotation.stringArg("name"),
            pure = annotation.boolArg("pure"),
            standalone = annotation.boolArg("standalone") ?: false,
        )
    }
}
