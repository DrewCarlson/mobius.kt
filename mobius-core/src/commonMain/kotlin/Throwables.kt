package kt.mobius.internal_util

import kotlin.jvm.JvmStatic

/** Utilities for working with throwables.  */
internal object Throwables {

    @JvmStatic
    fun propagate(e: Exception): RuntimeException {
        if (e is RuntimeException) {
            throw e
        }

        throw RuntimeException(e)
    }
}
