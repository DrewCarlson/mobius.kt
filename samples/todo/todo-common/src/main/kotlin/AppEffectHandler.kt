package todo

import com.spotify.mobius.*
import com.spotify.mobius.functions.Consumer
import todo.Effect.*

class AppEffectHandler(
    private val output: Consumer<Event>,
    private val store: TaskStore
) : Connection<Effect> {

  override fun accept(value: Effect) {
    when (value) {
      LoadTasks -> {
        val tasks = store.listTasks()
        output.accept(Event.OnTasksLoaded(tasks))
      }
      is SaveTask -> {
        store.saveTask(value.task)
      }
      is DeleteTask -> {
        store.deleteTask(value.taskId)
      }
      is UpdateTask -> {
        store.updateTask(value.task)
      }
    }
  }

  override fun dispose() {
    store.dispose()
  }
}
