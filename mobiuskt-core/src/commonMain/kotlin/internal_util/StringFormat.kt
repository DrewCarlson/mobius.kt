package kt.mobius.internal_util

internal fun String.format(vararg args: Any?): String {
    if (!contains("{}")) {
        return this
    }
    val formatParts = split("{}", limit = args.size + 1)

    return buildString(length) {
        for (i in args.indices) {
            append(formatParts[i])
            append(args[i])
        }

        append(formatParts.last())
    }
}
