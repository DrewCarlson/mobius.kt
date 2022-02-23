package kt.mobius.gen

import com.google.devtools.ksp.processing.*

class UpdateGeneratorSymbolProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return UpdateGeneratorSymbolProcessor(
            codeGenerator = environment.codeGenerator,
        )
    }
}
