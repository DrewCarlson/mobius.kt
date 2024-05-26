package kt.mobius.runners

import kotlinx.browser.window

public object AsyncWorkRunner : WorkRunner {
    override fun post(runnable: Runnable) {
        window.setTimeout({ runnable.run(); null }, 0)
    }

    override fun dispose() {
    }
}
