package kt.mobius

import kotlin.jvm.Volatile

class RecordingConnection<V> : RecordingConsumer<V>(), Connection<V> {

    @Volatile
    var disposed = false

    override fun dispose() {
        disposed = true
    }
}