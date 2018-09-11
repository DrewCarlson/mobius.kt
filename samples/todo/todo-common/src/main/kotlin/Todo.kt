import com.spotify.mobius.Effects.effects
import com.spotify.mobius.Next
import com.spotify.mobius.Next.Companion.next
import com.spotify.mobius.Next.Companion.noChange
import com.spotify.mobius.Update

data class Task(
    val id: Int,
    val description: String,
    val complete: Boolean
)

sealed class Event {
  object OnLoadTasks : Event()
  object OnAddTask : Event()

  data class OnSubmitNewTask(val description: String) : Event()
  object OnDiscardNewTask : Event()

  data class OnDeleteTask(val taskId: Int) : Event()
  data class OnToggleTaskComplete(val taskId: Int) : Event()

  data class OnTasksLoaded(val tasks: List<Task>) : Event()
}

sealed class Effect {
  object LoadTasks : Effect()
  data class CreateTask(val description: String) : Effect()
  data class DeleteTask(val taskId: Int) : Effect()
  data class UpdateTask(val task: Task) : Effect()
}

data class AppModel(
    val tasks: List<Task> = emptyList(),
    val loadingTasks: Boolean = false,
    val addingTask: Boolean = false
)

class AppUpdate : Update<AppModel, Event, Effect> {

  override fun update(model: AppModel, event: Event): Next<AppModel, Effect> {
    return when (event) {
      is Event.OnLoadTasks -> {
        if (model.loadingTasks) noChange()
        else next<AppModel, Effect>(
            model.copy(loadingTasks = true),
            setOf(Effect.LoadTasks)
        )
      }
      is Event.OnAddTask -> {
        if (model.addingTask) noChange()
        else next(model.copy(addingTask = true))
      }
      is Event.OnSubmitNewTask -> {
        next(
            model.copy(addingTask = false),
            effects(Effect.CreateTask(event.description))
        )
      }
      is Event.OnDiscardNewTask -> {
        if (model.addingTask)
          next(model.copy(addingTask = false))
        else noChange()
      }
      is Event.OnDeleteTask -> {
        next(
            model.copy(tasks = model.tasks.filter { it.id != event.taskId }),
            effects(Effect.DeleteTask(event.taskId))
        )
      }
      is Event.OnToggleTaskComplete -> {
        val task = model.tasks
            .find { it.id == event.taskId }!!
            .run { copy(complete = !complete) }
        val replace = { oldTask: Task ->
          if (oldTask.id == event.taskId) task
          else oldTask
        }
        next(
            model.copy(tasks = model.tasks.map(replace)),
            effects(Effect.UpdateTask(task))
        )
      }
      is Event.OnTasksLoaded -> {
        next(model.copy(loadingTasks = false, tasks = event.tasks))
      }
    }
  }
}
