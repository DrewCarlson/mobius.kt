package kt.mobius

import kt.mobius.internal_util.Throwables

class LoggingInit<M, F> internal constructor(
    private val actualInit: Init<M, F>,
    private val logger: MobiusLoop.Logger<M, *, F>
) : Init<M, F> {

    override fun init(model: M): First<M, F> {
        logger.beforeInit(model)
        val result = safeInvokeInit(model)
        logger.afterInit(model, result)
        return result
    }

    private fun safeInvokeInit(model: M): First<M, F> {
        try {
            return actualInit.init(model)
        } catch (e: Exception) {
            logger.exceptionDuringInit(model, e)
            throw Throwables.propagate(e)
        }
    }
}
