package kt.mobius

import kotlinx.collections.immutable.persistentHashSetOf
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/** Utility class for working with effects. */
@JsExport
public object Effects {

    /**
     * Convenience method for instantiating a set of effects. Note that this returns a mutable set of
     * effects to avoid creating too many copies - the set will normally be added to a [Next] or
     * [First], leading to another safe-copy being made.
     *
     * @return a *mutable* set of effects
     */
    // implementation note: the type signature of this method helps ensure that you can get a set of a
    // super type even if you only submit items of a sub type. Hence the 'G extends F' type parameter.
    @Suppress("NON_EXPORTABLE_TYPE")
    @JvmStatic
    @JsName("effects")
    public fun <F, G : F> effects(vararg effects: G): Set<F> {
        return persistentHashSetOf<F>(*effects)
    }
}
