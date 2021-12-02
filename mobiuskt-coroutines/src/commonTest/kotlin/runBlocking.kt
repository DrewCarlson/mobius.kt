package kt.mobius.flow

import kotlinx.coroutines.CoroutineScope

expect fun runBlocking(block: suspend CoroutineScope.() -> Unit)