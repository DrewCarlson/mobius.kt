package kt.mobius

import kotlin.jvm.Volatile

public class RecordingConnection<V> : RecordingConsumer<V>(), Connection<V> {

    @Volatile
    public var disposed: Boolean = false
        private set

    override fun dispose() {
        disposed = true
    }
}