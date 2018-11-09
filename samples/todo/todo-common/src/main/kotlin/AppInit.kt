package todo

import kt.mobius.Init
import kt.mobius.First
import kt.mobius.First.Companion.first

class AppInit : Init<AppModel, Effect> {

  override fun init(model: AppModel): First<AppModel, Effect> {
    if (model.isLoadingTasks) {
      return first(model, setOf<Effect>(Effect.LoadTasks))
    }
    return first(model)
  }
}
