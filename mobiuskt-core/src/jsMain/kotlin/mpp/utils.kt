package mpp

actual inline fun <R> synchronized(lock: Any, block: () -> R): R = run(block)

actual fun Any.ensureNeverFrozen() = Unit
