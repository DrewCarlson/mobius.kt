package kt.mobius.internal_util

import kotlin.annotation.AnnotationTarget.*

@OptIn(ExperimentalMultiplatform::class)
@Retention(AnnotationRetention.BINARY)
@Target(CLASS, PROPERTY, FUNCTION, FILE)
@OptionalExpectation
public expect annotation class JsExport()
