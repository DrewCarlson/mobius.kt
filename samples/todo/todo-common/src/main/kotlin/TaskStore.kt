package todo

expect class TaskStore {
  fun listTasks(): List<Task>
  fun saveTask(task: Task)
  fun updateTask(task: Task)
  fun deleteTask(taskId: Int)
  fun dispose()
}
