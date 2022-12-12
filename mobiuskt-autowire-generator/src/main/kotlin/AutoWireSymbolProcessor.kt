package kt.mobius.gen

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kt.mobius.*
import kt.mobius.autowire.AutoWireEffect
import kt.mobius.autowire.AutoWireEvent
import kt.mobius.autowire.AutoWireInject
import kt.mobius.flow.FlowTransformer
import kt.mobius.flow.subtypeEffectHandler

class AutoWireSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    data class LoopDetails(
        val modelClassName: KSName,
        val eventClassName: KSName,
        val effectClassName: KSName,
        val eventTargets: List<KSFunctionDeclaration>,
        val effectTargets: List<KSFunctionDeclaration>,
    )

    private val loopDetails = mutableListOf<LoopDetails>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val packageName = "demo"
        loopDetails.removeAll { (modelClassName, eventClassName, effectClassName, eventTargets, effectTargets) ->
            val modelClass = resolver.getClassDeclarationByName(modelClassName)!!
            val eventClass = resolver.getClassDeclarationByName(eventClassName)!!
            val effectClass = resolver.getClassDeclarationByName(effectClassName)!!
            // TODO: Generate effect handler
            generateUpdateFunc(packageName, modelClass, eventClass, effectClass, eventTargets)
                .run { writeTo(codeGenerator, kspDependencies(true)) }
            generateEffectHandler(packageName, eventClass, effectClass, effectTargets)
                .run { writeTo(codeGenerator, kspDependencies(true)) }
            true
        }
        // TODO: Check if targets are empty, group by model type
        val eventTargets = resolver.getSymbolsWithAnnotation(AutoWireEvent::class.qualifiedName!!)
        val effectTargets = resolver.getSymbolsWithAnnotation(AutoWireEffect::class.qualifiedName!!)
        val eventClass = createEventClass(packageName, eventTargets)
        val effectClass = createEffectClass(packageName, effectTargets)
        if (eventClass != null && effectClass != null) {
            loopDetails.add(
                LoopDetails(
                    resolver.getKSNameFromString("$packageName.${eventClass.name?.replace("Event", "Model")}"),
                    resolver.getKSNameFromString("$packageName.${eventClass.name}"),
                    resolver.getKSNameFromString("$packageName.${effectClass.name}"),
                    eventTargets.filterIsInstance<KSFunctionDeclaration>().toList(),
                    effectTargets.filterIsInstance<KSFunctionDeclaration>().toList(),
                )
            )
        }
        return emptyList()
    }

    private fun createEventClass(packageName: String, targets: Sequence<KSAnnotated>): TypeSpec? {
        val eventSealedClassName = buildString {
            val target = targets.firstOrNull() ?: return null
            append((target.parent as KSFile).fileName.replaceFirstChar { it.uppercase() }.dropLast(3))
            append("Event")
        }
        val eventTypeSpecs = targets.map { eventTarget ->
            eventTarget as KSFunctionDeclaration
            val eventClassName = buildString {
                val targetName = eventTarget.simpleName.asString().replaceFirstChar { it.uppercase() }
                if (!targetName.startsWith("On")) {
                    append("On")
                }
                append(targetName)
            }
            if (eventTarget.parameters.isEmpty()) {
                error("Functions using @${AutoWireEvent::class.simpleName} start with a `model: Model` parameter: $eventTarget")
            }
            generateSealedTypeChild(eventTarget, eventClassName, packageName, eventSealedClassName)
        }
        val eventClass = TypeSpec.classBuilder(eventSealedClassName)
            .addModifiers(KModifier.SEALED)
            .addTypes(eventTypeSpecs.toList())
            .addOriginatingKSFile(targets.first().containingFile!!)
            .build()
        FileSpec.builder(packageName, eventSealedClassName)
            .addType(eventClass)
            .build()
            .writeTo(codeGenerator, aggregating = false)
        return eventClass
    }

    private fun createEffectClass(packageName: String, targets: Sequence<KSAnnotated>): TypeSpec? {
        val eventSealedClassName = buildString {
            val target = targets.firstOrNull() ?: return null
            append((target.parent as KSFile).fileName.replaceFirstChar { it.uppercase() }.dropLast(3))
            append("Effect")
        }
        val eventTypeSpecs = targets.map { it as KSFunctionDeclaration }
            .map { effectTarget ->
                val eventClassName = effectTarget.simpleName.asString().replaceFirstChar { it.uppercase() }
                generateSealedTypeChild(
                    effectTarget,
                    eventClassName,
                    packageName,
                    eventSealedClassName,
                    isEffect = true
                )
            }
        val effectClass = TypeSpec.classBuilder(eventSealedClassName)
            .addModifiers(KModifier.SEALED)
            .addTypes(eventTypeSpecs.toList())
            .addOriginatingKSFile(targets.first().containingFile!!)
            .build()
        FileSpec.builder(packageName, eventSealedClassName)
            .addType(effectClass)
            .build()
            .writeTo(codeGenerator, aggregating = false)
        return effectClass
    }

    @OptIn(KspExperimental::class)
    private fun generateSealedTypeChild(
        funDeclaration: KSFunctionDeclaration,
        className: String,
        packageName: String,
        sealedClassName: String,
        isEffect: Boolean = false
    ) = if ((isEffect && funDeclaration.parameters.isEmpty()) || (!isEffect && funDeclaration.parameters.size == 1)) {
        TypeSpec.objectBuilder(className)
            .superclass(ClassName(packageName, sealedClassName))
            .build()
    } else {
        val params = funDeclaration.parameters
            .filterNot { it.isAnnotationPresent(AutoWireInject::class) }
            .drop(if (isEffect) 0 else 1) // Drop model param for events
        TypeSpec.classBuilder(className)
            .addModifiers(KModifier.DATA)
            .superclass(ClassName(packageName, sealedClassName))
            .primaryConstructor(FunSpec.constructorBuilder()
                .apply {
                    params.forEach { param ->
                        addParameter(
                            param.name!!.asString(),
                            param.type.toTypeName(),
                        )
                    }
                }
                .build()
            )
            .apply {
                params.forEach { param ->
                    val paramName = param.name!!.asString()
                    addProperty(
                        PropertySpec.builder(paramName, param.type.toTypeName())
                            .initializer(paramName)
                            .build()
                    )
                }
            }
            .build()
    }

    private fun generateUpdateFunc(
        packageName: String,
        modelClassDec: KSClassDeclaration,
        eventClassDec: KSClassDeclaration,
        effectClassDec: KSClassDeclaration,
        eventTargets: List<KSFunctionDeclaration>,
    ): FileSpec {
        val modelClassName = modelClassDec.toClassName()
        val eventClassName = eventClassDec.toClassName()
        val effectClassName = effectClassDec.toClassName()

        val eventSubclasses = eventClassDec.getSealedSubclasses()
        val objects = eventSubclasses.filter { it.classKind == ClassKind.OBJECT }
        val dataClasses = eventSubclasses.filter { it.classKind == ClassKind.CLASS }
        val sealedClasses = eventSubclasses.filter { it.modifiers.contains(Modifier.SEALED) }

        val nextReturnTypeName = Next::class.asTypeName()
            .parameterizedBy(modelClassName, effectClassName)
        val specName = "${modelClassDec.simpleName.asString().removeSuffix("Model")}Update"

        return FileSpec.builder(packageName, specName)
            .addType(
                TypeSpec.classBuilder(specName)
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
                            .add("return when (event) {\n")
                            .indent()
                            .apply {
                                dataClasses.forEach {
                                    val func = eventTargets.first { eventFunc ->
                                        eventFunc.asFunName() == it.asFunName()
                                    }
                                    val params = buildString {
                                        // drop model param
                                        func.parameters.drop(1).forEach { param ->
                                            append(", ")
                                            append(param.name?.asString())
                                            append(" = event.")
                                            append(param.name?.asString())
                                        }
                                    }
                                    addStatement(
                                        "is %T -> ${func.qualifiedName?.asString()}(model$params)",
                                        it.toClassName(),
                                    )
                                }
                                objects.forEach {
                                    val func = eventTargets.first { eventFunc ->
                                        eventFunc.asFunName() == it.asFunName()
                                    }
                                    addStatement(
                                        "%T -> ${func.qualifiedName?.asString()}(%L)",
                                        it.toClassName(),
                                        "model",
                                    )
                                }
                            }
                            .unindent()
                            .add("}\n")
                            .build())
                        .build())
                    .addOriginatingKSFile(modelClassDec.containingFile!!)
                    .addOriginatingKSFile(eventClassDec.containingFile!!)
                    .addOriginatingKSFile(effectClassDec.containingFile!!)
                    .build()
            )
            .build()
    }

    @OptIn(KspExperimental::class)
    private fun generateEffectHandler(
        packageName: String,
        eventClassDec: KSClassDeclaration,
        effectClassDec: KSClassDeclaration,
        effectTargets: List<KSFunctionDeclaration>,
    ): FileSpec {
        val eventClassName = eventClassDec.toClassName()
        val effectClassName = effectClassDec.toClassName()

        val effectSubclasses = effectClassDec.getSealedSubclasses()
        val specName = "${effectClassName.simpleName}Handler"

        val injectParams = mutableSetOf<KSValueParameter>()

        return FileSpec.builder(packageName, specName)
            .addType(
                TypeSpec.classBuilder(specName)
                    .addSuperinterface(
                        FlowTransformer::class.asTypeName()
                            .parameterizedBy(effectClassName, eventClassName)
                    )
                    .addProperty(
                        PropertySpec.builder(
                            "handler",
                            FlowTransformer::class.asTypeName()
                                .parameterizedBy(effectClassName, eventClassName)
                        ).initializer(
                            CodeBlock.builder()
                                .add("kt.mobius.flow.subtypeEffectHandler {\n")
                                .apply {
                                    effectTargets.forEach { func ->
                                        val effectClass = effectSubclasses.first { it.asFunName() == func.asFunName() }
                                        val returnType = func.returnType as KSTypeReference
                                        val extensionReceiver = func.extensionReceiver
                                        val (newInjectParams, dataParams) = func.parameters.partition { param ->
                                            param.isAnnotationPresent(AutoWireInject::class)
                                        }
                                        injectParams.addAll(newInjectParams)
                                        if (returnType.resolve().toClassName() == Unit::class.asClassName()) {
                                            if (extensionReceiver == null) {
                                                if (dataParams.isEmpty()) {
                                                    CodeBlock.builder()
                                                        .add("addAction<%L>", effectClass.toClassName())
                                                        .add(" {\n")
                                                        .indent()
                                                        .add("${func.qualifiedName?.asString()}()")
                                                        .unindent()
                                                        .add("\n}\n")
                                                        .build()
                                                        .run(::add)
                                                } else {
                                                    val injectParamString = buildString {
                                                        newInjectParams.forEachIndexed { index, param ->
                                                            append(param.name?.asString())
                                                            append(" = ")
                                                            append(param.name?.asString())
                                                            if (index < newInjectParams.lastIndex || dataParams.isNotEmpty()) {
                                                                append(", ")
                                                            }
                                                        }
                                                    }
                                                    val params = buildString {
                                                        dataParams.forEachIndexed { index, param ->
                                                            append(param.name?.asString())
                                                            append(" = effect.")
                                                            append(param.name?.asString())
                                                            if (index < dataParams.lastIndex) {
                                                                append(", ")
                                                            }
                                                        }
                                                    }
                                                    CodeBlock.builder()
                                                        .add("addConsumer<%L>", effectClass.toClassName())
                                                        .add(" { effect ->\n")
                                                        .indent()
                                                        .add("${func.qualifiedName?.asString()}($injectParamString$params)")
                                                        .unindent()
                                                        .add("\n}\n")
                                                        .build()
                                                        .run(::add)
                                                }
                                            } else {
                                                check(
                                                    extensionReceiver.toTypeName() == FlowCollector::class.asTypeName()
                                                        .parameterizedBy(eventClassName)
                                                ) {
                                                    "Function '${effectClass.asFunName()}' must only use extension receiver of `FlowCollector<${eventClassDec.simpleName.asString()}>"
                                                }

                                                val effectAnnotation =
                                                    func.getAnnotationsByType(AutoWireEffect::class).first()

                                                val injectParamString = buildString {
                                                    newInjectParams.forEachIndexed { index, param ->
                                                        append(param.name?.asString())
                                                        append(" = ")
                                                        append(param.name?.asString())
                                                        if (index < newInjectParams.lastIndex || dataParams.isNotEmpty()) {
                                                            append(", ")
                                                        }
                                                    }
                                                }
                                                val params = buildString {
                                                    dataParams.forEachIndexed { index, param ->
                                                        append(param.name?.asString())
                                                        append(" = effect.")
                                                        append(param.name?.asString())
                                                        if (index < dataParams.lastIndex) {
                                                            append(", ")
                                                        }
                                                    }
                                                }
                                                val builderFunc = if (effectAnnotation.latest) {
                                                    "addLatestValueCollector"
                                                } else {
                                                    "addValueCollector"
                                                }
                                                CodeBlock.builder()
                                                    .add("$builderFunc<%L>", effectClass.toClassName())
                                                    .add(" { effect ->\n")
                                                    .indent()
                                                    .add("${func.simpleName.asString()}($injectParamString$params)")
                                                    .unindent()
                                                    .add("\n}\n")
                                                    .build()
                                                    .run(::add)
                                            }
                                        } else if (returnType.resolve().toTypeName() == Flow::class.asTypeName()
                                                .parameterizedBy(eventClassName)
                                        ) {
                                            val injectParamString = buildString {
                                                newInjectParams.forEachIndexed { index, param ->
                                                    append(param.name?.asString())
                                                    append(" = ")
                                                    append(param.name?.asString())
                                                    if (index < newInjectParams.lastIndex || dataParams.isNotEmpty()) {
                                                        append(", ")
                                                    }
                                                }
                                            }
                                            val params = buildString {
                                                dataParams.forEachIndexed { index, param ->
                                                    append(param.name?.asString())
                                                    append(" = effect.")
                                                    append(param.name?.asString())
                                                    if (index < dataParams.lastIndex) {
                                                        append(", ")
                                                    }
                                                }
                                            }
                                            CodeBlock.builder()
                                                .add("addTransformer<%L>", effectClass.toClassName())
                                                .add(" { effects ->\n")
                                                .indent()
                                                .add("effects.${func.simpleName.asString()}($injectParamString$params)")
                                                .unindent()
                                                .add("\n}\n")
                                                .build()
                                                .run(::add)
                                        } else if (returnType.resolve().toTypeName() == eventClassName) {
                                            val injectParamString = buildString {
                                                newInjectParams.forEachIndexed { index, param ->
                                                    append(param.name?.asString())
                                                    append(" = ")
                                                    append(param.name?.asString())
                                                    if (index < newInjectParams.lastIndex || dataParams.isNotEmpty()) {
                                                        append(", ")
                                                    }
                                                }
                                            }
                                            val params = buildString {
                                                dataParams.forEachIndexed { index, param ->
                                                    append(param.name?.asString())
                                                    append(" = effect.")
                                                    append(param.name?.asString())
                                                    if (index < dataParams.lastIndex) {
                                                        append(", ")
                                                    }
                                                }
                                            }
                                            CodeBlock.builder()
                                                .add("addFunction<%L>", effectClass.toClassName())
                                                .add(" { effect ->\n")
                                                .indent()
                                                .add("${func.qualifiedName?.asString()}($injectParamString$params)")
                                                .unindent()
                                                .add("\n}\n")
                                                .build()
                                                .run(::add)
                                        }
                                    }
                                }
                                .add("\n}")
                                .build()
                        ).build()
                    )
                    .addFunction(
                        FunSpec.builder("invoke")
                            .addModifiers(KModifier.OPERATOR, KModifier.OVERRIDE)
                            .addParameter(
                                ParameterSpec.builder(
                                    "input",
                                    Flow::class.asTypeName()
                                        .parameterizedBy(effectClassName)
                                ).build()
                            )
                            .addCode("return handler(input)")
                            .returns(
                                Flow::class.asTypeName()
                                    .parameterizedBy(eventClassName)
                            )
                            .build()
                    )
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .apply {
                                injectParams.forEach { param ->
                                    addParameter(
                                        ParameterSpec.builder(param.name!!.asString(), param.type.toTypeName())
                                            .build()
                                    )
                                }
                            }
                            .build()
                    )
                    .addOriginatingKSFile(effectClassDec.containingFile!!)
                    .build()
            )
            .build()
    }

    private fun KSDeclaration.asFunName(): String {
        return simpleName.asString().replaceFirstChar(Char::lowercaseChar)
    }
}
