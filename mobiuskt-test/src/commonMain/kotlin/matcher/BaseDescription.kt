package kt.mobius.test.matcher

public abstract class BaseDescription : Description {
    override fun appendText(text: String): Description {
        append(text)
        return this
    }

    override fun appendDescriptionOf(value: SelfDescribing): Description {
        value.describeTo(this)
        return this
    }

    public override fun appendValue(value: Any?): Description {
        when (value) {
            null -> append("null")
            is String -> toJavaSyntax(value)
            is Char -> {
                append('"')
                toJavaSyntax(value)
                append('"')
            }
            is Byte -> {
                append('<')
                append(descriptionOf(value))
                append("b>")
            }
            is Short -> {
                append('<')
                append(descriptionOf(value))
                append("s>")
            }
            is Long -> {
                append('<')
                append(descriptionOf(value))
                append("L>")
            }
            is Float -> {
                append('<')
                append(descriptionOf(value))
                append("F>")
            }
            is Array<*> -> {
                appendValueList("[", ", ", "]", value.iterator())
            }
            else -> {
                append('<')
                append(descriptionOf(value))
                append('>')
            }
        }
        return this
    }

    override fun <T> appendValueList(start: String, separator: String, end: String, vararg values: T): Description {
        return appendValueList(start, separator, end, values.asList())
    }

    public override fun <T> appendValueList(start: String, separator: String, end: String, values: Iterable<T>): Description {
        return appendValueList(start, separator, end, values.iterator())
    }

    private fun <T> appendValueList(start: String, separator: String, end: String, values: Iterator<T>): Description {
        return appendList(start, separator, end, SelfDescribingValueIterator(values))
    }

    override fun appendList(
        start: String,
        separator: String,
        end: String,
        values: Iterable<SelfDescribing>
    ): Description {
        return appendList(start, separator, end, values.iterator())
    }

    private fun appendList(start: String, separator: String, end: String, i: Iterator<SelfDescribing>): Description {
        var separate = false
        append(start)
        while (i.hasNext()) {
            if (separate) append(separator)
            appendDescriptionOf(i.next())
            separate = true
        }
        append(end)
        return this
    }

    protected open fun append(str: String) {
        for (element in str) append(element)
    }

    protected abstract fun append(c: Char)
    private fun toJavaSyntax(unformatted: String) {
        append('"')
        for (element in unformatted) {
            toJavaSyntax(element)
        }
        append('"')
    }

    private fun toJavaSyntax(ch: Char) {
        when (ch) {
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '\\' -> append("\\\\")
            else -> append(ch)
        }
    }
}

internal fun descriptionOf(value: Any): String {
    return try {
        value.toString()
    } catch (e: Exception) {
        "${value::class.simpleName}@${value.hashCode().toString(16)}"
    }
}
