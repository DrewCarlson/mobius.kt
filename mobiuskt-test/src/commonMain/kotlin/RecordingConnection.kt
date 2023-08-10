package kt.mobius.test

import kt.mobius.Connection
import kotlin.concurrent.Volatile

public class RecordingConnection<V> : RecordingConsumer<V>(), Connection<V> {

    @Volatile
    public var disposed: Boolean = false
        private set

    override fun dispose() {
        disposed = true
    }
}
