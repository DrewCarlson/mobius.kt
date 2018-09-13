package todo

import com.spotify.mobius.Init
import com.spotify.mobius.First
import com.spotify.mobius.First.Companion.first

class AppInit : Init<AppModel, Effect> {

  override fun init(model: AppModel): First<AppModel, Effect> {
    if (model.isLoadingTasks) {
      return first(model, setOf<Effect>(Effect.LoadTasks))
    }
    return first(model)
  }
}
