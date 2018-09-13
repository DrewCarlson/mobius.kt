package todo

import com.spotify.mobius.Effects.effects
import com.spotify.mobius.Next
import com.spotify.mobius.Next.Companion.next
import com.spotify.mobius.Next.Companion.noChange
import com.spotify.mobius.Update


class AppUpdate : Update<AppModel, Event, Effect> {

  override fun update(model: AppModel, event: Event): Next<AppModel, Effect> {
    return when (event) {
      is Event.OnLoadTasks -> {
        if (model.isLoadingTasks) noChange()
        else next<AppModel, Effect>(
            model.copy(isLoadingTasks = true),
            effects(Effect.LoadTasks)
        )
      }
      is Event.OnAddTask -> {
        if (model.isAddingTask) noChange()
        else next(model.copy(isAddingTask = true))
      }
      is Event.OnSubmitNewTask -> {
        val nextId = model.tasks.size + 1
        val task = Task(nextId, event.todo, false)
        next(
            model.copy(
                isAddingTask = false,
                tasks = model.tasks + task
            ),
            effects(Effect.SaveTask(task))
        )
      }
      is Event.OnDiscardNewTask -> {
        if (model.isAddingTask)
          next(model.copy(isAddingTask = false))
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
            .run { copy(isComplete = !isComplete) }
        val newList = model.tasks.toMutableList()
        newList[task.id] = task
        next(
            model.copy(tasks = newList.toList()),
            effects(Effect.UpdateTask(task))
        )
      }
      is Event.OnTasksLoaded -> {
        next(model.copy(isLoadingTasks = false, tasks = event.tasks))
      }
    }
  }
}
