actual inline fun <R> synchronized2(lock: Any, block: () -> R): R = block()
