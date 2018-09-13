package todo

sealed class Event {
  object OnLoadTasks : Event()
  object OnAddTask : Event()

  data class OnSubmitNewTask(val todo: String) : Event()
  object OnDiscardNewTask : Event()

  data class OnDeleteTask(val taskId: Int) : Event()
  data class OnToggleTaskComplete(val taskId: Int) : Event()

  data class OnTasksLoaded(val tasks: List<Task>) : Event()
}
