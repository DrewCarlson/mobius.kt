package kt.mobius.gen

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.kspDependencies
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import kt.mobius.Next
import kt.mobius.Update

object UpdateSpecGenerator {

    fun generate(updateSymbol: KSAnnotated, codeGenerator: CodeGenerator) {
        val updateParent = (updateSymbol as KSClassDeclaration).superTypes.firstOrNull { typeRef ->
            typeRef.element?.typeArguments?.size == 3
        }
        checkNotNull(updateParent) {
            "Classes annotated with @GenerateUpdate must implement Update<M, E, F>: ${updateSymbol.simpleName}"
        }
        // Extract Update class type information and process names
        val typeArgs = updateParent.element?.typeArguments.orEmpty()
        val modelClassDec = checkNotNull(typeArgs[0].type?.resolve()).declaration as KSClassDeclaration
        val eventClassDec = checkNotNull(typeArgs[1].type?.resolve()).declaration as KSClassDeclaration
        val effectClassDec = checkNotNull(typeArgs[2].type?.resolve()).declaration as KSClassDeclaration

        val modelClassName = modelClassDec.toClassName()
        val eventClassName = eventClassDec.toClassName()
        val effectClassName = effectClassDec.toClassName()
        val updateTypeName = Update::class.asTypeName()
            .parameterizedBy(modelClassName, eventClassName, effectClassName)

        // Collect and sort event subclasses
        val eventSubclasses = eventClassDec.getSealedSubclasses()
        val sealedClasses = eventSubclasses.filterSealed()
        val objects = eventSubclasses.filterObjects()
        val dataClasses = eventSubclasses.filterDataClasses()

        val nextReturnTypeName = Next::class.asTypeName()
            .parameterizedBy(modelClassName, effectClassName)
        val specName = modelClassDec.createSpecName()

        // Create the function representations for each event
        val objectFunctions = objects.map { createObjectFunctionSpec(it, modelClassName, nextReturnTypeName) }.toList()
        val dataClassFunctions = dataClasses.map {
            createDataClassFunctionSpec(it, modelClassName, nextReturnTypeName)
        }.toList()
        val sealedClassFunctions = sealedClasses.map { sealedClass ->
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
        }.flatten().toList()

        // Construct and write the spec file
        val whenBlock = createWhenBlock(objects, dataClasses, sealedClasses, specName)
        val specFile = FileSpec.builder(modelClassDec.packageName.asString(), specName)
            .addType(
                TypeSpec.interfaceBuilder(specName)
                    .addModifiers(KModifier.INTERNAL)
                    .addSuperinterface(updateTypeName)
                    .addUpdateOverride(modelClassName, eventClassName, nextReturnTypeName, whenBlock)
                    .addFunctions(objectFunctions)
                    .addFunctions(dataClassFunctions)
                    .addFunctions(sealedClassFunctions)
                    .addOriginatingKSFile(updateSymbol.containingFile!!)
                    .addOriginatingKSFile(modelClassDec.containingFile!!)
                    .addOriginatingKSFile(eventClassDec.containingFile!!)
                    .addOriginatingKSFile(effectClassDec.containingFile!!)
                    .build()
            )
            .build()
        specFile.writeTo(codeGenerator, specFile.kspDependencies(true))
    }

    private fun TypeSpec.Builder.addUpdateOverride(
        modelClassName: ClassName,
        eventClassName: ClassName,
        nextReturnTypeName: ParameterizedTypeName,
        whenBlock: CodeBlock
    ): TypeSpec.Builder {
        return addFunction(
            FunSpec.builder("update")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("model", modelClassName)
                .addParameter("event", eventClassName)
                .returns(nextReturnTypeName)
                .addCode(whenBlock)
                .build()
        )
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
            .addObjectBranches(objects)
            .addDataClassBranches(dataClasses)
            .addSealedClassBranches(sealedClasses, specName)
            .add("else -> error(%P)", "$specName: unexpected missing branch for \$event")
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

    private fun CodeBlock.Builder.addSealedClassBranches(
        declarations: Sequence<KSClassDeclaration>,
        specName: String
    ): CodeBlock.Builder {
        declarations.forEach { sealedClass ->
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
        return this
    }

    private fun CodeBlock.Builder.addDataClassBranches(declarations: Sequence<KSClassDeclaration>): CodeBlock.Builder {
        declarations.forEach { declaration ->
            addStatement(
                "is %T -> ${declaration.asFunName()}(%L, %L)",
                declaration.toClassName(),
                "model",
                "event",
            )
        }
        return this
    }

    private fun CodeBlock.Builder.addObjectBranches(declarations: Sequence<KSClassDeclaration>): CodeBlock.Builder {
        declarations.forEach { declaration ->
            addStatement(
                "%T -> ${declaration.asFunName()}(%L)",
                declaration.toClassName(),
                "model"
            )
        }
        return this
    }

    private fun KSClassDeclaration.createSpecName(): String {
        return "${simpleName.asString().removeSuffix("Model")}GeneratedUpdate"
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