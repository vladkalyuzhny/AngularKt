package at.angular.processor.models

import com.google.devtools.ksp.symbol.KSFile

data class InjectableModel(
    override val fqn: String,
    override val simpleName: String,
    override val containingFile: KSFile?,
    override val ctorParams: List<CtorParam>,
    val providedIn: String?,
) : NgDeclaration
