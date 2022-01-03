package kt.mobius.test.matcher


internal class SelfDescribingValue<T>(private val value: T) : SelfDescribing {
    override fun describeTo(description: Description) {
        description.appendValue(value)
    }
}

internal class SelfDescribingValueIterator<T>(private val values: Iterator<T>) : Iterator<SelfDescribing> {
    override fun hasNext(): Boolean {
        return values.hasNext()
    }

    override fun next(): SelfDescribing {
        return SelfDescribingValue(values.next())
    }
}
