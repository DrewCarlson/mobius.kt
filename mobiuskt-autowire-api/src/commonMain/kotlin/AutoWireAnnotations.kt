package kt.mobius.autowire


@ExperimentalAutoWire
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
public annotation class AutoWireEvent

@ExperimentalAutoWire
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
public annotation class AutoWireEffect(
    val latest: Boolean = false
)

@ExperimentalAutoWire
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
public annotation class AutoWireInject
