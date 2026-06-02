package at.angular.processor.models

import com.google.devtools.ksp.symbol.KSFile

data class NgModuleModel(
    override val fqn: String,
    override val simpleName: String,
    override val containingFile: KSFile?,
    val declarations: List<String>,
    val imports: List<String>,
    val exports: List<String>,
    val providers: List<String>,
    val bootstrap: List<String>,
) : NgDeclaration {
    override val ctorParams: List<CtorParam> get() = emptyList()
}
