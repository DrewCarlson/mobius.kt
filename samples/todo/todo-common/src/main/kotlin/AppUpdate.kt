package todo

import kt.mobius.Effects.effects
import kt.mobius.Next
import kt.mobius.Next.Companion.next
import kt.mobius.Next.Companion.noChange
import kt.mobius.Update


class AppUpdate : Update<AppModel, Event, Effect> {

  override fun update(model: AppModel, event: Event): Next<AppModel, Effect> {
    return when (event) {
      is Event.OnLoadTasks -> {
        if (model.isLoadingTasks) {
          noChange()
        } else {
          val nextModel = model.copy(isLoadingTasks = true)
          val effects = effects(Effect.LoadTasks)
          next<AppModel, Effect>(nextModel, effects)
        }
      }
      is Event.OnAddTask -> {
        if (model.isAddingTask) {
          noChange()
        } else {
          next(model.copy(isAddingTask = true))
        }
      }
      is Event.OnSubmitNewTask -> {
        val nextId = model.tasks.size + 1
        val task = Task(nextId, event.todo, false)
        val nextModel = model.copy(
            isAddingTask = false,
            tasks = model.tasks + task
        )
        val effects = effects(Effect.SaveTask(task))
        next(nextModel, effects)
      }
      is Event.OnDiscardNewTask -> {
        if (model.isAddingTask) {
          next(model.copy(isAddingTask = false))
        } else {
          noChange()
        }
      }
      is Event.OnDeleteTask -> {
        val updatedTasksList = model.tasks.filter { it.id != event.taskId }
        val nextModel = model.copy(tasks = updatedTasksList)
        val effects = effects(Effect.DeleteTask(event.taskId))
        next(nextModel, effects)
      }
      is Event.OnToggleTaskComplete -> {
        val task = model.tasks
            .first { it.id == event.taskId }
            .run { copy(isComplete = !isComplete) }
        val newList = model.tasks.toMutableList()
        newList[task.id] = task
        val newModel = model.copy(tasks = newList.toList())
        val effects = effects(Effect.UpdateTask(task))
        next(newModel, effects)
      }
      is Event.OnTasksLoaded -> {
        next(model.copy(
            tasks = event.tasks,
            isLoadingTasks = false
        ))
      }
    }
  }
}
