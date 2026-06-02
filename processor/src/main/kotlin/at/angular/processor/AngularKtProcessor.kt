package at.angular.processor

import at.angular.processor.codegen.Codegen
import at.angular.processor.readers.AngularReader
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

class AngularKtProcessor(
    private val reader: AngularReader,
    private val codegen: Codegen,
) : SymbolProcessor {
    private var isFinished = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (!isFinished) {
            codegen.generate(reader.read(resolver))
            isFinished = true
        }
        return emptyList()
    }
}
