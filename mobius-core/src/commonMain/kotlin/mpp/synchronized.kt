package mpp

expect inline fun <R> synchronized(lock: Any, block: () -> R): R
