package kt.mobius

import kt.mobius.internal_util.Throwables

public class LoggingInit<M, F> internal constructor(
    private val actualInit: Init<M, F>,
    private val logger: MobiusLoop.Logger<M, *, F>
) : Init<M, F> {

    public companion object {
        public fun <M, F> fromLoop(
            init: Init<M, F>,
            loopBuilder: MobiusLoop.Builder<M, *, F>,
        ): Init<M, F> {
            return if (loopBuilder is Mobius.Builder<M, *, F>) {
                LoggingInit(init, loopBuilder.logger)
            } else {
                init
            }
        }
    }

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
