package kt.mobius.gen

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

class MobiusktSymbolProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val targets = resolver.getSymbolsWithAnnotation(GenerateUpdate::class.qualifiedName!!)
        targets.forEach { updateSymbol ->
            UpdateSpecGenerator.generate(updateSymbol, codeGenerator)
        }
        return emptyList()
    }
}
