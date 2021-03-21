package mpp

actual inline fun <R> synchronized(lock: Any, block: () -> R): R = block()
