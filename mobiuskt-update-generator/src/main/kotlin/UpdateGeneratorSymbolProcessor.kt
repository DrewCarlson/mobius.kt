package kt.mobius.gen

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*
import kt.mobius.*

class UpdateGeneratorSymbolProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val targets = resolver.getSymbolsWithAnnotation(GenerateUpdate::class.qualifiedName!!)
        targets.map(::generateSpecFile).forEach { specFile ->
            specFile.writeTo(codeGenerator, specFile.kspDependencies(true))
        }
        return emptyList()
    }

    private fun generateSpecFile(updateSymbol: KSAnnotated): FileSpec {
        val updateParent = (updateSymbol as KSClassDeclaration).superTypes.firstOrNull { typeRef ->
            typeRef.element?.typeArguments?.size == 3
        }
        checkNotNull(updateParent) {
            "Classes annotated with @GenerateUpdate must implement Update<M, E, F>: ${updateSymbol.simpleName}"
        }
        val typeArgs = updateParent.element?.typeArguments.orEmpty()
        val modelClassDec = checkNotNull(typeArgs[0].type?.resolve()).declaration as KSClassDeclaration
        val eventClassDec = checkNotNull(typeArgs[1].type?.resolve()).declaration as KSClassDeclaration
        val effectClassDec = checkNotNull(typeArgs[2].type?.resolve()).declaration as KSClassDeclaration

        val modelClassName = modelClassDec.toClassName()
        val eventClassName = eventClassDec.toClassName()
        val effectClassName = effectClassDec.toClassName()

        val eventSubclasses = eventClassDec.getSealedSubclasses()
        val objects = eventSubclasses.filter { it.classKind == ClassKind.OBJECT }
        val dataClasses = eventSubclasses.filter {
            it.classKind == ClassKind.CLASS || it.classKind == ClassKind.INTERFACE
        }
        val sealedClasses = eventSubclasses.filter { it.modifiers.contains(Modifier.SEALED) }

        val nextReturnTypeName = Next::class.asTypeName()
            .parameterizedBy(modelClassName, effectClassName)
        val specName = "${modelClassDec.simpleName.asString().removeSuffix("Model")}GeneratedUpdate"

        return FileSpec.builder(modelClassDec.packageName.asString(), specName)
            .addType(
                TypeSpec.interfaceBuilder(specName)
                    .addModifiers(KModifier.INTERNAL)
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
                    .addOriginatingKSFile(updateSymbol.containingFile!!)
                    .addOriginatingKSFile(modelClassDec.containingFile!!)
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
