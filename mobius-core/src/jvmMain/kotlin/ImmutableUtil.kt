package kt.mobius.internal_util

import java.util.Collections
import java.util.HashSet

/**
 * Defines static utility methods that help working with immutable collections. NOT FOR EXTERNAL
 * USE; this class is not a part of the Mobius API and backwards-incompatible changes may happen
 * between releases. If you want to use methods defined here, make your own copy.
 */
object ImmutableUtil {

  @JvmStatic
  fun <T> emptySet(): Set<T> {
    return emptySet()
  }

  @SafeVarargs
  @JvmStatic
  fun <T> setOf(vararg items: T): Set<T> {
    val result = HashSet<T>(items.size)
    Collections.addAll(result, *items)

    return Collections.unmodifiableSet(result)
  }

  @JvmStatic
  fun <T> immutableSet(set: Set<T>): Set<T> {
    val result = HashSet(set)
    return Collections.unmodifiableSet(result)
  }

  @SafeVarargs
  @JvmStatic
  fun <T> unionSets(vararg sets: Set<T>): Set<T> {
    val result = HashSet<T>()
    for (set in sets) {
      result.addAll(set)
    }

    return Collections.unmodifiableSet(result)
  }
}
