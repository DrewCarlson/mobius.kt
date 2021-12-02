package mpp

actual inline fun <R> synchronized(lock: Any, block: () -> R): R = kotlin.synchronized(lock, block)

actual fun Any.ensureNeverFrozen() = Unit
