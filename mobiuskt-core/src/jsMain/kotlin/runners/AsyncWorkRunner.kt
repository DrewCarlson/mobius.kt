package kt.mobius.runners

private external fun setTimeout(func: () -> Unit, timeout: Long)

public object AsyncWorkRunner : WorkRunner {
    override fun post(runnable: Runnable) {
        setTimeout(runnable::run, 0)
    }

    override fun dispose() {
    }
}