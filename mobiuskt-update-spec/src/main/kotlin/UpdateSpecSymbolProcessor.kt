package kt.mobius.gen

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*
import kt.mobius.*

class UpdateSpecSymbolProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val updateSpecs = resolver.getSymbolsWithAnnotation(UpdateSpec::class.qualifiedName!!)
        updateSpecs.map(::generateSpecFile).forEach { specFile ->
            specFile.writeTo(codeGenerator, specFile.kspDependencies(true))
        }
        return emptyList()
    }

    private fun generateSpecFile(modelSymbol: KSAnnotated): FileSpec {
        val specName = modelSymbol.toString().removeSuffix("Model") + "UpdateSpec"

        val annotationArgs = modelSymbol.annotations.first().arguments
        val eventSymbol = annotationArgs.first { it.name?.getShortName() == "eventClass" }.value
        val effectSymbol = annotationArgs.first { it.name?.getShortName() == "effectClass" }.value

        val modelClassDec = modelSymbol as KSClassDeclaration
        val eventClassDec = ((eventSymbol as KSType).declaration) as KSClassDeclaration
        val effectClassDec = ((effectSymbol as KSType).declaration) as KSClassDeclaration

        val modelClassName = modelClassDec.toClassName()
        val eventClassName = eventClassDec.toClassName()
        val effectClassName = effectClassDec.toClassName()

        val eventSubclasses = eventClassDec.getSealedSubclasses()
        val objects = eventSubclasses.filter { it.classKind == ClassKind.OBJECT }
        val dataClasses = eventSubclasses.filter { it.classKind == ClassKind.CLASS }
        val sealedClasses = eventSubclasses.filter { it.modifiers.contains(Modifier.SEALED) }

        val nextReturnTypeName = Next::class.asTypeName()
            .parameterizedBy(modelClassName, effectClassName)

        return FileSpec.builder(modelClassDec.packageName.asString(), specName)
            .addType(
                TypeSpec.interfaceBuilder(specName)
                    .addSuperinterface(
                        Update::class.asTypeName()
                            .parameterizedBy(modelClassName, eventClassName, effectClassName)
                    )
                    .addFunction(FunSpec.builder("update")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("model", modelClassName)
                        .addParameter("event", eventClassName)
                        .returns(nextReturnTypeName)
                        .addCode(CodeBlock.builder()
                            .add("@Suppress(\"REDUNDANT_ELSE_IN_WHEN\")")
                            .add("\nreturn when (event) {\n")
                            .indent()
                            .apply {
                                objects.forEach {
                                    addStatement(
                                        "%T -> ${it.asFunName()}(%L)",
                                        it.toClassName(),
                                        "model"
                                    )
                                }
                                (dataClasses + sealedClasses).forEach {
                                    addStatement(
                                        "is %T -> ${it.asFunName()}(%L, %L)",
                                        it.toClassName(),
                                        "model",
                                        "event",
                                    )
                                }
                                add("else -> error(\"unexpected missing branch\")")
                            }
                            .unindent()
                            .add("\n}\n")
                            .build())
                        .build())
                    .addFunctions(objects.map {
                        FunSpec.builder(it.asFunName())
                            .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
                            .addParameter("model", modelClassName)
                            .returns(nextReturnTypeName)
                            .build()
                    }.toList())
                    .addFunctions(dataClasses.map {
                        FunSpec.builder(it.asFunName())
                            .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
                            .addParameter("model", modelClassName)
                            .addParameter("event", it.toClassName())
                            .returns(nextReturnTypeName)
                            .build()
                    }.toList())
                    .addFunctions(sealedClasses.map {
                        FunSpec.builder(it.asFunName())
                            .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
                            .addParameter("model", modelClassName)
                            .addParameter("event", it.toClassName())
                            .returns(nextReturnTypeName)
                            .build()
                    }.toList())
                    .addOriginatingKSFile(modelSymbol.containingFile!!)
                    .addOriginatingKSFile(eventClassDec.containingFile!!)
                    .addOriginatingKSFile(effectClassDec.containingFile!!)
                    .build()
            )
            .build()
    }

    private fun KSDeclaration.asFunName(): String {
        return simpleName.asString().replaceFirstChar(Char::lowercaseChar)
    }
}
