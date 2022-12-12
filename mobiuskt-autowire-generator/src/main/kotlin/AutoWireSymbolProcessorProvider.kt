package kt.mobius.gen

import com.google.devtools.ksp.processing.*

class AutoWireSymbolProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AutoWireSymbolProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}
