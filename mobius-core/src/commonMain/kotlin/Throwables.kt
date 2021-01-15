package kt.mobius.internal_util

/** Utilities for working with throwables.  */
internal object Throwables {

    @mpp.JvmStatic
    fun propagate(e: Exception): RuntimeException {
        if (e is RuntimeException) {
            throw e
        }

        throw RuntimeException(e)
    }
}
