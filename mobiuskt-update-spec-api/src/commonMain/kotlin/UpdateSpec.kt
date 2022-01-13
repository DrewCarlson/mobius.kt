package kt.mobius.gen

import kotlin.reflect.*

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class UpdateSpec(
    val eventClass: KClass<*>,
    val effectClass: KClass<*>,
)
