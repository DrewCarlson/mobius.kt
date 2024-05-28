package kt.mobius.kotest

import io.kotest.assertions.print.print
import io.kotest.matchers.*
import io.kotest.matchers.Matcher.Companion.invoke
import io.kotest.matchers.collections.*
import io.kotest.matchers.compose.all
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.similarity.possibleMatchesDescription
import kt.mobius.Next
import kt.mobius.Next.Companion.next
import kt.mobius.kotest.NextMatchers.containModel
import kt.mobius.kotest.NextMatchers.containEffects
import kt.mobius.kotest.NextMatchers.containsEffects

public object NextMatchers {
    public fun <M, F> containModel(): Matcher<Next<M, F>> {
        return Matcher { next ->
            MatcherResult(
                next.hasModel(),
                { "Next did not have model when it should" },
                { "Next had model when it should not" }
            )
        }
    }

    public fun <M, F> containEffects(): Matcher<Next<M, F>> {
        return Matcher { next ->
            MatcherResult(
                next.hasEffects(),
                { "Next should have effects but did not" },
                { "Next had effects when it should not" }
            )
        }
    }

    public fun <M, F> containsEffects(
        effects: Collection<F>,
    ): Matcher<Next<M, F>> = neverNullMatcher { next ->

        val actual = next.effects()
        val valueGroupedCounts: Map<F, Int> = actual.groupBy { it }.mapValues { it.value.size }
        val expectedGroupedCounts: Map<F, Int> = effects.groupBy { it }.mapValues { it.value.size }

        val passed = expectedGroupedCounts.size == valueGroupedCounts.size
                && expectedGroupedCounts.all { (k, v) ->
            valueGroupedCounts.filterKeys { k == it }[k] == v
        }

        val missing = effects.filterNot { t ->
            actual.contains(t)
        }
        val extra = actual.filterNot { t ->
            effects.any { (t == it) }
        }
        val countMismatch = countMismatch(expectedGroupedCounts, valueGroupedCounts)
        val possibleMatches = extra
            .map { possibleMatchesDescription(effects.toSet(), it) }
            .filter { it.isNotEmpty() }
            .joinToString("\n")

        val failureMessage = {
            buildString {
                append("Collection should contain ${effects.print().value} in any order, but was ${actual.print().value}")
                appendLine()
                appendMissingAndExtra(missing, extra)
                if (missing.isNotEmpty() || extra.isNotEmpty()) {
                    appendLine()
                }
                if (countMismatch.isNotEmpty()) {
                    append("CountMismatches: ${countMismatch.joinToString(", ")}")
                }
                if (possibleMatches.isNotEmpty()) {
                    appendLine()
                    append("Possible matches for unexpected elements:\n$possibleMatches")
                }
            }
        }

        val negatedFailureMessage = { "Collection should not contain exactly ${effects.print().value} in any order" }

        MatcherResult(
            passed,
            failureMessage,
            negatedFailureMessage
        )
    }
}

public fun <M, F> Next<M, F>.shouldNotHaveModel(): Next<M, F> {
    this shouldNot containModel()
    return this
}


public fun <M, F> Next<M, F>.shouldHaveModel(
    body: (model: M) -> Unit = {}
): Next<M, F> {
    this should containModel()
    assertSoftlyWithMessage("Next model assertions failed\n${modelUnsafe()  }") {
        body(modelUnsafe())
    }
    return this
}


public fun <M, F> Next<M, F>.shouldContainEffects(effects: Collection<F>): Next<M, F> {
    this should containsEffects(effects)
    return this
}

public fun <M, F> Next<M, F>.shouldNotContainEffects(effects: Collection<F>): Next<M, F> {
    this shouldNot containsEffects(effects)
    return this
}


public fun <M, F> Next<M, F>.shouldHaveNothing(): Next<M, F> {
    this shouldNot Matcher.all(
        containModel(),
        containEffects()
    )
    return this
}
