package kt.mobius

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kt.mobius.runners.Runnable
import kt.mobius.runners.WorkRunner

public class MobiusLoopController<M, E, F>(
    private val loopFactory: MobiusLoop.Factory<M, E, F>,
    private val defaultModel: M,
    private val mainThreadRunner: WorkRunner
) : MobiusLoop.Controller<M, E>, ControllerActions<M, E> {
    private val lock = object : SynchronizedObject() {}

    private var currentState: ControllerStateBase<M, E>? = null

    override val isRunning: Boolean
        get() = synchronized(lock) {
            currentState!!.isRunning
        }

    override val model: M
        get() = synchronized(lock) {
            currentState!!.onGetModel()
        }

    init {
        goToStateInit(defaultModel)
    }

    private fun dispatchEvent(event: E) {
        currentState!!.onDispatchEvent(event)
    }

    private fun updateView(model: M) {
        currentState!!.onUpdateView(model)
    }

    override fun connect(view: Connectable<M, E>): Unit = synchronized(lock) {
        currentState!!.onConnect(view)
    }

    override fun disconnect(): Unit = synchronized(lock) {
        currentState!!.onDisconnect()
    }

    override fun start(): Unit = synchronized(lock) {
        currentState!!.onStart()
    }

    override fun stop(): Unit = synchronized(lock) {
        currentState!!.onStop()
    }

    override fun replaceModel(model: M): Unit = synchronized(lock) {
        currentState!!.onReplaceModel(model)
    }

    override fun postUpdateView(model: M) {
        mainThreadRunner.post(
            object : Runnable {
                override fun run() {
                    updateView(model)
                }
            })
    }

    override fun goToStateInit(nextModelToStartFrom: M): Unit = synchronized(lock) {
        currentState = ControllerStateInit(this, nextModelToStartFrom)
    }

    override fun goToStateCreated(renderer: Connection<M>, nextModelToStartFrom: M?): Unit =
        synchronized(lock) {
            val nextModel = nextModelToStartFrom ?: defaultModel
            currentState = ControllerStateCreated<M, E, F>(this, renderer, nextModel)
        }

    override fun goToStateCreated(view: Connectable<M, E>, nextModelToStartFrom: M) {

        val safeModelHandler = SafeConnectable(view)

        val modelConnection = safeModelHandler.connect { event -> dispatchEvent(event) }

        goToStateCreated(modelConnection, nextModelToStartFrom)
    }

    override fun goToStateRunning(renderer: Connection<M>, nextModelToStartFrom: M): Unit =
        synchronized(lock) {
            val stateRunning = ControllerStateRunning(this, renderer, loopFactory, nextModelToStartFrom)

            currentState = stateRunning

            stateRunning.start()
        }
}
