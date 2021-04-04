package kt.mobius.flow

/**
 * Used to indicate that an [FlowMobiusLoop] transformer has received an
 * [kotlinx.coroutines.flow.catch] call, which is illegal.
 * This exception means Mobius is in an undefined state and should be
 * considered a fatal programmer error.
 *
 * *Do not* try to handle this exception in your code, ensure it never gets thrown.
 */
class UnrecoverableIncomingException(
    override val cause: Throwable?
) : RuntimeException(
    "PROGRAMMER ERROR: Mobius cannot recover from this exception; ensure your event sources don't invoke catch"
)
