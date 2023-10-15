package kt.mobius.internal_util

internal fun String.format(vararg args: Any?): String {
    if (!contains("{}")) {
        return this
    }
    val formatParts = split("{}", limit = args.size + 1)

    require(formatParts.size == args.size + 1) {
        "String format expected ${formatParts.size - 1} args but found ${args.size}"
    }

    return buildString(length) {
        for (i in args.indices) {
            append(formatParts[i])
            append(args[i])
        }

        append(formatParts.last())

    }
}
