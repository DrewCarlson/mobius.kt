package kt.mobius

import kt.mobius.functions.Consumer
import kt.mobius.functions.Producer
import kt.mobius.runners.ImmediateWorkRunner
import kt.mobius.runners.WorkRunner
import kt.mobius.runners.WorkRunners
import kotlin.test.*

class MobiusLoopControllerTest {
    companion object {
        val effectHandler = Connectable<String, String> {
            object : Connection<String> {
                override fun accept(value: String) {}

                override fun dispose() {}
            }
        }

        fun view(): Connectable<String, String> {
            return Connectable {
                object : Connection<String> {
                    override fun accept(value: String) {}

                    override fun dispose() {}
                }
            }
        }

        inline fun <V> createConnection(crossinline block: (V) -> Unit): Connection<V> {
            return object : Connection<V> {
                override fun accept(value: V) {
                    block(value)
                }

                override fun dispose() {
                }
            }
        }
    }

    class Lifecycle {
        val underTest = MobiusLoopController(
            Mobius.loop<String, String, String>(
                Update { model, event -> Next.next(model + event) },
                effectHandler
            )
                .eventRunner(Producer { WorkRunners.immediate() })
                .effectRunner(Producer { WorkRunners.immediate() }),
            "init",
            WorkRunners.immediate()
        )

        @Test
        fun canCreateView() {
            underTest.connect(view())
        }

        @Test
        fun canStart() {
            underTest.connect(view())
            underTest.start()
        }

        @Test
        fun canStop() {
            underTest.connect(view())
            underTest.start()
            underTest.stop()
        }

        @Test
        fun canDestroyView() {
            underTest.connect(view())
            underTest.start()
            underTest.stop()
            underTest.disconnect()
        }

        @Test
        fun canRestartAfterStopping() {
            underTest.connect(view())
            underTest.start()
            underTest.stop()
            underTest.start()
        }

        @Test
        fun canDestroyEvenIfNeverStarted() {
            underTest.connect(view())
            underTest.disconnect()
        }

        @Test
        fun cannotStartWithoutViewCreated() {
            assertFailsWith(IllegalStateException::class, "init") {
                underTest.start()
            }
        }

        @Test
        fun cannotDestroyWhenRunning() {
            underTest.connect(view())
            underTest.start()

            assertFailsWith(IllegalStateException::class, "running") {
                underTest.disconnect()
            }
        }

        @Test
        fun cannotStopBeforeCreating() {
            assertFailsWith(IllegalStateException::class, "init") {
                underTest.stop()
            }
        }

        @Test
        fun cannotStopBeforeStarting() {
            underTest.connect(view())

            assertFailsWith(IllegalStateException::class, "created") {
                underTest.stop()
            }
        }

        @Test
        fun cannotCreateTwice() {
            underTest.connect(view())

            assertFailsWith(IllegalStateException::class, "created") {
                underTest.connect(view())
            }
        }

        @Test
        fun cannotStartTwice() {
            underTest.connect(view())
            underTest.start()

            assertFailsWith(IllegalStateException::class, "running") {
                underTest.start()
            }
        }

        @Test
        fun cannotStopTwice() {
            underTest.connect(view())
            underTest.start()
            underTest.stop()

            assertFailsWith(IllegalStateException::class, "created") {
                underTest.stop()
            }
        }

        @Test
        fun cannotDestroyTwice() {
            underTest.connect(view())
            underTest.disconnect()

            assertFailsWith(IllegalStateException::class, "init") {
                underTest.disconnect()
            }
        }
    }

    class StateSaveRestore {
        val underTest = MobiusLoopController(
            Mobius.loop<String, String, String>(
                Update { model, event -> Next.next(model + event) },
                effectHandler
            )
                .eventRunner(Producer { WorkRunners.immediate() })
                .effectRunner(Producer { WorkRunners.immediate() }),
            "init",
            WorkRunners.immediate()
        )

        @Test
        fun canSaveState() {
            underTest.connect(view())
            underTest.start()
            underTest.stop()
            val model = underTest.model

            assertEquals("init", model)
        }

        @Test
        fun canRestoreState() {
            underTest.replaceModel("restored")
            val model = underTest.model

            assertEquals("restored", model)
        }

        @Test
        fun canSaveStateAfterCreating() {
            underTest.connect(view())
            val model = underTest.model

            assertEquals("init", model)
        }

        @Test
        fun canRestoreStateAfterCreating() {
            underTest.connect(view())
            underTest.replaceModel("restored")
            val model = underTest.model

            assertEquals("restored", model)
        }

        @Test
        fun cannotRestoreStateAfterStarting() {
            underTest.connect(view())
            underTest.start()

            assertFailsWith(IllegalStateException::class, "running") {
                underTest.replaceModel("restored")
            }
        }

        @Test
        fun canSaveStateAfterStarting() {
            underTest.connect(view())
            underTest.start()

            val model = underTest.model
            assertEquals("init", model)
        }

        @Test
        fun canSaveStateAfterStopping() {
            underTest.connect(view())
            underTest.start()
            underTest.stop()
            val model = underTest.model

            assertEquals("init", model)
        }

        @Test
        fun canRestoreStateAfterStopping() {
            underTest.connect(view())
            underTest.start()
            underTest.stop()
            underTest.replaceModel("restored")
            val model = underTest.model

            assertEquals("restored", model)
        }
    }

    class Loop {
        val underTest = MobiusLoopController(
            Mobius.loop<String, String, String>(
                Update { model, event -> Next.next(model + event) },
                effectHandler
            )
                .eventRunner(Producer { WorkRunners.immediate() })
                .effectRunner(Producer { WorkRunners.immediate() })
                .init(Init { First.first(it) }),
            "init",
            WorkRunners.immediate()
        )

        @Test
        fun startsFromDefaultModel() {
            val renderer = Renderer<String, String>()

            underTest.connect(renderer)
            underTest.start()

            assertEquals("init", renderer.values.single())
        }

        @Test
        fun restoringStartsFromRestoredModel() {
            val renderer = Renderer<String, String>()

            underTest.replaceModel("restored")
            underTest.connect(renderer)
            underTest.start()

            assertEquals("restored", renderer.values.single())
        }

        @Test
        fun resumingStartsFromMostRecentModel() {
            val renderer = Renderer<String, String>()
            underTest.connect(renderer)

            underTest.start()
            renderer.consumer.accept("!")

            assertEquals("init!", renderer.values.last())

            underTest.stop()
            renderer.reset()
            underTest.start()

            assertEquals("init!", renderer.values.single())
        }
    }

    class Connect {
        val underTest = MobiusLoopController(
            Mobius.loop<String, String, String>(
                Update { model, event -> Next.next(model + event) },
                effectHandler
            )
                .eventRunner(Producer { WorkRunners.immediate() })
                .effectRunner(Producer { WorkRunners.immediate() })
                .init(Init { First.first(it) }),
            "init",
            WorkRunners.immediate()
        )

        @Test
        fun eventConsumerIsDisabledAfterDisconnect() {
            val renderer = Renderer<String, String>()

            underTest.connect(renderer)

            renderer.consumer.accept("1")
            underTest.start()
            assertEquals("init", renderer.values.first())
            renderer.consumer.accept("2")
            underTest.stop()
            renderer.consumer.accept("3")
            assertEquals("init2", renderer.values.last())

            assertFalse(renderer.disposed)
            underTest.disconnect()
            assertTrue(renderer.disposed)

            renderer.consumer.accept("4")
        }
    }

    class EventsAndUpdates {

        val mainThreadRunner = ImmediateWorkRunner()
        lateinit var underTest: MobiusLoopController<String, String, String>

        @BeforeTest
        fun setUp() {
            underTest = createWithWorkRunner(mainThreadRunner)
        }

        fun createWithWorkRunner(mainThreadRunner: WorkRunner) =
            MobiusLoopController(
                Mobius.loop<String, String, String>(Update { model, event ->
                    Next.next(model + event)
                }, effectHandler)
                    .eventRunner(Producer { WorkRunners.immediate() })
                    .effectRunner(Producer { WorkRunners.immediate() }),
                "init",
                mainThreadRunner
            )

        @Test
        fun updaterCanReceiveViewUpdates() {
            val renderer = Renderer<String, String>()

            underTest.connect(renderer)

            underTest.start()
            renderer.consumer.accept("!")

            assertEquals("init!", renderer.values.last())
        }

        /*
        https://github.com/spotify/mobius/blob/d8e2eea761658d90d44fbafa1195cb3ef6044798/mobius-core/src/test/java/com/spotify/mobius/MobiusLoopControllerTest.java#L451-L482
        @Test
          public void updaterReceivesViewUpdatesOnMainThread() throws Exception {
            KnownThreadWorkRunner mainThreadRunner = new KnownThreadWorkRunner();
            final AtomicReference<Thread> actualThread = new AtomicReference<>();
            final Semaphore rendererGotModel = new Semaphore(0);

            @SuppressWarnings("unchecked")
            Connection<String> renderer =
                new Connection<String>() {
                  @Override
                  public void accept(String value) {
                    actualThread.set(Thread.currentThread());
                    rendererGotModel.release();
                  }

                  @Override
                  public void dispose() {}
                };

            underTest = createWithWorkRunner(mainThreadRunner);

            underTest.connect(
                eventConsumer -> {
                  return renderer;
                });

            underTest.start();

            rendererGotModel.tryAcquire(5, TimeUnit.SECONDS);

            assertThat(actualThread.get(), is(mainThreadRunner.workerThread));
        }
        */

        @Test
        fun eventsWhenNotRunningAreDropped() {
            val renderer = Renderer<String, String>()

            underTest.connect(renderer)
            renderer.consumer.accept("!")
            underTest.start()

            assertEquals(renderer.values.single(), "init")
        }
    }

    class Renderer<I, O> : Connection<I>, Connectable<I, O> {
        lateinit var consumer: Consumer<O>
            private set

        var values: List<I> = emptyList()
            private set

        var disposed: Boolean = false
            private set

        override fun accept(value: I) {
            values += value
        }

        override fun dispose() {
            disposed = !disposed
        }

        fun reset() {
            values = emptyList()
            disposed = false
        }

        override fun connect(output: Consumer<O>): Connection<I> {
            consumer = output
            return this
        }
    }
}
