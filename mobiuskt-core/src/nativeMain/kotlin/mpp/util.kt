package mpp

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlin.native.concurrent.ensureNeverFrozen

actual inline fun <R> synchronized(lock: Any, block: () -> R): R {
    return kotlinx.atomicfu.locks.synchronized(lock as SynchronizedObject, block)
}

actual fun Any.ensureNeverFrozen() = ensureNeverFrozen()
