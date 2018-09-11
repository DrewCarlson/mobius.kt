import com.spotify.mobius.*
import com.spotify.mobius.runners.*

class Shim {

  fun connectable(): Connectable<Any, Any> = TODO()
  fun connection(): Connection<Any> = TODO()
  fun effects(): Effects = Effects
  fun eventProcessor(): EventProcessor<Any, Any, Any> = TODO()
  fun eventSource(): EventSource<Any> = TODO()
  fun connectionExc(): ConnectionException = TODO()
  fun connectionLimitExc(): ConnectionLimitExceededException = TODO()
  fun first(): First<Any, Any> = TODO()
  fun init(): Init<Any, Any> = TODO()
  fun mobius(): Mobius = TODO()
  fun mobiusLoop(): MobiusLoop<Any, Any, Any> = TODO()
  fun mobiusLoopController(): MobiusLoopController<Any, Any, Any> = TODO()
  fun immediateWorkRunner(): ImmediateWorkRunner = TODO()
  fun simpleLogger(): SimpleLogger<Any, Any, Any> = TODO()
}
