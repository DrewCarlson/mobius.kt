package mpp

public expect inline fun <R> synchronized(lock: Any, block: () -> R): R

internal expect fun Any.ensureNeverFrozen()