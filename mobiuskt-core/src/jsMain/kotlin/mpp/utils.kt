package mpp

public actual inline fun <R> synchronized(lock: Any, block: () -> R): R = run(block)

internal actual fun Any.ensureNeverFrozen() = Unit
