package mpp

import kotlin.native.concurrent.ensureNeverFrozen

actual inline fun <R> synchronized(lock: Any, block: () -> R): R = block()

actual fun Any.ensureNeverFrozen() = ensureNeverFrozen()
