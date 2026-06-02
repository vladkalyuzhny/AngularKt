package at.angular.processor.models

import com.google.devtools.ksp.symbol.KSFile

/**
 * [NgDeclaration] is the common shape of every annotated class we generate code for
 */
sealed interface NgDeclaration {
    val fqn: String
    val simpleName: String
    val containingFile: KSFile?
    val ctorParams: List<CtorParam>
}
