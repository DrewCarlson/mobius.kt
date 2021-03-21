package kt.mobius

import kt.mobius.disposables.Disposable
import kt.mobius.functions.Consumer
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/**
 * An [EventSource] that merges multiple sources into one
 *
 * @param E The type of Events the sources will emit
 */
class MergedEventSource<E> private constructor(
    private val eventSources: List<EventSource<E>>
) : EventSource<E> {

    override fun subscribe(eventConsumer: Consumer<E>): Disposable {
        val disposables = ArrayList<Disposable>(eventSources.size)
        for (eventSource in eventSources) {
            disposables.add(eventSource.subscribe(eventConsumer))
        }

        return Disposable {
            for (disposable in disposables) {
                disposable.dispose()
            }
        }
    }

    companion object {
        @JvmStatic
        @JsName("from")
        fun <E> from(vararg eventSources: EventSource<E>): EventSource<E> {
            val allSources = ArrayList<EventSource<E>>()
            allSources.addAll(eventSources)
            return MergedEventSource(allSources)
        }
    }
}
