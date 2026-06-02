package at.angular.processor.codegen

import at.angular.processor.models.AngularModel

/**
 * A code-generation strategy. The processor picks one per `angularKt.mode`
 * ([JitCodegen] or [AotCodegen]) and hands it the parsed [AngularModel]
 */
interface Codegen {
    fun generate(model: AngularModel)
}
