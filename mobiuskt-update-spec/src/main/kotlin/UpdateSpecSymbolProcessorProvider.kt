package kt.mobius.gen

import com.google.devtools.ksp.processing.*

class UpdateSpecSymbolProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return UpdateSpecSymbolProcessor(
            codeGenerator = environment.codeGenerator,
        )
    }
}
