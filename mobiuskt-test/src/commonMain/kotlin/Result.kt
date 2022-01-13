package kt.mobius.test

import kt.mobius.Next


/** Defines the final state of a Mobius loop after a sequence of events have been processed.  */
public data class Result<M, F>(
    /**
     * Returns the final model - note that was not necessarily produced by the last Next,
     * in case that returned an empty model.
     */
    val model: M,

    /** Returns the Next that resulted from the last processed event  */
    val lastNext: Next<M, F>?,
) {

    public companion object {
        /**
         * Create a [Result] with the provided model and next.
         *
         * @param model the model the loop ended with
         * @param lastNext the last next emitted by the loop
         * @param [M] the model type
         * @param [F] the effect type
         */
        public fun <M, F> of(model: M, lastNext: Next<M, F>?): Result<M, F> {
            return Result(model, lastNext)
        }
    }
}
