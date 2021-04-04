package mpp

import kotlin.native.concurrent.ensureNeverFrozen

actual fun Any.ensureNeverFrozen() = ensureNeverFrozen()
