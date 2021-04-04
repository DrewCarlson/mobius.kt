package kt.mobius

import kt.mobius.internal_util.Throwables

class LoggingUpdate<M, E, F> internal constructor(
    private val actualUpdate: Update<M, E, F>,
    private val logger: MobiusLoop.Logger<M, E, F>
) : Update<M, E, F> {

    override fun update(model: M, event: E): Next<M, F> {
        logger.beforeUpdate(model, event)
        val result = safeInvokeUpdate(model, event)
        logger.afterUpdate(model, event, result)
        return result
    }

    private fun safeInvokeUpdate(model: M, event: E): Next<M, F> {
        try {
            return actualUpdate.update(model, event)
        } catch (e: Exception) {
            logger.exceptionDuringUpdate(model, event, e)
            throw Throwables.propagate(e)
        }
    }
}
