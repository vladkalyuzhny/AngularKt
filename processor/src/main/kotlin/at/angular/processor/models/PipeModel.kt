package at.angular.processor.models

import com.google.devtools.ksp.symbol.KSFile

data class PipeModel(
    override val fqn: String,
    override val simpleName: String,
    override val containingFile: KSFile?,
    override val ctorParams: List<CtorParam>,
    val name: String?,
    val pure: Boolean?,
    val standalone: Boolean,
) : NgDeclaration
