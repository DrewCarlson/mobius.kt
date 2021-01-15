package kt.mobius.extras.patterns

import kt.mobius.Next
import kt.mobius.Update
import kt.mobius.extras.patterns.InnerEffectHandlers.ignoreEffects
import kt.mobius.functions.BiFunction
import kt.mobius.functions.Function
import kotlin.test.*

class InnerUpdateTest {

    @Test
    fun canExtractInnerModel() {
        val innerUpdate =
            InnerUpdate.builder<String, String, String, String, String, String>()
                .modelExtractor(Function { "extracted_model" })
                .eventExtractor(Function { it })
                .innerUpdate(Update { model: String, _: String ->
                    Next.next<String, String>(model)
                })
                .modelUpdater(BiFunction { _: String, mi: String -> mi })
                .innerEffectHandler(ignoreEffects())
                .build()

        val next = innerUpdate.update("model", "event")

        assertTrue(next.hasModel())
        assertEquals("extracted_model", next.model())
    }

    @Test
    fun canExtractInnerEvent() {
        val innerUpdate =
            InnerUpdate.builder<String, String, String, String, String, String>()
                .modelExtractor(Function { it })
                .eventExtractor(Function { "extracted_event" })
                .innerUpdate(Update { _: String, event: String ->
                    Next.next<String, String>(event)
                })
                .modelUpdater(BiFunction { _: String, mi: String -> mi })
                .innerEffectHandler(ignoreEffects())
                .build()

        val next = innerUpdate.update("model", "event")

        assertTrue(next.hasModel())
        assertEquals("extracted_event", next.model())
    }

    @Test
    fun callsInnerUpdate() {
        val innerUpdate =
            InnerUpdate.builder<String, String, String, String, String, String>()
                .modelExtractor(Function { it })
                .eventExtractor(Function { it })
                .innerUpdate(Update { _, _ -> Next.next("inner_update") })
                .modelUpdater(BiFunction { _, mi -> mi })
                .innerEffectHandler(ignoreEffects())
                .build()

        val next = innerUpdate.update("model", "event")

        assertTrue(next.hasModel())
        assertEquals("inner_update", next.model())
    }

    @Test
    fun noChangesDoesNotCallModelUpdater() {
        val innerUpdate =
            InnerUpdate.builder<String, String, String, String, String, String>()
                .modelExtractor(Function { it })
                .eventExtractor(Function { it })
                .innerUpdate(Update { _, _ -> Next.noChange() })
                .modelUpdater(BiFunction { _, _ -> "model_updater" })
                .innerEffectHandler(ignoreEffects())
                .build()

        val next = innerUpdate.update("model", "event")

        assertFalse(next.hasModel())
        assertFalse(next.hasEffects())
    }

    @Test
    fun updateModelCallsModelUpdater() {
        val innerUpdate =
            InnerUpdate.builder<String, String, String, String, String, String>()
                .modelExtractor(Function { it })
                .eventExtractor(Function { it })
                .innerUpdate(Update { _, _ -> Next.next("inner_update") })
                .modelUpdater(BiFunction { _, _ -> "model_updater" })
                .innerEffectHandler(ignoreEffects())
                .build()

        val next = innerUpdate.update("model", "event")

        assertTrue(next.hasModel())
        assertEquals("model_updater", next.model())
    }

    @Test
    fun dispatchEffectCallsInnerEffectHandler() {
        val innerUpdate =
            InnerUpdate.builder<String, String, String, String, String, String>()
                .modelExtractor(Function { it })
                .eventExtractor(Function { it })
                .innerUpdate(Update { _, _ -> Next.dispatch(setOf("1", "2", "3")) })
                .modelUpdater(BiFunction { _, mi -> mi })
                .innerEffectHandler(InnerEffectHandler { _, _, _ ->
                    Next.next("effect_handler")
                })
                .build()

        val next = innerUpdate.update("model", "event")

        assertTrue(next.hasModel())
        assertEquals("effect_handler", next.model())
    }

    @Test
    fun noEffectsStillCallsInnerEffectHandler() {
        val innerUpdate =
            InnerUpdate.builder<String, String, String, String, String, String>()
                .modelExtractor(Function { it })
                .eventExtractor(Function { it })
                .innerUpdate(Update { _, _ -> Next.next("inner_update") })
                .modelUpdater(BiFunction { _, mi -> mi })
                .innerEffectHandler(InnerEffectHandler { _, _, _ ->
                    Next.next("effect_handler")
                })
                .build()

        val next = innerUpdate.update("model", "event")

        assertFalse(next.hasEffects())
        assertTrue(next.hasModel())
        assertEquals("effect_handler", next.model())
    }

    @Test
    fun noChangeNoEffectsStillCallsInnerEffectHandler() {
        val innerUpdate =
            InnerUpdate.builder<String, String, String, String, String, String>()
                .modelExtractor(Function { it })
                .eventExtractor(Function { it })
                .innerUpdate(Update { _, _ -> Next.noChange() })
                .modelUpdater(BiFunction { _, mi -> mi })
                .innerEffectHandler(InnerEffectHandler { _, _, _ ->
                    Next.next("effect_handler")
                })
                .build()

        val next = innerUpdate.update("model", "event")

        assertFalse(next.hasEffects())
        assertTrue(next.hasModel())
        assertEquals("effect_handler", next.model())
    }

    @Test
    fun updatedModelNoEffectsStillCallsInnerEffectHandler() {
        val innerUpdate =
            InnerUpdate.builder<String, String, String, String, String, String>()
                .modelExtractor(Function { it })
                .eventExtractor(Function { it })
                .innerUpdate(Update { _, _ -> Next.next("inner_update") })
                .modelUpdater(BiFunction { _, mi -> mi })
                .innerEffectHandler(InnerEffectHandler { _, _, _ ->
                    Next.next("effect_handler")
                })
                .build()

        val next = innerUpdate.update("model", "event")

        assertFalse(next.hasEffects())
        assertTrue(next.hasModel())
        assertEquals("effect_handler", next.model())
    }
}
