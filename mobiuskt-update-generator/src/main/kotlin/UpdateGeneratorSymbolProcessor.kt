package kt.mobius.gen

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*
import kt.mobius.*

@OptIn(KspExperimental::class)
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
        val sealedClasses = eventSubclasses.filterSealed()
        val objects = eventSubclasses.filterObjects()
        val dataClasses = eventSubclasses.filterDataClasses()

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
                    .addFunction(
                        FunSpec.builder("update")
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameter("model", modelClassName)
                            .addParameter("event", eventClassName)
                            .returns(nextReturnTypeName)
                            .addCode(createWhenBlock(objects, dataClasses, sealedClasses, specName))
                            .build()
                    )
                    .addFunctions(objects.map {
                        createObjectFunctionSpec(it, modelClassName, nextReturnTypeName)
                    }.toList())
                    .addFunctions(dataClasses.map {
                        createDataClassFunctionSpec(it, modelClassName, nextReturnTypeName)
                    }.toList())
                    .addFunctions(sealedClasses.map { sealedClass ->
                        val subClasses = sealedClass.getSealedSubclasses()
                        val subSealedClasses = subClasses.flatMap {
                            if (it.isAnnotationPresent(DisableSubtypeSpec::class)) {
                                emptySequence()
                            } else {
                                it.getSealedSubclasses()
                            }
                        }
                        (subClasses + subSealedClasses).filterObjects().map {
                            createObjectFunctionSpec(it, modelClassName, nextReturnTypeName)
                        } + (subClasses + subSealedClasses).filterDataClasses().map {
                            createDataClassFunctionSpec(it, modelClassName, nextReturnTypeName)
                        }
                    }.flatten().toList())
                    .addOriginatingKSFile(updateSymbol.containingFile!!)
                    .addOriginatingKSFile(modelClassDec.containingFile!!)
                    .addOriginatingKSFile(eventClassDec.containingFile!!)
                    .addOriginatingKSFile(effectClassDec.containingFile!!)
                    .build()
            )
            .build()
    }

    private fun createWhenBlock(
        objects: Sequence<KSClassDeclaration>,
        dataClasses: Sequence<KSClassDeclaration>,
        sealedClasses: Sequence<KSClassDeclaration>,
        specName: String,
        returnValue: Boolean = true,
    ): CodeBlock {
        return CodeBlock.builder()
            .apply {
                if (returnValue) {
                    add("@Suppress(\"REDUNDANT_ELSE_IN_WHEN\")")
                    add("\nreturn when (event) {\n")
                } else {
                    add("when (event) {\n")
                }
            }
            .indent()
            .apply {
                objects.forEach {
                    addObjectBranch(it)
                }
                dataClasses.forEach {
                    addDataClassBranch(it)
                }
                sealedClasses.forEach { sealedClass ->
                    val sealedSubclasses = sealedClass.getSealedSubclasses()
                    val subObjects = sealedSubclasses.filterObjects()
                    val subDataClasses = sealedSubclasses.filterDataClasses()
                    val subSealedClasses = sealedSubclasses.filterSealed()

                    add(
                        CodeBlock.builder()
                            .addStatement("is %T -> ", sealedClass.toClassName())
                            .add(createWhenBlock(subObjects, subDataClasses, subSealedClasses, specName, false))
                            .build()
                    )
                }
                add("else -> error(\"$specName: unexpected missing branch for \$event\")")
            }
            .unindent()
            .add("\n}\n")
            .build()
    }

    private fun Sequence<KSClassDeclaration>.filterObjects() =
        filter { it.classKind == ClassKind.OBJECT }

    private fun Sequence<KSClassDeclaration>.filterDataClasses() =
        filter {
            (it.classKind == ClassKind.CLASS && !it.isSealed())
                    || (it.classKind == ClassKind.INTERFACE && !it.isSealed())
                    || it.isAnnotationPresent(DisableSubtypeSpec::class)
        }

    private fun Sequence<KSClassDeclaration>.filterSealed() =
        filter {
            it.modifiers.contains(Modifier.SEALED) && !it.isAnnotationPresent(DisableSubtypeSpec::class)
        }

    private fun createDataClassFunctionSpec(
        it: KSClassDeclaration,
        modelClassName: ClassName,
        nextReturnTypeName: ParameterizedTypeName
    ) = FunSpec.builder(it.asFunName())
        .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
        .addParameter("model", modelClassName)
        .addParameter("event", it.toClassName())
        .returns(nextReturnTypeName)
        .build()

    private fun createObjectFunctionSpec(
        it: KSClassDeclaration,
        modelClassName: ClassName,
        nextReturnTypeName: ParameterizedTypeName
    ) = FunSpec.builder(it.asFunName())
        .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
        .addParameter("model", modelClassName)
        .returns(nextReturnTypeName)
        .build()

    private fun CodeBlock.Builder.addDataClassBranch(ksClass: KSClassDeclaration) {
        addStatement(
            "is %T -> ${ksClass.asFunName()}(%L, %L)",
            ksClass.toClassName(),
            "model",
            "event",
        )
    }

    private fun CodeBlock.Builder.addObjectBranch(it: KSClassDeclaration) {
        addStatement(
            "%T -> ${it.asFunName()}(%L)",
            it.toClassName(),
            "model"
        )
    }

    private fun KSDeclaration.asFunName(): String {
        val name = if (this is KSClassDeclaration) {
            toClassName().simpleNames.drop(1).joinToString("")
        } else {
            simpleName.asString()
        }
        return name.replaceFirstChar(Char::lowercaseChar)
    }

    private fun KSDeclaration.isSealed(): Boolean = modifiers.contains(Modifier.SEALED)
}
