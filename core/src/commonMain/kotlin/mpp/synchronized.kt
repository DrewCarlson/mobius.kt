package mpp

internal expect inline fun <R> synchronized(lock: Any, block: () -> R): R
