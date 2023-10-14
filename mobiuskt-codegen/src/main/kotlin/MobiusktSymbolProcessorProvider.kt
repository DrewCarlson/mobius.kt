package kt.mobius.gen

import com.google.devtools.ksp.processing.*

class MobiusktSymbolProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MobiusktSymbolProcessor(
            codeGenerator = environment.codeGenerator,
        )
    }
}
