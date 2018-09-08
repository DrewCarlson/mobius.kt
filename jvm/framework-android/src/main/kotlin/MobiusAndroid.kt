package com.spotify.mobius.android;

import com.spotify.mobius.Mobius;
import com.spotify.mobius.MobiusLoop;
import com.spotify.mobius.android.runners.MainThreadWorkRunner;

object MobiusAndroid {
  fun <M, E, F> controller(
      loopFactory: MobiusLoop.Factory<M, E, F>,
      defaultModel: M
  ): MobiusLoop.Controller<M, E> {
    return Mobius.controller(loopFactory, defaultModel, MainThreadWorkRunner.create())
  }
}
