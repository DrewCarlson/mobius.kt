@file:OptIn(DelicateCoroutinesApi::class)

package kt.mobius.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual fun runBlocking(block: suspend CoroutineScope.() -> Unit): dynamic =
    GlobalScope.promise { block(this) }