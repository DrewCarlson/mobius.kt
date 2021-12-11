package mpp

public actual inline fun <R> synchronized(lock: Any, block: () -> R): R = kotlin.synchronized(lock, block)

internal actual fun Any.ensureNeverFrozen() = Unit
