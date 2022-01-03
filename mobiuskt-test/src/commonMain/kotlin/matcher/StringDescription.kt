package kt.mobius.test.matcher

internal class StringDescription(
    private val out: StringBuilder = StringBuilder()
) : BaseDescription() {

    override fun append(str: String) {
        out.append(str)
    }

    override fun append(c: Char) {
        out.append(c)
    }

    override fun toString(): String {
        return out.toString()
    }

    companion object {
        fun toString(selfDescribing: SelfDescribing): String {
            return StringDescription().appendDescriptionOf(selfDescribing).toString()
        }

        fun asString(selfDescribing: SelfDescribing): String {
            return toString(selfDescribing)
        }
    }
}