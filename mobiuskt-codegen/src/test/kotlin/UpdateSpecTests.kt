package kt.mobius.gen

import com.tschuchort.compiletesting.*
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCompilerApi::class)
class UpdateSpecTests {

    @Test
    fun testUpdateSpecGeneration() {
        val source = SourceFile.kotlin(
            "test.kt", """
                import kt.mobius.*
                import kt.mobius.Next.Companion.next
                import kt.mobius.gen.*
                
                data class TestModel(
                    val counter: Int,
                )
                
                sealed class TestEvent {
                    object Increment : TestEvent()
                    data object Decrement : TestEvent()
                    data class SetValue(val newCounter: Int) : TestEvent()
                }
                
                sealed class TestEffect
                
                @Suppress("unused")
                @GenerateUpdate
                object TestUpdate : Update<TestModel, TestEvent, TestEffect>, TestGeneratedUpdate {
                    override fun increment(model: TestModel): Next<TestModel, TestEffect> {
                        return next(model.copy(counter = model.counter + 1))
                    }
                
                    override fun decrement(model: TestModel): Next<TestModel, TestEffect> {
                        return next(model.copy(counter = model.counter - 1))
                    }
                
                    override fun setValue(model: TestModel, event: TestEvent.SetValue): Next<TestModel, TestEffect> {
                        return next(model.copy(counter = event.newCounter))
                    }
                }
            """
        )
        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            configureKsp {
                symbolProcessorProviders.add(MobiusktSymbolProcessorProvider())
            }
        }
        val result = compilation.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val kotlinFiles = compilation.getKspGeneratedFiles()
        val generatedFile = assertNotNull(kotlinFiles.singleOrNull())
        assertEquals("TestGeneratedUpdate.kt", generatedFile.name)

        assertKotlinEquals(
            """
                import kt.mobius.Next
                import kt.mobius.Update
                
                internal interface TestGeneratedUpdate : Update<TestModel, TestEvent, TestEffect> {
                  override fun update(model: TestModel, event: TestEvent): Next<TestModel, TestEffect> {
                    @Suppress("REDUNDANT_ELSE_IN_WHEN")
                    return when (event) {
                      TestEvent.Decrement -> decrement(model)
                      TestEvent.Increment -> increment(model)
                      is TestEvent.SetValue -> setValue(model, event)
                      else -> error(""${'"'}TestGeneratedUpdate: unexpected missing branch for ${'$'}event${'"'}"")
                    }
                  }
                
                  public fun decrement(model: TestModel): Next<TestModel, TestEffect>
                
                  public fun increment(model: TestModel): Next<TestModel, TestEffect>
                
                  public fun setValue(model: TestModel, event: TestEvent.SetValue): Next<TestModel, TestEffect>
                }
                
            """.trimIndent(),
            generatedFile.readText()
        )
    }

    @Test
    fun testUpdateSpecWithSealedGeneration() {
        val source = SourceFile.kotlin(
            "test.kt", """
                import kt.mobius.*
                import kt.mobius.gen.*
                                
                data class TestWithSealedModel(val counter: Int)
                
                sealed class TestEffect
                
                sealed class TestEventWithSealed {
                    data class Test1(val i: Int = 0) : TestEventWithSealed()
                    object Test2 : TestEventWithSealed()
                    sealed class Test3 : TestEventWithSealed() {
                        data class A(val i: Int = 0) : Test3()
                        object B : Test3()
                        @DisableSubtypeSpec
                        sealed class C : Test3() {
                            data class D(val i: Int = 0) : C()
                            data object E : C()
                        }
                    }
                }
                
                @GenerateUpdate
                object TestWithSealedUpdate : Update<TestWithSealedModel, TestEventWithSealed, TestEffect>,
                    TestWithSealedGeneratedUpdate {
                    override fun test2(model: TestWithSealedModel): Next<TestWithSealedModel, TestEffect> = TODO("")
                
                    override fun test1(
                        model: TestWithSealedModel,
                        event: TestEventWithSealed.Test1
                    ): Next<TestWithSealedModel, TestEffect> = TODO("")
                
                    override fun test3B(model: TestWithSealedModel): Next<TestWithSealedModel, TestEffect> = TODO("")
                
                    override fun test3A(
                        model: TestWithSealedModel,
                        event: TestEventWithSealed.Test3.A
                    ): Next<TestWithSealedModel, TestEffect> = TODO("")
                
                    override fun test3C(
                        model: TestWithSealedModel,
                        event: TestEventWithSealed.Test3.C
                    ): Next<TestWithSealedModel, TestEffect> = TODO("")
                }
            """
        )
        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            configureKsp {
                symbolProcessorProviders.add(MobiusktSymbolProcessorProvider())
            }
        }
        val result = compilation.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        val kotlinFiles = compilation.getKspGeneratedFiles()
        val generatedFile = assertNotNull(kotlinFiles.singleOrNull())
        assertEquals("TestWithSealedGeneratedUpdate.kt", generatedFile.name)

        assertKotlinEquals(
            """
                import kt.mobius.Next
                import kt.mobius.Update
                
                internal interface TestWithSealedGeneratedUpdate : Update<TestWithSealedModel, TestEventWithSealed, TestEffect> {
                  override fun update(model: TestWithSealedModel, event: TestEventWithSealed): Next<TestWithSealedModel, TestEffect> {
                    @Suppress("REDUNDANT_ELSE_IN_WHEN")
                    return when (event) {
                      TestEventWithSealed.Test2 -> test2(model)
                      is TestEventWithSealed.Test1 -> test1(model, event)
                      is TestEventWithSealed.Test3 -> 
                      when (event) {
                        TestEventWithSealed.Test3.B -> test3B(model)
                        is TestEventWithSealed.Test3.A -> test3A(model, event)
                        is TestEventWithSealed.Test3.C -> test3C(model, event)
                        else -> error(""${'"'}TestWithSealedGeneratedUpdate: unexpected missing branch for ${'$'}event""${'"'})
                      }
                      else -> error(""${'"'}TestWithSealedGeneratedUpdate: unexpected missing branch for ${'$'}event""${'"'})
                    }
                  }
                
                  public fun test2(model: TestWithSealedModel): Next<TestWithSealedModel, TestEffect>
                
                  public fun test1(model: TestWithSealedModel, event: TestEventWithSealed.Test1): Next<TestWithSealedModel, TestEffect>
                
                  public fun test3B(model: TestWithSealedModel): Next<TestWithSealedModel, TestEffect>
                
                  public fun test3A(model: TestWithSealedModel, event: TestEventWithSealed.Test3.A): Next<TestWithSealedModel, TestEffect>
                
                  public fun test3C(model: TestWithSealedModel, event: TestEventWithSealed.Test3.C): Next<TestWithSealedModel, TestEffect>
                }

            """.trimIndent(),
            generatedFile.readText()
        )
    }

    private fun assertKotlinEquals(
        @Language("kotlin") expected: String,
        @Language("kotlin") actual: String,
    ) {
        assertEquals(expected, actual)
    }

    private fun KotlinCompilation.getKspGeneratedFiles(): List<File> {
        return kspSourcesDir.listFiles()
            ?.firstOrNull { it.name == "kotlin" }
            ?.listFiles()
            .orEmpty()
            .toList()
    }
}