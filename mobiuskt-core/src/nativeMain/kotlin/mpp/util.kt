package mpp

import kotlin.native.concurrent.ensureNeverFrozen

internal actual fun Any.ensureNeverFrozen() = ensureNeverFrozen()
