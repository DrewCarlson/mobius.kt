package kt.mobius.test.matcher

public interface Description {
    public fun appendText(text: String): Description

    public fun appendDescriptionOf(value: SelfDescribing): Description

    public fun appendValue(value: Any?): Description

    public fun <T> appendValueList(
        start: String,
        separator: String,
        end: String,
        vararg values: T
    ): Description

    public fun <T> appendValueList(
        start: String,
        separator: String,
        end: String,
        values: Iterable<T>
    ): Description

    public fun appendList(
        start: String,
        separator: String,
        end: String,
        values: Iterable<SelfDescribing>
    ): Description

    private class NullDescription : Description {
        override fun appendDescriptionOf(value: SelfDescribing): Description = this

        override fun appendList(
            start: String,
            separator: String,
            end: String,
            values: Iterable<SelfDescribing>
        ): Description = this

        override fun appendText(text: String): Description = this

        override fun appendValue(value: Any?): Description = this

        override fun <T> appendValueList(
            start: String,
            separator: String,
            end: String,
            vararg values: T
        ): Description = this

        override fun <T> appendValueList(
            start: String,
            separator: String,
            end: String,
            values: Iterable<T>
        ): Description = this

        override fun toString(): String = ""
    }

    public companion object {
        public val NONE: Description = NullDescription()
    }
}
