package at.angular.processor

import at.angular.processor.codegen.CodegenFactory
import at.angular.processor.readers.AngularReader
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class AngularKtProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        AngularKtProcessor(AngularReader(), CodegenFactory(environment).create())
}
