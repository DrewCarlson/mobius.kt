package mpp

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlin.native.concurrent.ensureNeverFrozen

public actual inline fun <R> synchronized(lock: Any, block: () -> R): R {
    return kotlinx.atomicfu.locks.synchronized(lock as SynchronizedObject, block)
}

internal actual fun Any.ensureNeverFrozen() = ensureNeverFrozen()
