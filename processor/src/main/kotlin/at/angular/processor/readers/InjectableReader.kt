package at.angular.processor.readers

import at.angular.core.Injectable
import at.angular.processor.models.InjectableModel
import at.angular.processor.utils.annotation
import at.angular.processor.utils.ctorParams
import at.angular.processor.utils.fqn
import at.angular.processor.utils.stringArg
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal class InjectableReader {
    fun read(declaration: KSClassDeclaration) = InjectableModel(
        fqn = declaration.fqn(),
        simpleName = declaration.simpleName.asString(),
        containingFile = declaration.containingFile,
        ctorParams = declaration.ctorParams(),
        providedIn = declaration.annotation(Injectable::class.qualifiedName)
            .stringArg("providedIn"),
    )
}
