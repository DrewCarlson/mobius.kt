package kt.mobius.runners


public object AsyncWorkRunner : WorkRunner {
    override fun post(runnable: Runnable) {
        setTimeout({ runnable.run() }, 0)
    }

    override fun dispose() {
    }
}

private external fun setTimeout(function: () -> Unit, timeoutMs: Long)
