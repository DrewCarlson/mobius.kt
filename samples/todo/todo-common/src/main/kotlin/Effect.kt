package todo

sealed class Effect {
  object LoadTasks : Effect()
  data class SaveTask(val task: Task) : Effect()
  data class DeleteTask(val taskId: Int) : Effect()
  data class UpdateTask(val task: Task) : Effect()
}
