# Useful Libraries

This page contains a list of useful libraries which can be useful in larger projects using Mobius.kt.

## [kopykat](https://github.com/kopykat-kt/kopykat)

When writing `Update` functions you will typically use the `copy` method
provided by `data class`es to create updated model instances.  The standard `copy` method is adequate in simple cases but
can quickly clutter your `Update` functions.  Kopykat provides generated builder `copy` methods which provide instance
variables to set instead of a long list of function parameters.


## [redacted-compiler-plugin](https://github.com/ZacSweers/redacted-compiler-plugin)

`data class`es provide a `toString` in Model classes which make Logging simple and useful in Mobius.kt.
When Model's contain sensitive information you do not want logged, overriding and keeping the `toString` method updated
is tedious.  With Redacted, you can annotate individual properties with `@Redacted` to omit the actual data from the
standard `toString` implementation.


## [Poko](https://github.com/drewhamilton/Poko)

`data class`es are a convenient utility in a lot of situations, especially with Mobius.kt.
But there may be cases where they may be generating a lot of code that you do not use.
If you're not using dead-code elimination tools, this can bloat your application or library.
Poko allows you to get the `toString`, `equals`, and `hashCode` implementations provided by `data class` without
generated `copy` methods.
This can be particularly useful with `Event` and `Effect` class subtypes.