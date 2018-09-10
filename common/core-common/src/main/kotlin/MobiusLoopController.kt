package com.spotify.mobius

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.runners.Runnable
import com.spotify.mobius.runners.WorkRunner
import synchronized2

class MobiusLoopController<M, E, F>(
    private val loopFactory: MobiusLoop.Factory<M, E, F>,
    private val defaultModel: M,
    private val mainThreadRunner: WorkRunner
) : MobiusLoop.Controller<M, E>, ControllerActions<M, E> {
  private object LOCK

  private var currentState: ControllerStateBase<M, E>? = null

  override val isRunning: Boolean
    get() = synchronized2(LOCK) {
      currentState!!.isRunning
    }

  override val model: M
    get() = synchronized2(LOCK) {
      currentState!!.onGetModel()
    }

  init {
    goToStateInit(defaultModel)
  }

  private fun dispatchEvent(event: E): Unit = synchronized2(LOCK) {
    currentState!!.onDispatchEvent(event)
  }

  private fun updateView(model: M): Unit = synchronized2(LOCK) {
    currentState!!.onUpdateView(model)
  }

  override fun connect(view: Connectable<M, E>): Unit = synchronized2(LOCK) {
    currentState!!.onConnect(view)
  }

  override fun disconnect(): Unit = synchronized2(LOCK) {
    currentState!!.onDisconnect()
  }

  override fun start(): Unit = synchronized2(LOCK) {
    currentState!!.onStart()
  }

  override fun stop(): Unit = synchronized2(LOCK) {
    currentState!!.onStop()
  }

  override fun replaceModel(model: M): Unit = synchronized2(LOCK) {
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

  override fun goToStateInit(nextModelToStartFrom: M): Unit = synchronized2(LOCK) {
    currentState = ControllerStateInit(this, nextModelToStartFrom)
  }

  override fun goToStateCreated(renderer: Connection<M>, nextModelToStartFrom: M?): Unit =
      synchronized2(LOCK) {
        val nextModel = nextModelToStartFrom ?: defaultModel
        currentState = ControllerStateCreated<M, E, F>(this, renderer, nextModel)
      }

  override fun goToStateCreated(view: Connectable<M, E>, nextModelToStartFrom: M) {

    val safeModelHandler = SafeConnectable(view)

    val modelConnection = safeModelHandler.connect(
        object : Consumer<E> {
          override fun accept(event: E) {
            dispatchEvent(event)
          }
        })

    goToStateCreated(modelConnection, nextModelToStartFrom)
  }

  override fun goToStateRunning(renderer: Connection<M>, nextModelToStartFrom: M): Unit =
      synchronized2(LOCK) {
        val stateRunning = ControllerStateRunning(this, renderer, loopFactory, nextModelToStartFrom)

        currentState = stateRunning

        stateRunning.start()
      }
}
